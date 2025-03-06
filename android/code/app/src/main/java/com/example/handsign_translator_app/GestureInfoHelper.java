package com.example.handsign_translator_app;

import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;

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
    public List<Map<String, String>> gestures_dataset;

    public GestureInfoHelper(AssetManager assetManager) {
        String csvFile = "gesture_info.csv";
        String line;
        String delimiter = ", "; // Adjust if your CSV uses a different delimiter

        String[] labels = {};
        gestures_dataset = new ArrayList<>();
        Map<String, String> map;
        try {
            // get input stream
            InputStream inputStream = assetManager.open(csvFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

            line = br.readLine();
            labels = line.split(delimiter);

            while ((line = br.readLine()) != null) {
                // Splitting the line into fields
                String[] values = line.split(delimiter);
                map = new HashMap<String, String>();
                // Process each value
                for(int i = 0;i < labels.length; i++){
                    map.put(labels[i], values[i]);
                }
                gestures_dataset.add(map);
            }
        }
        catch(IOException ex) {
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            line = br.readLine();
            labels = line.split(delimiter);

            while ((line = br.readLine()) != null) {
                // Splitting the line into fields
                String[] values = line.split(delimiter);
                map = new HashMap<String, String>();
                // Process each value
                for(int i = 0;i < labels.length; i++){
                    map.put(labels[i], values[i]);
                }
                gestures_dataset.add(map);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
