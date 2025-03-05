package com.example.handsign_translator_app.ml_module;
import java.util.concurrent.TimeUnit;
import java.util.*;
public class GestureDetection {
    // Constants
    private static final int STABILITY_WINDOW = 10; // Number of readings to track for stability
    private static final double STABILITY_VARIANCE_THRESHOLD = 15; // Allowed variance for stable gestures
    private static final double STABILITY_STANDARD_DEVIATION_THRESHOLD = 20; // Allowed standard deviation for stable gestures

    private static final int HOLD_TIME_MS = 500; // Time in milliseconds required to confirm a sign



    public static boolean isGestureStable(Deque<int[]> flexReadingsHistory) {
        if (flexReadingsHistory.size() < STABILITY_WINDOW) return false; // Not enough data

        // Separate history into finger-wise lists
        List<List<Integer>> fingerData = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) {
            fingerData.add(new ArrayList<>());
        }

        for (int[] readings : flexReadingsHistory) {
            for (int i = 0; i < 5; i++) {
                fingerData.get(i).add(readings[i]);
            }
        }

        // Compute variance for each finger
        for (int i = 0; i < 5; i++) {
            double variance = calculateVariance(fingerData.get(i));
            double standardDeviation = calculateStandardDeviation(fingerData.get(i));
//            System.out.println("Finger " + (i + 1) + " variance           = " + variance);
//            System.out.println("Finger " + (i + 1) + " standard deviation = " + standardDeviation);
//            System.out.println("Finger " + (i + 1) + " standard deviation = " + Math.sqrt(variance));



            if (standardDeviation > STABILITY_STANDARD_DEVIATION_THRESHOLD) {
                System.out.println("Fingers are UNSTABLE...");
                return false; // Movement detected
            }
        }
        System.out.println("Fingers are STABLE...");
        return true; // Fingers are stable
    }

    // Function to calculate standard deviation
    private static double calculateStandardDeviation(List<Integer> data) {
        if (data.size() < 2) return 0.0; // Not enough data for SD

        double mean = data.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        double variance = data.stream().mapToDouble(val -> Math.pow(val - mean, 2)).average().orElse(0.0);

        return Math.sqrt(variance); // Standard Deviation
    }

    // Function to calculate variance
    private static double calculateVariance(List<Integer> data) {
        if (data.size() < 2) return 0.0; // Not enough data for variance

        double mean = data.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        return data.stream().mapToDouble(val -> Math.pow(val - mean, 2)).average().orElse(0.0);
    }

    // Function to check if gesture is stable using variance


    private static void printFlexReadings(int[] readings) {
        StringBuilder readingString = new StringBuilder();
        for (int i = 0; i < readings.length; i++) {
            readingString.append("Flex").append(i + 1).append(": ").append(readings[i]).append("\t");
        }
        System.out.println(readingString);
    }

    private static int randomNumber(int min, int max) {
        return new Random().nextInt(max - min + 1) + min;
    }

}
