package com.tbit.tbitblesdk.services.scanner;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import com.tbit.tbitblesdk.services.ScannerCallback;

/**
 * Created by Salmon on 2017/3/3 0003.
 */

public class BikeScanner {

    protected BluetoothAdapter bluetoothAdapter;
    protected Scanner scanner;

    public BikeScanner(BluetoothAdapter bluetoothAdapter) {
        this.bluetoothAdapter = bluetoothAdapter;
        this.scanner = Scanner.getInstance(bluetoothAdapter);
    }


    public void start(String macAddress, ScannerCallback callback) {
        scanner.start(new ScannerCallback() {
            @Override
            public void onScanTimeout() {

            }

            @Override
            public void onDeviceFounded(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {

            }
        });
    }


    public void stop() {
        scanner.stop();
    }
}
