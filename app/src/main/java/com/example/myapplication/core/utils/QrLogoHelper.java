package com.example.myapplication.core.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class QrLogoHelper {

    private QrLogoHelper() {
    }

    public static String rawSvgToDataUrl(Context context, int rawResId, int sizePx) {
        return rawSvgToDataUrl(context, rawResId, sizePx, 1f);
    }

    /**
     * כמו ב-RN ({@code svgModuleToDataUrl}): שולחים SVG raw ל-API.
     * השרת מרנדר עם sharp/canvas — איכות ויישור נכונים על ה-QR.
     * אם קריאת ה-SVG נכשלת, נופלים ל-PNG מקומי.
     */
    public static String rawSvgToDataUrl(Context context, int rawResId, int sizePx, float unusedInset) {
        String svgDataUrl = readSvgAsDataUrl(context, rawResId);
        if (svgDataUrl != null) {
            return svgDataUrl;
        }
        return rasterPngDataUrl(context, rawResId, sizePx);
    }

    private static String readSvgAsDataUrl(Context context, int rawResId) {
        try (InputStream in = context.getResources().openRawResource(rawResId)) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int n;
            while ((n = in.read(chunk)) >= 0) {
                buffer.write(chunk, 0, n);
            }
            String svg = new String(buffer.toByteArray(), StandardCharsets.UTF_8).trim();
            if (svg.isEmpty()) {
                return null;
            }
            // encodeURIComponent-equivalent
            String encoded = URLEncoder.encode(svg, "UTF-8").replace("+", "%20");
            return "data:image/svg+xml;charset=utf-8," + encoded;
        } catch (Exception e) {
            return null;
        }
    }

    private static String rasterPngDataUrl(Context context, int rawResId, int sizePx) {
        try {
            Bitmap full = SvgThumbHelper.renderSvgFullBleed(context, rawResId, sizePx);
            if (full == null) {
                return null;
            }
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            full.compress(Bitmap.CompressFormat.PNG, 100, stream);
            String base64 = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP);
            return "data:image/png;base64," + base64;
        } catch (Exception e) {
            return null;
        }
    }
}
