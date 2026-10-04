package com.dynamiqr.android.core.utils;

import com.dynamiqr.android.BuildConfig;
import com.dynamiqr.android.data.models.QrCode;
import com.dynamiqr.android.data.models.QrStyle;
import java.util.HashMap;
import java.util.Map;

public final class SavedQrPreviewHelper {

    private static final int PREVIEW_WIDTH = 200;
    private static final int EXPORT_WIDTH = 2400;

    private SavedQrPreviewHelper() {
    }

    public static Map<String, Object> buildGenerateBody(QrCode row) {
        return buildGenerateBody(row, PREVIEW_WIDTH);
    }

    public static Map<String, Object> buildExportBody(QrCode row) {
        return buildGenerateBody(row, EXPORT_WIDTH);
    }

    public static Map<String, Object> buildGenerateBody(QrCode row, int width) {
        if (row == null) {
            return null;
        }
        String text = effectiveEncodedText(row);
        if (text.isEmpty()) {
            return null;
        }

        QrStyle style = row.getStyle();
        String fg = style != null && style.getFgColor() != null ? style.getFgColor() : "#000000";
        String qrColorMode = style != null && style.getQrColorMode() != null
                ? style.getQrColorMode() : "solid";
        String dotsType = style != null && style.getDotsType() != null
                ? style.getDotsType() : "square";
        String cornersType = style != null && style.getCornersType() != null
                ? style.getCornersType() : "square";
        String logoShape = style != null && style.getLogoShape() != null
                ? style.getLogoShape() : "overlay";
        String errorLevel = normalizeEcl(
                style != null ? style.getErrorCorrectionLevel() : null);
        String stickerType = style != null && style.getStickerType() != null
                ? style.getStickerType() : "none";
        double logoInset = style != null ? style.getLogoInsetScale() : 1.0;

        Map<String, Object> body = new HashMap<>();
        body.put("text", text);
        body.put("width", width);
        body.put("color", fg);
        body.put("bgColor", buildBgForApi(style, stickerType));
        body.put("dotsType", dotsType);
        body.put("cornersType", cornersType);
        body.put("logoShape", logoShape);
        body.put("errorCorrectionLevel", errorLevel);

        if ("gradient".equals(qrColorMode) && style != null && style.getDotsGradient() != null) {
            body.put("dotsGradient", style.getDotsGradient());
            Object primary = extractGradientPrimary(style.getDotsGradient(), fg);
            body.put("color", primary);
        }

        if (style != null && style.getLogoUrl() != null && !style.getLogoUrl().isEmpty()
                && style.getLogoUrl().length() < 400_000) {
            body.put("image", style.getLogoUrl());
            body.put("logoInsetScale", logoInset);
        }

        return body;
    }

    public static String bgColorForComposite(QrCode row) {
        QrStyle style = row != null ? row.getStyle() : null;
        if (style == null || style.getBgColor() == null) {
            return "#ffffff";
        }
        return style.getBgColor();
    }

    public static String stickerType(QrCode row) {
        QrStyle style = row != null ? row.getStyle() : null;
        if (style == null || style.getStickerType() == null) {
            return "none";
        }
        return style.getStickerType();
    }

    public static String fgColorForComposite(QrCode row) {
        QrStyle style = row != null ? row.getStyle() : null;
        if (style == null || style.getFgColor() == null || style.getFgColor().isEmpty()) {
            return "#111111";
        }
        return style.getFgColor();
    }

    public static QrPreviewCompositor.StickerInk stickerInkFor(QrCode row) {
        QrStyle style = row != null ? row.getStyle() : null;
        String fg = fgColorForComposite(row);
        if (style != null && "gradient".equals(style.getQrColorMode())
                && style.getDotsGradient() != null) {
            return QrPreviewCompositor.StickerInk.fromDotsGradient(style.getDotsGradient(), fg);
        }
        return QrPreviewCompositor.StickerInk.solid(fg);
    }

    private static String effectiveEncodedText(QrCode row) {
        if ("dynamic".equals(row.getLinkMode()) && row.getPublicSlug() != null
                && !row.getPublicSlug().isEmpty()) {
            String base = BuildConfig.API_BASE_URL.replaceAll("/$", "");
            return base + "/api/r/" + row.getPublicSlug().trim().toLowerCase();
        }
        if (row.getQrValue() != null && !row.getQrValue().trim().isEmpty()) {
            return row.getQrValue().trim();
        }
        return QrEncoder.encode(row.getQrType(), row.getQrValue());
    }

    private static String buildBgForApi(QrStyle style, String stickerType) {
        if (stickerType != null && !"none".equals(stickerType)) {
            return "transparent";
        }
        String bgMode = style != null ? style.getBgColorMode() : "solid";
        if ("gradient".equals(bgMode) || "none".equals(bgMode)) {
            return "transparent";
        }
        return style != null && style.getBgColor() != null ? style.getBgColor() : "#ffffff";
    }

    private static String normalizeEcl(String level) {
        if ("L".equals(level) || "M".equals(level) || "Q".equals(level) || "H".equals(level)) {
            return level;
        }
        return "Q";
    }

    @SuppressWarnings("unchecked")
    private static Object extractGradientPrimary(Map<String, Object> gradient, String fallback) {
        if (gradient == null) {
            return fallback;
        }
        Object stops = gradient.get("colorStops");
        if (stops instanceof Iterable) {
            for (Object stop : (Iterable<?>) stops) {
                if (stop instanceof Map) {
                    Object color = ((Map<?, ?>) stop).get("color");
                    if (color instanceof String) {
                        return color;
                    }
                }
                break;
            }
        }
        return fallback;
    }
}
