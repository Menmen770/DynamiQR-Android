package com.example.myapplication.features.generator;

import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.myapplication.DynamiQRApplication;
import com.example.myapplication.core.i18n.AppI18n;
import com.example.myapplication.core.utils.QrEncoder;
import com.example.myapplication.core.utils.QrPreviewCompositor;
import com.example.myapplication.core.utils.QrStyleMapper;
import com.example.myapplication.data.repository.QrRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GeneratorViewModel extends ViewModel {

    public static final int STEP_CONTENT = 1;
    public static final int STEP_STYLE = 2;
    public static final int STEP_EXPORT = 3;

    private static final long PREVIEW_DEBOUNCE_MS = 450;

    private final QrRepository repository;
    private final Handler debounceHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingPreview;

    private final MutableLiveData<Integer> currentStep = new MutableLiveData<>(STEP_CONTENT);
    private final MutableLiveData<String> qrPreviewImage = new MutableLiveData<>();
    private final MutableLiveData<String> stickerType = new MutableLiveData<>("none");
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> saveMessage = new MutableLiveData<>();

    private String qrType = "url";
    private String linkMode = "static";
    private String content = "";
    private String fgColor = "#111111";
    private String bgColor = "#ffffff";
    private String colorMode = "solid";
    private String bgColorMode = "solid";
    private String gradientStart = "#0a9396";
    private String gradientEnd = "#005f73";
    private int gradientAngle = 135;
    private String bgGradientStart = "#fff7ed";
    private String bgGradientEnd = "#fdba74";
    private int bgGradientAngle = 135;
    private String dotsType = "square";
    private String cornersType = "square";
    private String selectedBodyId = "body_1";
    private String selectedCornerId = "corner_1";
    private String logoId = "";
    private String logoDataUrl = null;
    private String logoShape = "overlay";
    private float logoInsetScale = 1f;
    private String stickerId = "none";
    private String errorCorrectionLevel = "Q";

    public GeneratorViewModel(QrRepository repository) {
        this.repository = repository;
    }

    public void setQrType(String type) {
        qrType = type;
    }

    public void setLinkMode(String mode) {
        linkMode = "dynamic".equals(mode) ? "dynamic" : "static";
    }

    public String getLinkMode() {
        return linkMode;
    }

    public void setContent(String value) {
        content = value != null ? value.trim() : "";
    }

    public void setFgColor(String color) {
        fgColor = color;
        schedulePreview();
    }

    public void setBgColor(String color) {
        bgColor = color;
        schedulePreview();
    }

    public void setBgColorMode(String mode) {
        bgColorMode = mode != null ? mode : "solid";
        schedulePreview();
    }

    public void goToStep(int step) {
        currentStep.setValue(step);
        if (step >= STEP_STYLE) {
            schedulePreview();
        }
    }

    public void setColorMode(String mode) {
        colorMode = mode;
        schedulePreview();
    }

    public void setGradient(String start, String end, int angle) {
        gradientStart = start;
        gradientEnd = end;
        gradientAngle = angle;
        schedulePreview();
    }

    public void setBgGradient(String start, String end, int angle) {
        bgGradientStart = start;
        bgGradientEnd = end;
        bgGradientAngle = angle;
        schedulePreview();
    }

    public void setBodyShape(String bodyId) {
        selectedBodyId = bodyId;
        dotsType = QrStyleMapper.bodyShapeFromId(bodyId);
        schedulePreview();
    }

    public void setCornerShape(String cornerId) {
        selectedCornerId = cornerId;
        cornersType = QrStyleMapper.cornerShapeFromId(cornerId);
        schedulePreview();
    }

    public void setLogo(String id, String dataUrl, float insetScale) {
        logoId = id != null ? id : "";
        logoDataUrl = dataUrl;
        logoInsetScale = clampLogoInset(insetScale);
        schedulePreview();
    }

    public void setLogoInsetScale(float insetScale) {
        logoInsetScale = clampLogoInset(insetScale);
        schedulePreview();
    }

    public void clearLogo() {
        logoId = "";
        logoDataUrl = null;
        logoInsetScale = 1f;
        schedulePreview();
    }

    public void setLogoShape(String shape) {
        logoShape = shape != null && !shape.isEmpty() ? shape : "overlay";
        schedulePreview();
    }

    private static float clampLogoInset(float value) {
        if (Float.isNaN(value)) {
            return 1f;
        }
        return Math.min(1f, Math.max(0.55f, value));
    }

    public void setSticker(String sticker) {
        stickerId = sticker != null ? sticker : "none";
        stickerType.setValue(stickerId);
        schedulePreview();
    }

    public void schedulePreview() {
        if (pendingPreview != null) {
            debounceHandler.removeCallbacks(pendingPreview);
        }
        pendingPreview = this::generatePreview;
        debounceHandler.postDelayed(pendingPreview, PREVIEW_DEBOUNCE_MS);
    }

    public void generatePreview() {
        generateQrImage(480, new ExportCallback() {
            @Override
            public void onSuccess(String dataUrl) {
                qrPreviewImage.setValue(dataUrl);
            }

            @Override
            public void onError(String message) {
                error.setValue(message != null ? message : AppI18n.t(DynamiQRApplication.getInstance(),
                        "generator", "errors.generateFailed", "Preview failed"));
            }
        }, true);
    }

    /**
     * ייצור QR באיכות הדפסה (2400px) לשיתוף/הורדה — כמו QR_EXPORT_PIXEL_SIZE באתר.
     */
    public void generateExport(ExportCallback callback) {
        generateQrImage(QrPreviewCompositor.EXPORT_PIXEL_SIZE, callback, true);
    }

    public interface ExportCallback {
        void onSuccess(String dataUrl);

        void onError(String message);
    }

    private void generateQrImage(int widthPx, ExportCallback callback, boolean manageLoading) {
        String encoded = QrEncoder.encode(qrType, content);
        if (encoded.isEmpty()) {
            if (callback != null) {
                callback.onError(AppI18n.t(DynamiQRApplication.getInstance(),
                        "generator", "preview.emptyHint", "Enter valid content"));
            } else {
                error.setValue(AppI18n.t(DynamiQRApplication.getInstance(),
                        "generator", "errors.fillBeforeDynamicSave", "Enter valid content"));
            }
            return;
        }
        if (manageLoading) {
            isLoading.setValue(true);
        }

        Map<String, Object> body = buildGenerateBody(widthPx);
        repository.generateQr(body).enqueue(new Callback<com.example.myapplication.data.models.GenerateQrResponse>() {
            @Override
            public void onResponse(Call<com.example.myapplication.data.models.GenerateQrResponse> call,
                                   Response<com.example.myapplication.data.models.GenerateQrResponse> response) {
                if (manageLoading) {
                    isLoading.setValue(false);
                }
                if (response.isSuccessful() && response.body() != null
                        && response.body().getQrImage() != null) {
                    if (callback != null) {
                        callback.onSuccess(response.body().getQrImage());
                    }
                } else if (callback != null) {
                    callback.onError(AppI18n.t(DynamiQRApplication.getInstance(),
                            "generator", "errors.generateFailed", "QR creation failed"));
                } else {
                    error.setValue(AppI18n.t(DynamiQRApplication.getInstance(),
                            "generator", "errors.generateFailed", "Preview failed"));
                }
            }

            @Override
            public void onFailure(Call<com.example.myapplication.data.models.GenerateQrResponse> call, Throwable t) {
                if (manageLoading) {
                    isLoading.setValue(false);
                }
                String message = t != null ? t.getMessage() : AppI18n.t(DynamiQRApplication.getInstance(),
                        "common", "api.serverUnexpected", "Network error");
                if (callback != null) {
                    callback.onError(message);
                } else {
                    error.setValue(message);
                }
            }
        });
    }

    private Map<String, Object> buildGenerateBody(int widthPx) {
        boolean hasSticker = stickerId != null && !"none".equals(stickerId);
        String bgForApi;
        if (hasSticker || "none".equals(bgColorMode) || "gradient".equals(bgColorMode)) {
            bgForApi = "transparent";
        } else {
            bgForApi = bgColor;
        }
        String colorForApi = "gradient".equals(colorMode)
                ? gradientStart
                : fgColor;

        Map<String, Object> body = new HashMap<>();
        body.put("text", QrEncoder.encode(qrType, content));
        body.put("color", colorForApi);
        body.put("bgColor", bgForApi);
        body.put("dotsType", dotsType);
        body.put("cornersType", cornersType);
        body.put("logoShape", logoShape);
        body.put("errorCorrectionLevel", errorCorrectionLevel);
        body.put("width", widthPx);

        if ("gradient".equals(colorMode)) {
            body.put("dotsGradient", buildDotsGradient());
        }

        if (logoDataUrl != null && !logoDataUrl.isEmpty()) {
            body.put("image", logoDataUrl);
            body.put("logoInsetScale", logoInsetScale);
        }
        return body;
    }

    private Map<String, Object> buildDotsGradient() {
        Map<String, Object> gradient = new HashMap<>();
        gradient.put("type", "linear");
        gradient.put("rotation", Math.toRadians(gradientAngle));

        List<Map<String, Object>> stops = new ArrayList<>();
        Map<String, Object> stop0 = new HashMap<>();
        stop0.put("offset", 0.0);
        stop0.put("color", gradientStart);
        stops.add(stop0);

        Map<String, Object> stop1 = new HashMap<>();
        stop1.put("offset", 1.0);
        stop1.put("color", gradientEnd);
        stops.add(stop1);

        gradient.put("colorStops", stops);
        return gradient;
    }

    public void saveQr(String displayName) {
        String encoded = QrEncoder.encode(qrType, content);
        if (encoded.isEmpty()) {
            error.setValue(AppI18n.t(DynamiQRApplication.getInstance(),
                    "generator", "preview.emptyHint", "Enter valid content"));
            return;
        }
        isLoading.setValue(true);
        Map<String, Object> body = new HashMap<>();
        body.put("displayName", displayName != null && !displayName.isEmpty() ? displayName
                : AppI18n.t(DynamiQRApplication.getInstance(), "generator", "save.defaultName", "New QR"));
        body.put("qrType", qrType);
        body.put("qrInputs", QrEncoder.buildQrInputs(qrType, content));
        body.put("qrValue", encoded);
        body.put("linkMode", linkMode);

        Map<String, Object> style = new HashMap<>();
        style.put("fgColor", fgColor);
        style.put("bgColor", bgColor);
        style.put("qrColorMode", colorMode);
        if ("gradient".equals(colorMode)) {
            style.put("dotsGradient", buildDotsGradient());
            style.put("gradientStart", gradientStart);
            style.put("gradientEnd", gradientEnd);
            style.put("gradientAngle", gradientAngle);
            style.put("gradientType", "linear");
        }
        style.put("bgColorMode", bgColorMode);
        if ("gradient".equals(bgColorMode)) {
            style.put("bgGradientStart", bgGradientStart);
            style.put("bgGradientEnd", bgGradientEnd);
            style.put("bgGradientAngle", bgGradientAngle);
        }
        style.put("dotsType", dotsType);
        style.put("cornersType", cornersType);
        style.put("logoShape", logoShape);
        style.put("stickerType", stickerId);
        style.put("errorCorrectionLevel", errorCorrectionLevel);
        style.put("logoInsetScale", logoInsetScale);
        if (logoDataUrl != null && !logoDataUrl.isEmpty()) {
            style.put("logoUrl", logoDataUrl);
        }
        if (logoId != null && !logoId.isEmpty()) {
            style.put("logoId", logoId);
        }
        body.put("style", style);

        repository.saveQr(body).enqueue(new Callback<com.example.myapplication.data.models.SaveQrResponse>() {
            @Override
            public void onResponse(Call<com.example.myapplication.data.models.SaveQrResponse> call,
                                   Response<com.example.myapplication.data.models.SaveQrResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    saveMessage.setValue(response.body().isUpdated()
                            ? AppI18n.t(DynamiQRApplication.getInstance(), "generator", "save.updated", "Updated in collection")
                            : AppI18n.t(DynamiQRApplication.getInstance(), "generator", "save.savedSuccess", "Saved successfully"));
                } else {
                    error.setValue(AppI18n.t(DynamiQRApplication.getInstance(),
                            "generator", "errors.saveFailed", "Save failed"));
                }
            }

            @Override
            public void onFailure(Call<com.example.myapplication.data.models.SaveQrResponse> call, Throwable t) {
                isLoading.setValue(false);
                error.setValue(t.getMessage());
            }
        });
    }

    public String getSelectedBodyId() {
        return selectedBodyId;
    }

    public String getSelectedCornerId() {
        return selectedCornerId;
    }

    public String getStickerId() {
        return stickerId;
    }

    public String getBgColor() {
        if ("none".equals(bgColorMode)) {
            return "#ffffff";
        }
        if ("gradient".equals(bgColorMode)) {
            return bgGradientStart;
        }
        return bgColor;
    }

    public String getBgColorMode() {
        return bgColorMode;
    }

    public String getFgColor() {
        return fgColor;
    }

    public String getColorMode() {
        return colorMode;
    }

    public int getGradientAngle() {
        return gradientAngle;
    }

    public String getGradientStart() {
        return gradientStart;
    }

    public String getGradientEnd() {
        return gradientEnd;
    }

    public String getLogoShape() {
        return logoShape;
    }

    public String getLogoId() {
        return logoId;
    }

    public String getLogoDataUrl() {
        return logoDataUrl;
    }

    public float getLogoInsetScale() {
        return logoInsetScale;
    }

    public boolean hasLogo() {
        return logoDataUrl != null && !logoDataUrl.isEmpty();
    }

    public LiveData<Integer> getCurrentStep() {
        return currentStep;
    }

    public LiveData<String> getQrPreviewImage() {
        return qrPreviewImage;
    }

    public LiveData<String> getStickerType() {
        return stickerType;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<String> getSaveMessage() {
        return saveMessage;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (pendingPreview != null) {
            debounceHandler.removeCallbacks(pendingPreview);
        }
    }
}
