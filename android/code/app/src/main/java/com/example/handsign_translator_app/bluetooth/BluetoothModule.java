package com.example.handsign_translator_app.bluetooth;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

public class BluetoothModule {

    private static final String TAG = "BluetoothModule";
    private BluetoothSocket bluetoothSocket = null;
    private Context context;
    // Latest data read from the Bluetooth socket (volatile for thread-safety)
    private volatile String latestData = "";
    private Thread dataThread;
    private boolean keepReading = false;

    public BluetoothModule(Context context) {
        this.context = context;
        bluetoothSocket = null;
    }

    public boolean isDeviceConnected(){
        return (bluetoothSocket != null);
    }

    /**
     * Connects to the given Bluetooth device on a background thread.
     * After connecting, starts the data reading thread.
     */
    public void connectToDevice(Activity activity, BluetoothDevice device) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // Permission check should be performed by the caller
            return;
        }
        Toast.makeText(context, "Attempting to connect to " + device.getName(), Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            try {
                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);
                bluetoothSocket.connect();
                activity.runOnUiThread(() -> Toast.makeText(context, "Connected to " + device.getName(), Toast.LENGTH_SHORT).show());
                // Start continuously reading data from the socket
                startDataReading();
            } catch (Exception e) {
                final String message = e.getMessage();
                activity.runOnUiThread(() -> Toast.makeText(context, "Connection failed: " + message, Toast.LENGTH_SHORT).show());
                Log.e(TAG, "Connection failed", e);
            }
        }).start();
    }

    /**
     * Starts a background thread that continuously reads data from the Bluetooth socket.
     */
    //added a Toast message that shows when the device haS been disconnected:
    private void startDataReading() {
        if (bluetoothSocket == null) return;
        keepReading = true;
        dataThread = new Thread(() -> {
            try {
                InputStream inputStream = bluetoothSocket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while (keepReading && (line = reader.readLine()) != null) {
                    // Update latestData every time new data is received.
                    latestData = line;
                    // Optionally, you could invoke a callback here to immediately forward raw data.
                }
            } catch (Exception e) {
                Log.e(TAG, "Error reading data", e);

                if (bluetoothSocket !=null){
                    try{
                        boolean isConnected = bluetoothSocket.isConnected();
                        if(!isConnected){
                            if(context instanceof Activity){
                                Activity activity= (Activity) context;
                                activity.runOnUiThread(()-> Toast.makeText(context,"Bluetooth device has been disconnected", Toast.LENGTH_SHORT).show());
                            }
                            Log.d(TAG, "Bluetooth device disconnected");
                        }
                    }catch (Exception ex){
                        Log.e(TAG, "ERROR checking connection status",ex);
                    }
                }
            } finally{
                keepReading=false;
            }
        });
        dataThread.start();
    }

    /**
     * Stops the background data reading thread.
     */
    public void stopDataReading() {
        keepReading = false;
        if (dataThread != null && dataThread.isAlive()) {
            dataThread.interrupt();
        }
    }

    /**
     * Returns the latest glove data as an array of integers.
     * If no data is available, returns simulated (mock) data.
     */
    // Returns the latest glove data as an array of doubles (for dynamic gestures)
    public double[] getGloveDataDouble() {
        double[] flexReadings;
        if (!latestData.isEmpty()) {
            try {
                flexReadings = processSensorData(latestData);

                // Ensure flexReadings has at least 9 elements before accessing index 8
                if (flexReadings.length > 8) {
                    double isDynamic = flexReadings[8];

                    // Return data only if dynamic
                    if (isDynamic == 1) {
                        return Arrays.copyOfRange(flexReadings, 0, 7);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing sensor data: " + e.getMessage());
                flexReadings = getMockReadings();
            }
        } else {
            flexReadings = getMockReadings();
        }
        return new double[]{0, 0, 0, 0, 0, 0, 0}; // Return default values if no valid data is found
    }

    // Returns the latest glove data as an array of integers (for static gestures)
    public int[] getGloveDataInt() {
        if (!latestData.isEmpty()) {
            try {
                double[] flexReadings = processSensorData(latestData);

                // Ensure flexReadings has at least 9 elements before accessing index 8
                if (flexReadings.length > 8) {
                    double isDynamic = flexReadings[8];

                    // Return data only if static
                    if (isDynamic != 1) {
                        int[] convertedData = processSensorDataInt(latestData);

                        // Ensure enough data before slicing
                        if (convertedData.length >= 4) {
                            return Arrays.copyOfRange(convertedData, 0, 4);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing sensor data: " + e.getMessage());
            }
        }
        return new int[]{0, 0, 0, 0}; // Return default values if no valid data is found
    }




    public String getRawData(){
        String data = latestData.toString();
        return data;
    }

    /**
     * Processes a comma-separated string of sensor data into an integer array.
     */
    private double[] processSensorData(String data) {
        String[] valueParts = data.split(",");
        double[] sensorValues = new double[valueParts.length];
        for (int i = 0; i < valueParts.length; i++) {
            sensorValues[i] =Double.parseDouble(valueParts[i].trim());
        }
        return sensorValues;
    }
    private int[] processSensorDataInt(String data) {
        String[] valueParts = data.split(",");
        int[] sensorValues = new int[valueParts.length];
        for (int i = 0; i < valueParts.length; i++) {
            sensorValues[i] =Integer.parseInt(valueParts[i].trim());
        }
        return sensorValues;
    }

    /**
     * Provides simulated sensor data.
     */
    private double[] getMockReadings() {

        return new double[]{180,0,180,180,180,0,0,0};
    }

    /**
     * Generates a random number between min and max (inclusive).
     */
    private static int randomNumber(int min, int max) {
        return new Random().nextInt(max - min + 1) + min;
    }
}