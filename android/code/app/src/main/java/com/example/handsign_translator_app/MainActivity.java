package com.example.handsign_translator_app;
import java.util.*;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
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
import com.example.handsign_translator_app.ml_module.GestureDetection;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
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

    private BluetoothModule bluetoothModule;
    private Handler handler = new Handler();
//    private Handler handler = new Handler(Looper.getMainLooper());

    private Runnable runnable;


    // Buffer to track recent readings
    private static Deque<int[]> flexReadingsHistory;


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
        readGesture();
    }

    /**
     * SetUP the Main Activity
     */
    private void setUp(){
        titleTranslate = findViewById(R.id.title_translate);

        labelHandSign = findViewById(R.id.label_hand_sign);
        labelLanguage = findViewById(R.id.label_language);

        textTranslatedOutput = findViewById(R.id.text_translated_output);
        imageHandSign = findViewById(R.id.image_hand_sign);
        buttonHistory = findViewById(R.id.button_history);
        buttonMoreOptions = findViewById(R.id.button_more_options);
        buttonSpeaker = findViewById(R.id.button_speaker);

        bluetoothModule = new BluetoothModule();
        flexReadingsHistory = new ArrayDeque<>();

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setSelectedItemId(R.id.translation);
        bottomNavigationView.setItemActiveIndicatorColor(ContextCompat.getColorStateList(this, R.color.blue_gray_100));
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.settings) {
                navigateToSettingsActivity();
                return true; // Event handled
            }
            else if (item.getItemId() == R.id.gestures) {
                navigateToGesturesActivity();
                return true; // Event handled
            }
            return false; // Event not handled
        });

    }

    private void readGesture(){
        runnable = new Runnable() {
            @Override
            public void run() {
                int[] currentFlexReadings = bluetoothModule.getGloveData();
                flexReadingsHistory.addLast(currentFlexReadings);

                // Maintain sliding window size
                if (flexReadingsHistory.size() > STABILITY_WINDOW) {
                    flexReadingsHistory.removeFirst();
                }

                // Check gesture stability
                boolean isStable = GestureDetection.isGestureStable(flexReadingsHistory);

                if(isStable){
                    textTranslatedOutput.setText("STABLE...");
                }
                else {
                    textTranslatedOutput.setText("READING...");
                }

                handler.postDelayed(this, 500); // 0.5 seconds delay
            }
        };

        handler.post(runnable);



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