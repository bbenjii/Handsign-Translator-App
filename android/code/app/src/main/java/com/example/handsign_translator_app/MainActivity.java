package com.example.handsign_translator_app;

// Import necessary packages and classes

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;

import java.io.IOException;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.handsign_translator_app.bluetooth.BluetoothModule;
import com.example.handsign_translator_app.controllers.GestureController;
import com.example.handsign_translator_app.ml_module.GestureClassifier;
import com.example.handsign_translator_app.ml_module.GestureStabilityChecker;
import com.example.handsign_translator_app.models.Gesture;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener, GestureController.GestureListener {

    // UI element declarations
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

    // Text-to-Speech engine and state flag for loading animation
    private TextToSpeech tts;
    private boolean loading = false;

    // Controller, classifier, and bluetooth module instances
    private GestureController gestureController;
    private GestureClassifier gestureClassifier;
    private BluetoothModule bluetoothModule;
    BluetoothAdapter mBluetoothAdapter;

    private List<BluetoothDevice> mBTDevices = new ArrayList<>(); //List to store discover devices
    private ArrayAdapter<String> mDeviceListAdapter; //Used to list device info in the listview


    // Used to track the last translation spoken to prevent repeated TTS
    private String lastSpokenTranslation = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); //ensures that no dark mode i think? check after:
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setUp();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start gesture detection when activity resumes
        gestureController.start();

    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop gesture detection when activity is paused
        gestureController.stop();
    }

    /**
     * Initializes UI elements and sets up components like TTS, Bluetooth, and GestureController.
     */
    private void setUp() {
        // Link UI components from layout
        titleTranslate = findViewById(R.id.title_translate);
        labelHandSign = findViewById(R.id.label_hand_sign);
        labelLanguage = findViewById(R.id.label_language);
        textTranslatedOutput = findViewById(R.id.text_translated_output);
        imageHandSign = findViewById(R.id.image_hand_sign);
        buttonHistory = findViewById(R.id.button_history);
        buttonMoreOptions = findViewById(R.id.button_more_options);
        popUpBT();

        buttonSpeaker = findViewById(R.id.button_speaker);

        // Initially disable TTS button until TTS is properly initialized
        buttonSpeaker.setEnabled(false);
        // Set up a click listener to speak the current translation when the speaker button is pressed
        buttonSpeaker.setOnClickListener(v -> {
            String text = textTranslatedOutput.getText().toString();
            speak(text);
        });

        // Initialize TextToSpeech engine
        tts = new TextToSpeech(this, this);

        // Instantiate Bluetooth module, asset manager, gesture classifier, and gesture controller
        bluetoothModule = new BluetoothModule(getApplicationContext());
        assetManager = getApplicationContext().getAssets();
        gestureClassifier = new GestureClassifier(assetManager);
        // Pass this activity as the GestureListener so that callbacks can update the UI
        gestureController = new GestureController(bluetoothModule, gestureClassifier, this);

        // Set up bottom navigation view for activity navigation
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.translation);
        bottomNavigationView.setItemActiveIndicatorColor(ContextCompat.getColorStateList(this, R.color.blue_gray_100));
        bottomNavigationView.setOnItemSelectedListener(item -> {
            // Navigate to Settings if selected
            if (item.getItemId() == R.id.settings) {
                navigateToSettingsActivity();
                return true;
                // Navigate to Gestures Activity if selected
            } else if (item.getItemId() == R.id.gestures) {
                navigateToGesturesActivity();
                return true;
            }
            return false;
        });
    }

    //The broastcast receiver is used to listen for changes in the BT adapater state
    private final BroadcastReceiver mBroastCastReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) { //checks if the action is related to BT state change
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR); //gets new state

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Toast.makeText(MainActivity.this, "Bluetooth turned off", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Toast.makeText(MainActivity.this, "Bluetooth turning off", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Toast.makeText(MainActivity.this, "Bluetooth turned on", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Toast.makeText(MainActivity.this, "Bluetooth turning on", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    };

    //this one is used to get the new bt devices when scanning, Fixed issue where it would show null devices
    //null devices mean that they are not available for scanning/BT so we are skipping them instead of s
    private final BroadcastReceiver mBroastCastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //checks if the BT device was found from the broadcast receiver
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //gets the discovered bt
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //checks permissions
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                //retrieves device name and address

                String deviceName = device.getName();

                if(deviceName == null || deviceName.isEmpty()){
                    return;
                }

                String deviceAddress = device.getAddress();
                // If this device isn't already in our list, add it and update the adapter
                if (!mBTDevices.contains(device)) {
                    mBTDevices.add(device);
                    mDeviceListAdapter.add(deviceName + "\n" + deviceAddress);
                    mDeviceListAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    // Sets up the Bluetooth popup menu
    private void popUpBT() {
        buttonMoreOptions.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(MainActivity.this, v);
            popupMenu.getMenuInflater().inflate(R.menu.main_activity_3dots, popupMenu.getMenu());

            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.bluetoothPopUp) {
                    if (!mBluetoothAdapter.isEnabled()) {
                        enableDisableBT();
                    } else {
                        showBluetoothDevicesDialog();
                    }
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });
    }


    // Method to enable or disable Bluetooth
    @SuppressLint("RestrictedApi")
    private void enableDisableBT() {
        if (mBluetoothAdapter == null) {
            Log.d("TAG", "enableDisableBT: does not have BT capacity");
        }
        // If Bluetooth is off, request to enable it
        if (!mBluetoothAdapter.isEnabled()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
                return;
            }
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);
            //broadcast receiver that listens for state changes
            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroastCastReceiver1, BTIntent);
        }

        // If Bluetooth is already enabled, disable it
        if (mBluetoothAdapter.isEnabled()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
                return;
            }
            mBluetoothAdapter.disable();
            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroastCastReceiver1, BTIntent);
        }
    }

    private void showBluetoothDevicesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_bluetooth_devices, null);
        builder.setView(view);

        ListView pairedDevices = view.findViewById(R.id.paired_devices);
        ListView newDevices = view.findViewById(R.id.new_devices);
        Button discover = view.findViewById(R.id.btn_discover);
        Button testBT = view.findViewById(R.id.btn_test);

        ArrayList<String> pairedDevicesNames = new ArrayList<>();
        ArrayList<BluetoothDevice> pairedDevicesList = new ArrayList<>();
        ArrayAdapter<String> pairedDevicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, pairedDevicesNames);

        pairedDevices.setAdapter(pairedDevicesAdapter);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
        } else {
            Set<BluetoothDevice> pairedDevices1 = mBluetoothAdapter.getBondedDevices();
            for (BluetoothDevice device : pairedDevices1) {
                pairedDevicesNames.add(device.getName() + "\n" + device.getAddress());
                pairedDevicesList.add(device);
            }

            if (pairedDevicesNames.isEmpty()) {
                pairedDevicesNames.add("No paired devices");
            }

            pairedDevicesAdapter.notifyDataSetChanged();
        }

        mBTDevices = new ArrayList<>();
        mDeviceListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        newDevices.setAdapter(mDeviceListAdapter);

        discover.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 1);
                return;
            }
            Toast.makeText(this, "Discovering devices...", Toast.LENGTH_SHORT).show();
            discover.setText("Scanning...");
            discover.setEnabled(false);

            // Clear previous list
            mDeviceListAdapter.clear();
            mBTDevices.clear();

            // Register for broadcasts when a device is found
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroastCastReceiver3, discoverDevicesIntent);

            // Check if device is already discovering
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }

            // Start discovery
            mBluetoothAdapter.startDiscovery();

            // Re-enable button after 12 seconds
            new Handler().postDelayed(() -> {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mBluetoothAdapter.cancelDiscovery();
                discover.setText("Discover");
                discover.setEnabled(true);
            }, 12000);
        });


        pairedDevices.setOnItemClickListener((parent, itemView, position, id) -> {
            if (pairedDevicesList.isEmpty() || pairedDevicesNames.get(0).equals("No paired devices")) {
                return;
            }

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
                return;
            }

            BluetoothDevice device = pairedDevicesList.get(position);
            Toast.makeText(this, "Connecting to " + device.getName(), Toast.LENGTH_SHORT).show();

            bluetoothModule.connectToDevice(this, device);
        });

        newDevices.setOnItemClickListener((parent, itemView, position, id) -> {
            if (mBTDevices.isEmpty()) {
                return;
            }

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
                return;
            }

            BluetoothDevice device = mBTDevices.get(position);
            Toast.makeText(this, "Connecting to " + device.getName(), Toast.LENGTH_SHORT).show();

            // Cancel discovery before connecting
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 1);
                return;
            }
            mBluetoothAdapter.cancelDiscovery();

            // Connect to the device
            bluetoothModule.connectToDevice(this, device);
        });

        final AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(dialogInterface -> {
            // Clean up when dialog is dismissed
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mBluetoothAdapter.cancelDiscovery();
            try {
                unregisterReceiver(mBroastCastReceiver3);
            } catch (Exception e) {
                // Receiver might not be registered
            }
        });

        dialog.show();
    }


    //processes the data from the from the readDataFromSocket.



    /**
     * Callback from GestureController when a new gesture is detected.
     * Updates UI and speaks new translation if it has changed.
     */
    @Override
    public void onGestureDetected(Gesture gesture) {
        runOnUiThread(() -> {
            String newTranslation = gesture.getTranslation();
            // Update text view with the new translation
            textTranslatedOutput.setText(newTranslation);
            // Update the image view with the gesture image
            setGestureImageView(gesture.getImagePath());
            // Clear any loading animation
            clearLoadingAnimation();
            // Speak new translation if it has changed from the last spoken translation
            if (!newTranslation.equals(lastSpokenTranslation)) {
                speak(newTranslation);
                lastSpokenTranslation = newTranslation;
            }
        });
    }

    @Override
    public void rawDataOutput(String data) {
        // Update UI with loading animation and message
        setGestureImageView("lebronjames.png");
        // Clear any loading animation
        clearLoadingAnimation();        textTranslatedOutput.setText(data);
    }

    /**
     * Callback from GestureController when gesture translation is in progress.
     * Updates UI to indicate translation is ongoing.
     */
    @Override
    public void onTranslationInProgress() {
        // Update UI with loading animation and message
        setLoadingAnimation();
        textTranslatedOutput.setText("Translating...");
    }

    @Override
    public void onNoDeviceConnected() {
        setLoadingAnimation();
        textTranslatedOutput.setText("No device connected...");
    }

    /**
     * Starts a loading animation on the image view to indicate processing.
     */
    private void setLoadingAnimation() {
        if (loading) return; // Prevent duplicate animations
        loading = true;
        RotateAnimation anim = new RotateAnimation(0.0f, 360.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.INFINITE);
        anim.setDuration(5000);
        imageHandSign.startAnimation(anim);
    }

    /**
     * Clears the loading animation.
     */
    private void clearLoadingAnimation() {
        if (!loading) return;
        loading = false;
        imageHandSign.clearAnimation();
    }

    /**
     * Loads and sets the gesture image based on the given asset path.
     */
    private void setGestureImageView(String path) {
        try {
            InputStream ims = assetManager.open(path);
            Drawable d = Drawable.createFromStream(ims, null);
            imageHandSign.setImageDrawable(d);
        } catch (IOException ex) {
            // Handle error (for example, show a default image or log the error)
        }
    }

    /**
     * Uses the TextToSpeech engine to speak the provided text.
     */
    private void speak(String text) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    /**
     * Called when the TextToSpeech engine is initialized.
     */
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            // Set TTS language to Canadian English
            int result = tts.setLanguage(Locale.UK);
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                buttonSpeaker.setEnabled(true);
            }
        }
    }

    /**
     * Navigates to the Settings Activity.
     */
    private void navigateToSettingsActivity() {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }

    /**
     * Navigates to the Gestures Activity.
     */
    private void navigateToGesturesActivity() {
        Intent intent = new Intent(this, GesturesActivity.class);
        startActivity(intent);
    }

    /**
     * Ensures proper shutdown of TTS and closing of resources.
     */
    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.shutdown();
        }
        gestureClassifier.close();
        super.onDestroy();
    }
}