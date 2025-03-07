package com.example.handsign_translator_app;

import static androidx.fragment.app.FragmentManager.TAG;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
    private BluetoothAdapter bluetoothAdapter;
    private Handler handler = new Handler();

    private Runnable runnable;

    private List<BluetoothDevice> mBTDevices = new ArrayList<>(); //List to store discover devices
    private ArrayAdapter<String> mDeviceListAdapter; //Used to list device info in the listview
    private BluetoothSocket bluetoothSocket;




    // Buffer to track recent readings
    private static Deque<int[]> flexReadingsHistory;
    BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //gets the default bluetooth adapter

        //Checks if BT is supported on tbhe device
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show();
            finish(); // Exit the app if Bluetooth is not supported
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUp();
//        readGesture();
        popUpBT();
    }

    /**
     * SetUP the Main Activity
     */
    private void setUp() {
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
            } else if (item.getItemId() == R.id.gestures) {
                navigateToGesturesActivity();
                return true; // Event handled
            }
            return false; // Event not handled
        });
    }

    //The broastcast receiver is used to listen for changes in the BT adapater state
    private final BroadcastReceiver mBroastCastReceiver1= new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action= intent.getAction();
            if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){ //checks if the action is related to BT state change
                final int state= intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR); //gets new state

                switch  (state){
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

    //this one is used to get the new bt devices when scanning
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
                assert device != null;
                String deviceName = device.getName();
                String deviceAddress = device.getAddress();
                // If this device isn't already in our list, add it and update the adapter
                if (device != null && !mBTDevices.contains(device)) {
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
                    if(!mBluetoothAdapter.isEnabled()){
                        enableDisableBT();
                    }
                    else{
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
    private void enableDisableBT(){
        if(mBluetoothAdapter == null){
            Log.d(TAG, "enableDisableBT: does not have BT capacity");
        }
        // If Bluetooth is off, request to enable it
        if(!mBluetoothAdapter.isEnabled()){
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
        if(mBluetoothAdapter.isEnabled()){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
                return;
            }
            mBluetoothAdapter.disable();
            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroastCastReceiver1, BTIntent);
        }
    }

    private void showBluetoothDevicesDialog(){
        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_bluetooth_devices, null);
        builder.setView(view);

        ListView pairedDevices = view.findViewById(R.id.paired_devices);
        ListView newDevices = view.findViewById(R.id.new_devices);
        Button discover = view.findViewById(R.id.btn_discover);
        Button testBT= view.findViewById(R.id.btn_test);

        ArrayList<String> pairedDevicesNames = new ArrayList<>();
        ArrayList<BluetoothDevice> pairedDevicesList = new ArrayList<>();
        ArrayAdapter<String> pairedDevicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, pairedDevicesNames);

        pairedDevices.setAdapter(pairedDevicesAdapter);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
        }else{
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

        mBTDevices= new ArrayList<>();
        mDeviceListAdapter= new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,new ArrayList<>());
        newDevices.setAdapter(mDeviceListAdapter);

        discover.setOnClickListener(v -> {
            if(ActivityCompat.checkSelfPermission(this,Manifest.permission.BLUETOOTH_SCAN)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.BLUETOOTH_SCAN},1);
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

            connectToDevice(device);
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
            connectToDevice(device);
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

    private void connectToDevice(BluetoothDevice device){

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Toast.makeText(this, "Attempting to connect to " + device.getName(), Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

                // Store the socket in the class-level variable
                bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);
                bluetoothSocket.connect();
                runOnUiThread(() -> Toast.makeText(this, "Connected to " + device.getName(), Toast.LENGTH_SHORT).show());

                // Now that the socket is properly assigned, call this method
                readDataFromSocket();

            } catch (Exception e) {
                final String message = e.getMessage();
                runOnUiThread(() -> Toast.makeText(this, "Connection failed: " + message, Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    //reads data from the ESP32
    private void readDataFromSocket() {
        new Thread(() -> {
            try {
                if (bluetoothSocket == null) {
                    runOnUiThread(() -> Toast.makeText(this, "Error: Bluetooth socket not connected", Toast.LENGTH_SHORT).show());
                    return;
                }

                InputStream inputStream = bluetoothSocket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    final String data = line;
                    runOnUiThread(() -> processSensorData(data)); // Process data on UI thread
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error reading data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    //processes the data from the from the readDataFromSocket.
    private void processSensorData(String data){
        try {
            int sensorValue = Integer.parseInt(data); // Convert string to integer
            textTranslatedOutput.setText("Sensor Value: " + sensorValue); // Display on UI
        } catch (NumberFormatException e) {
            Log.e("SensorData", "Invalid data received: " + data);
            textTranslatedOutput.setText("Error: Invalid data");
        }

    }

    private void readGesture() {
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

                if (isStable) {
                    textTranslatedOutput.setText("STABLE...");
                } else {
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
        handler.removeCallbacks(runnable); // Stop handler when activity not visible
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(mBroastCastReceiver1);
        } catch (Exception e) {
            // Receiver might not be registered
        }

        try {
            unregisterReceiver(mBroastCastReceiver3);
        } catch (Exception e) {
            // Receiver might not be registered
        }
    }
}
