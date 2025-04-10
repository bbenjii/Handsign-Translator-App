package com.example.handsign_translator_app;

import com.example.handsign_translator_app.models.Gesture;
import com.example.handsign_translator_app.GestureInfoHelper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GesturesActivity extends AppCompatActivity {

    private TextView titleGestures;
    private ImageButton buttonMoreOptions;
    private BottomNavigationView bottomNavigationView;
    private SharedPreferences gesturePrefs;
    private GestureInfoHelper gestureInfoHelper;
    private List<Gesture> all_gestures;
    private AssetManager assetManager;
    private Map<String, String> originalMeanings;
    private static final String PREFS_NAME = "gesture_mappings";

    // Active collection info.
    private String collectionName = "Default Collection";
    private String collectionDescription = "Default gesture collection.";

    private TextView textViewCollectionName;
    private TextView textViewCollectionDescription;
    private Button buttonChangeCollection;

    // Key prefixes – keys now include the active collection name.
    private static final String KEY_CUSTOM_PREFIX = "_custom_gesture_";
    private static final String KEY_ORIGINAL_PREFIX = "original_gesture_";
    // Key for storing the set of collections.
    private static final String KEY_COLLECTION_SET = "gestureCollections";

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

        // Initialize SharedPreferences.
        gesturePrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        // Ensure active collection is stored.
        if (!gesturePrefs.contains("collectionName")) {
            gesturePrefs.edit().putString("collectionName", collectionName).apply();
        } else {
            collectionName = gesturePrefs.getString("collectionName", collectionName);
        }
        if (!gesturePrefs.contains("collectionDescription")) {
            gesturePrefs.edit().putString("collectionDescription", collectionDescription).apply();
        } else {
            collectionDescription = gesturePrefs.getString("collectionDescription", collectionDescription);
        }

        setUp();
        // Populate the gestures list and store original meanings.
        initializeOriginalMeanings();
        // Build the grid dynamically.
        populateGestureGrid();
        setupMoreOptionsMenu();
    }

    private void setUp() {
        titleGestures = findViewById(R.id.title_settings);
        buttonMoreOptions = findViewById(R.id.button_more_options);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        assetManager = getApplicationContext().getAssets();
        gestureInfoHelper = new GestureInfoHelper(assetManager, getApplicationContext());

        // Collection views and change collection button.
        textViewCollectionName = findViewById(R.id.textViewCollectionName);
        textViewCollectionDescription = findViewById(R.id.textViewCollectionDescription);
        buttonChangeCollection = findViewById(R.id.buttonChangeCollection);

        textViewCollectionName.setText(collectionName);
        textViewCollectionDescription.setText(collectionDescription);

        // Launch our change-collection dialog.
        buttonChangeCollection.setOnClickListener(v -> showChangeCollectionDialog());

        bottomNavigationView.setSelectedItemId(R.id.gestures);
        bottomNavigationView.setItemActiveIndicatorColor(
                ContextCompat.getColorStateList(this, R.color.blue_gray_100));

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.translation) {
                navigateToMainActivity();
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

    private void navigateToMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
    }

    private void navigateToSettingsActivity() {
        startActivity(new Intent(this, SettingActivity.class));
    }

    private void navigateToLearningActivity() {
        startActivity(new Intent(this, LearningActivity.class));
    }

    private void initializeOriginalMeanings() {
        all_gestures = gestureInfoHelper.getGestures();
        originalMeanings = new HashMap<>();
        for (Gesture gesture : all_gestures) {
            String meaning = gesture.getTranslation();
            originalMeanings.put(gesture.getLabel(), meaning);
            String key = collectionName + KEY_ORIGINAL_PREFIX + gesture.getLabel();
            if (!gesturePrefs.contains(key) || !gesturePrefs.getString(key, "").equals(meaning)) {
                gesturePrefs.edit().putString(key, meaning).apply();
            }
        }
    }

    private void populateGestureGrid() {
        GridLayout gridLayout = findViewById(R.id.gesture_grid);
        gridLayout.removeAllViews();
        float density = getResources().getDisplayMetrics().density;
        int padding = (int) (8 * density);

        for (Gesture gesture : all_gestures) {
            CardView cardView = new CardView(this);
            GridLayout.LayoutParams cardParams = new GridLayout.LayoutParams();
            cardParams.width = 0;
            cardParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            cardParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            cardView.setLayoutParams(cardParams);
            cardView.setRadius(8 * density);
            cardView.setCardElevation(2 * density);
            cardView.setUseCompatPadding(true);
            cardView.setClickable(true);
            cardView.setFocusable(true);
            cardView.setCardBackgroundColor(Color.parseColor("#FFFFFF"));

            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setGravity(Gravity.CENTER);
            linearLayout.setPadding(padding, padding, padding, padding);
            LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, (int) (140 * density));
            linearLayout.setLayoutParams(llParams);

            ImageView imageView = new ImageView(this);
            int imageSize = (int) (80 * density);
            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(imageSize, imageSize);
            imageView.setLayoutParams(imageParams);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            try {
                InputStream ims = assetManager.open(gesture.getImagePath());
                Drawable drawable = Drawable.createFromStream(ims, null);
                imageView.setImageDrawable(drawable);
                ims.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            TextView textView = new TextView(this);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            textView.setLayoutParams(textParams);
            textView.setGravity(Gravity.CENTER);
            textView.setMaxLines(2);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setTextSize(16);
            String customKey = collectionName + KEY_CUSTOM_PREFIX + gesture.getLabel();
            String originalGestureMeaning = gesture.getTranslation();
            String labelText = gesturePrefs.getString(customKey, originalGestureMeaning);
            textView.setText(labelText);

            linearLayout.addView(imageView);
            linearLayout.addView(textView);
            cardView.addView(linearLayout);
            cardView.setTag(gesture);
            cardView.setOnClickListener(v -> {
                Gesture clickedGesture = (Gesture) v.getTag();
                showEditDialog(clickedGesture);
            });

            gridLayout.addView(cardView);
        }
    }

    private void showEditDialog(Gesture gesture) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_gesture, null);
        ImageView dialogImage = dialogView.findViewById(R.id.dialog_gesture_image);
        EditText dialogInput = dialogView.findViewById(R.id.dialog_input);
        try {
            InputStream is = getAssets().open(gesture.getImagePath());
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            dialogImage.setImageBitmap(bitmap);
            is.close();

            String key = gesture.getLabel();
            String originalMeaning = gesturePrefs.getString(collectionName + KEY_ORIGINAL_PREFIX + key, gesture.getTranslation());
            String currentLabel = gesturePrefs.getString(collectionName + KEY_CUSTOM_PREFIX + key, originalMeaning);
            dialogInput.setHint("Original: " + originalMeaning);
            dialogInput.setText(currentLabel);
            dialogInput.setSelection(currentLabel.length());
        } catch (IOException e) {
            e.printStackTrace();
        }
        builder.setView(dialogView)
                .setTitle("Edit Gesture Meaning")
                .setPositiveButton("Save", (dialog, which) -> {
                    String newLabel = dialogInput.getText().toString().trim();
                    if (!newLabel.isEmpty()) {
                        String prefKey = collectionName + KEY_CUSTOM_PREFIX + gesture.getLabel();
                        gesturePrefs.edit().putString(prefKey, newLabel).apply();
                        populateGestureGrid();
                    }
                })
                .setNegativeButton("Cancel", null)
                .setNeutralButton("Reset", (dialog, which) -> resetGesture(gesture.getLabel()))
                .show();
    }

    private void resetGesture(String gestureLabel) {
        String originalMeaning = gesturePrefs.getString(collectionName + KEY_ORIGINAL_PREFIX + gestureLabel, gestureLabel);
        gesturePrefs.edit().remove(collectionName + KEY_CUSTOM_PREFIX + gestureLabel).apply();
        populateGestureGrid();
        Toast.makeText(this, "Gesture reset to default", Toast.LENGTH_SHORT).show();
    }

    private void setupMoreOptionsMenu() {
        buttonMoreOptions.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Gesture Options")
                    .setItems(new CharSequence[]{"Reset All to Default", "Reset Current Gesture"}, (dialog, which) -> {
                        if (which == 0) {
                            resetAllGestures();
                        }
                        // "Reset Current Gesture" is handled in the edit dialog.
                    })
                    .show();
        });
    }

    private void resetAllGestures() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset All Gestures")
                .setMessage("Are you sure you want to reset all gestures to their original meanings?")
                .setPositiveButton("Reset", (dialog, which) -> {
                    SharedPreferences.Editor editor = gesturePrefs.edit();
                    for (Gesture gesture : all_gestures) {
                        editor.remove(collectionName + KEY_CUSTOM_PREFIX + gesture.getLabel());
                    }
                    editor.apply();
                    populateGestureGrid();
                    Toast.makeText(this, "All gestures reset to default", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Opens a dialog that lists existing collections.
     * A single-tap will let the user select a collection.
     * A long-tap (on an item) will prompt deletion of that collection (except for "Default Collection").
     * Also includes an option to create a new collection.
     */
    private void showChangeCollectionDialog() {
        // Retrieve existing collection set.
        Set<String> collectionSet = gesturePrefs.getStringSet(KEY_COLLECTION_SET, null);
        if (collectionSet == null || collectionSet.isEmpty()) {
            collectionSet = new HashSet<>();
            collectionSet.add("Default Collection");
            gesturePrefs.edit().putStringSet(KEY_COLLECTION_SET, collectionSet).apply();
        }
        final String[] collections = collectionSet.toArray(new String[0]);
        int selectedIndex = 0;
        for (int i = 0; i < collections.length; i++) {
            if (collections[i].equals(collectionName)) {
                selectedIndex = i;
                break;
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Gesture Collection")
                .setSingleChoiceItems(collections, selectedIndex, null)
                // "Select" button.
                .setPositiveButton("Select", (dialog, which) -> {
                    AlertDialog alertDialog = (AlertDialog) dialog;
                    int selectedPosition = alertDialog.getListView().getCheckedItemPosition();
                    String chosenCollection = collections[selectedPosition];
                    collectionName = chosenCollection;
                    gesturePrefs.edit().putString("collectionName", collectionName).apply();
                    textViewCollectionName.setText(collectionName);
                    // Reinitialize original mappings and update grid.
                    initializeOriginalMeanings();
                    populateGestureGrid();
                    Toast.makeText(this, "Collection changed to " + collectionName, Toast.LENGTH_SHORT).show();
                })
                // "Create New" button.
                .setNeutralButton("Create New", (dialog, which) -> {
                    showCreateNewCollectionDialog();
                })
                .setNegativeButton("Cancel", null);

        AlertDialog changeDialog = builder.create();
        changeDialog.show();

        // Add long press listener to the ListView to enable deletion.
        ListView listView = changeDialog.getListView();
        Set<String> finalCollectionSet = collectionSet;
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            String selectedCollection = collections[position];
            // Prevent deletion of "Default Collection".
            if (selectedCollection.equals("Default Collection")) {
                Toast.makeText(this, "Default Collection cannot be deleted", Toast.LENGTH_SHORT).show();
                return true;
            }
            // Confirm deletion.
            new AlertDialog.Builder(this)
                    .setTitle("Delete Collection")
                    .setMessage("Are you sure you want to delete the collection: " + selectedCollection + "?")
                    .setPositiveButton("Delete", (d, w) -> {
                        // Remove from the set.
                        Set<String> newSet = new HashSet<>(finalCollectionSet);
                        newSet.remove(selectedCollection);
                        gesturePrefs.edit().putStringSet(KEY_COLLECTION_SET, newSet).apply();
                        // If the deleted collection is active, revert to Default Collection.
                        if (selectedCollection.equals(collectionName)) {
                            collectionName = "Default Collection";
                            gesturePrefs.edit().putString("collectionName", collectionName).apply();
                            textViewCollectionName.setText(collectionName);
                            // Reinitialize keys.
                            initializeOriginalMeanings();
                            populateGestureGrid();
                        }
                        Toast.makeText(this, "Collection " + selectedCollection + " deleted", Toast.LENGTH_SHORT).show();
                        // Dismiss the change dialog and reopen it to refresh the list.
                        changeDialog.dismiss();
                        showChangeCollectionDialog();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        });
    }

    private void showCreateNewCollectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create New Collection");
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_collection, null);
        EditText editTextName = dialogView.findViewById(R.id.editTextCollectionName);
        EditText editTextDescription = dialogView.findViewById(R.id.editTextCollectionDescription);
        builder.setView(dialogView);
        // Set positive button initially with null listener so we can override it.
        builder.setPositiveButton("Create", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(d -> {
            Button createButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            createButton.setOnClickListener(v -> {
                // Retrieve and trim input.
                String newCollectionName = editTextName.getText().toString().trim();
                String newCollectionDescription = editTextDescription.getText().toString().trim();

                // Process the collection name:
                // 1. Remove disallowed characters (allow only letters, numbers, and spaces).
                newCollectionName = newCollectionName.replaceAll("[^a-zA-Z0-9 ]", "");
                // 2. Replace any multiple spaces with a single space.
                newCollectionName = newCollectionName.replaceAll("\\s+", " ").trim();

                // Process the description (if needed, you can filter out unwanted characters).
                newCollectionDescription = newCollectionDescription.replaceAll("[^a-zA-Z0-9 ,\\.]", "");
                newCollectionDescription = newCollectionDescription.replaceAll("\\s+", " ").trim();

                // Check maximum length restrictions.
                if (newCollectionName.length() > 20) {
                    Toast.makeText(this, "Collection name max length is 20 characters", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (newCollectionDescription.length() > 100) {
                    Toast.makeText(this, "Collection description max length is 100 characters", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Convert collection name to title case.
                newCollectionName = toTitleCase(newCollectionName);

                // Ensure the collection name is not empty.
                if (newCollectionName.isEmpty()) {
                    Toast.makeText(this, "Collection name cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check for duplicates.
                Set<String> collectionSet = gesturePrefs.getStringSet(KEY_COLLECTION_SET, new HashSet<>());
                if (collectionSet != null && collectionSet.contains(newCollectionName)) {
                    Toast.makeText(this, "Collection already exists", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Add the new collection to the stored set.
                Set<String> newCollectionSet = new HashSet<>(collectionSet);
                newCollectionSet.add(newCollectionName);
                gesturePrefs.edit().putStringSet(KEY_COLLECTION_SET, newCollectionSet).apply();

                // Update active collection.
                collectionName = newCollectionName;
                collectionDescription = newCollectionDescription.isEmpty() ? "No description" : newCollectionDescription;
                gesturePrefs.edit().putString("collectionName", collectionName).apply();
                gesturePrefs.edit().putString("collectionDescription", collectionDescription).apply();
                textViewCollectionName.setText(collectionName);
                textViewCollectionDescription.setText(collectionDescription);

                // Reinitialize original mappings and update the grid.
                initializeOriginalMeanings();
                populateGestureGrid();
                Toast.makeText(this, "New collection " + collectionName + " created", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        });
        dialog.show();
    }

    /**
     * Converts a string to title case: first letter of each word uppercase, rest lowercase.
     * For example, "hello world" becomes "Hello World".
     */
    private String toTitleCase(String input) {
        String[] words = input.split(" ");
        StringBuilder titleCase = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                titleCase.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    titleCase.append(word.substring(1).toLowerCase());
                }
                titleCase.append(" ");
            }
        }
        return titleCase.toString().trim();
    }

}
