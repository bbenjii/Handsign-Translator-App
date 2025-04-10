package com.example.handsign_translator_app;

import static android.view.View.INVISIBLE;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.handsign_translator_app.bluetooth.BluetoothModule;
import com.example.handsign_translator_app.controllers.GestureController;
import com.example.handsign_translator_app.ml_module.GestureClassifier;
import com.example.handsign_translator_app.models.Gesture;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;
import java.util.Objects;

public class LearningActivity extends AppCompatActivity implements GestureController.GestureListener{

    private TextView titleLearning;
    private ImageButton buttonMoreOptions;

    private TextView textViewInstructions;
    private TextView textViewInstructedGesture;
    private ImageButton buttonSpeaker;
    private ImageView imageViewUserGesture;
    private Button buttonNextGesture;
    private FrameLayout frameSlideUp;
    private Boolean frameUp = false;
    private TextView textViewResult;
    private CardView cardViewNextButton;

    private final int correctPanelColor = Color.parseColor("#AED581");
    private final int correctNextButtonColor =  Color.parseColor("#81C784");

    private final int incorrectPanelColor = Color.parseColor("#E57373");
    private final int incorrectNextButtonColor =  Color.parseColor("#F44336");
    private final int defaultNextButtonColor =  Color.parseColor("#90A4AE");

    private GestureInfoHelper gestureInfoHelper;
    private List<Gesture> all_gestures;
    private Gesture instructedGesture;
    private AssetManager assetManager;


    // Controller, classifier, and bluetooth module instances
    private GestureController gestureController;
    private GestureClassifier gestureClassifier;
    private BluetoothModule bluetoothModule;
    private BluetoothService bluetoothService;
    private boolean isBound = false;
    private BottomNavigationView bottomNavigationView;


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
            bluetoothService = binder.getService();
            bluetoothModule = bluetoothService.getBluetoothModule();
            isBound = true;

            setUp();
            nextGesture();
            defaultFrame();
            gestureController.start();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    /**
     * Callback from GestureController when a new gesture is detected.
     * Updates UI and speaks new translation if it has changed.
     */
    @Override
    public void onGestureDetected(Gesture gesture) {
        runOnUiThread(() -> {
            Boolean correct = Objects.equals(gesture.getLabel(), instructedGesture.getLabel());

            if(correct){
                correctFrame();
            }
            else{
                incorrectFrame();
            }

        });
    }

    @Override
    public void rawDataOutput(String data) {
        // Update UI with loading animation and message
//        setGestureImageView("lebronjames.png");
        // Clear any loading animation
//        clearLoadingAnimation();        textTranslatedOutput.setText(data);
    }

    /**
     * Callback from GestureController when gesture translation is in progress.
     * Updates UI to indicate translation is ongoing.
     */
    @Override
    public void onTranslationInProgress() {
        // Update UI with loading animation and message
//        setLoadingAnimation();
//        textTranslatedOutput.setText("Translating...");
    }

    @Override
    public void onNoDeviceConnected() {
//        setLoadingAnimation();
//        textTranslatedOutput.setText("No device connected...");
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_learning);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Start and bind to the service.
        Intent serviceIntent = new Intent(this, BluetoothService.class);
        startService(serviceIntent); // This makes the service live beyond activity binding.
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

    }


    private void setUp() {
        titleLearning = findViewById(R.id.title_learning);
        buttonMoreOptions = findViewById(R.id.button_more_options);

        textViewInstructions = findViewById(R.id.textViewInstructions);
        textViewInstructedGesture = findViewById(R.id.textViewInstructedGesture);
        buttonSpeaker = findViewById(R.id.button_speaker);


        buttonNextGesture = findViewById(R.id.button_next_gesture);
        buttonNextGesture.setOnClickListener(v -> {
            nextGesture();
            defaultFrame();

//            if(frameUp){
//                correctFrame();
////                slideFrameDown();
//            }
//            else{
//                incorrectFrame();
////                slideFrameUp();
//            }
//            frameUp = !frameUp;

        });

        frameSlideUp = findViewById(R.id.frame_slide_up);

        textViewResult = findViewById(R.id.textViewResult);
        imageViewUserGesture = findViewById(R.id.imageViewUserGesture);
        cardViewNextButton = findViewById(R.id.cardViewNextButton);

        assetManager = getApplicationContext().getAssets();
        gestureInfoHelper = new GestureInfoHelper(assetManager, getApplicationContext());

        all_gestures = gestureInfoHelper.getGestures();

        gestureClassifier = new GestureClassifier(assetManager, getApplicationContext());
        // Pass this activity as the GestureListener so that callbacks can update the UI
        gestureController = new GestureController(bluetoothModule, gestureClassifier, this, getApplicationContext());


        // Bottom Navigation
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setSelectedItemId(R.id.learning);
        bottomNavigationView.setItemActiveIndicatorColor(ContextCompat.getColorStateList(this, R.color.blue_gray_100));

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.translation) {
                navigateToMainActivity();
                return true; // Event handled
            }
            else if (item.getItemId() == R.id.gestures) {
                navigateToGesturesActivity();
                return true; // Event handled
            } else if (item.getItemId() == R.id.settings) {
                navigateToSettingsActivity();
                return true;
            }
            return false; // Event not handled
        });

    }

    private float dpToPx(int dp){

        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }

    private void defaultFrame(){
        frameSlideUp.setVisibility(View.GONE);
        cardViewNextButton.setBackgroundTintList(ColorStateList.valueOf(defaultNextButtonColor));
        buttonNextGesture.setText("SKIP");
    }

    private void correctFrame(){
        frameSlideUp.setVisibility(View.VISIBLE);
        frameSlideUp.setBackgroundColor(correctPanelColor);
        cardViewNextButton.setBackgroundTintList(ColorStateList.valueOf(correctNextButtonColor));

        textViewResult.setText("Correct!");
        buttonNextGesture.setText("NEXT");
    }

    private void incorrectFrame(){
        frameSlideUp.setVisibility(View.VISIBLE);
        frameSlideUp.setBackgroundColor(incorrectPanelColor);
        cardViewNextButton.setBackgroundTintList(ColorStateList.valueOf(incorrectNextButtonColor));

        textViewResult.setText("Keep trying! or skip");
        buttonNextGesture.setText("SKIP");
    }

    private void slideFrameUp(){
        frameSlideUp.setVisibility(View.VISIBLE);

//        frameSlideUp.setTranslationY(0);
//        frameSlideUp.animate()
//                .translationY(0)  // move to original position
//                .setDuration(50) // 1 second
//                .start();
    }

    private void slideFrameDown(){
        frameSlideUp.setVisibility(View.GONE);

//        frameSlideUp.setTranslationY(dpToPx(100));

//        frameSlideUp.animate()
//                .translationY(dpToPx(100))  // move to original position
//                .setDuration(200) // 1 second
//                .start();
    }

    private int getRandomGestureIndex(int length){

        int randomInt = (int)(Math.random() * length); // 0 to 10 inclusive
        return randomInt;
    }
    private void nextGesture(){
        int gestureIndex = getRandomGestureIndex(all_gestures.size());
        instructedGesture = all_gestures.get(gestureIndex);
        textViewInstructedGesture.setText(instructedGesture.getTranslation());
        imageViewUserGesture.setImageDrawable(instructedGesture.getImage());
        gestureController.resetFlexReadingHistory();
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void navigateToGesturesActivity() {
        Intent intent = new Intent(this, GesturesActivity.class);
        startActivity(intent);
    }

    private void navigateToSettingsActivity() {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }


    /**
     * Navigates to the Learning Activity.
     */
    private void navigateToLearningActivity() {
        Intent intent = new Intent(this, LearningActivity.class);
        startActivity(intent);
    }


}