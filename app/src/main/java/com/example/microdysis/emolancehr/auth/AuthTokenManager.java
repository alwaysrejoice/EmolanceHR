package com.example.microdysis.emolancehr.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.example.microdysis.emolancehr.util.Constants;

/**
 * Created by hui-jou on 7/4/17.
 */

public class AuthTokenManager {

    private static final String TOKEN_KEY = "token-key";

    private static String CURRENT_TOKEN;

    public static void putToken(Context context, String username, String password) {
        final String credentials = username + ":" + password;
        String token = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        putToken(context, token);
    }

    public static void putToken(Context context, String token) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                Constants.SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TOKEN_KEY, token);
        editor.commit();
        CURRENT_TOKEN = token;
    }

    public static String getToken(Context context) {
        if (CURRENT_TOKEN == null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(
                    Constants.SP_NAME, Context.MODE_PRIVATE);
            CURRENT_TOKEN = sharedPreferences.getString(TOKEN_KEY, null);
        }
        return CURRENT_TOKEN;
    }
}

