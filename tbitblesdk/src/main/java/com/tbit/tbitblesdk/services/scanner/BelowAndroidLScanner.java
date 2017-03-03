package com.tbit.tbitblesdk.services.scanner;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import com.tbit.tbitblesdk.protocol.BluEvent;
import com.tbit.tbitblesdk.services.ScannerCallback;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Salmon on 2017/3/3 0003.
 */

public class BelowAndroidLScanner extends BaseScanner {

    public BelowAndroidLScanner(BluetoothAdapter bluetoothAdapter) {
        super(bluetoothAdapter);
    }

    private BluetoothAdapter.LeScanCallback bleCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice bluetoothDevice, final int i, final byte[] bytes) {
            if (!needProcessScan.get())
                return;
            if (callback != null)
                callback.onDeviceFounded(bluetoothDevice, i, bytes);
        }
    };

    @Override
    public void start(final ScannerCallback callback) {
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
        bluetoothAdapter.startLeScan(bleCallback);
    }

    @Override
    public void stop() {
        needProcessScan.set(false);
        printLogStop();
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
                    EventBus.getDefault().post(new BluEvent.BleNotOpened());
                    return;
                }
                bluetoothAdapter.stopLeScan(bleCallback);
            }
        });
    }
}
