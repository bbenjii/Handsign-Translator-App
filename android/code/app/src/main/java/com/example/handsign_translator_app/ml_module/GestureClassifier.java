package com.example.handsign_translator_app.ml_module;

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
    private static final int INPUT_FEATURES = Constants.INPUT_FEATURES; // Number of sensor readings (THUMB, INDEX, MIDDLE, RING, LITTLE)
    private static final int OUTPUT_CLASSES = Constants.OUTPUT_CLASSES; // Number of gestures (adjust based on your model)
    private Interpreter tflite;
    private GestureInfoHelper gestureInfoHelper;

    public GestureClassifier(AssetManager assetManager) {

        try {
            tflite = new Interpreter(loadModelFile(assetManager, "gesture_model.tflite"));

        } catch (IOException e) {
            throw new RuntimeException("Failed to load TensorFlow Lite model", e);
        }
        gestureInfoHelper = new GestureInfoHelper(assetManager);
    }

    private MappedByteBuffer loadModelFile(AssetManager assetManager, String modelFile) throws IOException {
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelFile);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.getStartOffset(), fileDescriptor.getDeclaredLength());
    }

    public Gesture classifyGesture(float[] sensorData) {
        if (sensorData.length != INPUT_FEATURES) {
            throw new IllegalArgumentException("Expected " + INPUT_FEATURES + " sensor readings, but got " + sensorData.length);
        }
        float[][] output = new float[1][OUTPUT_CLASSES];
        tflite.run(sensorData, output);
        int predictedIndex = getMaxIndex(output[0]);
        return getGestureFromIndex(predictedIndex);
    }

    private int getMaxIndex(float[] probabilities) {
        int maxIndex = 0;
        for (int i = 1; i < probabilities.length; i++) {
            if (probabilities[i] > probabilities[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    private Gesture getGestureFromIndex(int index) {
        return gestureInfoHelper.getGestureAt(index);
    }

    public void close() {
        if (tflite != null) {
            tflite.close();
        }
    }
}


