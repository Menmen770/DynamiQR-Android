package com.dynamiqr.android.features.generator;

import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.dynamiqr.android.DynamiQRApplication;
import com.dynamiqr.android.R;
import com.dynamiqr.android.core.utils.QrEncoder;
import com.dynamiqr.android.core.utils.QrPreviewCompositor;
import com.dynamiqr.android.core.utils.QrStyleMapper;
import com.dynamiqr.android.data.repository.QrRepository;
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
    private String message = "";
    private String wifiSsid = "";
    private String wifiPassword = "";
    private String wifiSecurity = "WPA";
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

    public void setMessage(String value) {
        message = value != null ? value : "";
    }

    public void setWifiFields(String ssid, String password, String security) {
        wifiSsid = ssid != null ? ssid.trim() : "";
        wifiPassword = password != null ? password : "";
        wifiSecurity = QrEncoder.normalizeWifiSecurity(security);
        content = QrEncoder.encodeWifi(wifiSsid, wifiPassword, wifiSecurity);
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
                error.setValue(message != null ? message : DynamiQRApplication.getInstance().getString(R.string.generator_errors_generate_failed));
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
        String encoded = encodedContent();
        if (encoded.isEmpty()) {
            if (callback != null) {
                callback.onError(DynamiQRApplication.getInstance().getString(R.string.generator_preview_empty_hint));
            } else {
                error.setValue(DynamiQRApplication.getInstance().getString(R.string.generator_errors_fill_before_dynamic_save));
            }
            return;
        }
        if (manageLoading) {
            isLoading.setValue(true);
        }

        Map<String, Object> body = buildGenerateBody(widthPx);
        repository.generateQr(body).enqueue(new Callback<com.dynamiqr.android.data.models.GenerateQrResponse>() {
            @Override
            public void onResponse(Call<com.dynamiqr.android.data.models.GenerateQrResponse> call,
                                   Response<com.dynamiqr.android.data.models.GenerateQrResponse> response) {
                if (manageLoading) {
                    isLoading.setValue(false);
                }
                if (response.isSuccessful() && response.body() != null
                        && response.body().getQrImage() != null) {
                    if (callback != null) {
                        callback.onSuccess(response.body().getQrImage());
                    }
                } else if (callback != null) {
                    callback.onError(DynamiQRApplication.getInstance().getString(R.string.generator_errors_generate_failed));
                } else {
                    error.setValue(DynamiQRApplication.getInstance().getString(R.string.generator_errors_generate_failed));
                }
            }

            @Override
            public void onFailure(Call<com.dynamiqr.android.data.models.GenerateQrResponse> call, Throwable t) {
                if (manageLoading) {
                    isLoading.setValue(false);
                }
                String message = t != null ? t.getMessage() : DynamiQRApplication.getInstance().getString(R.string.common_api_server_unexpected);
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
        body.put("text", encodedContent());
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
        String encoded = encodedContent();
        if (encoded.isEmpty()) {
            error.setValue(DynamiQRApplication.getInstance().getString(R.string.generator_preview_empty_hint));
            return;
        }
        isLoading.setValue(true);
        Map<String, Object> body = new HashMap<>();
        body.put("displayName", displayName != null && !displayName.isEmpty() ? displayName
                : DynamiQRApplication.getInstance().getString(R.string.generator_save_default_name));
        body.put("qrType", qrType);
        body.put("qrInputs", buildInputsForSave());
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

        repository.saveQr(body).enqueue(new Callback<com.dynamiqr.android.data.models.SaveQrResponse>() {
            @Override
            public void onResponse(Call<com.dynamiqr.android.data.models.SaveQrResponse> call,
                                   Response<com.dynamiqr.android.data.models.SaveQrResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    saveMessage.setValue(response.body().isUpdated()
                            ? DynamiQRApplication.getInstance().getString(R.string.generator_save_updated)
                            : DynamiQRApplication.getInstance().getString(R.string.generator_save_saved_success));
                } else {
                    error.setValue(DynamiQRApplication.getInstance().getString(R.string.generator_errors_save_failed));
                }
            }

            @Override
            public void onFailure(Call<com.dynamiqr.android.data.models.SaveQrResponse> call, Throwable t) {
                isLoading.setValue(false);
                error.setValue(t.getMessage());
            }
        });
    }

    public String getSelectedBodyId() {
        return selectedBodyId;
    }

    private String encodedContent() {
        if ("wifi".equals(qrType)) {
            return QrEncoder.encodeWifi(wifiSsid, wifiPassword, wifiSecurity);
        }
        return QrEncoder.encode(qrType, content, message);
    }

    private Map<String, Object> buildInputsForSave() {
        if ("wifi".equals(qrType)) {
            return QrEncoder.buildWifiQrInputs(wifiSsid, wifiPassword, wifiSecurity);
        }
        return QrEncoder.buildQrInputs(qrType, content, message);
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
