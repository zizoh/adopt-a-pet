package com.zizohanto.adoptapet.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONObject;

import java.util.HashMap;

public class SharedPreferenceUtils {
    public SharedPreferenceUtils() {
    }

    public static void setPrefDefaults(String key, String value, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getPrefDefaults(String key, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, null);
    }

    public static boolean prefExists(String key, Context context) {
        Boolean isExist = false;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences.contains(key)) {
            isExist = true;
        }
        return isExist;
    }

    public static String convertToJSONString(HashMap<String, String> input) {
        String jsonString = "";
        JSONObject jsonObject = new JSONObject(input);
        jsonString = jsonObject.toString();
        return jsonString;
    }
}
