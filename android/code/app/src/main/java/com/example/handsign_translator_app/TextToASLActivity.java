package com.example.handsign_translator_app;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class TextToASLActivity extends AppCompatActivity {
    private TextInputEditText inputText;
    private MaterialButton clearButton;
    private GridLayout imagesContainer;
    private BottomNavigationView bottomNavigationView;
    private MaterialButtonToggleGroup modeToggle;
    private AssetManager assetManager;
    private boolean isWordMode = false;
    private MaterialButton backButton;

    // List of available word gestures
    private final List<String> availableWords = Arrays.asList(
            "hello", "yes", "no", "thank you", "my name is", "i love you",
            "help", "please", "sorry", "stop", "eat", "drink", "more",
            "what", "when", "where", "bathroom", "pain"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_text_to_asl);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setUp();
    }

    private void setUp() {

        inputText = findViewById(R.id.input_text);
        clearButton = findViewById(R.id.button_clear);
        imagesContainer = findViewById(R.id.images_container);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        modeToggle = findViewById(R.id.mode_toggle);
        assetManager = getApplicationContext().getAssets();
        backButton = findViewById(R.id.button_back);

        backButton.setOnClickListener(v -> {Intent intent = new Intent(TextToASLActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

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

        clearButton.setOnClickListener(v -> {
            inputText.setText("");
            imagesContainer.removeAllViews();
        });
        modeToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                isWordMode = checkedId == R.id.button_word_mode;
                inputText.setText("");
                imagesContainer.removeAllViews();
                inputText.setHint(isWordMode ? "Enter a word" : "Enter a letter or number");
            }
        });
        inputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    translateInput();
                } else {
                    imagesContainer.removeAllViews();
                }
            }
        });
    }
    private void translateInput() {
        String input = inputText.getText().toString().toLowerCase().trim();
        if (input.isEmpty()) {
            return;
        }

        imagesContainer.removeAllViews();

        if (isWordMode) {
            // Split input into words
            String[] words = input.split("\\s+");
            StringBuilder currentPhrase = new StringBuilder();
            
            for (String word : words) {
                currentPhrase.append(word).append(" ");
                String phrase = currentPhrase.toString().trim();
                
                // Check if the current phrase is a known word/phrase
                if (availableWords.contains(phrase)) {
                    // Add the phrase image
                    addWordImage(phrase);
                    currentPhrase = new StringBuilder();
                } else if (!isPartOfKnownPhrase(phrase)) {
                    // If it's not part of a known phrase, spell it out
                    if (currentPhrase.length() > 0) {
                        spellOutWord(currentPhrase.toString().trim());
                        currentPhrase = new StringBuilder();
                    }
                }
            }
            // Handle any remaining text
            if (currentPhrase.length() > 0) {
                spellOutWord(currentPhrase.toString().trim());
            }
        } else {
            spellOutWord(input);
        }
    }
    private boolean isPartOfKnownPhrase(String partialPhrase) {
        for (String word : availableWords) {
            if (word.startsWith(partialPhrase)) {
                return true;
            }
        }
        return false;
    }
    private void spellOutWord(String word) {
        for (char c : word.toCharArray()) {
            if (Character.isLetter(c)) {
                addLetterImage(Character.toUpperCase(c));
            } else if (Character.isDigit(c)) {
                addNumberImage(Character.getNumericValue(c));
            }
        }
    }
    private void addWordImage(String word) {
        try {
            String imagePath = "hand_signs_images/asl_words/" + word + ".png";
            addImageToContainer(imagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void addLetterImage(char letter) {
        try {
            String imagePath = "hand_signs_images/asl_alphabet/" + letter + ".png";
            addImageToContainer(imagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void addNumberImage(int number) {
        try {
            String imagePath = "hand_signs_images/asl_numbers/" + String.format("%03d", number) + ".png";
            addImageToContainer(imagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addImageToContainer(String imagePath) throws IOException {
        InputStream is = assetManager.open(imagePath);
        Bitmap bitmap = BitmapFactory.decodeStream(is);
        is.close();
        ImageView imageView = new ImageView(this);

        int screenWidth = getResources().getDisplayMetrics().widthPixels;

        int imageSize = (screenWidth - dpToPx(48)) / 3;
        
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = imageSize;
        params.height = imageSize;

        params.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
        
        imageView.setLayoutParams(params);

        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        imageView.setImageBitmap(bitmap);
        
        imagesContainer.addView(imageView);
    }
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
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