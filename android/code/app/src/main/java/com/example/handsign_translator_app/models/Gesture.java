package com.example.handsign_translator_app.models;

public class Gesture {
    private final String translation;
    private final String imagePath;

    public Gesture(String translation, String imagePath) {
        this.translation = translation;
        this.imagePath = imagePath;
    }

    public String getTranslation() {
        return translation;
    }

    public String getImagePath() {
        return imagePath;
    }
}
