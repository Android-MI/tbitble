package com.tbit.tbitblesdk.services.scanner.decorator;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.tbit.tbitblesdk.protocol.BluEvent;
import com.tbit.tbitblesdk.services.scanner.ScannerCallback;

import org.greenrobot.eventbus.EventBus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Salmon on 2017/3/8 0008.
 */

public class LogCallback extends BaseCallback {
    private Map<String, Integer> results = new ConcurrentHashMap<>();
    private EventBus bus = EventBus.getDefault();
    private StringBuilder sb;

    public LogCallback(ScannerCallback callback) {
        super(callback);
    }

    @Override
    public void onScanStart() {
        results.clear();
        printLogStart();
        callback.onScanStart();
    }

    @Override
    public void onScanStop() {
        printLogScannedLog();
        printLogTimeout();
        callback.onScanStop();
    }

    @Override
    public void onScanCanceled() {
        printLogScannedLog();
        printLogCanceled();
        callback.onScanCanceled();
    }

    @Override
    public void onDeviceFounded(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
        results.put(bluetoothDevice.getAddress(), i);
        callback.onDeviceFounded(bluetoothDevice, i, bytes);
    }

    private void printLogScannedLog() {
        sb = new StringBuilder();
        sb.append("#####################################\n");
        for (Map.Entry<String, Integer> entry : results.entrySet()) {
            sb.append("mac: " + entry.getKey() + " rssi : " + entry.getValue())
                    .append("\n");
        }
        sb.append("#####################################");
        Log.d("DebugScanner", sb.toString());
        bus.post(new BluEvent.DebugLogEvent("Scan Record", sb.toString()));
    }

    private void printLogStart() {
        bus.post(new BluEvent.DebugLogEvent("Scan Started", "Scan Started : "));
    }

    private void printLogTimeout() {
        bus.post(new BluEvent.DebugLogEvent("Scan Timeout", "Scan Timeout : "));
    }

    protected void printLogCanceled() {
        bus.post(new BluEvent.DebugLogEvent("Scan Canceled", "Scan Canceled"));
    }
}
