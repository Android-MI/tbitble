package com.tbit.tbitblesdk.services.scanner;

import android.bluetooth.BluetoothAdapter;
import android.os.Build;

/**
 * Created by Salmon on 2017/3/8 0008.
 */

public class ScanHelper implements Scanner {
    private Scanner scanner;
    public static final int DEFAULT_SCAN_TIMEOUT = 10000;

    public ScanHelper(Scanner scanner) {
        this.scanner = scanner;
    }

    public ScanHelper(BluetoothAdapter bluetoothAdapter, long timeout) {
        this(getScanner(bluetoothAdapter, timeout));
    }

    public ScanHelper(BluetoothAdapter bluetoothAdapter) {
        this(bluetoothAdapter, DEFAULT_SCAN_TIMEOUT);
    }

    public static Scanner getScanner(BluetoothAdapter bluetoothAdapter, long timeout) {
        Scanner scanner;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            scanner = new AndroidLScanner(timeout, bluetoothAdapter);
        } else {
            scanner = new BelowAndroidLScanner(timeout, bluetoothAdapter);
        }
        return scanner;
    }

    public static Scanner getScanner(BluetoothAdapter bluetoothAdapter) {
        return getScanner(bluetoothAdapter, Long.MAX_VALUE);
    }

    @Override
    public void start(ScannerCallback callback) {
        scanner.start(callback);
    }

    public void start(ScannerCallback callback, long timeout) {
        scanner.setTimeout(timeout);
        scanner.start(callback);
    }

    @Override
    public void stop() {
        scanner.stop();
    }

    @Override
    public void setTimeout(long timeout) {
        scanner.setTimeout(timeout);
    }

    @Override
    public boolean isScanning() {
        return scanner.isScanning();
    }
}
