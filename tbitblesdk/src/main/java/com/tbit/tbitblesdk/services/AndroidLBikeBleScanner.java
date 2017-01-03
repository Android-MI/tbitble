package com.tbit.tbitblesdk.services;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.tbit.tbitblesdk.protocol.BluEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Salmon on 2016/12/6 0006.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class AndroidLBikeBleScanner extends Scanner {
    private static final String TAG = "AndroidLBikeBleScanner";
    private static final String DEVICE_NAME = "[TBIT_WA-205]";
    private ScanCallback bleCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            if (!needProcessScan.get())
                return;

            String dataStr = bytesToHexString(result.getScanRecord().getBytes());

            String address = result.getDevice().getAddress();
            Integer rssi = result.getRssi();
            if (results.get(address) != null)
                rssi = (rssi + results.get(address)) / 2;
            results.put(address, rssi);

            if (dataStr.contains(encryptedTid)) {
                needProcessScan.set(false);
                removeHandlerMsg();
                stop();
                if (callback != null) {
                    printLogFound();
                    publishVersion(result.getScanRecord().getBytes());
                    printLogScannedLog();
                    printLogFound();
                    callback.onDeviceFounded(result.getDevice(), result.getRssi(),
                            result.getScanRecord().getBytes());
                }
            }
        }

    };

    public AndroidLBikeBleScanner(BluetoothAdapter bluetoothAdapter) {
        super(bluetoothAdapter);
    }

    @Override
    public void start(String tid, final ScannerCallback callback) {
        setMacAddress(tid);
        this.callback = callback;
        reset();
        results.clear();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (needProcessScan.get() && callback != null) {
                    printLogScannedLog();
                    printLogTimeout();
                    callback.onScanTimeout();
                }
                needProcessScan.set(false);
            }
        }, timeoutMillis);
        printLogStart();
        bluetoothAdapter.getBluetoothLeScanner().startScan(getFilters(), getSettings(), bleCallback);
//        bluetoothAdapter.getBluetoothLeScanner().startScan(bleCallback);
    }

    @Override
    public void stop() {
        printLogStop();
        if (bluetoothAdapter == null) {
            EventBus.getDefault().post(new BluEvent.BleNotOpened());
            return;
        }
        bluetoothAdapter.getBluetoothLeScanner().stopScan(bleCallback);
    }

    private List<ScanFilter> getFilters() {
        List<ScanFilter> filters = new ArrayList<>();
        ScanFilter.Builder builder = new ScanFilter.Builder()
                .setDeviceName(DEVICE_NAME);
//        String mac = resolveMAC();
//        if (!TextUtils.isEmpty(mac)) {
//            builder.setDeviceAddress(mac);
//            Log.d(TAG, "resolvedMac: " + mac);
//        }
        filters.add(builder.build());
        return filters;
    }

    private ScanSettings getSettings() {
        ScanSettings.Builder settingBuilder = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        return settingBuilder.build();
    }

    private String resolveMAC() {
        if (TextUtils.isEmpty(originTid))
            return "";
        if (originTid.length() != 9)
            return "";
        String temp = originTid + "FFF";
        int length = temp.length();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i+=2) {
            builder.insert(0, temp.substring(i, i+2));
            if (i != length-2)
                builder.insert(0, ":");
        }
        return builder.toString();
    }

}
