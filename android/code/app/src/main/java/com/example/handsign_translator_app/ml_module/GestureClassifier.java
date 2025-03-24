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
    // Number of input features (sensor readings for THUMB, INDEX, MIDDLE, RING, LITTLE)
    private static final int INPUT_FEATURES = Constants.INPUT_FEATURES;
    // Number of output classes
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
            tflite = new Interpreter(loadModelFile(assetManager, "gesture_model.tflite"));
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
        // Open the model file descriptor from assets
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelFile);
        // Create a FileInputStream to read the file
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        // Get the file channel to map the file into memory
        FileChannel fileChannel = inputStream.getChannel();
        // Map the file into a MappedByteBuffer and return it
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.getStartOffset(), fileDescriptor.getDeclaredLength());
    }

    /**
     * Classifies the gesture based on provided sensor data.
     */
    public Gesture classifyGesture(float[] sensorData) {
        // Validate that the input sensor data has the expected number of features
        if (sensorData.length != INPUT_FEATURES) {
            throw new IllegalArgumentException("Expected " + INPUT_FEATURES + " sensor readings, but got " + sensorData.length);
        }
        // Prepare a 2D output array to store the output probabilities for each class
        float[][] output = new float[1][OUTPUT_CLASSES];
        // Run inference on the sensor data using the TensorFlow Lite model
        tflite.run(sensorData, output);
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
        // Iterate through probabilities starting from the second element
        for (int i = 1; i < probabilities.length; i++) {
            if (probabilities[i] > probabilities[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    /**
     * Maps the predicted index to a Gesture object.
     */
    private Gesture getGestureFromIndex(int index) {
        // Delegate to the GestureInfoHelper to obtain the Gesture details
        return gestureInfoHelper.getGestureAt(index);
    }

    /**
     * Releases the resources held by the TensorFlow Lite interpreter.
     */
    public void close() {
        if (tflite != null) {
            tflite.close();
        }
    }
}