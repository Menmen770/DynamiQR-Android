package com.example.myapplication.core.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import com.caverock.androidsvg.SVG;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * קומפוזיציית תצוגה מקדימה — מקביל ל-QrPreviewComposite ב-RN:
 * רקע → QR בחריץ → מסגרת סטיקר צבועה בצבע ה-QR.
 */
public final class QrPreviewCompositor {

    public static final int DEFAULT_STAGE_PX = 320;

    /** כמו STICKER_QR_INNER_SCALE ב-React/RN */
    private static final float INNER_SCALE = 0.78f;
    private static final float CANVAS_REF = 1125f;

    /** כמו STICKER_QR_NORMALIZED_RECT ב-RN */
    private static final float BASE_X = 202f / CANVAS_REF;
    private static final float BASE_Y = 200f / CANVAS_REF;
    private static final float BASE_W = (915.730469f - 202f) / CANVAS_REF;
    private static final float BASE_H = (920.261719f - 200f) / CANVAS_REF;

    /**
     * כמו STICKER_QR_RECT_OVERRIDES ב-React ({@code stickerAssets.js}):
     * היסטים בפיקסלים על קנבס הייחוס 1125.
     */
    private static final Map<String, float[]> STICKER_QR_RECT_OVERRIDES;

    static {
        Map<String, float[]> overrides = new HashMap<>();
        // { xOffsetPx, yOffsetPx } — כמו ב-React: frame-17 דוחף את ה-QR למטה ב-39px
        overrides.put("frame-17", new float[]{0f, 39f});
        STICKER_QR_RECT_OVERRIDES = Collections.unmodifiableMap(overrides);
    }

    private QrPreviewCompositor() {
    }

    public static Bitmap composite(Context context, Bitmap qrBitmap, String stickerId,
                                   String bgColor, int stageSizePx) {
        return composite(context, qrBitmap, stickerId, bgColor, "#111111", stageSizePx);
    }

    public static Bitmap composite(Context context, Bitmap qrBitmap, String stickerId,
                                   String bgColor, String fgColor, int stageSizePx) {
        if (qrBitmap == null) {
            return null;
        }
        int size = stageSizePx > 0 ? stageSizePx : DEFAULT_STAGE_PX;
        boolean withSticker = stickerId != null && !"none".equals(stickerId);

        Bitmap result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);

        // כמו ב-RN: הרקע תמיד על כל הבמה (גם עם סטיקר) — לא מוחקים ללבן
        int bg = parseColor(bgColor, Color.WHITE);
        canvas.drawColor(bg);

        RectF qrDest = withSticker
                ? computeStickerSlot(size, stickerId)
                : new RectF(0, 0, size, size);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(qrBitmap, null, qrDest, paint);

        if (withSticker) {
            drawTintedOverlay(context, canvas, size, stickerId, fgColor);
        }

        return result;
    }

    private static void drawTintedOverlay(Context context, Canvas canvas, int size,
                                          String stickerId, String fgColor) {
        try {
            String num = stickerId.replace("frame-", "");
            int overlayRes = context.getResources().getIdentifier(
                    "sticker_overlay_" + num, "raw", context.getPackageName());
            if (overlayRes == 0) {
                return;
            }

            Bitmap overlayBmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            Canvas overlayCanvas = new Canvas(overlayBmp);
            SVG overlay = SVG.getFromResource(context, overlayRes);
            overlay.setDocumentWidth(size);
            overlay.setDocumentHeight(size);
            RectF viewBox = overlay.getDocumentViewBox();
            if (viewBox != null) {
                overlay.setDocumentViewBox(viewBox.left, viewBox.top, viewBox.width(), viewBox.height());
            }
            overlay.renderToCanvas(overlayCanvas);

            Paint tintPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
            tintPaint.setColorFilter(new PorterDuffColorFilter(
                    parseColor(fgColor, Color.BLACK),
                    PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(overlayBmp, 0, 0, tintPaint);
            overlayBmp.recycle();
        } catch (Exception ignored) {
        }
    }

    /**
     * כמו ב-RN / stickerCompose.js:
     * slotLeft = x + (width * (1 - scale)) / 2
     * slotTop  = y + (height * (1 - scale)) / 2
     * slotW/H  = width/height * scale
     */
    private static RectF computeStickerSlot(int size, String stickerId) {
        float x = BASE_X;
        float y = BASE_Y;
        float w = BASE_W;
        float h = BASE_H;

        float[] tweak = STICKER_QR_RECT_OVERRIDES.get(stickerId);
        if (tweak != null) {
            x += tweak[0] / CANVAS_REF;
            y += tweak[1] / CANVAS_REF;
        }

        float slotLeft = (x + (w * (1f - INNER_SCALE)) / 2f) * size;
        float slotTop = (y + (h * (1f - INNER_SCALE)) / 2f) * size;
        float slotW = w * INNER_SCALE * size;
        float slotH = h * INNER_SCALE * size;
        return new RectF(slotLeft, slotTop, slotLeft + slotW, slotTop + slotH);
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
