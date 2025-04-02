package com.example.handsign_translator_app.controllers;

import android.os.Handler;
import android.util.Log;

import com.example.handsign_translator_app.bluetooth.BluetoothModule;
import com.example.handsign_translator_app.ml_module.GestureClassifier;
import com.example.handsign_translator_app.ml_module.StaticGestureClassifier;
import com.example.handsign_translator_app.models.Gesture;
import com.example.handsign_translator_app.models.StaticGesture;
import com.example.handsign_translator_app.utils.Constants;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

public class GestureController {
    private static final int STABILITY_WINDOW = Constants.STABILITY_WINDOW;
    private static final int SENSOR_READ_DELAY_MS = Constants.SENSOR_READ_DELAY_MS;

    private Deque<double[]> flexReadingsHistory = new ArrayDeque<>();
    private BluetoothModule bluetoothModule;
    private GestureClassifier gestureClassifier;
    private StaticGestureClassifier staticClassifier;
    private Handler handler = new Handler();
    private GestureListener listener;
    public Gesture currentGesture;

    public interface GestureListener {
        void onGestureDetected(Gesture gesture);
        void onTranslationInProgress();
        void onNoDeviceConnected();
        void rawDataOutput(String data);
    }

    public GestureController(BluetoothModule bluetoothModule, GestureClassifier gestureClassifier, StaticGestureClassifier staticClassifier, GestureListener listener) {
        this.bluetoothModule = bluetoothModule;
        this.gestureClassifier = gestureClassifier;
        this.staticClassifier = staticClassifier;
        this.listener = listener;
    }

    public void start() {
        handler.post(runnable);
    }

    public void stop() {
        handler.removeCallbacks(runnable);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            boolean deviceConnected = bluetoothModule.isDeviceConnected();
            if (deviceConnected) {
                // Get sensor data as double array for dynamic gestures and int array for static gestures.
                double[] currentFlexReadings = bluetoothModule.getGloveDataDouble();
                int[] staticCurrentFlexReadings = bluetoothModule.getGloveDataInt();

                if (currentFlexReadings == null) {
                    Log.e("GestureController", "Invalid sensor data received.");
                    listener.onTranslationInProgress();
                    handler.postDelayed(this, SENSOR_READ_DELAY_MS);
                    return;
                }

                // Use the 8th reading to determine if the gesture is dynamic (1.0) or static (0).
                double isDynamic = currentFlexReadings[8];

                if (isDynamic == 1.0) {
                    // Dynamic branch
                    // For dynamic gestures, use the historical readings and pass the first 8 sensor values as floats.
                    flexReadingsHistory.addLast(currentFlexReadings);
                    if (flexReadingsHistory.size() > STABILITY_WINDOW) {
                        flexReadingsHistory.removeFirst();
                    }
                    if (flexReadingsHistory.size() >= STABILITY_WINDOW) {
                        // Convert each historical reading to a float array with only the first 8 values.
                        float[][] readingsArray = new float[flexReadingsHistory.size()][8];
                        int index = 0;
                        for (double[] reading : flexReadingsHistory) {
                            // Copy the first 8 sensor readings and convert to float.
                            for (int i = 0; i < 8; i++) {
                                readingsArray[index][i] = (float) reading[i];
                            }
                            index++;
                        }
                        // Pass the float[][] to the dynamic ML model.
                        currentGesture = gestureClassifier.classifyGesture(readingsArray);
                        listener.onGestureDetected(currentGesture);
                    } else {
                        currentGesture = null;
                        listener.onTranslationInProgress();
                    }
                } else {
                    //static branch
                    // For static gestures, use the first 5 sensor readings as integers.
                    if (staticCurrentFlexReadings != null && staticCurrentFlexReadings.length >= 5) {
                        int[] fiveReadings = new int[5];
                        System.arraycopy(staticCurrentFlexReadings, 0, fiveReadings, 0, 5);
                        currentGesture = staticClassifier.classifyStaticGesture(fiveReadings);
                        listener.onGestureDetected(currentGesture);
                    } else {
                        Log.e("GestureController", "Invalid number of readings for static gesture classification.");
                        listener.onTranslationInProgress();
                    }
                }
            } else {
                listener.onNoDeviceConnected();
            }
            handler.postDelayed(this, SENSOR_READ_DELAY_MS);
        }
    };
}