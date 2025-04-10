package com.example.handsign_translator_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.handsign_translator_app.database.GestureLogDbHelper;
import com.example.handsign_translator_app.models.GestureLog;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class LogsActivity extends AppCompatActivity {
    private TextView titleLogs;
    private ImageButton buttonMoreOptions;
    private ListView logsListView;
    private GestureLogDbHelper dbHelper;
    private ArrayAdapter<GestureLog> logsAdapter;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_logs);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setUp();
        loadLogs();
    }

    private void setUp() {
        titleLogs = findViewById(R.id.title_logs);
        buttonMoreOptions = findViewById(R.id.button_more_options);
        logsListView = findViewById(R.id.logs_list_view);
        dbHelper = new GestureLogDbHelper(this);

        buttonMoreOptions.setOnClickListener(v -> showOptionsDialog());

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setItemActiveIndicatorColor(ContextCompat.getColorStateList(this, R.color.blue_gray_100));

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.translation) {
                navigateToMainActivity();
                return true;
            } else if (item.getItemId() == R.id.gestures) {
                navigateToGesturesActivity();
                return true;
            } else if (item.getItemId() == R.id.settings) {
                navigateToSettingsActivity();
                return true;
            } else if (item.getItemId() == R.id.learning) {
                navigateToLearningActivity();
                return true;
            }
            return false;
        });
    }

    private void loadLogs() {
        List<GestureLog> logs = dbHelper.getAllLogs();
        logsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, logs);
        logsListView.setAdapter(logsAdapter);
    }

    private void showOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Log Options")
                .setItems(new CharSequence[]{"Clear All Logs"}, (dialog, which) -> {
                    if (which == 0) {
                        showClearLogsConfirmation();
                    }
                })
                .show();
    }

    private void showClearLogsConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Clear All Logs")
                .setMessage("Are you sure you want to clear all gesture logs? This action cannot be undone.")
                .setPositiveButton("Clear", (dialog, which) -> {
                    dbHelper.clearAllLogs();
                    loadLogs();
                })
                .setNegativeButton("Cancel", null)
                .show();
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
        finish();
    }
} 