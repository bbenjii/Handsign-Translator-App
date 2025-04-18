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
    private boolean running = false;

    private Context context;
    private GestureLogDbHelper dbHelper;

    /**
     * Interface definition for callbacks to be invoked when a gesture is detected
     * or when translation is in progress.
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
        if (!running) {
            running = true;
            handler.post(runnable);
        }
    }
    /**
     * Stops the periodic reading of sensor data.
     */
    public void stop() {
        running = false;
        handler.removeCallbacks(runnable);
        listener = null;
    }



    /**
     * Runnable that reads sensor data, checks for gesture stability, and triggers classification.
     */
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (!running) return;  // 👈 Prevent execution if stopped

            boolean deviceConnected = bluetoothModule.isDeviceConnected();
//            deviceConnected = true;
            if (deviceConnected){

                // Retrieve the latest sensor data from the Bluetooth module
                int[] currentFlexReadings = bluetoothModule.getGloveData();
                boolean isStable = false;
                if (currentFlexReadings.length != 0) {

                    // Optionally, you could invoke a callback here:
                    // listener.onSensorDataReceived(currentFlexReadings);

                    // Add the new readings to the sliding window
                    flexReadingsHistory.addLast(currentFlexReadings);
                    // Maintain a fixed-size sliding window
                    if (flexReadingsHistory.size() > STABILITY_WINDOW) {
                        flexReadingsHistory.removeFirst();
                    }
                    // Check if the current gesture (based on recent sensor readings) is stable
                    isStable = GestureStabilityChecker.isGestureStable(flexReadingsHistory);
                }
                if (isStable) {
                    // Convert sensor readings from int[] to float[]
                    float[] sensorData = convertIntArrayToFloatArray(currentFlexReadings);
                    // Classify the gesture using the gesture classifier
                    Gesture gesture = gestureClassifier.classifyGesture(sensorData);
                    currentGesture = gesture;
                    // Notify listener that a gesture has been detected
                    listener.onGestureDetected(gesture);

                    // Log the detected gesture
                    GestureLog log = new GestureLog(
                            gesture.getLabel(),
                            gesture.getCustomTranslation().isEmpty() ? gesture.getTranslation() : gesture.getCustomTranslation(),
                            System.currentTimeMillis()
                    );
//                    dbHelper.addGestureLog(log);

                } else {
                    currentGesture = null;
                    // Notify listener that translation is in progress (gesture unstable)
                    listener.onTranslationInProgress();
                }
            }
            else{
                listener.onNoDeviceConnected();
            }

            // Schedule the next sensor reading after the specified delay
            // Only schedule the next run if still running
            if (running) {
                handler.postDelayed(this, SENSOR_READ_DELAY_MS);
            }
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

    public void resetFlexReadingHistory(){
        flexReadingsHistory = new ArrayDeque<>();
    }
}