package com.example.myapplication.features.generator;

import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.myapplication.core.utils.QrEncoder;
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
    private String content = "";
    private String fgColor = "#111111";
    private String bgColor = "#ffffff";
    private String colorMode = "solid";
    private String gradientStart = "#111111";
    private String gradientEnd = "#0A9396";
    private int gradientAngle = 135;
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
        logoInsetScale = insetScale;
        schedulePreview();
    }

    public void clearLogo() {
        logoId = "";
        logoDataUrl = null;
        logoInsetScale = 1f;
        schedulePreview();
    }

    public void setLogoShape(String shape) {
        logoShape = shape != null ? shape : "square";
        schedulePreview();
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
        String encoded = QrEncoder.encode(qrType, content);
        if (encoded.isEmpty()) {
            error.setValue("הזן תוכן תקין");
            return;
        }
        isLoading.setValue(true);

        boolean hasSticker = stickerId != null && !"none".equals(stickerId);
        String bgForApi = hasSticker ? "transparent" : bgColor;
        String colorForApi = "gradient".equals(colorMode)
                ? gradientStart
                : fgColor;

        Map<String, Object> body = new HashMap<>();
        body.put("text", encoded);
        body.put("color", colorForApi);
        body.put("bgColor", bgForApi);
        body.put("dotsType", dotsType);
        body.put("cornersType", cornersType);
        body.put("logoShape", logoShape);
        body.put("errorCorrectionLevel", errorCorrectionLevel);
        body.put("width", 480);

        if ("gradient".equals(colorMode)) {
            body.put("dotsGradient", buildDotsGradient());
        }

        if (logoDataUrl != null && !logoDataUrl.isEmpty()) {
            body.put("image", logoDataUrl);
            body.put("logoInsetScale", logoInsetScale);
        }

        repository.generateQr(body).enqueue(new Callback<com.example.myapplication.data.models.GenerateQrResponse>() {
            @Override
            public void onResponse(Call<com.example.myapplication.data.models.GenerateQrResponse> call,
                                   Response<com.example.myapplication.data.models.GenerateQrResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    qrPreviewImage.setValue(response.body().getQrImage());
                } else {
                    error.setValue("יצירת תצוגה מקדימה נכשלה");
                }
            }

            @Override
            public void onFailure(Call<com.example.myapplication.data.models.GenerateQrResponse> call, Throwable t) {
                isLoading.setValue(false);
                error.setValue(t.getMessage());
            }
        });
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
            error.setValue("הזן תוכן תקין");
            return;
        }
        isLoading.setValue(true);
        Map<String, Object> body = new HashMap<>();
        body.put("displayName", displayName != null && !displayName.isEmpty() ? displayName : "קוד חדש");
        body.put("qrType", qrType);
        body.put("qrInputs", QrEncoder.buildQrInputs(qrType, content));
        body.put("qrValue", encoded);
        body.put("linkMode", "static");

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
        style.put("bgColorMode", "solid");
        style.put("dotsType", dotsType);
        style.put("cornersType", cornersType);
        style.put("logoShape", logoShape);
        style.put("stickerType", stickerId);
        style.put("errorCorrectionLevel", errorCorrectionLevel);
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
                    saveMessage.setValue(response.body().isUpdated() ? "הקוד עודכן" : "הקוד נשמר בהצלחה");
                } else {
                    error.setValue("שמירה נכשלה");
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

    public String getLogoId() {
        return logoId;
    }

    public String getStickerId() {
        return stickerId;
    }

    public String getBgColor() {
        return bgColor;
    }

    public String getLogoShape() {
        return logoShape;
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
