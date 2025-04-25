package com.example.handsign_translator_app.bluetooth;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class BluetoothService extends Service {
    private final IBinder binder = new LocalBinder();
    private BluetoothModule bluetoothModule;

    public class LocalBinder extends Binder {
        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Instantiate the BluetoothModule using the application context.
        bluetoothModule = new BluetoothModule(getApplicationContext());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public BluetoothModule getBluetoothModule() {
        return bluetoothModule;
    }

    @Override
    public void onDestroy() {
        // Clean up resources, such as stopping data reading.
        if (bluetoothModule != null) {
            bluetoothModule.stopDataReading();
        }
        super.onDestroy();
    }
}