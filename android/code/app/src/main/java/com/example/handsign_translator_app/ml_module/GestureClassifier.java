package com.example.handsign_translator_app.ml_module;

// Import necessary Android classes and TensorFlow Lite interpreter
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;

import com.example.handsign_translator_app.GestureInfoHelper;
import com.example.handsign_translator_app.models.Gesture;
import com.example.handsign_translator_app.utils.Constants;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Map;

public class GestureClassifier {
    // For dynamic gestures, we need a fixed number of time steps.
    private static final int TIME_STEPS = 100;  // e.g., 100
    private static final int INPUT_FEATURES = Constants.INPUT_FEATURES; // e.g., 11 (5 flex + 6 IMU)
    private static final int OUTPUT_CLASSES = Constants.OUTPUT_CLASSES;

    // TensorFlow Lite interpreter instance for running the ML model
    private Interpreter tflite;
    // Helper to load gesture information (translation and image path) from a CSV
    private GestureInfoHelper gestureInfoHelper;

    /**
     * Constructor initializes the TensorFlow Lite interpreter and loads gesture information.
     */
    public GestureClassifier(AssetManager assetManager, Context context) {
        try {
            // Load the TensorFlow Lite model file and initialize the interpreter
            tflite = new Interpreter(loadModelFile(assetManager, "gesture_gru_model.tflite"));
        } catch (IOException e) {
            // Throw a runtime exception if the model fails to load
            throw new RuntimeException("Failed to load TensorFlow Lite model", e);
        }
        // Initialize the helper that loads gesture metadata from CSV
        gestureInfoHelper = new GestureInfoHelper(assetManager, context);
    }

    /**
     * Loads the model file from the assets folder.
     */
    private MappedByteBuffer loadModelFile(AssetManager assetManager, String modelFile) throws IOException {
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelFile);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.getStartOffset(), fileDescriptor.getDeclaredLength());
    }

    /**
     * Classifies the gesture based on a sequence of sensor readings.
     *
     * @param sensorSequence A 2D array of sensor data with shape (TIME_STEPS, INPUT_FEATURES),
     *                       where each inner array represents a time step.
     * @return The predicted Gesture.
     */
    public Gesture classifyGesture(float[][] sensorSequence) {
        // Validate that the sequence has the expected number of time steps
        if (sensorSequence.length != TIME_STEPS) {
            throw new IllegalArgumentException("Expected " + TIME_STEPS + " time steps, but got " + sensorSequence.length);
        }
        // Validate that each time step has the expected number of features
        for (int i = 0; i < sensorSequence.length; i++) {
            if (sensorSequence[i].length != INPUT_FEATURES) {
                throw new IllegalArgumentException("Expected " + INPUT_FEATURES + " features per time step, but got " + sensorSequence[i].length + " at index " + i);
            }
        }

        // Prepare input: create a 3D array of shape (1, TIME_STEPS, INPUT_FEATURES)
        float[][][] input = new float[1][TIME_STEPS][INPUT_FEATURES];
        input[0] = sensorSequence;

        // Prepare output: a 2D array to store the probabilities for each class
        float[][] output = new float[1][OUTPUT_CLASSES];

        // Run inference on the sequence using the TensorFlow Lite model
        tflite.run(input, output);

        // Get the index of the class with the highest probability
        int predictedIndex = getMaxIndex(output[0]);
        // Convert the index to a Gesture using the helper and return it
        return getGestureFromIndex(predictedIndex);
    }

    /**
     * Returns the index of the highest probability in the array.
     */
    private int getMaxIndex(float[] probabilities) {
        int maxIndex = 0;
        for (int i = 1; i < probabilities.length; i++) {
            if (probabilities[i] > probabilities[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    /**
     * Converts the predicted index into a Gesture.
     * (This method should use gestureInfoHelper to get gesture metadata.)
     */
    private Gesture getGestureFromIndex(int index) {
        // Implementation depends on how you m ap indices to gestures.
        // For example:
        return gestureInfoHelper.getGestureAt(index);
    }
}