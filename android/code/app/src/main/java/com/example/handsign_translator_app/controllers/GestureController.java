package com.example.handsign_translator_app.controllers;

import android.content.Context;
import android.os.Handler;
import com.example.handsign_translator_app.bluetooth.BluetoothModule;
import com.example.handsign_translator_app.database.GestureLogDbHelper;
import com.example.handsign_translator_app.ml_module.GestureClassifier;
import com.example.handsign_translator_app.ml_module.GestureStabilityChecker;
import com.example.handsign_translator_app.models.Gesture;
import com.example.handsign_translator_app.models.GestureLog;
import com.example.handsign_translator_app.utils.Constants;
import java.util.ArrayDeque;
import java.util.Deque;

public class GestureController {
    // Number of sensor readings to maintain in the sliding window
    private static final int STABILITY_WINDOW = Constants.STABILITY_WINDOW;
    // Delay between consecutive sensor readings (in milliseconds)
    private static final int SENSOR_READ_DELAY_MS = Constants.SENSOR_READ_DELAY_MS;

    // Deque to hold the history of sensor readings
    private Deque<int[]> flexReadingsHistory = new ArrayDeque<>();
    // Module responsible for obtaining sensor data from the glove
    private BluetoothModule bluetoothModule;
    // Module that performs gesture classification using TensorFlow Lite
    private GestureClassifier gestureClassifier;
    // Handler for scheduling periodic sensor readings
    private Handler handler = new Handler();
    // Listener to provide gesture detection updates to the UI
    private GestureListener listener;
    // Stores the currently detected gesture (if any)
    public Gesture currentGesture;
    private Context context;
    private GestureLogDbHelper dbHelper;

    /**
     * Interface definition for callbacks to be invoked when a gesture is detected
     * or when translation is in progress.
     *
     * Additionally, you might consider adding a callback like onSensorDataReceived(int[] data)
     * if you want to update the UI or log raw sensor data.
     */
    public interface GestureListener {
        void onGestureDetected(Gesture gesture);
        void onTranslationInProgress();
        void onNoDeviceConnected();
        void rawDataOutput(String data);
        // Optionally:
        // void onSensorDataReceived(int[] sensorData);
    }

    /**
     * Constructor for GestureController.
     */
    public GestureController(BluetoothModule bluetoothModule, GestureClassifier gestureClassifier, GestureListener listener, Context context) {
        this.bluetoothModule = bluetoothModule;
        this.gestureClassifier = gestureClassifier;
        this.listener = listener;
        this.context = context;
        this.dbHelper = new GestureLogDbHelper(context);
    }

    /**
     * Starts the periodic reading of sensor data.
     */
    public void start() {
        handler.post(runnable);
    }

    /**
     * Stops the periodic reading of sensor data.
     */
    public void stop() {
        handler.removeCallbacks(runnable);
    }

    /**
     * Runnable that reads sensor data, checks for gesture stability, and triggers classification.
     */
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(bluetoothModule.isDeviceConnected()){
                String data = bluetoothModule.getRawData();
                if(data.isEmpty()){
                    listener.onTranslationInProgress();
                }
                else{
                    int[] currentFlexReadings = parseFlexReadings(data);
                    flexReadingsHistory.addLast(currentFlexReadings);
                    if (flexReadingsHistory.size() > STABILITY_WINDOW) {
                        flexReadingsHistory.removeFirst();
                    }

                    boolean isStable = GestureStabilityChecker.isGestureStable(flexReadingsHistory);

                    if (isStable) {
                        float[] sensorData = convertIntArrayToFloatArray(currentFlexReadings);
                        Gesture gesture = gestureClassifier.classifyGesture(sensorData);
                        currentGesture = gesture;
                        listener.onGestureDetected(gesture);
                        
                        // Log the detected gesture
                        GestureLog log = new GestureLog(
                            gesture.getLabel(),
                            gesture.getCustomTranslation().isEmpty() ? gesture.getTranslation() : gesture.getCustomTranslation(),
                            System.currentTimeMillis()
                        );
                        dbHelper.addGestureLog(log);
                    } else {
                        currentGesture = null;
                        listener.onTranslationInProgress();
                    }
                }
            } else {
                listener.onNoDeviceConnected();
            }

            handler.postDelayed(this, SENSOR_READ_DELAY_MS);
        }
    };

    /**
     * Helper method to convert an array of integers to an array of floats.
     */
    private float[] convertIntArrayToFloatArray(int[] intArray) {
        float[] floatArray = new float[intArray.length];
        for (int i = 0; i < intArray.length; i++) {
            floatArray[i] = intArray[i];
        }
        return floatArray;
    }

    private int[] parseFlexReadings(String data) {
        String[] parts = data.split(",");
        int[] readings = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            readings[i] = Integer.parseInt(parts[i].trim());
        }
        return readings;
    }
}