package com.example.myapplication.core.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Base64;
import com.caverock.androidsvg.SVG;
import java.io.ByteArrayOutputStream;

public final class QrLogoHelper {

    private QrLogoHelper() {
    }

    public static String rawSvgToDataUrl(Context context, int rawResId, int sizePx) {
        return rawSvgToDataUrl(context, rawResId, sizePx, 1f);
    }

    public static String rawSvgToDataUrl(Context context, int rawResId, int sizePx, float insetScale) {
        try {
            Bitmap bitmap = SvgThumbHelper.renderSvgResource(context, rawResId, sizePx);
            if (bitmap == null) {
                return null;
            }
            if (insetScale > 0 && insetScale < 1f) {
                int inset = Math.round(sizePx * (1f - insetScale) / 2f);
                Bitmap padded = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(padded);
                canvas.drawBitmap(bitmap, inset, inset, null);
                bitmap = padded;
            }
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            String base64 = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP);
            return "data:image/png;base64," + base64;
        } catch (Exception e) {
            return null;
        }
    }
}
