package com.example.myapplication.core.i18n;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import com.example.myapplication.data.local.AppPreferences;
import java.util.Locale;

/** עוטף Context בשפת האפליקציה (he/en) — אמין יותר מ־setApplicationLocales בלבד. */
public final class LocaleHelper {

    private LocaleHelper() {
    }

    public static Context wrap(Context context) {
        if (context == null) {
            return null;
        }
        String lang = AppPreferences.normalizeLang(new AppPreferences(context).getLanguage());
        return wrap(context, lang);
    }

    public static Context wrap(Context context, String languageTag) {
        String lang = AppPreferences.normalizeLang(languageTag);
        Locale locale = Locale.forLanguageTag(lang);
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(new LocaleList(locale));
        } else {
            config.locale = locale;
        }
        config.setLayoutDirection(locale);
        return context.createConfigurationContext(config);
    }
}
