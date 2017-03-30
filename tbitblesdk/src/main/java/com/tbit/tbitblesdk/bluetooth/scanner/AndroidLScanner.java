package com.tbit.tbitblesdk.bluetooth.scanner;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.tbit.tbitblesdk.bluetooth.BleGlob;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Salmon on 2017/3/3 0003.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class AndroidLScanner implements Scanner {
    private static final int HANDLE_TIMEOUT = 0;

    private ScannerCallback callback;
    private BluetoothAdapter bluetoothAdapter;
    private AndroidLScannerHandler handler;
    private AtomicBoolean needProcess = new AtomicBoolean(false);
    private long timeoutMillis;
    private ScanCallback bleCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            if (!needProcess.get())
                return;
            BluetoothDevice device = result.getDevice();
            Integer rssi = result.getRssi();
            byte[] bytes = result.getScanRecord().getBytes();
            if (callback != null)
                callback.onDeviceFounded(device, rssi, bytes);
        }

    };

    public AndroidLScanner() {
        this(Long.MAX_VALUE);
    }

    public AndroidLScanner(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
        this.bluetoothAdapter = BleGlob.getBluetoothAdapter();
        handler = new AndroidLScannerHandler(this);
    }

    @Override
    public void start(final ScannerCallback callback) {
        this.callback = callback;
        needProcess.set(true);
        if (callback != null)
            callback.onScanStart();
        handler.sendEmptyMessageDelayed(HANDLE_TIMEOUT, timeoutMillis);
        bluetoothAdapter.getBluetoothLeScanner().startScan(getFilters(), getSettings(), bleCallback);
    }

    @Override
    public void setTimeout(long timeout) {
        this.timeoutMillis = timeout;
    }

    private List<ScanFilter> getFilters() {
        List<ScanFilter> filters = new ArrayList<>();
        ScanFilter.Builder builder = new ScanFilter.Builder();
        filters.add(builder.build());
        return filters;
    }

    private ScanSettings getSettings() {
        ScanSettings.Builder settingBuilder = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        return settingBuilder.build();
    }

    @Override
    public boolean isScanning() {
        return needProcess.get();
    }

    @Override
    public void stop() {
        if (!needProcess.get())
            return;
        needProcess.set(false);
        if (callback != null)
            callback.onScanCanceled();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            // TODO: 2017/3/27 0027 notify bluetoothAdapter not enabled
            return;
        }
        handler.removeCallbacksAndMessages(null);
        BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        if (bluetoothLeScanner != null)
            bluetoothLeScanner.stopScan(bleCallback);
    }

    private void timeUp() {
        if (!needProcess.get())
            return;
        needProcess.set(false);
        if (callback != null)
            callback.onScanStop();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            // TODO: 2017/3/27 0027 notify bluetoothAdapter not enabled
            return;
        }
        handler.removeCallbacksAndMessages(null);
        BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        if (bluetoothLeScanner != null)
            bluetoothLeScanner.stopScan(bleCallback);
    }

    private static class AndroidLScannerHandler extends Handler {
        WeakReference<AndroidLScanner> scannerReference;

        public AndroidLScannerHandler(AndroidLScanner androidLScanner) {
            super(Looper.getMainLooper());
            scannerReference = new WeakReference<>(androidLScanner);
        }

        @Override
        public void handleMessage(Message msg) {
            AndroidLScanner scanner = scannerReference.get();
            if (scanner == null)
                return;
            switch (msg.what) {
                case HANDLE_TIMEOUT:
                    scanner.timeUp();
                    break;
                default:
                    break;
            }
        }
    }
}
