package com.dynamiqr.android.core.utils;

import android.content.Context;
import androidx.annotation.StringRes;
import com.dynamiqr.android.R;
import com.dynamiqr.android.data.models.QrType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class QrTypeProvider {

    private static final Set<String> MORE_TYPE_IDS = new HashSet<>(Arrays.asList(
            "pdf", "email", "contact", "whatsapp", "phone", "sms", "wifi",
            "facebook", "instagram", "twitter", "linkedin", "youtube", "tiktok"
    ));

    private QrTypeProvider() {
    }

    private static String label(Context context, String key, String fallback) {
        @StringRes int resId = typeLabelRes(key);
        if (resId != 0) {
            return context.getString(resId);
        }
        return fallback;
    }

    @StringRes
    private static int typeLabelRes(String key) {
        if (key == null) {
            return 0;
        }
        switch (key) {
            case "url":
                return R.string.generator_types_url;
            case "pdfFile":
            case "pdf":
                return R.string.generator_types_pdf_file;
            case "email":
                return R.string.generator_types_email;
            case "contact":
                return R.string.generator_types_contact;
            case "whatsapp":
                return R.string.generator_types_whatsapp;
            case "phone":
                return R.string.generator_types_phone;
            case "sms":
                return R.string.generator_types_sms;
            case "wifi":
                return R.string.generator_types_wifi;
            case "facebook":
                return R.string.generator_types_facebook;
            case "instagram":
                return R.string.generator_types_instagram;
            case "twitterHe":
            case "twitter":
                return R.string.generator_types_twitter_he;
            case "linkedin":
                return R.string.generator_types_linkedin;
            case "youtube":
                return R.string.generator_types_youtube;
            case "tiktok":
                return R.string.generator_types_tiktok;
            default:
                return 0;
        }
    }

    public static List<QrType> getMainTypes(Context context) {
        List<QrType> types = new ArrayList<>();
        types.add(new QrType("url", label(context, "url", "Website"), R.drawable.ic_qr_type_url));
        return types;
    }

    public static List<QrType> getMoreTypes(Context context) {
        List<QrType> types = new ArrayList<>();
        types.add(new QrType("pdf", label(context, "pdfFile", "PDF file"), R.drawable.ic_qr_type_pdf));
        types.add(new QrType("email", label(context, "email", "Email"), R.drawable.ic_qr_type_email));
        types.add(new QrType("contact", label(context, "contact", "Contact"), R.drawable.ic_qr_type_contact));
        types.add(new QrType("whatsapp", label(context, "whatsapp", "WhatsApp"), R.drawable.ic_qr_type_whatsapp));
        types.add(new QrType("phone", label(context, "phone", "Phone"), R.drawable.ic_qr_type_phone));
        types.add(new QrType("sms", label(context, "sms", "SMS"), R.drawable.ic_qr_type_sms));
        types.add(new QrType("wifi", label(context, "wifi", "Wi-Fi"), R.drawable.ic_qr_type_wifi));
        types.add(new QrType("facebook", label(context, "facebook", "Facebook"), R.drawable.ic_qr_type_facebook));
        types.add(new QrType("instagram", label(context, "instagram", "Instagram"), R.drawable.ic_qr_type_instagram));
        types.add(new QrType("twitter", label(context, "twitterHe", "X / Twitter"), R.drawable.ic_qr_type_twitter));
        types.add(new QrType("linkedin", label(context, "linkedin", "LinkedIn"), R.drawable.ic_qr_type_linkedin));
        types.add(new QrType("youtube", label(context, "youtube", "YouTube"), R.drawable.ic_qr_type_youtube));
        types.add(new QrType("tiktok", label(context, "tiktok", "TikTok"), R.drawable.ic_qr_type_tiktok));
        return types;
    }

    public static List<QrType> getSelectableTypes(Context context) {
        return getTypes(context);
    }

    public static List<QrType> getTypes(Context context) {
        List<QrType> all = new ArrayList<>(getMainTypes(context));
        all.addAll(getMoreTypes(context));
        return all;
    }

    public static QrType findById(Context context, String id) {
        for (QrType type : getTypes(context)) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        return getMainTypes(context).get(0);
    }

    public static boolean isMoreType(String id) {
        return id != null && MORE_TYPE_IDS.contains(id);
    }
}
