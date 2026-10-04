package com.dynamiqr.android.core.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import com.caverock.androidsvg.SVG;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * קומפוזיציית תצוגה מקדימה / ייצוא — מקביל ל-QrPreviewComposite + composeQrExportCanvas:
 * רקע → QR בחריץ → מסגרת סטיקר צבועה בצבע / גרדיאנט ה-QR.
 */
public final class QrPreviewCompositor {

    public static final int DEFAULT_STAGE_PX = 320;
    /** כמו QR_EXPORT_PIXEL_SIZE באתר — ~10 ס״מ ב־600 DPI */
    public static final int EXPORT_PIXEL_SIZE = 2400;

    /** כמו STICKER_QR_INNER_SCALE ב-React/RN */
    private static final float INNER_SCALE = 0.78f;
    private static final float CANVAS_REF = 1125f;
    /** כמו composeQrExportCanvas ללא סטיקר */
    private static final float EXPORT_QR_SCALE = 1.12f;
    private static final float EXPORT_PADDING_FRAC = 0.12f;
    private static final int DEFAULT_GRADIENT_ANGLE = 135;

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
        overrides.put("frame-17", new float[]{0f, 39f});
        STICKER_QR_RECT_OVERRIDES = Collections.unmodifiableMap(overrides);
    }

    /** דיו לסטיקר — צבע אחיד או גרדיאנט כמו ink ב-stickerCompose.js */
    public static final class StickerInk {
        public final String solidColor;
        public final boolean useGradient;
        public final String gradientStart;
        public final String gradientEnd;
        public final int gradientAngleDeg;

        public StickerInk(String solidColor) {
            this.solidColor = solidColor != null ? solidColor : "#111111";
            this.useGradient = false;
            this.gradientStart = this.solidColor;
            this.gradientEnd = this.solidColor;
            this.gradientAngleDeg = DEFAULT_GRADIENT_ANGLE;
        }

        public StickerInk(String start, String end, int angleDeg) {
            this.useGradient = true;
            this.gradientStart = start != null ? start : "#0a9396";
            this.gradientEnd = end != null ? end : "#005f73";
            this.gradientAngleDeg = angleDeg;
            this.solidColor = this.gradientStart;
        }

        public static StickerInk solid(String color) {
            return new StickerInk(color);
        }

        public static StickerInk gradient(String start, String end, int angleDeg) {
            return new StickerInk(start, end, angleDeg);
        }

        /** מתוך dotsGradient של ה-API / שמירה. */
        @SuppressWarnings("unchecked")
        public static StickerInk fromDotsGradient(Map<String, Object> gradient, String fallbackColor) {
            if (gradient == null) {
                return solid(fallbackColor);
            }
            String start = fallbackColor != null ? fallbackColor : "#0a9396";
            String end = "#005f73";
            int angle = DEFAULT_GRADIENT_ANGLE;
            Object stopsObj = gradient.get("colorStops");
            if (stopsObj instanceof List) {
                List<?> stops = (List<?>) stopsObj;
                if (!stops.isEmpty() && stops.get(0) instanceof Map) {
                    Object c0 = ((Map<?, ?>) stops.get(0)).get("color");
                    if (c0 instanceof String) {
                        start = (String) c0;
                    }
                }
                if (stops.size() > 1 && stops.get(stops.size() - 1) instanceof Map) {
                    Object c1 = ((Map<?, ?>) stops.get(stops.size() - 1)).get("color");
                    if (c1 instanceof String) {
                        end = (String) c1;
                    }
                }
            }
            Object rotation = gradient.get("rotation");
            if (rotation instanceof Number) {
                angle = (int) Math.round(Math.toDegrees(((Number) rotation).doubleValue()));
            }
            return gradient(start, end, angle);
        }
    }

    private QrPreviewCompositor() {
    }

    public static Bitmap composite(Context context, Bitmap qrBitmap, String stickerId,
                                   String bgColor, String fgColor, int stageSizePx) {
        return composite(context, qrBitmap, stickerId, bgColor, StickerInk.solid(fgColor), stageSizePx, false);
    }

    public static Bitmap composite(Context context, Bitmap qrBitmap, String stickerId,
                                   String bgColor, StickerInk ink, int stageSizePx) {
        return composite(context, qrBitmap, stickerId, bgColor, ink, stageSizePx, false);
    }

    public static Bitmap compositeForExport(Context context, Bitmap qrBitmap, String stickerId,
                                            String bgColor, String fgColor) {
        return compositeForExport(context, qrBitmap, stickerId, bgColor, StickerInk.solid(fgColor));
    }

    public static Bitmap compositeForExport(Context context, Bitmap qrBitmap, String stickerId,
                                            String bgColor, StickerInk ink) {
        boolean withSticker = stickerId != null && !"none".equals(stickerId);
        if (withSticker) {
            return composite(context, qrBitmap, stickerId, bgColor, ink, EXPORT_PIXEL_SIZE, true);
        }
        return compositePaddedExport(qrBitmap, bgColor);
    }

    private static Bitmap composite(Context context, Bitmap qrBitmap, String stickerId,
                                    String bgColor, StickerInk ink, int stageSizePx,
                                    boolean crispQr) {
        if (qrBitmap == null) {
            return null;
        }
        int size = stageSizePx > 0 ? stageSizePx : DEFAULT_STAGE_PX;
        boolean withSticker = stickerId != null && !"none".equals(stickerId);

        Bitmap result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);

        int bg = parseColor(bgColor, Color.WHITE);
        canvas.drawColor(bg);

        RectF qrDest = withSticker
                ? computeStickerSlot(size, stickerId)
                : new RectF(0, 0, size, size);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        if (!crispQr) {
            paint.setFilterBitmap(true);
        }
        canvas.drawBitmap(qrBitmap, null, qrDest, paint);

        if (withSticker) {
            drawTintedOverlay(context, canvas, size, stickerId, ink != null ? ink : StickerInk.solid("#111111"));
        }

        return result;
    }

    private static Bitmap compositePaddedExport(Bitmap qrBitmap, String bgColor) {
        if (qrBitmap == null) {
            return null;
        }
        int outputSize = EXPORT_PIXEL_SIZE;
        int extraPadding = Math.round(outputSize * EXPORT_PADDING_FRAC);
        int qrDraw = Math.round(outputSize * EXPORT_QR_SCALE);
        int canvasSize = qrDraw + extraPadding * 2;

        Bitmap result = Bitmap.createBitmap(canvasSize, canvasSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        canvas.drawColor(parseColor(bgColor, Color.WHITE));

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setFilterBitmap(false);
        RectF dest = new RectF(extraPadding, extraPadding,
                extraPadding + qrDraw, extraPadding + qrDraw);
        canvas.drawBitmap(qrBitmap, null, dest, paint);
        return result;
    }

    /**
     * כמו stickerCompose.js: מילוי צבע/גרדיאנט + destination-in עם מסכת הסטיקר.
     */
    private static void drawTintedOverlay(Context context, Canvas canvas, int size,
                                          String stickerId, StickerInk ink) {
        Bitmap overlayBmp = null;
        Bitmap tintBmp = null;
        try {
            overlayBmp = loadOverlayBitmap(context, stickerId, size);
            if (overlayBmp == null) {
                return;
            }

            tintBmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            Canvas tintCanvas = new Canvas(tintBmp);
            Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            if (ink.useGradient) {
                float[] line = gradientLine(size, size, ink.gradientAngleDeg);
                fillPaint.setShader(new LinearGradient(
                        line[0], line[1], line[2], line[3],
                        parseColor(ink.gradientStart, 0xFF0A9396),
                        parseColor(ink.gradientEnd, 0xFF005F73),
                        Shader.TileMode.CLAMP));
            } else {
                fillPaint.setColor(parseColor(ink.solidColor, Color.BLACK));
            }
            tintCanvas.drawRect(0, 0, size, size, fillPaint);

            Paint maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
            maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
            tintCanvas.drawBitmap(overlayBmp, 0, 0, maskPaint);

            canvas.drawBitmap(tintBmp, 0, 0, null);
        } catch (Exception ignored) {
        } finally {
            if (overlayBmp != null) {
                overlayBmp.recycle();
            }
            if (tintBmp != null) {
                tintBmp.recycle();
            }
        }
    }

    /** כמו createCanvasGradient ב-qrGradients.js (זווית CSS → קו על הקנבס). */
    private static float[] gradientLine(int width, int height, int angleDeg) {
        double angleRad = Math.toRadians(angleDeg - 90);
        float cx = width / 2f;
        float cy = height / 2f;
        float dx = (float) (Math.cos(angleRad) * (width / 2.0));
        float dy = (float) (Math.sin(angleRad) * (height / 2.0));
        return new float[]{cx - dx, cy - dy, cx + dx, cy + dy};
    }

    private static Bitmap loadOverlayBitmap(Context context, String stickerId, int size) throws Exception {
        String num = stickerId == null ? "" : stickerId.replace("frame-", "");
        String resName = "sticker_overlay_" + num;
        String pkg = context.getPackageName();

        int pngRes = context.getResources().getIdentifier(resName, "drawable", pkg);
        if (pngRes != 0) {
            Bitmap src = BitmapFactory.decodeResource(context.getResources(), pngRes);
            if (src == null) {
                return null;
            }
            Bitmap scaled = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(scaled);
            c.drawBitmap(src, null, new Rect(0, 0, size, size),
                    new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
            src.recycle();
            return scaled;
        }

        int svgRes = context.getResources().getIdentifier(resName, "raw", pkg);
        if (svgRes == 0) {
            return null;
        }
        Bitmap overlayBmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas overlayCanvas = new Canvas(overlayBmp);
        SVG overlay = SVG.getFromResource(context, svgRes);
        overlay.setDocumentWidth(size);
        overlay.setDocumentHeight(size);
        RectF viewBox = overlay.getDocumentViewBox();
        if (viewBox != null) {
            overlay.setDocumentViewBox(viewBox.left, viewBox.top, viewBox.width(), viewBox.height());
        }
        overlay.renderToCanvas(overlayCanvas);
        return overlayBmp;
    }

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
