package com.example.handsign_translator_app;

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

    private TextToSpeech tts;
    private boolean loading = false;
    private GestureController gestureController;
    private GestureClassifier gestureClassifier;
    private BluetoothModule bluetoothModule;

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
        setUp();

    }

    @Override
    protected void onResume() {
        super.onResume();
        gestureController.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gestureController.stop();
    }

    /**
     * SetUP the Main Activity
     */
    private void setUp() {
        titleTranslate = findViewById(R.id.title_translate);

        labelHandSign = findViewById(R.id.label_hand_sign);
        labelLanguage = findViewById(R.id.label_language);

        textTranslatedOutput = findViewById(R.id.text_translated_output);
        imageHandSign = findViewById(R.id.image_hand_sign);
        buttonHistory = findViewById(R.id.button_history);
        buttonMoreOptions = findViewById(R.id.button_more_options);
        buttonSpeaker = findViewById(R.id.button_speaker);
        buttonSpeaker.setEnabled(false);
        buttonSpeaker.setOnClickListener(v -> {
            String text = textTranslatedOutput.getText().toString();
            speak(text);
        });

        tts = new TextToSpeech(this, this);

        bluetoothModule = new BluetoothModule();
        assetManager = getApplicationContext().getAssets();
        gestureClassifier = new GestureClassifier(assetManager);
        gestureController = new GestureController(bluetoothModule, gestureClassifier, this);


        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.translation);
        bottomNavigationView.setItemActiveIndicatorColor(ContextCompat.getColorStateList(this, R.color.blue_gray_100));
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.settings) {
                navigateToSettingsActivity();
                return true;
            } else if (item.getItemId() == R.id.gestures) {
                navigateToGesturesActivity();
                return true;
            }
            return false;
        });
    }

    @Override
    public void onGestureDetected(Gesture gesture) {
        runOnUiThread(() -> {
            String newTranslation = gesture.getTranslation();
            textTranslatedOutput.setText(newTranslation);
            setGestureImageView(gesture.getImagePath());
            clearLoadingAnimation();
            // Speak new translation if it has changed
            if (!newTranslation.equals(lastSpokenTranslation)) {
                speak(newTranslation);
                lastSpokenTranslation = newTranslation;
            }
        });
    }

    @Override
    public void onTranslationInProgress() {
        setLoadingAnimation();
        textTranslatedOutput.setText("Translating...");
    }

    private void setLoadingAnimation() {
        if (loading) return;
        loading = true;
        RotateAnimation anim = new RotateAnimation(0.0f, 360.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.INFINITE);
        anim.setDuration(5000);
        imageHandSign.startAnimation(anim);
    }

    private void clearLoadingAnimation() {
        if (!loading) return;
        loading = false;
        imageHandSign.clearAnimation();
    }

    private void setGestureImageView(String path) {
        try {
            InputStream ims = assetManager.open(path);
            Drawable d = Drawable.createFromStream(ims, null);
            imageHandSign.setImageDrawable(d);
        } catch (IOException ex) {
            // Handle error (for example, show a default image)
        }
    }

    // Method to speak text
    private void speak(String text) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.CANADA);
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                buttonSpeaker.setEnabled(true);
            }
        }
    }

    // Activity Navigation
    private void navigateToSettingsActivity() {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }

    private void navigateToGesturesActivity() {
        Intent intent = new Intent(this, GesturesActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.shutdown();
        }
        gestureClassifier.close();
        super.onDestroy();
    }
}