package com.dynamiqr.android.core.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Logo payload for generate-qr — always PNG like the website (browser rasterize),
 * so sharp on the server never mis-orients complex SVGs.
 */
public final class QrLogoHelper {

    private static final int PRESET_LOGO_PX = 512;
    private static final int DEVICE_LOGO_MAX_PX = 400;

    private QrLogoHelper() {
    }

    public static String rawSvgToDataUrl(Context context, int rawResId, int sizePx) {
        return rawSvgToDataUrl(context, rawResId, sizePx, 1f);
    }

    /**
     * Rasterize preset SVG to a centered square PNG (web parity).
     * {@code insetScale} shrinks the glyph inside the square (PresetLogos.rasterInset).
     */
    public static String rawSvgToDataUrl(Context context, int rawResId, int sizePx, float insetScale) {
        int px = sizePx > 0 ? sizePx : PRESET_LOGO_PX;
        String png = rasterPngDataUrl(context, rawResId, px, insetScale);
        if (png != null) {
            return png;
        }
        // Last resort — may mis-orient on server; prefer never hitting this.
        return rasterPngDataUrl(context, rawResId, px, 1f);
    }

    /** Gallery / device image → upright square-ish PNG data URL. */
    public static String uriToPngDataUrl(Context context, Uri uri) {
        if (context == null || uri == null) {
            return null;
        }
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            if (in == null) {
                return null;
            }
            Bitmap decoded = BitmapFactory.decodeStream(in);
            if (decoded == null) {
                return null;
            }
            Bitmap oriented = applyExifOrientation(context, uri, decoded);
            if (oriented != decoded) {
                decoded.recycle();
            }
            Bitmap scaled = scaleDown(oriented, DEVICE_LOGO_MAX_PX);
            if (scaled != oriented) {
                oriented.recycle();
            }
            Bitmap square = centerOnSquare(scaled);
            if (square != scaled) {
                scaled.recycle();
            }
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            square.compress(Bitmap.CompressFormat.PNG, 92, stream);
            square.recycle();
            String base64 = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP);
            return "data:image/png;base64," + base64;
        } catch (Exception e) {
            return null;
        }
    }

    private static Bitmap applyExifOrientation(Context context, Uri uri, Bitmap src) {
        try (InputStream exifStream = context.getContentResolver().openInputStream(uri)) {
            if (exifStream == null) {
                return src;
            }
            ExifInterface exif = new ExifInterface(exifStream);
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int degrees = 0;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                degrees = 90;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                degrees = 180;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                degrees = 270;
            }
            if (degrees == 0) {
                return src;
            }
            Matrix matrix = new Matrix();
            matrix.postRotate(degrees);
            return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
        } catch (Exception e) {
            return src;
        }
    }

    private static Bitmap centerOnSquare(Bitmap src) {
        int side = Math.max(src.getWidth(), src.getHeight());
        if (src.getWidth() == side && src.getHeight() == side) {
            return src;
        }
        Bitmap out = Bitmap.createBitmap(side, side, Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(out);
        float left = (side - src.getWidth()) / 2f;
        float top = (side - src.getHeight()) / 2f;
        canvas.drawBitmap(src, left, top, null);
        return out;
    }

    private static Bitmap scaleDown(Bitmap src, int maxEdge) {
        int w = src.getWidth();
        int h = src.getHeight();
        int max = Math.max(w, h);
        if (max <= maxEdge) {
            return src;
        }
        float scale = maxEdge / (float) max;
        int nw = Math.max(1, Math.round(w * scale));
        int nh = Math.max(1, Math.round(h * scale));
        return Bitmap.createScaledBitmap(src, nw, nh, true);
    }

    private static String rasterPngDataUrl(Context context, int rawResId, int sizePx, float insetScale) {
        try {
            Bitmap full = SvgThumbHelper.renderSvgForQrLogo(context, rawResId, sizePx, insetScale);
            if (full == null) {
                return null;
            }
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            full.compress(Bitmap.CompressFormat.PNG, 100, stream);
            full.recycle();
            String base64 = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP);
            return "data:image/png;base64," + base64;
        } catch (Exception e) {
            return null;
        }
    }
}
