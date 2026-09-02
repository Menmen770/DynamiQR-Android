package com.example.myapplication.core.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import com.caverock.androidsvg.SVG;

public final class SvgThumbHelper {

    private SvgThumbHelper() {
    }

    public static Bitmap renderSvgResource(Context context, int rawResId, int sizePx) {
        try {
            SVG svg = SVG.getFromResource(context, rawResId);
            Bitmap bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
            bitmap.eraseColor(Color.TRANSPARENT);
            Canvas canvas = new Canvas(bitmap);

            RectF viewBox = svg.getDocumentViewBox();
            float docW = viewBox != null ? viewBox.width() : svg.getDocumentWidth();
            float docH = viewBox != null ? viewBox.height() : svg.getDocumentHeight();
            if (docW <= 0) {
                docW = sizePx;
            }
            if (docH <= 0) {
                docH = sizePx;
            }

            float scale = Math.min(sizePx / docW, sizePx / docH);
            float tx = (sizePx - docW * scale) / 2f;
            float ty = (sizePx - docH * scale) / 2f;
            canvas.translate(tx, ty);
            canvas.scale(scale, scale);
            if (viewBox != null) {
                canvas.translate(-viewBox.left, -viewBox.top);
            }
            svg.renderToCanvas(canvas);
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }
}
