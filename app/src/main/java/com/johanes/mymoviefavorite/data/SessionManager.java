package com.johanes.mymoviefavorite.data;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

public final class SessionManager {

    private static final String PREFS_NAME = "user_session";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";

    private SessionManager() {
    }

    public static void saveSession(Context context, String name, String email) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean(KEY_LOGGED_IN, true)
                .putString(KEY_USER_NAME, name)
                .putString(KEY_USER_EMAIL, email)
                .apply();
    }

    public static void clearSession(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .remove(KEY_LOGGED_IN)
                .remove(KEY_USER_NAME)
                .remove(KEY_USER_EMAIL)
                .apply();
    }

    public static boolean isLoggedIn(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_LOGGED_IN, false);
    }

    @Nullable
    public static String getUserName(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER_NAME, null);
    }

    @Nullable
    public static String getUserEmail(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER_EMAIL, null);
    }
}
