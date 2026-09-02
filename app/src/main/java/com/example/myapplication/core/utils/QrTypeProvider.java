package com.example.myapplication.core.utils;

import com.example.myapplication.data.models.QrType;
import java.util.ArrayList;
import java.util.List;

public class QrTypeProvider {
    public static List<QrType> getTypes() {
        List<QrType> types = new ArrayList<>();
        types.add(new QrType("url", "קישור", android.R.drawable.ic_menu_send));
        types.add(new QrType("whatsapp", "וואטסאפ", android.R.drawable.stat_notify_chat));
        types.add(new QrType("wifi", "Wi-Fi", android.R.drawable.ic_menu_compass));
        types.add(new QrType("email", "אימייל", android.R.drawable.ic_dialog_email));
        return types;
    }
}
