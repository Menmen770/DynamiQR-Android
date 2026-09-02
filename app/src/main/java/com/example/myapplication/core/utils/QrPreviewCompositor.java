package com.example.myapplication.core.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import com.caverock.androidsvg.SVG;
import android.content.Context;

public final class QrPreviewCompositor {

    public static final int DEFAULT_STAGE_PX = 320;
    private static final float INNER_SCALE = 0.78f;
    private static final RectF NORMALIZED_RECT = new RectF(
            202f / 1125f, 200f / 1125f,
            (202f + (915.730469f - 202f)) / 1125f,
            (200f + (920.261719f - 200f)) / 1125f);

    private QrPreviewCompositor() {
    }

    public static Bitmap composite(Context context, Bitmap qrBitmap, String stickerId,
                                   String bgColor, int stageSizePx) {
        if (qrBitmap == null) {
            return null;
        }
        int size = stageSizePx > 0 ? stageSizePx : DEFAULT_STAGE_PX;
        boolean withSticker = stickerId != null && !"none".equals(stickerId);

        Bitmap result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);

        int bg = parseColor(bgColor, Color.WHITE);
        canvas.drawColor(bg);

        RectF qrDest = withSticker ? computeStickerSlot(size) : new RectF(0, 0, size, size);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(qrBitmap, null, qrDest, paint);

        if (withSticker) {
            try {
                String num = stickerId.replace("frame-", "");
                int overlayRes = context.getResources().getIdentifier(
                        "sticker_overlay_" + num, "raw", context.getPackageName());
                if (overlayRes != 0) {
                    SVG overlay = SVG.getFromResource(context, overlayRes);
                    overlay.renderToCanvas(canvas);
                }
            } catch (Exception ignored) {
            }
        }

        return result;
    }

    public static Bitmap applySticker(Context context, Bitmap qrBitmap, String stickerId) {
        return composite(context, qrBitmap, stickerId, "#ffffff", DEFAULT_STAGE_PX);
    }

    private static RectF computeStickerSlot(int size) {
        float left = NORMALIZED_RECT.left * size;
        float top = NORMALIZED_RECT.top * size;
        float slotW = NORMALIZED_RECT.width() * size;
        float slotH = NORMALIZED_RECT.height() * size;
        float qrW = slotW * INNER_SCALE;
        float qrH = slotH * INNER_SCALE;
        float cx = left + slotW / 2f;
        float cy = top + slotH / 2f;
        return new RectF(cx - qrW / 2f, cy - qrH / 2f, cx + qrW / 2f, cy + qrH / 2f);
    }

    private static int parseColor(String hex, int fallback) {
        if (hex == null || hex.isEmpty()) {
            return fallback;
        }
        try {
            if ("transparent".equalsIgnoreCase(hex)) {
                return Color.TRANSPARENT;
            }
            return Color.parseColor(hex);
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
}
