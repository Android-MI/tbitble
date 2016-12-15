package com.tbit.tbitblesdk.services;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Salmon on 2016/12/6 0006.
 */

public class BikeBleScanner implements BluetoothAdapter.LeScanCallback {
    private static final String TAG = "BikeBleScanner";
    private Handler handler = new Handler(Looper.getMainLooper());
    private long timeoutMillis = 10000;
    private String encryptedTid;
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
        this.encryptedTid = encryptStr(macAddress);
    }

    public void setBluetoothAdapter(BluetoothAdapter bluetoothAdapter) {
        this.bluetoothAdapter = bluetoothAdapter;
    }

    public void start(String macAddress, BluetoothAdapter bluetoothAdapter,
                      final ScannerCallback callback) {
        this.bluetoothAdapter = bluetoothAdapter;
        setMacAddress(macAddress);
        this.callback = callback;
        reset();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (needProcessScan.get() && callback != null)
                    callback.onScanTimeout();
                needProcessScan.set(false);
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

        String dataStr = bytesToHexString(bytes);
        Log.d(TAG, "onLeScan: " + bluetoothDevice.getName() + "\n" + dataStr + "\nmac： " + bluetoothDevice.getAddress() +
            "\nrssi: " + i);

        if (dataStr.contains(encryptedTid)) {
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
        needProcessScan.set(true);
    }

    public void removeHandlerMsg() {
        handler.removeCallbacksAndMessages(null);
    }

    private void runOnMainThread(Runnable runnable) {
        handler.post(runnable);
    }

    public interface ScannerCallback {
        void onScanTimeout();

        void onDeviceFounded(BluetoothDevice bluetoothDevice, int i, byte[] bytes);
    }

    private String bytesToHexString(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

    private static final int MAX_ENCRYPT_COUNT = 95;

    public static char[] szKey = {
            0x35,0x41,0x32,0x42,0x33,0x43,0x36,0x44,0x39,0x45,
            0x38,0x46,0x37,0x34,0x31,0x30};

    public static String encryptStr(String in_str) {
        int count = 0;

        StringBuilder builder = new StringBuilder();
        if (in_str == null || in_str.length() == 0) {
            return null;
        }

        count = in_str.length();
        if (count > MAX_ENCRYPT_COUNT) {
            return null;
        }

        for (int i = 0; i < count; i++) {
            builder.append(szKey[in_str.charAt(i) - 0x2A]);
        }
        return builder.toString();
    }
}
