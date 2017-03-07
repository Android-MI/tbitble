package com.tbit.tbitblesdk.services.scanner;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Build;

import com.tbit.tbitblesdk.protocol.BluEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Salmon on 2017/3/3 0003.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class AndroidLScanner implements Scanner {
    private ScannerCallback callback;
    private BluetoothAdapter bluetoothAdapter;

    public AndroidLScanner(BluetoothAdapter bluetoothAdapter) {
        this.bluetoothAdapter = bluetoothAdapter;
    }

    private ScanCallback bleCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            BluetoothDevice device = result.getDevice();
            Integer rssi = result.getRssi();
            byte[] bytes = result.getScanRecord().getBytes();
            if (callback != null)
                callback.onDeviceFounded(device, rssi, bytes);
        }

    };

    @Override
    public void start(final ScannerCallback callback) {
        this.callback = callback;
        bluetoothAdapter.getBluetoothLeScanner().startScan(getFilters(), getSettings(), bleCallback);
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
    public void stop() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            EventBus.getDefault().post(new BluEvent.BleNotOpened());
            return;
        }
        BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        if (bluetoothLeScanner != null)
            bluetoothLeScanner.stopScan(bleCallback);
        callback.onScanStop();
    }
}
