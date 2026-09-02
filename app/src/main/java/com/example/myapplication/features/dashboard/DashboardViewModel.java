package com.example.myapplication.features.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.myapplication.data.models.QrCode;
import com.example.myapplication.data.models.QrListResponse;
import com.example.myapplication.data.repository.QrRepository;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardViewModel extends ViewModel {

    public enum ActivityFilter {
        ALL("הכל"),
        ACTIVE("פעיל"),
        INACTIVE("לא פעיל");

        private final String label;

        ActivityFilter(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    private final QrRepository repository;
    private final MutableLiveData<List<QrCode>> allQrs = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<QrCode>> filteredQrs = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    private String searchQuery = "";
    private ActivityFilter activityFilter = ActivityFilter.ALL;

    public DashboardViewModel(QrRepository repository) {
        this.repository = repository;
    }

    public void loadQrs(String query) {
        searchQuery = query != null ? query : "";
        isLoading.setValue(true);
        repository.getSavedQrs(searchQuery).enqueue(new Callback<QrListResponse>() {
            @Override
            public void onResponse(Call<QrListResponse> call, Response<QrListResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<QrCode> items = response.body().getItems();
                    allQrs.setValue(items != null ? items : new ArrayList<>());
                    applyFilters();
                } else {
                    error.setValue("טעינה נכשלה");
                }
            }

            @Override
            public void onFailure(Call<QrListResponse> call, Throwable t) {
                isLoading.setValue(false);
                error.setValue(t.getMessage());
            }
        });
    }

    public void setActivityFilter(ActivityFilter filter) {
        activityFilter = filter != null ? filter : ActivityFilter.ALL;
        applyFilters();
    }

    public ActivityFilter getActivityFilter() {
        return activityFilter;
    }

    private void applyFilters() {
        List<QrCode> source = allQrs.getValue();
        if (source == null) {
            filteredQrs.setValue(new ArrayList<>());
            return;
        }
        List<QrCode> result = new ArrayList<>();
        for (QrCode qr : source) {
            if (activityFilter == ActivityFilter.ACTIVE && !qr.isActive()) {
                continue;
            }
            if (activityFilter == ActivityFilter.INACTIVE && qr.isActive()) {
                continue;
            }
            result.add(qr);
        }
        filteredQrs.setValue(result);
    }

    public void deleteQr(String id, Runnable onSuccess) {
        repository.deleteQr(id).enqueue(new Callback<java.util.Map<String, Object>>() {
            @Override
            public void onResponse(Call<java.util.Map<String, Object>> call,
                                   Response<java.util.Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    loadQrs(searchQuery);
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                } else {
                    error.setValue("מחיקה נכשלה");
                }
            }

            @Override
            public void onFailure(Call<java.util.Map<String, Object>> call, Throwable t) {
                error.setValue(t.getMessage());
            }
        });
    }

    public void updateQr(String id, String name, String target, Runnable onSuccess) {
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        if (name != null) body.put("displayName", name);
        if (target != null) body.put("content", target);

        repository.patchQr(id, body).enqueue(new Callback<com.example.myapplication.data.models.SaveQrResponse>() {
            @Override
            public void onResponse(Call<com.example.myapplication.data.models.SaveQrResponse> call,
                                   Response<com.example.myapplication.data.models.SaveQrResponse> response) {
                if (response.isSuccessful()) {
                    loadQrs(searchQuery);
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                } else {
                    error.setValue("עדכון נכשל");
                }
            }

            @Override
            public void onFailure(Call<com.example.myapplication.data.models.SaveQrResponse> call, Throwable t) {
                error.setValue(t.getMessage());
            }
        });
    }

    public LiveData<List<QrCode>> getQrs() {
        return filteredQrs;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }
}
