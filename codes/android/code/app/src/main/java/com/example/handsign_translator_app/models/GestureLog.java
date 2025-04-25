package com.example.handsign_translator_app.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GestureLog {
    private String gestureLabel;
    private String translation;
    private long timestamp;

    public GestureLog(String gestureLabel, String translation, long timestamp) {
        this.gestureLabel = gestureLabel;
        this.translation = translation;
        this.timestamp = timestamp;
    }

    public String getGestureLabel() {
        return gestureLabel;
    }

    public String getTranslation() {
        return translation;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getFormattedDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    @Override
    public String toString() {
        return String.format("%s - %s: %s", getFormattedDateTime(), gestureLabel, translation);
    }
} 