package com.tbit.tbitblesdk.services;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Build;
import android.util.Log;

/**
 * Created by Salmon on 2016/12/6 0006.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class AndroidLBikeBleScanner extends Scanner {
    private static final String TAG = "AndroidLBikeBleScanner";
    private ScanCallback bleCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            if (!needProcessScan.get())
                return;

            final BluetoothDevice device = result.getDevice();
            String dataStr = bytesToHexString(result.getScanRecord().getBytes());
            Log.d(TAG, "onLeScan: " + device.getName() + "\n" + dataStr + "\nmac： " + device.getAddress() +
                    "\nrssi: " + result.getRssi());

            if (dataStr.contains(encryptedTid)) {
                needProcessScan.set(false);
                removeHandlerMsg();
                stop();
                if (callback != null) {
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onDeviceFounded(device, result.getRssi(), result.getScanRecord().getBytes());
                        }
                    });
                }
            }
        }
    };

    public AndroidLBikeBleScanner(BluetoothAdapter bluetoothAdapter) {
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
        bluetoothAdapter.getBluetoothLeScanner().startScan(bleCallback);
    }

    @Override
    public void stop() {
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                bluetoothAdapter.getBluetoothLeScanner().stopScan(bleCallback);
            }
        });
    }

}
