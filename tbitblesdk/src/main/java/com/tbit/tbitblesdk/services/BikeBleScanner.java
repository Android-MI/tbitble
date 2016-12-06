package com.tbit.tbitblesdk.services;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Salmon on 2016/12/6 0006.
 */

public class BikeBleScanner implements BluetoothAdapter.LeScanCallback {

    private Handler handler = new Handler(Looper.getMainLooper());
    private long timeoutMillis = 10000;
    private String macAddress;
    private ScannerCallback callback;
    private BluetoothAdapter bluetoothAdapter;
    private AtomicBoolean needProcessScan = new AtomicBoolean(true);

    public BikeBleScanner() {
    }

    public BikeBleScanner(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    public void setTimeoutMillis(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public void setBluetoothAdapter(BluetoothAdapter bluetoothAdapter) {
        this.bluetoothAdapter = bluetoothAdapter;
    }

    public void start(String macAddress, BluetoothAdapter bluetoothAdapter,
                      final ScannerCallback callback) {
        this.bluetoothAdapter = bluetoothAdapter;
        this.macAddress = macAddress;
        this.callback = callback;
        reset();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                needProcessScan.set(false);
                if (callback != null)
                    callback.onScanTimeout();
            }
        }, timeoutMillis);
        bluetoothAdapter.startLeScan(this);
    }

    private void stop() {
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                bluetoothAdapter.stopLeScan(BikeBleScanner.this);
            }
        });
    }

    public void stop(final BluetoothAdapter adapter) {
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                bluetoothAdapter = adapter;
                adapter.stopLeScan(BikeBleScanner.this);
            }
        });
    }

    @Override
    public void onLeScan(final BluetoothDevice bluetoothDevice, final int i, final byte[] bytes) {
        if (!needProcessScan.get())
            return;
        if (bluetoothDevice == null)
            return;

        if (TextUtils.equals(macAddress, bluetoothDevice.getAddress())) {
            needProcessScan.set(false);
            removeHandlerMsg();
            stop();
            if (callback != null) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onDeviceFounded(bluetoothDevice, i, bytes);
                    }
                });
            }

        }
    }

    public void reset() {
        removeHandlerMsg();
        handler.removeCallbacksAndMessages(null);
    }

    public void removeHandlerMsg() {
        handler.removeCallbacksAndMessages(null);
    }

    private void runOnMainThread(Runnable runnable) {
        if (isMainThread()) {
            runnable.run();
        } else {
            handler.post(runnable);
        }
    }

    private boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    public interface ScannerCallback {
        void onScanTimeout();

        void onDeviceFounded(BluetoothDevice bluetoothDevice, int i, byte[] bytes);
    }
}
