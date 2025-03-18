package com.example.unipiplishopping;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.widget.TextView;

public class UserPreferences {
    private static final String PREFS_NAME = "SettingsPrefs";
    private static final String KEY_BACKGROUND_COLOR = "background_color";
    private static final String KEY_FONT_SIZE = "font_size";

    public static void saveBackgroundColor(Context context, String color) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_BACKGROUND_COLOR, color);
        editor.apply();
    }

    public static void saveFontSize(Context context, float fontSize) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat(KEY_FONT_SIZE, fontSize);
        editor.apply();
    }

    public static String getBackgroundColor(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_BACKGROUND_COLOR, "#FFFFFF"); // Default: White
    }

    public static float getFontSize(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getFloat(KEY_FONT_SIZE, 18f); // Default: 18sp
    }

    // Εφαρμογή των ρυθμίσεων στις σελίδες
    public static void applySettings(Context context, TextView[] textViews, android.view.View layout) {
        // Εφαρμογή background color
        layout.setBackgroundColor(Color.parseColor(getBackgroundColor(context)));

        // Εφαρμογή μεγέθους γραμματοσειράς σε όλα τα TextView
        float fontSize = getFontSize(context);
        for (TextView textView : textViews) {
            textView.setTextSize(fontSize);
        }
    }
}