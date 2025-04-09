package com.example.handsign_translator_app;

import static android.view.View.INVISIBLE;

import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
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

import com.example.handsign_translator_app.models.Gesture;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class LearningActivity extends AppCompatActivity {

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
    List<Gesture> all_gestures;
    AssetManager assetManager;


    private BottomNavigationView bottomNavigationView;

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

        setUp();
        nextGesture();
        defaultFrame();

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

            if(frameUp){
                correctFrame();
//                slideFrameDown();
            }
            else{
                incorrectFrame();
//                slideFrameUp();
            }
            frameUp = !frameUp;

        });

        frameSlideUp = findViewById(R.id.frame_slide_up);

        textViewResult = findViewById(R.id.textViewResult);
        imageViewUserGesture = findViewById(R.id.imageViewUserGesture);
        cardViewNextButton = findViewById(R.id.cardViewNextButton);

        assetManager = getApplicationContext().getAssets();
        gestureInfoHelper = new GestureInfoHelper(assetManager, getApplicationContext());

        all_gestures = gestureInfoHelper.getGestures();


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
        Gesture gesture = all_gestures.get(gestureIndex);
        textViewInstructedGesture.setText(gesture.getTranslation());
        imageViewUserGesture.setImageDrawable(gesture.getImage());
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