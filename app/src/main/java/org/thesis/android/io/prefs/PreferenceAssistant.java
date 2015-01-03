package org.thesis.android.io.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public final class PreferenceAssistant {

    private PreferenceAssistant() {
        throw new IllegalStateException("Do not instantiate " + getClass().getName());
    }

    public static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
    public static final String PREF_USER_HAS_SET_NAME = "navigation_drawer_learned";
    public static final String PREF_USER_NAME = "navigation_drawer_learned";

    public static void saveSharedSetting(Context ctx, String settingName, String settingValue) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(settingName, settingValue);
        editor.apply();
    }

    public static String readSharedSetting(Context ctx, String settingName, String defaultValue) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        return sharedPref.getString(settingName, defaultValue);
    }

    public static void saveSharedSetting(Context ctx, String settingName, Boolean settingValue) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(settingName, settingValue);
        editor.apply();
    }

    public static Boolean readSharedSetting(Context ctx, String settingName, Boolean defaultValue) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        return sharedPref.getBoolean(settingName, defaultValue);
    }
}
