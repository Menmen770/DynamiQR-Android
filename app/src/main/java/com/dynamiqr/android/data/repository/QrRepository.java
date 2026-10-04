package com.dynamiqr.android.data.repository;

import com.dynamiqr.android.data.api.ApiService;
import com.dynamiqr.android.data.models.FolderState;
import com.dynamiqr.android.data.models.GenerateQrResponse;
import com.dynamiqr.android.data.models.QrListResponse;
import com.dynamiqr.android.data.models.SaveQrResponse;
import java.util.Map;
import retrofit2.Call;

public class QrRepository {

    private final ApiService apiService;

    public QrRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    public Call<QrListResponse> getSavedQrs(String query) {
        return apiService.getSavedQrs(query, 50);
    }

    public Call<GenerateQrResponse> generateQr(Map<String, Object> body) {
        return apiService.generateQr(body);
    }

    public Call<SaveQrResponse> saveQr(Map<String, Object> body) {
        return apiService.saveQr(body);
    }

    public Call<SaveQrResponse> patchQr(String id, Map<String, Object> body) {
        return apiService.patchQr(id, body);
    }

    public Call<Map<String, Object>> deleteQr(String id) {
        return apiService.deleteQr(id);
    }

    public Call<Map<String, Object>> getQrStats(String id) {
        return apiService.getQrStats(id);
    }

    public Call<FolderState> getFolders() {
        return apiService.getFolders();
    }

    public Call<FolderState> putFolders(FolderState body) {
        return apiService.putFolders(body);
    }

    public Call<Map<String, Object>> uploadPdf(Map<String, Object> body) {
        return apiService.uploadPdf(body);
    }
}
