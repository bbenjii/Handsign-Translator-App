package com.example.handsign_translator_app;

import com.example.handsign_translator_app.GestureInfoHelper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.handsign_translator_app.models.Gesture;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GesturesActivity extends AppCompatActivity {

    private TextView titleGestures;
    private ImageButton buttonMoreOptions;
    private BottomNavigationView bottomNavigationView;
    private SharedPreferences gesturePrefs;
    private GestureInfoHelper gestureInfoHelper;
    List<Gesture> all_gestures;
    AssetManager assetManager;
    private Map<String, String> originalMeanings;
    private static final String PREFS_NAME = "gesture_mappings";
    private static final String KEY_CUSTOM_PREFIX = "custom_gesture_";
    private static final String KEY_ORIGINAL_PREFIX = "original_gesture_";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gestures);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize SharedPreferences
        gesturePrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        setUp();

        // Initialize original meanings map
        initializeOriginalMeanings();

        loadGestureImages();
        setupClickListeners();
        setupMoreOptionsMenu();
    }

    private void initializeOriginalMeanings() {
        all_gestures = gestureInfoHelper.getGestures();


        originalMeanings = new HashMap<>();
        // Store original meanings from CSV
        for (int i = 1; i <= 10; i++) {
            String meaning = String.valueOf(i);
            originalMeanings.put(String.valueOf(i), meaning);
            // Save to SharedPreferences if not already saved
            String key = KEY_ORIGINAL_PREFIX + i;
            if (!gesturePrefs.contains(key)) {
                gesturePrefs.edit().putString(key, meaning).apply();
            }
        }
        // Add Y gesture
        originalMeanings.put("y", "Y");
        if (!gesturePrefs.contains(KEY_ORIGINAL_PREFIX + "y")) {
            gesturePrefs.edit().putString(KEY_ORIGINAL_PREFIX + "y", "Y").apply();
        }
    }

    private void loadGestureImages() {
        try {
            for (Gesture gesture : all_gestures) {
                String path = gesture.getImagePath();
                InputStream ims = assetManager.open(path);
                Drawable d = Drawable.createFromStream(ims, null);

                ImageView imageView = findViewById(getResources().getIdentifier("gesture_image_" + gesture.getLabel(), "id", getPackageName()));
                imageView.setImageDrawable(d);

                TextView labelView = findViewById(getResources().getIdentifier("gesture_label_" + gesture.getLabel(), "id", getPackageName()));
                String customKey = KEY_CUSTOM_PREFIX + gesture.getLabel();

                String originalMeaning = gesture.getTranslation();
                String customMeaning = gesturePrefs.getString(customKey, originalMeaning);
                labelView.setText(customMeaning);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupMoreOptionsMenu() {
        buttonMoreOptions.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Gesture Options")
                    .setItems(new CharSequence[]{"Reset All to Default", "Reset Current Gesture"}, (dialog, which) -> {
                        switch (which) {
                            case 0:
                                resetAllGestures();
                                break;
                            case 1:
                                // Will be handled in showEditDialog
                                break;
                        }
                    })
                    .show();
        });
    }

    private void resetAllGestures() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset All Gestures")
                .setMessage("Are you sure you want to reset all gestures to their original meanings?")
                .setPositiveButton("Reset", (dialog, which) -> {
                    // Clear all custom meanings
                    SharedPreferences.Editor editor = gesturePrefs.edit();
                    for (int i = 1; i <= 10; i++) {
                        editor.remove(KEY_CUSTOM_PREFIX + i);
                    }
                    editor.remove(KEY_CUSTOM_PREFIX + "y");
                    editor.apply();

                    // Reload the UI
                    loadGestureImages();
                    Toast.makeText(this, "All gestures reset to default", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void resetGesture(int gestureId) {
        String key = gestureId > 0 ? String.valueOf(gestureId) : "y";
        String originalMeaning = gesturePrefs.getString(KEY_ORIGINAL_PREFIX + key, key);

        // Remove custom meaning
        gesturePrefs.edit().remove(KEY_CUSTOM_PREFIX + key).apply();

        // Update UI
        TextView labelView = findViewById(gestureId > 0 ?
                getResources().getIdentifier("gesture_label_" + gestureId, "id", getPackageName()) :
                R.id.gesture_label_y);
        labelView.setText(originalMeaning);

        Toast.makeText(this, "Gesture reset to default", Toast.LENGTH_SHORT).show();
    }

    private void setupClickListeners() {
        // Set up click listeners for each card
        for (int i = 1; i <= 10; i++) {
            CardView card = findViewById(getResources().getIdentifier(
                    "gesture_card_" + i, "id", getPackageName()));
            final int gestureId = i;
            card.setOnClickListener(v -> showEditDialog(gestureId));
        }

        // Setup Y gesture card click listener
        CardView yCard = findViewById(R.id.gesture_card_y);
        yCard.setOnClickListener(v -> showEditDialog(-1)); // -1 indicates Y gesture
    }

    private void showEditDialog(int gestureId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_gesture, null);

        // Find views in dialog
        ImageView dialogImage = dialogView.findViewById(R.id.dialog_gesture_image);
        EditText dialogInput = dialogView.findViewById(R.id.dialog_input);

        // Set current image and text
        try {
            InputStream is;
            String currentLabel;
            String key = gestureId > 0 ? String.valueOf(gestureId) : "y";

            if (gestureId > 0) {
                is = getAssets().open(String.format("hand_signs_images/asl_numbers/%03d.png", gestureId));
                TextView labelView = findViewById(getResources().getIdentifier(
                        "gesture_label_" + gestureId, "id", getPackageName()));
                currentLabel = labelView.getText().toString();
            } else {
                is = getAssets().open("hand_signs_images/asl_alphabet/Y.png");
                TextView labelView = findViewById(R.id.gesture_label_y);
                currentLabel = labelView.getText().toString();
            }

            Bitmap bitmap = BitmapFactory.decodeStream(is);
            dialogImage.setImageBitmap(bitmap);
            is.close();

            // Show original meaning in hint
            String originalMeaning = gesturePrefs.getString(KEY_ORIGINAL_PREFIX + key, key);
            dialogInput.setHint("Original: " + originalMeaning);
            dialogInput.setText(currentLabel);
            dialogInput.setSelection(currentLabel.length());

        } catch (IOException e) {
            e.printStackTrace();
        }

        builder.setView(dialogView)
                .setTitle("Edit Gesture Meaning")
                .setPositiveButton("Save", (dialog, which) -> {
                    String newLabel = dialogInput.getText().toString().trim();
                    if (!newLabel.isEmpty()) {
                        // Save to SharedPreferences with custom prefix
                        String prefKey = KEY_CUSTOM_PREFIX + (gestureId > 0 ? gestureId : "y");
                        gesturePrefs.edit().putString(prefKey, newLabel).apply();

                        // Update UI
                        TextView labelView = findViewById(gestureId > 0 ?
                                getResources().getIdentifier("gesture_label_" + gestureId, "id", getPackageName()) :
                                R.id.gesture_label_y);
                        labelView.setText(newLabel);
                    }
                })
                .setNegativeButton("Cancel", null)
                .setNeutralButton("Reset", (dialog, which) -> resetGesture(gestureId))
                .show();
    }

    private void setUp() {
        titleGestures = findViewById(R.id.title_settings);
        buttonMoreOptions = findViewById(R.id.button_more_options);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        assetManager = getApplicationContext().getAssets();
        gestureInfoHelper = new GestureInfoHelper(assetManager, getApplicationContext());

        bottomNavigationView.setSelectedItemId(R.id.gestures);
        bottomNavigationView.setItemActiveIndicatorColor(ContextCompat.getColorStateList(this, R.color.blue_gray_100));

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.translation) {
                navigateToMainActivity();
                return true;
            } else if (item.getItemId() == R.id.settings) {
                navigateToSettingsActivity();
                return true;
            }
            return false;
        });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void navigateToSettingsActivity() {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }
}