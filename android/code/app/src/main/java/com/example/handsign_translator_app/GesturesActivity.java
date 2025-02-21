package com.example.handsign_translator_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class GesturesActivity extends AppCompatActivity {

    private TextView titleGestures;
    private ImageButton buttonMoreOptions;
    private BottomNavigationView bottomNavigationView;

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

        setUp();
    }

    private void setUp() {
        titleGestures = findViewById(R.id.title_settings);

        buttonMoreOptions = findViewById(R.id.button_more_options);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setSelectedItemId(R.id.gestures);
        bottomNavigationView.setItemActiveIndicatorColor(ContextCompat.getColorStateList(this, R.color.blue_gray_100));

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.translation) {
                navigateToMainActivity();
                return true; // Event handled
            }
            else if (item.getItemId() == R.id.settings) {
                navigateToSettingsActivity();
                return true; // Event handled
            }
            return false; // Event not handled
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
}