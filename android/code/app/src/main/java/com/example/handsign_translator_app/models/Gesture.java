package com.example.handsign_translator_app.models;

/**
 * Model class representing a Gesture.
 * Contains information such as the translated text and the associated image path.
 */
public class Gesture {
    // Translation of the gesture (e.g., the word or phrase corresponding to the sign)
    private final String translation;
    // Path to the image resource that represents the gesture visually
    private final String imagePath;

    /**
     * Constructor for creating a Gesture instance.
     */
    public Gesture(String translation, String imagePath) {
        this.translation = translation;
        this.imagePath = imagePath;
    }

    /**
     * Retrieves the translation of the gesture.
     */
    public String getTranslation() {
        return translation;
    }

    /**
     * Retrieves the image path associated with the gesture.
     */
    public String getImagePath() {
        return imagePath;
    }
}