package com.tbit.tbitblesdk.services;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.tbit.tbitblesdk.protocol.BluEvent;
import com.tbit.tbitblesdk.protocol.ParsedAd;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

            String address = bluetoothDevice.getAddress();
            int rssi = i;
            if (results.get(address) != null)
                rssi = (rssi + results.get(address)) / 2;
            results.put(address, rssi);

            if (dataStr.contains(encryptedTid)) {
                needProcessScan.set(false);
                removeHandlerMsg();
                stop();
                if (callback != null) {
                    publishVersion(bytes);
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            printLogScannedLog();
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
        results.clear();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (needProcessScan.get() && callback != null) {
                    printLogScannedLog();
                    callback.onScanTimeout();
                }
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
                if (bluetoothAdapter == null) {
                    EventBus.getDefault().post(new BluEvent.BleNotOpened());
                    return;
                }
                bluetoothAdapter.stopLeScan(bleCallback);
            }
        });
    }

}
