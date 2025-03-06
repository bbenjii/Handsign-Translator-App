package com.example.handsign_translator_app;

import java.io.InputStream;
import java.util.*;

import android.content.res.AssetManager;

import java.io.IOException;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;


import com.example.handsign_translator_app.bluetooth.BluetoothModule;
import com.example.handsign_translator_app.ml_module.GestureClassifier;
import com.example.handsign_translator_app.ml_module.GestureDetection;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity  implements TextToSpeech.OnInitListener{
    private final int STABILITY_WINDOW = 10; // Number of readings to track for stability

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
    private GestureClassifier gestureClassifier;
    private BluetoothModule bluetoothModule;
    private Handler handler = new Handler();

    private Runnable runnable;
    private TextToSpeech tts;


    // Buffer to track recent readings
    private static Deque<int[]> flexReadingsHistory;
    int[] currentFlexReadings;
    private Boolean loading = false;
    Map<String, String> currentGesture = new HashMap<>();


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

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUp();
        asynchronousDataRead();

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
            String text= currentGesture.get("translation");
            speak(text);
        });
        tts = new TextToSpeech(this, this);


        bluetoothModule = new BluetoothModule();
        flexReadingsHistory = new ArrayDeque<>();

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setSelectedItemId(R.id.translation);
        bottomNavigationView.setItemActiveIndicatorColor(ContextCompat.getColorStateList(this, R.color.blue_gray_100));
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.settings) {
                navigateToSettingsActivity();
                return true; // Event handled
            } else if (item.getItemId() == R.id.gestures) {
                navigateToGesturesActivity();
                return true; // Event handled
            }
            return false; // Event not handled
        });

        assetManager = getApplicationContext().getAssets();
        gestureClassifier = new GestureClassifier(assetManager);

    }

    // Called when TextToSpeech engine is initialized
    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {
            // Set language (e.g., US English)
            int result = tts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
//                Log.e("TTS", "Language not supported.");
            } else {
                buttonSpeaker.setEnabled(true);
            }
        } else {
//            Log.e("TTS", "Initialization failed.");
        }
    }

    // Method to speak text
    private void speak(String text) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }


    private void showGestureAndTranslation(){
        currentGesture = gestureClassifier.classifyGesture(convertIntArrayToFloatArray(currentFlexReadings));
        String gesture_translation = (currentGesture.get("translation"));
        textTranslatedOutput.setText(gesture_translation);
        setGestureImageView(currentGesture.get("path"));
//        speak(currentGesture.get("translation"));
    }

    private void asynchronousDataRead(){
        runnable = new Runnable() {
            @Override
            public void run() {
                currentFlexReadings = bluetoothModule.getGloveData();
                flexReadingsHistory.addLast(currentFlexReadings);

                // Maintain sliding window size
                if (flexReadingsHistory.size() > STABILITY_WINDOW) {
                    flexReadingsHistory.removeFirst();
                }

                // Check gesture stability
                boolean isStable = GestureDetection.isGestureStable(flexReadingsHistory);
//                isStable = true;

                if (isStable) {
                    showGestureAndTranslation();
                    loading = false;

                } else {
                    setLoading();
                    loading = true;

                }


                handler.postDelayed(this, 500); // 0.5 seconds delay
            }
        };

        handler.post(runnable);

    }

    private void setLoading(){
        if(loading) return;
        loading = true;
        RotateAnimation anim = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.INFINITE);
        anim.setDuration(5000);
        imageHandSign.startAnimation(anim);

        textTranslatedOutput.setText("Translating...");
    }

    private void setGestureImageView(String path){
        // load image
        try {
            // get input stream
            InputStream ims = getAssets().open(path);
            // load image as Drawable
            Drawable d = Drawable.createFromStream(ims, null);
            // set image to ImageView
            imageHandSign.setImageDrawable(d);
            imageHandSign.setAnimation(null);
        }
        catch(IOException ex) {
            return;
        }
    }

    public static float[] convertIntArrayToFloatArray(int[] intArray) {
        if (intArray == null) {
            return null;
        }
        float[] floatArray = new float[intArray.length];
        for (int i = 0; i < intArray.length; i++) {
            // Automatic widening conversion from int to float
            floatArray[i] = intArray[i];
        }
        return floatArray;
    }


    private void navigateToSettingsActivity() {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }

    private void navigateToGesturesActivity() {
        Intent intent = new Intent(this, GesturesActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable); //stop handler when activity not visible super.onPause();

    }
}