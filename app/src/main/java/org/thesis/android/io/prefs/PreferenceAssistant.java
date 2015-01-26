package org.thesis.android.io.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public final class PreferenceAssistant {

    private PreferenceAssistant() {
        throw new IllegalStateException("Do not instantiate " + getClass().getName());
    }

    public static final String PROPERTY_APP_VERSION = "PROPERTY_APP_VERSION";
    public static final String PROPERTY_REG_ID = "PROPERTY_REG_ID";
    public static final String PREF_USER_LEARNED_DRAWER = "PREF_USER_LEARNED_DRAWER";
    public static final String PREF_USER_HAS_SET_NAME = "PREF_USER_HAS_SET_NAME";
    public static final String PREF_USER_NAME = "PREF_USER_NAME";

    public static void saveSharedString(Context ctx, String settingName, String settingValue) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(settingName, settingValue);
        editor.apply();
    }

    public static String readSharedString(Context ctx, String settingName, String defaultValue) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        return sharedPref.getString(settingName, defaultValue);
    }

    public static void saveSharedBoolean(Context ctx, String settingName, Boolean settingValue) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(settingName, settingValue);
        editor.apply();
    }

    public static Boolean readSharedBoolean(Context ctx, String settingName, Boolean defaultValue) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        return sharedPref.getBoolean(settingName, defaultValue);
    }

    public static void saveSharedInteger(Context ctx, String settingName, Integer settingValue) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(settingName, settingValue);
        editor.apply();
    }


    public static Integer readSharedInteger(Context ctx, String settingName, Integer defaultValue) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        return sharedPref.getInt(settingName, defaultValue);
    }
}
