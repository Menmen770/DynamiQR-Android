package com.example.myapplication.core.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.LruCache;
import android.widget.ImageView;
import com.example.myapplication.data.models.GenerateQrResponse;
import com.example.myapplication.data.models.QrCode;
import com.example.myapplication.data.repository.QrRepository;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QrPreviewLoader {

    private static final int CACHE_SIZE = 40;
    private static final int CARD_STAGE_PX = 148;

    private final QrRepository repository;
    private final LruCache<String, Bitmap> cache = new LruCache<>(CACHE_SIZE);
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public QrPreviewLoader(QrRepository repository) {
        this.repository = repository;
    }

    public void loadInto(Context context, QrCode qr, ImageView imageView) {
        if (qr == null || qr.getId() == null) {
            imageView.setImageDrawable(null);
            return;
        }

        String cacheKey = qr.getId() + "_" + qr.getUpdatedAt();
        imageView.setTag(cacheKey);

        Bitmap cached = cache.get(cacheKey);
        if (cached != null) {
            imageView.setImageBitmap(cached);
            return;
        }

        imageView.setImageDrawable(null);

        Map<String, Object> body = SavedQrPreviewHelper.buildGenerateBody(qr);
        if (body == null) {
            return;
        }

        repository.generateQr(body).enqueue(new Callback<GenerateQrResponse>() {
            @Override
            public void onResponse(Call<GenerateQrResponse> call, Response<GenerateQrResponse> response) {
                if (!cacheKey.equals(imageView.getTag())) {
                    return;
                }
                if (!response.isSuccessful() || response.body() == null) {
                    return;
                }
                String dataUrl = response.body().getQrImage();
                executor.execute(() -> {
                    Bitmap bitmap = decodeAndComposite(context, dataUrl, qr);
                    if (bitmap == null) {
                        return;
                    }
                    cache.put(cacheKey, bitmap);
                    mainHandler.post(() -> {
                        if (cacheKey.equals(imageView.getTag())) {
                            imageView.setImageBitmap(bitmap);
                        }
                    });
                });
            }

            @Override
            public void onFailure(Call<GenerateQrResponse> call, Throwable t) {
                // keep placeholder empty
            }
        });
    }

    public void clearCache() {
        cache.evictAll();
    }

    private Bitmap decodeAndComposite(Context context, String dataUrl, QrCode qr) {
        try {
            String base64 = dataUrl.contains(",")
                    ? dataUrl.substring(dataUrl.indexOf(',') + 1) : dataUrl;
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            Bitmap qrBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (qrBitmap == null) {
                return null;
            }
            return QrPreviewCompositor.composite(
                    context.getApplicationContext(),
                    qrBitmap,
                    SavedQrPreviewHelper.stickerType(qr),
                    SavedQrPreviewHelper.bgColorForComposite(qr),
                    CARD_STAGE_PX);
        } catch (Exception e) {
            return null;
        }
    }
}
