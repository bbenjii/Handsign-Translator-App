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

        String english_file = "gesture_info.csv";
        String french_file = "gesture_info_fr.csv";


        SharedPreferences prefs = context.getSharedPreferences("language_pref", MODE_PRIVATE);
        String langCode = prefs.getString("selected_language", "en");

        String csvFile = english_file;
        if (langCode.equals("fr")) {
            csvFile = french_file;
        }

        try {
            InputStream inputStream = assetManager.open(csvFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

            String line = br.readLine(); // header line
            String[] labels = line.split(",\\s*"); // handles comma and optional space

            String[] values;
            while ((line = br.readLine()) != null) {
                values = line.split(",\\s*");
                Map<String, String> map = new HashMap<>();
                for (int i = 0; i < labels.length; i++) {
                    map.put(labels[i], values[i]);
                }

                String translation = map.get("translation").trim();
                String imagePath = map.get("path").trim();
                String label = map.get("label").trim();

                String customKey = KEY_CUSTOM_PREFIX + label;
                String customTranslation = gesturePrefs.getString(customKey, "");

                gestures.add(new Gesture(translation, imagePath, label, customTranslation));
            }

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
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