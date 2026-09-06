package com.example.myapplication.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import com.example.myapplication.core.i18n.AppI18n;

public final class AppPreferences {

    private static final String PREFS = "dynamiqr_prefs";
    private static final String KEY_DARK = "dark_mode";
    private static final String KEY_LANG = "language";

    public static final String LANG_HE = "he";
    public static final String LANG_EN = "en";

    private final SharedPreferences prefs;
    private final Context appContext;

    public AppPreferences(Context context) {
        Context app = context.getApplicationContext();
        appContext = app != null ? app : context;
        prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public boolean isDarkMode() {
        return prefs.getBoolean(KEY_DARK, false);
    }

    public void setDarkMode(boolean dark) {
        prefs.edit().putBoolean(KEY_DARK, dark).apply();
        AppCompatDelegate.setDefaultNightMode(
                dark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    public void applyStoredTheme() {
        AppCompatDelegate.setDefaultNightMode(
                isDarkMode() ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    public String getLanguage() {
        return normalizeLang(prefs.getString(KEY_LANG, LANG_HE));
    }

    /**
     * שומר שפה, מרענן AppI18n ומחיל AppCompat locales.
     * לא לקרוא ל־Activity.recreate() — setApplicationLocales כבר מרענן Activities.
     */
    public void setLanguage(String lang) {
        String normalized = normalizeLang(lang);
        prefs.edit().putString(KEY_LANG, normalized).commit();
        AppI18n.init(appContext);
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(normalized));
    }

    public void applyStoredLanguage() {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(getLanguage()));
        AppI18n.init(appContext);
    }

    public static String normalizeLang(String lang) {
        if (LANG_EN.equals(lang)) {
            return LANG_EN;
        }
        return LANG_HE;
    }

    public static String languageLabel(String lang) {
        if (LANG_EN.equals(normalizeLang(lang))) {
            return "English";
        }
        return "עברית";
    }

    public static String[] supportedLanguages() {
        return new String[]{LANG_HE, LANG_EN};
    }
}
