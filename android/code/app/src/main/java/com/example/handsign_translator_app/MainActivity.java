package com.example.handsign_translator_app;

// Import necessary packages and classes
import java.io.InputStream;
import java.util.*;

import android.content.res.AssetManager;
import java.io.IOException;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.handsign_translator_app.bluetooth.BluetoothModule;
import com.example.handsign_translator_app.controllers.GestureController;
import com.example.handsign_translator_app.ml_module.GestureClassifier;
import com.example.handsign_translator_app.ml_module.GestureStabilityChecker;
import com.example.handsign_translator_app.models.Gesture;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener, GestureController.GestureListener {

    // UI element declarations
    private TextView titleTranslate;
    private TextView labelHandSign;
    private TextView labelLanguage;
    private TextView textTranslatedOutput;
    private ImageButton buttonHistory;
    private ImageButton buttonMoreOptions;
    private ImageButton buttonSpeaker;
    private ImageView imageHandSign;
    private BottomNavigationView bottomNavigationView;
    private AssetManager assetManager;

    // Text-to-Speech engine and state flag for loading animation
    private TextToSpeech tts;
    private boolean loading = false;

    // Controller, classifier, and bluetooth module instances
    private GestureController gestureController;
    private GestureClassifier gestureClassifier;
    private BluetoothModule bluetoothModule;

    // Used to track the last translation spoken to prevent repeated TTS
    private String lastSpokenTranslation = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Set up UI elements and components
        setUp();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start gesture detection when activity resumes
        gestureController.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop gesture detection when activity is paused
        gestureController.stop();
    }

    /**
     * Initializes UI elements and sets up components like TTS, Bluetooth, and GestureController.
     */
    private void setUp() {
        // Link UI components from layout
        titleTranslate = findViewById(R.id.title_translate);
        labelHandSign = findViewById(R.id.label_hand_sign);
        labelLanguage = findViewById(R.id.label_language);
        textTranslatedOutput = findViewById(R.id.text_translated_output);
        imageHandSign = findViewById(R.id.image_hand_sign);
        buttonHistory = findViewById(R.id.button_history);
        buttonMoreOptions = findViewById(R.id.button_more_options);
        buttonSpeaker = findViewById(R.id.button_speaker);

        // Initially disable TTS button until TTS is properly initialized
        buttonSpeaker.setEnabled(false);
        // Set up a click listener to speak the current translation when the speaker button is pressed
        buttonSpeaker.setOnClickListener(v -> {
            String text = textTranslatedOutput.getText().toString();
            speak(text);
        });

        // Initialize TextToSpeech engine
        tts = new TextToSpeech(this, this);

        // Instantiate Bluetooth module, asset manager, gesture classifier, and gesture controller
        bluetoothModule = new BluetoothModule();
        assetManager = getApplicationContext().getAssets();
        gestureClassifier = new GestureClassifier(assetManager);
        // Pass this activity as the GestureListener so that callbacks can update the UI
        gestureController = new GestureController(bluetoothModule, gestureClassifier, this);

        // Set up bottom navigation view for activity navigation
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.translation);
        bottomNavigationView.setItemActiveIndicatorColor(ContextCompat.getColorStateList(this, R.color.blue_gray_100));
        bottomNavigationView.setOnItemSelectedListener(item -> {
            // Navigate to Settings if selected
            if (item.getItemId() == R.id.settings) {
                navigateToSettingsActivity();
                return true;
                // Navigate to Gestures Activity if selected
            } else if (item.getItemId() == R.id.gestures) {
                navigateToGesturesActivity();
                return true;
            }
            return false;
        });
    }

    /**
     * Callback from GestureController when a new gesture is detected.
     * Updates UI and speaks new translation if it has changed.
     */
    @Override
    public void onGestureDetected(Gesture gesture) {
        runOnUiThread(() -> {
            String newTranslation = gesture.getTranslation();
            // Update text view with the new translation
            textTranslatedOutput.setText(newTranslation);
            // Update the image view with the gesture image
            setGestureImageView(gesture.getImagePath());
            // Clear any loading animation
            clearLoadingAnimation();
            // Speak new translation if it has changed from the last spoken translation
            if (!newTranslation.equals(lastSpokenTranslation)) {
                speak(newTranslation);
                lastSpokenTranslation = newTranslation;
            }
        });
    }

    /**
     * Callback from GestureController when gesture translation is in progress.
     * Updates UI to indicate translation is ongoing.
     */
    @Override
    public void onTranslationInProgress() {
        // Update UI with loading animation and message
        setLoadingAnimation();
        textTranslatedOutput.setText("Translating...");
    }

    /**
     * Starts a loading animation on the image view to indicate processing.
     */
    private void setLoadingAnimation() {
        if (loading) return; // Prevent duplicate animations
        loading = true;
        RotateAnimation anim = new RotateAnimation(0.0f, 360.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.INFINITE);
        anim.setDuration(5000);
        imageHandSign.startAnimation(anim);
    }

    /**
     * Clears the loading animation.
     */
    private void clearLoadingAnimation() {
        if (!loading) return;
        loading = false;
        imageHandSign.clearAnimation();
    }

    /**
     * Loads and sets the gesture image based on the given asset path.
     */
    private void setGestureImageView(String path) {
        try {
            InputStream ims = assetManager.open(path);
            Drawable d = Drawable.createFromStream(ims, null);
            imageHandSign.setImageDrawable(d);
        } catch (IOException ex) {
            // Handle error (for example, show a default image or log the error)
        }
    }

    /**
     * Uses the TextToSpeech engine to speak the provided text.
     */
    private void speak(String text) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    /**
     * Called when the TextToSpeech engine is initialized.
     */
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            // Set TTS language to Canadian English
            int result = tts.setLanguage(Locale.CANADA);
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                buttonSpeaker.setEnabled(true);
            }
        }
    }

    /**
     * Navigates to the Settings Activity.
     */
    private void navigateToSettingsActivity() {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }

    /**
     * Navigates to the Gestures Activity.
     */
    private void navigateToGesturesActivity() {
        Intent intent = new Intent(this, GesturesActivity.class);
        startActivity(intent);
    }

    /**
     * Ensures proper shutdown of TTS and closing of resources.
     */
    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.shutdown();
        }
        gestureClassifier.close();
        super.onDestroy();
    }
}