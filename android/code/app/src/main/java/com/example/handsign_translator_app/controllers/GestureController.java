package com.example.handsign_translator_app.controllers;

import android.os.Handler;
import com.example.handsign_translator_app.bluetooth.BluetoothModule;
import com.example.handsign_translator_app.ml_module.GestureClassifier;
import com.example.handsign_translator_app.ml_module.GestureStabilityChecker;
import com.example.handsign_translator_app.models.Gesture;
import com.example.handsign_translator_app.utils.Constants;

import java.util.ArrayDeque;
import java.util.Deque;


public class GestureController {
    private static final int STABILITY_WINDOW = Constants.STABILITY_WINDOW; // Number of readings to track for stability
    private static final int SENSOR_READ_DELAY_MS = Constants.SENSOR_READ_DELAY_MS;
    private Deque<int[]> flexReadingsHistory = new ArrayDeque<>();
    private BluetoothModule bluetoothModule;
    private GestureClassifier gestureClassifier;
    private Handler handler = new Handler();
    private GestureListener listener;
    public Gesture currentGesture;

    public interface GestureListener {
        void onGestureDetected(Gesture gesture);
        void onTranslationInProgress();
    }

    public GestureController(BluetoothModule bluetoothModule, GestureClassifier gestureClassifier, GestureListener listener) {
        this.bluetoothModule = bluetoothModule;
        this.gestureClassifier = gestureClassifier;
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
            int[] currentFlexReadings = bluetoothModule.getGloveData();
            flexReadingsHistory.addLast(currentFlexReadings);
            if (flexReadingsHistory.size() > STABILITY_WINDOW) {
                flexReadingsHistory.removeFirst();
            }
            boolean isStable = GestureStabilityChecker.isGestureStable(flexReadingsHistory);
//            isStable = false;
            if (isStable) {
                float[] sensorData = convertIntArrayToFloatArray(currentFlexReadings);
                Gesture gesture = gestureClassifier.classifyGesture(sensorData);
                currentGesture = gesture;
                listener.onGestureDetected(gesture);
            } else {
                currentGesture = null;
                listener.onTranslationInProgress();
            }
            handler.postDelayed(this, SENSOR_READ_DELAY_MS);
        }
    };

    private float[] convertIntArrayToFloatArray(int[] intArray) {
        float[] floatArray = new float[intArray.length];
        for (int i = 0; i < intArray.length; i++) {
            floatArray[i] = intArray[i];
        }
        return floatArray;
    }
}
