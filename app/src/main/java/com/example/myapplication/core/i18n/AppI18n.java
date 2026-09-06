package com.example.myapplication.core.i18n;

import android.content.Context;
import android.content.res.AssetManager;
import com.example.myapplication.data.local.AppPreferences;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * תרגומים כמו באתר / RN — קבצי JSON מ־shared/locales ב־assets.
 */
public final class AppI18n {

    private static final String[] NAMESPACES = {
            "common", "auth", "dashboard", "generator", "learn", "brand"
    };

    private static volatile String loadedLang;
    private static final Map<String, JsonObject> namespaces = new HashMap<>();

    private AppI18n() {
    }

    public static synchronized void init(Context context) {
        reload(context);
    }

    public static synchronized void reload(Context context) {
        if (context == null) {
            return;
        }
        Context app = context.getApplicationContext();
        String lang = AppPreferences.normalizeLang(new AppPreferences(app).getLanguage());
        load(app, lang);
    }

    private static void load(Context context, String lang) {
        namespaces.clear();
        AssetManager assets = context.getAssets();
        for (String ns : NAMESPACES) {
            String path = "locales/" + lang + "/" + ns + ".json";
            JsonObject obj = readJson(assets, path);
            if (obj == null && !AppPreferences.LANG_HE.equals(lang)) {
                obj = readJson(assets, "locales/he/" + ns + ".json");
            }
            if (obj != null) {
                namespaces.put(ns, obj);
            }
        }
        loadedLang = lang;
    }

    private static JsonObject readJson(AssetManager assets, String path) {
        try (InputStream in = assets.open(path);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            JsonElement el = JsonParser.parseString(sb.toString());
            return el != null && el.isJsonObject() ? el.getAsJsonObject() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static void ensureLoaded(Context context) {
        Context app = context != null ? context.getApplicationContext() : null;
        if (app == null) {
            return;
        }
        String lang = AppPreferences.normalizeLang(new AppPreferences(app).getLanguage());
        if (!lang.equals(loadedLang) || namespaces.isEmpty()) {
            synchronized (AppI18n.class) {
                if (!lang.equals(loadedLang) || namespaces.isEmpty()) {
                    load(app, lang);
                }
            }
        }
    }

    public static String lang(Context context) {
        ensureLoaded(context);
        return loadedLang != null ? loadedLang : AppPreferences.LANG_HE;
    }

    public static boolean isRtl(Context context) {
        return !AppPreferences.LANG_EN.equals(lang(context));
    }

    /** t("generator", "screen.title") */
    public static String t(Context context, String ns, String path) {
        return t(context, ns, path, path);
    }

    public static String t(Context context, String ns, String path, String fallback) {
        ensureLoaded(context);
        JsonObject root = namespaces.get(ns);
        if (root == null) {
            return fallback;
        }
        JsonElement cur = root;
        for (String part : path.split("\\.")) {
            if (cur == null || !cur.isJsonObject() || !cur.getAsJsonObject().has(part)) {
                return fallback;
            }
            cur = cur.getAsJsonObject().get(part);
        }
        if (cur == null || cur.isJsonNull()) {
            return fallback;
        }
        if (cur.isJsonPrimitive()) {
            return cur.getAsString();
        }
        return fallback;
    }

    /** החלפת {{key}} בערכים. */
    public static String t(Context context, String ns, String path, Map<String, String> vars) {
        String value = t(context, ns, path, path);
        if (vars == null || vars.isEmpty()) {
            return value;
        }
        for (Map.Entry<String, String> e : vars.entrySet()) {
            value = value.replace("{{" + e.getKey() + "}}", e.getValue() != null ? e.getValue() : "");
        }
        return value;
    }

    /** רשימת מחרוזות מ־JSON array (למשל learn.benefits.items). */
    public static List<String> tList(Context context, String ns, String path) {
        ensureLoaded(context);
        JsonObject root = namespaces.get(ns);
        List<String> out = new ArrayList<>();
        if (root == null) {
            return out;
        }
        JsonElement cur = root;
        for (String part : path.split("\\.")) {
            if (cur == null || !cur.isJsonObject() || !cur.getAsJsonObject().has(part)) {
                return out;
            }
            cur = cur.getAsJsonObject().get(part);
        }
        if (cur != null && cur.isJsonArray()) {
            JsonArray arr = cur.getAsJsonArray();
            for (JsonElement el : arr) {
                if (el != null && el.isJsonPrimitive()) {
                    out.add(el.getAsString());
                } else if (el != null && el.isJsonObject()) {
                    JsonObject obj = el.getAsJsonObject();
                    if (obj.has("title") && obj.has("text")) {
                        out.add(obj.get("title").getAsString() + "\n" + obj.get("text").getAsString());
                    }
                }
            }
        }
        return out;
    }

    /** אובייקטים במערך עם title/text. */
    public static List<String[]> tTitleTextList(Context context, String ns, String path) {
        ensureLoaded(context);
        JsonObject root = namespaces.get(ns);
        List<String[]> out = new ArrayList<>();
        if (root == null) {
            return out;
        }
        JsonElement cur = root;
        for (String part : path.split("\\.")) {
            if (cur == null || !cur.isJsonObject() || !cur.getAsJsonObject().has(part)) {
                return out;
            }
            cur = cur.getAsJsonObject().get(part);
        }
        if (cur != null && cur.isJsonArray()) {
            for (JsonElement el : cur.getAsJsonArray()) {
                if (el != null && el.isJsonObject()) {
                    JsonObject obj = el.getAsJsonObject();
                    String title = obj.has("title") ? obj.get("title").getAsString() : "";
                    String text = obj.has("text") ? obj.get("text").getAsString() : "";
                    out.add(new String[]{title, text});
                }
            }
        }
        return out;
    }
}
