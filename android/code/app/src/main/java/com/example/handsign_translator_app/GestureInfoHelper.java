package com.example.handsign_translator_app;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;

import com.example.handsign_translator_app.models.Gesture;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GestureInfoHelper {
    // List to hold Gesture objects loaded from CSV data.
    private List<Gesture> gestures;
    private SharedPreferences gesturePrefs;
    private static final String PREFS_NAME = "gesture_mappings";
private static final String KEY_CUSTOM_PREFIX = "custom_gesture_";


    /**
     * Constructor loads gesture information from a CSV file using the provided AssetManager.
     */
    public GestureInfoHelper(AssetManager assetManager, Context context) {
        gestures = new ArrayList<>();
        gesturePrefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // CSV file name and delimiter definition
        String csvFile = "gesture_info.csv";
        String line;
        String delimiter = ", ";

        String[] labels = {};
        Map<String, String> map;
        try {
            // Open the CSV file from assets as an InputStream
            InputStream inputStream = assetManager.open(csvFile);
            // Wrap the InputStream in a BufferedReader for easier line-by-line reading
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

            // Read the header line to get the column labels
            line = br.readLine();
            labels = line.split(delimiter);

            // Loop through each subsequent line in the CSV file
            while ((line = br.readLine()) != null) {
                // Split the line into individual field values using the delimiter
                String[] values = line.split(delimiter);
                map = new HashMap<String, String>();
                // Map each label to its corresponding value from the current line
                for (int i = 0; i < labels.length; i++) {
                    map.put(labels[i], values[i]);
                }
                // Retrieve and trim the translation and image path from the map
                String translation = map.get("translation").trim();
                String imagePath = map.get("path").trim();
                String label = map.get("label").trim();

                String customKey = KEY_CUSTOM_PREFIX + label;
                String customTranslation = gesturePrefs.getString(customKey, "");


                // Create a new Gesture object and add it to the list
                gestures.add(new Gesture(translation, imagePath, label, customTranslation));
            }
        }
        catch(IOException e) {
            // Print stack trace for any IO exception encountered during file reading
            e.printStackTrace();
            return;
        }
    }

    public List<Gesture> getGestures(){
        return gestures;
    }

    /**
     * Retrieves the Gesture at the specified index from the list.
     * return the Gesture if index is valid; otherwise, a default "Unknown Gesture".
     */
    public Gesture getGestureAt(int index) {
        if (index >= 0 && index < gestures.size()) {
            return gestures.get(index);
        }
        // Return a default gesture if index is out of bounds
        return new Gesture("Unknown Gesture", "default_image.png", "unknown");
    }
}