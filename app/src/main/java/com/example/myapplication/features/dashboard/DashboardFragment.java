package com.example.myapplication.features.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.myapplication.core.base.BaseFragment;
import com.example.myapplication.data.local.AuthManager;
import com.example.myapplication.data.models.QrListResponse;
import com.example.myapplication.data.api.ApiService;
import com.example.myapplication.data.api.RetrofitClient;
import com.example.myapplication.databinding.FragmentDashboardBinding;
import com.example.myapplication.ui.adapters.QrAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends BaseFragment<FragmentDashboardBinding> {
    private QrAdapter adapter;
    private ApiService apiService;

    @Override
    protected FragmentDashboardBinding inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentDashboardBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        adapter = new QrAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);

        AuthManager authManager = new AuthManager(requireContext());
        apiService = RetrofitClient.getService(authManager);

        loadQrs(null);

        binding.searchInput.setOnEditorActionListener((v, actionId, event) -> {
            loadQrs(v.getText().toString());
            return true;
        });
    }

    private void loadQrs(String query) {
        binding.progressBar.setVisibility(View.VISIBLE);
        apiService.getSavedQrs(query).enqueue(new Callback<QrListResponse>() {
            @Override
            public void onResponse(Call<QrListResponse> call, Response<QrListResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setItems(response.body().getItems());
                } else {
                    Toast.makeText(getContext(), "טעינה נכשלה", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<QrListResponse> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "שגיאה: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
