package com.tbit.tbitblesdk.services.scanner.decorator;

import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;

import com.tbit.tbitblesdk.services.scanner.ScannerCallback;

/**
 * Created by Salmon on 2017/3/8 0008.
 */

public class FilterNameCallback extends BaseCallback {
    private String deviceName;
    public FilterNameCallback(String deviceName, ScannerCallback callback) {
        super(callback);
        this.deviceName = deviceName;
    }

    @Override
    public void onScanStart() {
        callback.onScanStart();
    }

    @Override
    public void onDeviceFounded(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
        String name = bluetoothDevice.getName();
        if (TextUtils.equals(deviceName, name)) {
            callback.onDeviceFounded(bluetoothDevice, i, bytes);
        }
    }

    @Override
    public void onScanStop() {
        callback.onScanStop();
    }

    @Override
    public void onScanCanceled() {
        callback.onScanCanceled();
    }
}
