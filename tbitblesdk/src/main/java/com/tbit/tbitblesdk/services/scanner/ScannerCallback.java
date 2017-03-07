package com.tbit.tbitblesdk.services.scanner;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Salmon on 2016/12/23 0023.
 */

public interface ScannerCallback {
    void onScanStop();

    void onDeviceFounded(BluetoothDevice bluetoothDevice, int i, byte[] bytes);
}