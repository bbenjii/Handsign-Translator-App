package com.example.handsign_translator_app.utils;

public class ConstantsStatic {

    public static final int STABILITY_WINDOW = 10; // Number of readings in sliding window
    public static final int SENSOR_READ_DELAY_MS = 300;

    public static final double STABILITY_VARIANCE_THRESHOLD = 15;
    public static final double STABILITY_STANDARD_DEVIATION_THRESHOLD = 200;

    public static final int INPUT_FEATURES = 5;  // e.g., THUMB, INDEX, MIDDLE, RING, LITTLE
    public static final int OUTPUT_CLASSES = 11;  // Adjust based on your model
}
