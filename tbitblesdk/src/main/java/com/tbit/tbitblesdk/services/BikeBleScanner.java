package com.tbit.tbitblesdk.services;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

/**
 * Created by Salmon on 2016/12/6 0006.
 */

public class BikeBleScanner extends Scanner {

    private static final String TAG = "BikeBleScanner";

    private BluetoothAdapter.LeScanCallback bleCallback = new BluetoothAdapter.LeScanCallback() {
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
    };

    public BikeBleScanner(BluetoothAdapter bluetoothAdapter) {
        super(bluetoothAdapter);
    }

    @Override
    public void start(String macAddress, final ScannerCallback callback) {
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
        bluetoothAdapter.startLeScan(bleCallback);
    }

    @Override
    public void stop() {
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                bluetoothAdapter.stopLeScan(bleCallback);
            }
        });
    }

}
