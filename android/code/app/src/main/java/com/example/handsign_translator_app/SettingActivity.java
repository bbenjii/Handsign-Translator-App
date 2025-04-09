package com.example.handsign_translator_app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Locale;

public class SettingActivity extends AppCompatActivity {

    private TextView titleSettings;
    private ImageButton buttonMoreOptions;
    private BottomNavigationView bottomNavigationView;

    private SwitchCompat switchCompat;
    private TextView modeLabel;
    private LinearLayout languageRow;
    private TextView currentLanguage;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applySavedLanguage();

        sharedPreferences = getSharedPreferences("theme_pref", Context.MODE_PRIVATE);
        boolean nightMode = sharedPreferences.getBoolean("dark_mode", false);

        if (nightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setting);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Theme toggle setup
        switchCompat = findViewById(R.id.theme_switch);
        modeLabel = findViewById(R.id.text_mode_label);
        switchCompat.setChecked(nightMode);
        updateLabelText(nightMode);

        switchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor = sharedPreferences.edit();
            editor.putBoolean("dark_mode", isChecked);
            editor.apply();

            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );

            // Restart activity
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        });

        // Language setup
        languageRow = findViewById(R.id.language_row);
        currentLanguage = findViewById(R.id.current_language);

        // Display saved language
        Locale current = getResources().getConfiguration().locale;
        if (current.getLanguage().equals("fr")) {
            currentLanguage.setText(getString(R.string.french));
        } else {
            currentLanguage.setText(getString(R.string.english));
        }

        languageRow.setOnClickListener(view -> {
            final String[] languages = {getString(R.string.english), getString(R.string.french)};

            AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
            builder.setTitle(getString(R.string.select_language))
                    .setItems(languages, (dialog, which) -> {
                        if (which == 0) {
                            setLocale("en");
                            currentLanguage.setText(getString(R.string.english));
                        } else {
                            setLocale("fr");
                            currentLanguage.setText(getString(R.string.french));
                        }
                    });
            builder.show();
        });

        setUp(); // Bottom nav
    }

    private void updateLabelText(boolean isDarkMode) {
        if (isDarkMode) {
            modeLabel.setText(getString(R.string.dark_mode));
        } else {
            modeLabel.setText(getString(R.string.light_mode));
        }
    }

    // Save selected language and apply it
    private void setLocale(String langCode) {
        SharedPreferences.Editor langEditor = getSharedPreferences("language_pref", MODE_PRIVATE).edit();
        langEditor.putString("selected_language", langCode);
        langEditor.apply();

        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        recreate(); // Refresh activity
    }

    // Load and apply previously saved language before onCreate()
    private void applySavedLanguage() {
        SharedPreferences prefs = getSharedPreferences("language_pref", MODE_PRIVATE);
        String langCode = prefs.getString("selected_language", "en");

        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    private void setUp() {
        titleSettings = findViewById(R.id.title_settings);
        buttonMoreOptions = findViewById(R.id.button_more_options);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setSelectedItemId(R.id.settings);
        bottomNavigationView.setItemActiveIndicatorColor(
                ContextCompat.getColorStateList(this, R.color.blue_gray_100)
        );

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.translation) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (item.getItemId() == R.id.gestures) {
                startActivity(new Intent(this, GesturesActivity.class));
                return true;
            }
            return false;
        });
    }
}
