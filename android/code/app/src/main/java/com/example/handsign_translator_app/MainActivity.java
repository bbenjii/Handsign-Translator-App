package com.example.handsign_translator_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private TextView titleTranslate;
    private TextView labelHandSign;
    private TextView labelLanguage;
    private TextView textTranslatedOutput;
    private ImageButton buttonHistory;
    private ImageButton buttonMoreOptions;
    private ImageButton buttonSpeaker;
    private ImageView imageHandSign;
    private BottomNavigationView bottomNavigationView;

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

    private void navigateToSettingsActivity() {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }

    private void navigateToGesturesActivity() {
        Intent intent = new Intent(this, GesturesActivity.class);
        startActivity(intent);
    }
}