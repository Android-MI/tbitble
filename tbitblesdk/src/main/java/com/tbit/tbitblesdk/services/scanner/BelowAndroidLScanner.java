package com.tbit.tbitblesdk.services.scanner;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.tbit.tbitblesdk.protocol.BluEvent;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;

/**
 * Created by Salmon on 2017/3/3 0003.
 */

public class BelowAndroidLScanner implements Scanner {
    private static final int HANDLE_STOP = 0;
    private ScannerCallback callback;
    private BluetoothAdapter bluetoothAdapter;
    private ScanHandler handler;
    private BluetoothAdapter.LeScanCallback bleCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice bluetoothDevice, final int i, final byte[] bytes) {

            if (callback != null)
                callback.onDeviceFounded(bluetoothDevice, i, bytes);
        }
    };

    public BelowAndroidLScanner(BluetoothAdapter bluetoothAdapter) {
        this.bluetoothAdapter = bluetoothAdapter;
        handler = new ScanHandler(this);
    }

    @Override
    public void start(final ScannerCallback callback) {
        this.callback = callback;
        bluetoothAdapter.startLeScan(bleCallback);
    }

    @Override
    public void stop() {
        handler.sendEmptyMessage(HANDLE_STOP);
    }

    private void stopInternal() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            EventBus.getDefault().post(new BluEvent.BleNotOpened());
            return;
        }
        bluetoothAdapter.stopLeScan(bleCallback);
        callback.onScanStop();
    }

    static class ScanHandler extends Handler {
        WeakReference<BelowAndroidLScanner> scannerReference;

        public ScanHandler(BelowAndroidLScanner belowAndroidLScanner) {
            super(Looper.getMainLooper());
            this.scannerReference = new WeakReference<>(belowAndroidLScanner);
        }

        @Override
        public void handleMessage(Message msg) {
            BelowAndroidLScanner scanner = scannerReference.get();
            if (scanner == null)
                return;
            switch (msg.what) {
                case HANDLE_STOP:
                    scanner.stopInternal();
                    break;
                default:
                    break;
            }
        }
    }
}
