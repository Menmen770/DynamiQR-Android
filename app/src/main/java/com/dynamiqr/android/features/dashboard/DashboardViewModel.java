package com.dynamiqr.android.features.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.dynamiqr.android.DynamiQRApplication;
import com.dynamiqr.android.R;
import com.dynamiqr.android.data.models.FolderState;
import com.dynamiqr.android.data.models.QrCode;
import com.dynamiqr.android.data.models.QrListResponse;
import com.dynamiqr.android.data.models.SaveQrResponse;
import com.dynamiqr.android.data.repository.QrRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardViewModel extends ViewModel {

    public enum ActivityFilter {
        ALL,
        ACTIVE,
        INACTIVE
    }

    private final QrRepository repository;
    private final MutableLiveData<List<QrCode>> allQrs = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<QrCode>> filteredQrs = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<FolderState> folderState = new MutableLiveData<>(new FolderState());
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
                    error.setValue(DynamiQRApplication.getInstance().getString(R.string.dashboard_list_load_failed));
                }
            }

            @Override
            public void onFailure(Call<QrListResponse> call, Throwable t) {
                isLoading.setValue(false);
                error.setValue(DynamiQRApplication.getInstance().getString(R.string.dashboard_list_load_failed));
            }
        });
        loadFolders();
    }

    public void loadFolders() {
        repository.getFolders().enqueue(new Callback<FolderState>() {
            @Override
            public void onResponse(Call<FolderState> call, Response<FolderState> response) {
                if (response.isSuccessful() && response.body() != null) {
                    folderState.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<FolderState> call, Throwable t) {
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
        repository.deleteQr(id).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    loadQrs(searchQuery);
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                } else {
                    error.setValue(DynamiQRApplication.getInstance().getString(R.string.dashboard_card_delete_failed));
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                error.setValue(DynamiQRApplication.getInstance().getString(R.string.dashboard_card_delete_failed));
            }
        });
    }

    public void updateQr(String id, String name, String target, Runnable onSuccess) {
        Map<String, Object> body = new HashMap<>();
        if (name != null) {
            body.put("displayName", name);
        }
        if (target != null) {
            body.put("content", target);
        }
        patch(id, body, onSuccess);
    }

    public void setActive(String id, boolean active, Runnable onSuccess) {
        Map<String, Object> body = new HashMap<>();
        body.put("isActive", active);
        patch(id, body, onSuccess);
    }

    public void renameQr(String id, String name, Runnable onSuccess) {
        Map<String, Object> body = new HashMap<>();
        body.put("displayName", name);
        patch(id, body, onSuccess);
    }

    private void patch(String id, Map<String, Object> body, Runnable onSuccess) {
        repository.patchQr(id, body).enqueue(new Callback<SaveQrResponse>() {
            @Override
            public void onResponse(Call<SaveQrResponse> call, Response<SaveQrResponse> response) {
                if (response.isSuccessful()) {
                    loadQrs(searchQuery);
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                } else {
                    error.setValue(DynamiQRApplication.getInstance().getString(R.string.dashboard_card_update_failed));
                }
            }

            @Override
            public void onFailure(Call<SaveQrResponse> call, Throwable t) {
                error.setValue(DynamiQRApplication.getInstance().getString(R.string.dashboard_card_update_failed));
            }
        });
    }

    public String folderNameForQr(String qrId) {
        FolderState state = folderState.getValue();
        if (state == null || qrId == null) {
            return null;
        }
        String folderId = state.getAssignments().get(qrId);
        if (folderId == null || folderId.isEmpty()) {
            return null;
        }
        for (FolderState.FolderItem folder : state.getFolders()) {
            if (folderId.equals(folder.getId())) {
                return folder.getName();
            }
        }
        return null;
    }

    public void assignFolder(String qrId, String folderIdOrNull, Runnable onSuccess) {
        FolderState current = folderState.getValue();
        if (current == null) {
            current = new FolderState();
        }
        Map<String, String> assignments = new HashMap<>(current.getAssignments());
        if (folderIdOrNull == null || folderIdOrNull.isEmpty()) {
            assignments.remove(qrId);
        } else {
            assignments.put(qrId, folderIdOrNull);
        }
        current.setAssignments(assignments);
        saveFolderState(current, onSuccess);
    }

    public void createFolder(String name, Runnable onSuccess) {
        String trimmed = name != null ? name.trim() : "";
        if (trimmed.isEmpty()) {
            error.setValue(DynamiQRApplication.getInstance().getString(R.string.dashboard_sidebar_folder_name_empty));
            return;
        }
        FolderState current = folderState.getValue();
        if (current == null) {
            current = new FolderState();
        }
        List<FolderState.FolderItem> folders = new ArrayList<>(current.getFolders());
        String id = "f_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        folders.add(new FolderState.FolderItem(id, trimmed));
        current.setFolders(folders);
        saveFolderState(current, onSuccess);
    }

    private void saveFolderState(FolderState state, Runnable onSuccess) {
        repository.putFolders(state).enqueue(new Callback<FolderState>() {
            @Override
            public void onResponse(Call<FolderState> call, Response<FolderState> response) {
                if (response.isSuccessful() && response.body() != null) {
                    folderState.setValue(response.body());
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                } else {
                    error.setValue(DynamiQRApplication.getInstance().getString(R.string.dashboard_folders_save_failed));
                }
            }

            @Override
            public void onFailure(Call<FolderState> call, Throwable t) {
                error.setValue(DynamiQRApplication.getInstance().getString(R.string.dashboard_folders_save_failed));
            }
        });
    }

    public LiveData<List<QrCode>> getQrs() {
        return filteredQrs;
    }

    public LiveData<FolderState> getFolderState() {
        return folderState;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }
}
