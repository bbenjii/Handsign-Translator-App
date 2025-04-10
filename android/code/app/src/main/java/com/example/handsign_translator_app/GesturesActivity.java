package com.example.handsign_translator_app;

import com.example.handsign_translator_app.models.Gesture;
import com.example.handsign_translator_app.GestureInfoHelper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private List<Gesture> all_gestures;
    private AssetManager assetManager;
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
        // Populate the gestures list and store original meanings.
        initializeOriginalMeanings();
        // Build the grid dynamically.
        populateGestureGrid();
        setupMoreOptionsMenu();
    }

    /**
     * Initialize view references, assetManager, and gestureInfoHelper; also set up bottom navigation.
     */
    private void setUp() {
        titleGestures = findViewById(R.id.title_settings);
        buttonMoreOptions = findViewById(R.id.button_more_options);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        assetManager = getApplicationContext().getAssets();
        gestureInfoHelper = new GestureInfoHelper(assetManager, getApplicationContext());

        bottomNavigationView.setSelectedItemId(R.id.gestures);
        bottomNavigationView.setItemActiveIndicatorColor(
                ContextCompat.getColorStateList(this, R.color.blue_gray_100));

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

    /**
     * Load the gestures from the helper and store their original meanings into SharedPreferences.
     */
    private void initializeOriginalMeanings() {
        all_gestures = gestureInfoHelper.getGestures();
        originalMeanings = new HashMap<>();
        for (Gesture gesture : all_gestures) {
            String meaning = gesture.getTranslation();
            originalMeanings.put(gesture.getLabel(), meaning);
            String key = KEY_ORIGINAL_PREFIX + gesture.getLabel();
            // If the original is not stored or differs, store it.
            if (!gesturePrefs.contains(key) || !gesturePrefs.getString(key, "").equals(meaning)) {
                gesturePrefs.edit().putString(key, meaning).apply();
            }
        }
    }

    /**
     * Dynamically builds the GridLayout with gesture cards based on the list of gestures.
     */
    private void populateGestureGrid() {
        GridLayout gridLayout = findViewById(R.id.gesture_grid);
        gridLayout.removeAllViews();
        float density = getResources().getDisplayMetrics().density;
        int padding = (int) (8 * density);

        for (Gesture gesture : all_gestures) {
            // Create CardView and set layout parameters for even distribution in a 3-column grid.
            CardView cardView = new CardView(this);
            GridLayout.LayoutParams cardParams = new GridLayout.LayoutParams();
            cardParams.width = 0; // Weight-based width
            cardParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            cardParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            cardView.setLayoutParams(cardParams);
            cardView.setRadius(8 * density);
            cardView.setCardElevation(2 * density);
            cardView.setUseCompatPadding(true);
            cardView.setClickable(true);
            cardView.setFocusable(true);

            // Create a LinearLayout to hold the image and label.
            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setGravity(Gravity.CENTER);
            linearLayout.setPadding(padding, padding, padding, padding);
            LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, (int) (140 * density));
            linearLayout.setLayoutParams(llParams);

            // Create ImageView for gesture image.
            ImageView imageView = new ImageView(this);
            int imageSize = (int) (80 * density);
            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(imageSize, imageSize);
            imageView.setLayoutParams(imageParams);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            try {
                InputStream ims = assetManager.open(gesture.getImagePath());
                Drawable drawable = Drawable.createFromStream(ims, null);
                imageView.setImageDrawable(drawable);
                ims.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Create TextView for gesture label.
            TextView textView = new TextView(this);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            textView.setLayoutParams(textParams);
            textView.setGravity(Gravity.CENTER);
            textView.setMaxLines(2);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setTextSize(16);
            String customKey = KEY_CUSTOM_PREFIX + gesture.getLabel();
            String originalMeaning = gesture.getTranslation();
            String labelText = gesturePrefs.getString(customKey, originalMeaning);
            textView.setText(labelText);

            // Assemble views.
            linearLayout.addView(imageView);
            linearLayout.addView(textView);
            cardView.addView(linearLayout);
            // Use the gesture label as tag to identify later.
            cardView.setTag(gesture);
            // Set click listener to open the edit dialog.
            cardView.setOnClickListener(v -> {
                Gesture clickedGesture = (Gesture) v.getTag();
                showEditDialog(clickedGesture);
            });

            gridLayout.addView(cardView);
        }
    }

    /**
     * Opens a dialog to edit the gesture's custom label.
     * @param gesture The Gesture object to be edited.
     */
    private void showEditDialog(Gesture gesture) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_gesture, null);
        ImageView dialogImage = dialogView.findViewById(R.id.dialog_gesture_image);
        EditText dialogInput = dialogView.findViewById(R.id.dialog_input);
        try {
            InputStream is = getAssets().open(gesture.getImagePath());
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            dialogImage.setImageBitmap(bitmap);
            is.close();

            String key = gesture.getLabel();
            String originalMeaning = gesturePrefs.getString(KEY_ORIGINAL_PREFIX + key, gesture.getTranslation());
            String currentLabel = gesturePrefs.getString(KEY_CUSTOM_PREFIX + key, originalMeaning);
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
                        String prefKey = KEY_CUSTOM_PREFIX + gesture.getLabel();
                        gesturePrefs.edit().putString(prefKey, newLabel).apply();
                        populateGestureGrid();
                    }
                })
                .setNegativeButton("Cancel", null)
                .setNeutralButton("Reset", (dialog, which) -> resetGesture(gesture.getLabel()))
                .show();
    }

    /**
     * Resets a specific gesture's custom label back to its original meaning.
     * @param gestureLabel The unique label of the gesture.
     */
    private void resetGesture(String gestureLabel) {
        String originalMeaning = gesturePrefs.getString(KEY_ORIGINAL_PREFIX + gestureLabel, gestureLabel);
        gesturePrefs.edit().remove(KEY_CUSTOM_PREFIX + gestureLabel).apply();
        populateGestureGrid();
        Toast.makeText(this, "Gesture reset to default", Toast.LENGTH_SHORT).show();
    }

    /**
     * Sets up the More Options button to allow for resetting all gestures.
     */
    private void setupMoreOptionsMenu() {
        buttonMoreOptions.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Gesture Options")
                    .setItems(new CharSequence[]{"Reset All to Default", "Reset Current Gesture"}, (dialog, which) -> {
                        if (which == 0) {
                            resetAllGestures();
                        }
                        // "Reset Current Gesture" is handled in the edit dialog.
                    })
                    .show();
        });
    }

    /**
     * Resets all gestures back to their original meanings by clearing custom mappings.
     */
    private void resetAllGestures() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset All Gestures")
                .setMessage("Are you sure you want to reset all gestures to their original meanings?")
                .setPositiveButton("Reset", (dialog, which) -> {
                    SharedPreferences.Editor editor = gesturePrefs.edit();
                    for (Gesture gesture : all_gestures) {
                        editor.remove(KEY_CUSTOM_PREFIX + gesture.getLabel());
                    }
                    editor.apply();
                    populateGestureGrid();
                    Toast.makeText(this, "All gestures reset to default", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}