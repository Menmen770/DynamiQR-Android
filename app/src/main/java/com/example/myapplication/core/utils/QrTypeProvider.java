package com.example.myapplication.core.utils;

import com.example.myapplication.R;
import com.example.myapplication.data.models.QrType;
import java.util.ArrayList;
import java.util.List;

public final class QrTypeProvider {

    private QrTypeProvider() {
    }

    public static List<QrType> getMainTypes() {
        List<QrType> types = new ArrayList<>();
        types.add(new QrType("url", "קישור", R.drawable.ic_qr_type_url));
        types.add(new QrType("pdf", "קובץ PDF", R.drawable.ic_qr_type_pdf));
        types.add(new QrType("email", "אימייל", R.drawable.ic_qr_type_email));
        types.add(new QrType("contact", "איש קשר", R.drawable.ic_qr_type_contact));
        types.add(new QrType("whatsapp", "וואטסאפ", R.drawable.ic_qr_type_whatsapp));
        types.add(new QrType("phone", "טלפון", R.drawable.ic_qr_type_phone));
        return types;
    }

    public static List<QrType> getMoreTypes() {
        List<QrType> types = new ArrayList<>();
        types.add(new QrType("sms", "SMS", R.drawable.ic_qr_type_sms));
        types.add(new QrType("wifi", "Wi-Fi", R.drawable.ic_qr_type_wifi));
        types.add(new QrType("facebook", "פייסבוק", R.drawable.ic_qr_type_facebook));
        types.add(new QrType("instagram", "אינסטגרם", R.drawable.ic_qr_type_instagram));
        types.add(new QrType("twitter", "X / טוויטר", R.drawable.ic_qr_type_twitter));
        types.add(new QrType("linkedin", "לינקדאין", R.drawable.ic_qr_type_linkedin));
        types.add(new QrType("youtube", "יוטיוב", R.drawable.ic_qr_type_youtube));
        types.add(new QrType("tiktok", "טיקטוק", R.drawable.ic_qr_type_tiktok));
        return types;
    }

    public static List<QrType> getTypes() {
        List<QrType> all = new ArrayList<>(getMainTypes());
        all.addAll(getMoreTypes());
        return all;
    }

    public static QrType findById(String id) {
        for (QrType type : getTypes()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        return getMainTypes().get(0);
    }

    public static boolean isMoreType(String id) {
        for (QrType type : getMoreTypes()) {
            if (type.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }
}
