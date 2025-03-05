package com.example.handsign_translator_app.ml_module;
import java.util.*;

public class DataPreprocessing {

    // Constants
    private final int STABILITY_WINDOW = 10; // Number of readings to track for stability
    private final double STABILITY_VARIANCE_THRESHOLD = 15; // Allowed variance for stable gestures
    private final int HOLD_TIME_MS = 500; // Time in milliseconds required to confirm a sign

    // Sensor readings (simulated)
    private int finger1_raw_data = 0;
    private int finger2_raw_data = 0;
    private int finger3_raw_data = 0;
    private int finger4_raw_data = 0;
    private int finger5_raw_data = 0;

    // Buffer to track recent readings
    private final Deque<int[]> flexReadingsHistory = new ArrayDeque<>();


    public void calculateStandardDeviation(){

    }

    public void calculateVariance(){

    }

    private void printFlexReadings(int[] readings) {
        StringBuilder readingString = new StringBuilder();
        for (int i = 0; i < readings.length; i++) {
            readingString.append("Flex").append(i + 1).append(": ").append(readings[i]).append("\t");
        }
        System.out.println(readingString);
    }



}
