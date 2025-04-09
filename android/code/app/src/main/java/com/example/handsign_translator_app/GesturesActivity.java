package com.example.handsign_translator_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Configuration;
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
import androidx.appcompat.app.AppCompatDelegate;
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
import java.util.Locale;
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
        applySavedLanguage();

        SharedPreferences sharedPreferences = getSharedPreferences("theme_pref", MODE_PRIVATE);
        boolean nightMode = sharedPreferences.getBoolean("dark_mode", false);

        if (nightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gestures);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        gesturePrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        setUp();
        initializeOriginalMeanings();
        loadGestureImages();
        setupClickListeners();
        setupMoreOptionsMenu();
    }

    private void initializeOriginalMeanings() {
        all_gestures = gestureInfoHelper.getGestures();
        originalMeanings = new HashMap<>();

        for (Gesture gesture : all_gestures) {
            String meaning = gesture.getTranslation();
            originalMeanings.put(gesture.getLabel(), meaning);

            String key = KEY_ORIGINAL_PREFIX + gesture.getLabel();
            if (!gesturePrefs.getString(key, "").equals(meaning)) {
                gesturePrefs.edit().putString(key, meaning).apply();
            }
            if (!gesturePrefs.contains(key)) {
                gesturePrefs.edit().putString(key, meaning).apply();
            }
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
            builder.setTitle(getString(R.string.gesture_options_title))
                    .setItems(new CharSequence[]{
                            getString(R.string.reset_all_to_default),
                            getString(R.string.reset_current_gesture)
                    }, (dialog, which) -> {
                        if (which == 0) {
                            resetAllGestures();
                        }
                    })
                    .show();
        });
    }

    private void resetAllGestures() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.reset_all_gestures_title))
                .setMessage(getString(R.string.reset_all_gestures_message))
                .setPositiveButton(getString(R.string.reset), (dialog, which) -> {
                    SharedPreferences.Editor editor = gesturePrefs.edit();
                    for (Gesture gesture : all_gestures) {
                        editor.remove(KEY_CUSTOM_PREFIX + gesture.getLabel());
                    }
                    for (int i = 1; i <= 10; i++) {
                        editor.remove(KEY_CUSTOM_PREFIX + i);
                    }
                    editor.apply();
                    loadGestureImages();
                    Toast.makeText(this, getString(R.string.gesture_reset_success), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private void resetGesture(int gestureId) {
        String key = gestureId > 0 ? String.valueOf(gestureId) : "y";
        String originalMeaning = gesturePrefs.getString(KEY_ORIGINAL_PREFIX + key, key);
        gesturePrefs.edit().remove(KEY_CUSTOM_PREFIX + key).apply();

        TextView labelView = findViewById(gestureId > 0 ?
                getResources().getIdentifier("gesture_label_" + gestureId, "id", getPackageName()) :
                R.id.gesture_label_y);
        labelView.setText(originalMeaning);

        Toast.makeText(this, getString(R.string.gesture_reset_success), Toast.LENGTH_SHORT).show();
    }

    private void setupClickListeners() {
        for (int i = 1; i <= 10; i++) {
            CardView card = findViewById(getResources().getIdentifier("gesture_card_" + i, "id", getPackageName()));
            final int gestureId = i;
            card.setOnClickListener(v -> showEditDialog(gestureId));
        }

        CardView yCard = findViewById(R.id.gesture_card_y);
        yCard.setOnClickListener(v -> showEditDialog(-1));
    }

    private void showEditDialog(int gestureId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_gesture, null);

        ImageView dialogImage = dialogView.findViewById(R.id.dialog_gesture_image);
        EditText dialogInput = dialogView.findViewById(R.id.dialog_input);

        try {
            InputStream is;
            String currentLabel;
            String key = gestureId > 0 ? String.valueOf(gestureId) : "y";

            if (gestureId > 0) {
                is = getAssets().open(String.format("hand_signs_images/asl_numbers/%03d.png", gestureId));
                TextView labelView = findViewById(getResources().getIdentifier("gesture_label_" + gestureId, "id", getPackageName()));
                currentLabel = labelView.getText().toString();
            } else {
                is = getAssets().open("hand_signs_images/asl_alphabet/Y.png");
                TextView labelView = findViewById(R.id.gesture_label_y);
                currentLabel = labelView.getText().toString();
            }

            Bitmap bitmap = BitmapFactory.decodeStream(is);
            dialogImage.setImageBitmap(bitmap);
            is.close();

            String originalMeaning = gesturePrefs.getString(KEY_ORIGINAL_PREFIX + key, key);
            dialogInput.setHint(getString(R.string.enter_new_meaning) + " (" + originalMeaning + ")");
            dialogInput.setText(currentLabel);
            dialogInput.setSelection(currentLabel.length());

        } catch (IOException e) {
            e.printStackTrace();
        }

        builder.setView(dialogView)
                .setTitle(getString(R.string.edit_gesture_title))
                .setPositiveButton(getString(R.string.save), (dialog, which) -> {
                    String newLabel = dialogInput.getText().toString().trim();
                    if (!newLabel.isEmpty()) {
                        String prefKey = KEY_CUSTOM_PREFIX + (gestureId > 0 ? gestureId : "y");
                        gesturePrefs.edit().putString(prefKey, newLabel).apply();

                        TextView labelView = findViewById(gestureId > 0 ?
                                getResources().getIdentifier("gesture_label_" + gestureId, "id", getPackageName()) :
                                R.id.gesture_label_y);
                        labelView.setText(newLabel);
                    }
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .setNeutralButton(getString(R.string.reset), (dialog, which) -> resetGesture(gestureId))
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

    private void applySavedLanguage() {
        SharedPreferences prefs = getSharedPreferences("language_pref", MODE_PRIVATE);
        String langCode = prefs.getString("selected_language", "en");

        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    private void navigateToMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
    }

    private void navigateToSettingsActivity() {
        startActivity(new Intent(this, SettingActivity.class));
    }
}
