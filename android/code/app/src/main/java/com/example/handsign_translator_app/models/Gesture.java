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
    private final String label;

    private final String customTranslation;

    /**
     * Constructor for creating a Gesture instance.
     */
    public Gesture(String translation, String imagePath, String label) {
        this.translation = translation;
        this.imagePath = imagePath;
        this.customTranslation = "";
        this.label = label;

    }

    /**
     * Constructor for creating a Gesture instance with custom translation.
     */
    public Gesture(String translation, String imagePath, String label, String customTranslation) {
        this.translation = translation;
        this.imagePath = imagePath;
        this.customTranslation = customTranslation;
        this.label = label;

    }

    public String getLabel(){return label;}

    public String getCustomTranslation(){return customTranslation;}

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