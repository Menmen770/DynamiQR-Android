package com.dynamiqr.android.core.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Picture;
import android.graphics.RectF;
import com.caverock.androidsvg.SVG;

public final class SvgThumbHelper {

    /** כמה מהכפתור ממלא הלוגו — אחיד לכל הלוגואים */
    private static final float LOGO_FILL_RATIO = 0.78f;

    private SvgThumbHelper() {
    }

    public static Bitmap renderSvgResource(Context context, int rawResId, int sizePx) {
        return renderSvgResource(context, rawResId, sizePx, 1f);
    }

    /**
     * רינדור ממורכז לקנבס מרובע.
     * משתמשים ב-{@link SVG#renderToPicture()} ואז scale ידני — בלי double-transform
     * של renderToCanvas אחרי translate/scale (שגרם ללוגואים קטנים/שבורים בפינה).
     *
     * @param visualScale מקדם נוסף (למשל לאיזון X/Bit עם inset נמוך בתצוגת כפתור)
     */
    public static Bitmap renderSvgResource(Context context, int rawResId, int sizePx, float visualScale) {
        try {
            SVG svg = SVG.getFromResource(context, rawResId);
            Bitmap bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
            bitmap.eraseColor(Color.TRANSPARENT);
            Canvas canvas = new Canvas(bitmap);

            float fill = sizePx * LOGO_FILL_RATIO * Math.max(0.4f, Math.min(1.25f, visualScale));
            drawSvgCentered(svg, canvas, sizePx, fill);
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    /** Full-bleed render for stickers / fallback. */
    public static Bitmap renderSvgFullBleed(Context context, int rawResId, int sizePx) {
        try {
            SVG svg = SVG.getFromResource(context, rawResId);
            Bitmap bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
            bitmap.eraseColor(Color.TRANSPARENT);
            Canvas canvas = new Canvas(bitmap);
            drawSvgCentered(svg, canvas, sizePx, sizePx);
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Square PNG for the generate-qr API — glyph centered with optional inset
     * (matches web {@code rasterizeSvgDataUrlToPng} + preset rasterInset).
     */
    public static Bitmap renderSvgForQrLogo(Context context, int rawResId, int sizePx, float insetScale) {
        try {
            SVG svg = SVG.getFromResource(context, rawResId);
            Bitmap bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
            bitmap.eraseColor(Color.TRANSPARENT);
            Canvas canvas = new Canvas(bitmap);
            float inset = Math.max(0.35f, Math.min(1f, insetScale));
            float fill = sizePx * inset;
            drawSvgCentered(svg, canvas, sizePx, fill);
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    private static void drawSvgCentered(SVG svg, Canvas canvas, int canvasSize, float targetFill) {
        Picture picture = null;
        try {
            picture = svg.renderToPicture();
        } catch (Exception ignored) {
        }

        if (picture != null && picture.getWidth() > 0 && picture.getHeight() > 0) {
            float pw = picture.getWidth();
            float ph = picture.getHeight();
            float scale = Math.min(targetFill / pw, targetFill / ph);
            float dw = pw * scale;
            float dh = ph * scale;
            float left = (canvasSize - dw) / 2f;
            float top = (canvasSize - dh) / 2f;
            canvas.save();
            canvas.translate(left, top);
            canvas.scale(scale, scale);
            canvas.drawPicture(picture);
            canvas.restore();
            return;
        }

        // Fallback: setDocumentWidth/Height + renderToCanvas (בלי transform כפול)
        RectF viewBox = svg.getDocumentViewBox();
        float docW = viewBox != null ? viewBox.width() : svg.getDocumentWidth();
        float docH = viewBox != null ? viewBox.height() : svg.getDocumentHeight();
        if (docW <= 0) {
            docW = targetFill;
        }
        if (docH <= 0) {
            docH = targetFill;
        }
        float scale = Math.min(targetFill / docW, targetFill / docH);
        float dw = docW * scale;
        float dh = docH * scale;
        float left = (canvasSize - dw) / 2f;
        float top = (canvasSize - dh) / 2f;

        svg.setDocumentWidth(dw);
        svg.setDocumentHeight(dh);
        canvas.save();
        canvas.translate(left, top);
        svg.renderToCanvas(canvas);
        canvas.restore();
    }
}
