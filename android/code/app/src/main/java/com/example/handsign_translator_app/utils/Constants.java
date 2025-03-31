package com.example.handsign_translator_app.utils;

public class Constants {
    public static final int STABILITY_WINDOW = 100; // Number of readings in sliding window
    public static final int SENSOR_READ_DELAY_MS = 20;

    public static final double STABILITY_VARIANCE_THRESHOLD = 15;
    public static final double STABILITY_STANDARD_DEVIATION_THRESHOLD = 200;

    public static final int INPUT_FEATURES = 11;  // e.g., THUMB, INDEX, MIDDLE, RING, LITTLE
    public static final int OUTPUT_CLASSES = 4;  // Adjust based on your model
}
