package com.tbit.tbitblesdk.Bike.services;

import android.bluetooth.BluetoothDevice;

import com.tbit.tbitblesdk.Bike.ResultCode;
import com.tbit.tbitblesdk.Bike.model.SearchResult;
import com.tbit.tbitblesdk.protocol.callback.ResultCallback;
import com.tbit.tbitblesdk.protocol.callback.SearchCallback;
import com.tbit.tbitblesdk.bluetooth.scanner.ScannerCallback;
import com.tbit.tbitblesdk.bluetooth.util.ByteUtil;

/**
 * Created by Salmon on 2017/3/9 0009.
 */

public class BikeCallback implements ScannerCallback {

    private String encryptedDeviceId;
    private ResultCallback resultCallback;
    private SearchCallback searchCallback;

    public BikeCallback(String encryptedDeviceId,
                        ResultCallback resultCallback, SearchCallback searchCallback) {
        this.encryptedDeviceId = encryptedDeviceId;
        this.resultCallback = resultCallback;
        this.searchCallback = searchCallback;
    }

    @Override
    public void onScanStart() {

    }

    @Override
    public void onScanStop() {
        resultCallback.onResult(ResultCode.DEVICE_NOT_FOUNDED);
    }

    @Override
    public void onScanCanceled() {

    }

    @Override
    public void onDeviceFounded(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
        String dataStr = ByteUtil.bytesToHexStringWithoutSpace(bytes);
        boolean isFound = encryptedDeviceId != null && dataStr.contains(encryptedDeviceId);
        if (isFound) {
//            scanner.stop();
//            publishVersion(bytes);
//            bluetoothIO.connect(bluetoothDevice, false);
            searchCallback.onDeviceFound(new SearchResult(bluetoothDevice, i, bytes));
        }
    }

}
