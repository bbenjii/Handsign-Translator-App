package com.example.handsign_translator_app.ml_module;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import org.tensorflow.lite.Interpreter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

public class GestureClassifier {
    private static final int INPUT_FEATURES = 5; // Number of sensor readings (THUMB, INDEX, MIDDLE, RING, LITTLE)
    private static final int OUTPUT_CLASSES = 5; // Number of gestures (adjust based on your model)
    private Interpreter tflite;

    public GestureClassifier(AssetManager assetManager) {
        try{
            tflite = new Interpreter(loadModelFile(assetManager, "gesture_model.tflite"));

        } catch (IOException e){
            throw new RuntimeException("Failed to load TensorFlow Lite model", e);

        }
    }

    private MappedByteBuffer loadModelFile(AssetManager assetManager, String modelFile) throws IOException {
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelFile);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.getStartOffset(), fileDescriptor.getDeclaredLength());
    }

    public String classifyGesture(float[] sensorData) {
        if (sensorData.length != INPUT_FEATURES) {
            throw new IllegalArgumentException("Expected " + INPUT_FEATURES + " sensor readings, but got " + sensorData.length);
        }

        float[][] output = new float[1][OUTPUT_CLASSES]; // Output probabilities for each gesture
        tflite.run(sensorData, output);

        // Get the index of the highest probability class
        int predictedIndex = getMaxIndex(output[0]);
        return getGestureLabel(predictedIndex); // Convert index to gesture label
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

    private String getGestureLabel(int index) {
        String[] gestureLabels = {"Gesture 1", "Gesture 2", "Gesture 3", "Gesture 4", "Gesture 5"}; // Adjust based on your training data
        return gestureLabels[index];
    }

    public void close() {
        if (tflite != null) {
            tflite.close();
        }
    }
}


