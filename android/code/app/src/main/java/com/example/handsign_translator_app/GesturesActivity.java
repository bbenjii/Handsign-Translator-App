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
import androidx.constraintlayout.widget.ConstraintLayout;
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

        // Initialize SharedPreferences and set up UI components.
        gesturePrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        setUp();

        // Initialize the original meanings and load gestures.
        initializeOriginalMeanings();
        populateGestureGrid();
        setupMoreOptionsMenu();
    }

    private void setUp() {
        // Initialize views. Note that in your layout the title TextView has id "title_gestures".
        titleGestures = findViewById(R.id.title_gestures);
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

    private void initializeOriginalMeanings() {
        all_gestures = gestureInfoHelper.getGestures();
        originalMeanings = new HashMap<>();
        for (Gesture gesture : all_gestures) {
            String meaning = gesture.getTranslation();
            originalMeanings.put(gesture.getLabel(), meaning);
            String key = KEY_ORIGINAL_PREFIX + gesture.getLabel();
            if (!gesturePrefs.contains(key) || !gesturePrefs.getString(key, "").equals(meaning)) {
                gesturePrefs.edit().putString(key, meaning).apply();
            }
        }
    }

    /**
     * Dynamically creates and adds CardViews for each gesture in all_gestures to the GridLayout.
     */
    private void populateGestureGrid() {
        GridLayout gridLayout = findViewById(R.id.gesture_grid);
        gridLayout.removeAllViews();
        float density = getResources().getDisplayMetrics().density;
        int padding = (int) (8 * density);

        for (Gesture gesture : all_gestures) {
            // Create CardView and set layout parameters
            CardView cardView = new CardView(this);
            GridLayout.LayoutParams cardParams = new GridLayout.LayoutParams();
            cardParams.width = 0;
            cardParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            // Set column weight so that cards are evenly distributed in a 3-column grid.
            cardParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            cardView.setLayoutParams(cardParams);
            cardView.setRadius(8 * density);
            cardView.setCardElevation(2 * density);
            cardView.setUseCompatPadding(true);
            cardView.setClickable(true);
            cardView.setFocusable(true);

            // Create a LinearLayout to hold the gesture image and label.
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
            textView.setTextSize(16); // sp
            String customKey = KEY_CUSTOM_PREFIX + gesture.getLabel();
            String originalMeaning = gesture.getTranslation();
            String labelText = gesturePrefs.getString(customKey, originalMeaning);
            textView.setText(labelText);

            // Assemble the LinearLayout.
            linearLayout.addView(imageView);
            linearLayout.addView(textView);
            cardView.addView(linearLayout);

            // Use the gesture label as a tag for identification.
            cardView.setTag(gesture.getLabel());
            // Set click listener to allow editing.
            cardView.setOnClickListener(v -> {
                String label = (String) v.getTag();
                int gestureId;
                try {
                    gestureId = Integer.parseInt(label);
                } catch (NumberFormatException e) {
                    gestureId = -1; // Use -1 for non-numeric (e.g., special gestures)
                }
                showEditDialog(gesture);
            });

            gridLayout.addView(cardView);
        }
    }

    private void setupMoreOptionsMenu() {
        buttonMoreOptions.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Gesture Options")
                    .setItems(new CharSequence[]{"Reset All to Default", "Reset Current Gesture"}, (dialog, which) -> {
                        if (which == 0) {
                            resetAllGestures();
                        }
                        // The "Reset Current Gesture" will be handled in the edit dialog.
                    })
                    .show();
        });
    }

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

    private void resetGesture(int gestureId) {
        String key = gestureId > 0 ? String.valueOf(gestureId) : "y";
        String originalMeaning = gesturePrefs.getString(KEY_ORIGINAL_PREFIX + key, key);
        gesturePrefs.edit().remove(KEY_CUSTOM_PREFIX + key).apply();
        populateGestureGrid();
        Toast.makeText(this, "Gesture reset to default", Toast.LENGTH_SHORT).show();
    }

    private void showEditDialog(Gesture gesture) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_gesture, null);
        ImageView dialogImage = dialogView.findViewById(R.id.dialog_gesture_image);
        EditText dialogInput = dialogView.findViewById(R.id.dialog_input);
        try {
            // Open the image using the gesture's image path
            InputStream is = getAssets().open(gesture.getImagePath());
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            dialogImage.setImageBitmap(bitmap);
            is.close();

            // Retrieve the original meaning and current custom label from preferences
            String originalMeaning = gesturePrefs.getString(KEY_ORIGINAL_PREFIX + gesture.getLabel(), gesture.getTranslation());
            String currentLabel = gesturePrefs.getString(KEY_CUSTOM_PREFIX + gesture.getLabel(), originalMeaning);
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

    // Updated resetGesture method to accept a String label
    private void resetGesture(String gestureLabel) {
        String key = gestureLabel;
        String originalMeaning = gesturePrefs.getString(KEY_ORIGINAL_PREFIX + key, gestureLabel);
        gesturePrefs.edit().remove(KEY_CUSTOM_PREFIX + key).apply();
        populateGestureGrid();
        Toast.makeText(this, "Gesture reset to default", Toast.LENGTH_SHORT).show();
    }

}
