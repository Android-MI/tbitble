package com.tbit.tbitblesdk.services.scanner;

import android.bluetooth.BluetoothAdapter;
import android.os.Build;

import com.tbit.tbitblesdk.services.ScannerCallback;

/**
 * Created by Salmon on 2017/3/3 0003.
 */

public abstract class Scanner {

    protected BluetoothAdapter bluetoothAdapter;

    public Scanner(BluetoothAdapter bluetoothAdapter) {
        this.bluetoothAdapter = bluetoothAdapter;
    }

    public void setBluetoothAdapter(BluetoothAdapter bluetoothAdapter) {
        this.bluetoothAdapter = bluetoothAdapter;
    }

    abstract void start(ScannerCallback callback);

    abstract void stop();

    public static BaseScanner getInstance(BluetoothAdapter bluetoothAdapter) {
        BaseScanner scanner;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            scanner = new AndroidLScanner(bluetoothAdapter);
        } else {
            scanner = new BelowAndroidLScanner(bluetoothAdapter);
        }
        return scanner;
    }
}
