package com.example.handsign_translator_app.ml_module;

import com.example.handsign_translator_app.utils.Constants;

import java.util.*;

/**
 * Utility class to check whether a gesture is stable based on a sliding window of sensor readings.
 */
public class GestureStabilityChecker {
    // Number of readings to consider for stability
    private static final int STABILITY_WINDOW = Constants.STABILITY_WINDOW;
    // Allowed variance threshold for a gesture to be considered stable
    private static final double STABILITY_VARIANCE_THRESHOLD = Constants.STABILITY_VARIANCE_THRESHOLD;
    // Allowed standard deviation threshold for a gesture to be considered stable
    private static final double STABILITY_STANDARD_DEVIATION_THRESHOLD = Constants.STABILITY_STANDARD_DEVIATION_THRESHOLD;

    // Time in milliseconds required to confirm a gesture; though declared, it's not used in the current code
    private static final int HOLD_TIME_MS = 500;

    /**
     * Checks if the gesture represented by the flexReadingsHistory is stable.
     */
    public static boolean isGestureStable(Deque<int[]> flexReadingsHistory) {
        // If not enough data has been collected, consider gesture unstable.
        if (flexReadingsHistory.size() < STABILITY_WINDOW) return false;

        // Create lists for each finger's data (assumes 5 fingers)
        List<List<Integer>> fingerData = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) {
            fingerData.add(new ArrayList<>());
        }

        // Populate each finger's data list from the flexReadingsHistory
        for (int[] readings : flexReadingsHistory) {
            for (int i = 0; i < 5; i++) {
                fingerData.get(i).add(readings[i]);
            }
        }

        // For each finger, compute the standard deviation
        for (int i = 0; i < 5; i++) {
            double variance = calculateVariance(fingerData.get(i));
            double standardDeviation = calculateStandardDeviation(fingerData.get(i));

            // Debug statements that print variance and standard deviation for each finger
//            System.out.println("Finger " + (i + 1) + " variance           = " + variance);
//            System.out.println("Finger " + (i + 1) + " standard deviation = " + standardDeviation);
//            System.out.println("Finger " + (i + 1) + " standard deviation = " + Math.sqrt(variance));

            // If the standard deviation exceeds the allowed threshold, gesture is not stable
            if (standardDeviation > STABILITY_STANDARD_DEVIATION_THRESHOLD) {
                System.out.println("Fingers are UNSTABLE...");
                return false; // Movement detected; gesture unstable
            }
        }
        System.out.println("Fingers are STABLE...");
        return true; // All fingers are within the stability threshold
    }

    /**
     * Calculates the standard deviation of a list of integer sensor values.
     */
    private static double calculateStandardDeviation(List<Integer> data) {
        if (data.size() < 2) return 0.0; // Not enough data to compute standard deviation

        // Compute the mean of the data
        double mean = data.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        // Compute the variance as the average squared difference from the mean
        double variance = data.stream().mapToDouble(val -> Math.pow(val - mean, 2)).average().orElse(0.0);

        // Return the square root of variance, which is the standard deviation
        return Math.sqrt(variance);
    }

    /**
     * Calculates the variance of a list of integer sensor values.
     */
    private static double calculateVariance(List<Integer> data) {
        if (data.size() < 2) return 0.0; // Not enough data to compute variance

        // Compute the mean of the data
        double mean = data.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        // Compute the variance as the average squared difference from the mean
        double variance = data.stream().mapToDouble(val -> Math.pow(val - mean, 2)).average().orElse(0.0);
        // Return the square root of the variance (i.e., standard deviation) as implemented
        return variance;
    }
}