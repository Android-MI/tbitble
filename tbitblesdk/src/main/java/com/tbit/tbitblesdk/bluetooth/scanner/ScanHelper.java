package com.tbit.tbitblesdk.bluetooth.scanner;

import android.bluetooth.BluetoothAdapter;
import android.os.Build;

/**
 * Created by Salmon on 2017/3/8 0008.
 */

public class ScanHelper implements Scanner {
    public static final int DEFAULT_SCAN_TIMEOUT = 10000;
    private Scanner scanner;

    public ScanHelper(Scanner scanner) {
        this.scanner = scanner;
    }

    public ScanHelper(long timeout) {
        this(getScanner(timeout));
    }

    public ScanHelper() {
        this(DEFAULT_SCAN_TIMEOUT);
    }

    public static Scanner getScanner(long timeout) {
        Scanner scanner;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            scanner = new AndroidLScanner(timeout);
        } else {
            scanner = new BelowAndroidLScanner(timeout);
        }
        return scanner;
    }

    public static Scanner getScanner() {
        return getScanner(Long.MAX_VALUE);
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
