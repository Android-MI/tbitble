package com.tbit.tbitblesdk.bluetooth.scanner;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.tbit.tbitblesdk.bluetooth.BleGlob;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Salmon on 2017/3/3 0003.
 */

public class BelowAndroidLScanner implements Scanner, Handler.Callback {
    private static final int HANDLE_STOP = 0;
    private static final int HANDLE_TIMEOUT = 1;

    private ScannerCallback callback;
    private BluetoothAdapter bluetoothAdapter;
    private Handler handler;
    private AtomicBoolean needProcess = new AtomicBoolean(false);
    private long timeoutMillis;

    private BluetoothAdapter.LeScanCallback bleCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice bluetoothDevice, final int i, final byte[] bytes) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (callback != null)
                        callback.onDeviceFounded(bluetoothDevice, i, bytes);
                }
            });
        }
    };

    public BelowAndroidLScanner() {
        this.bluetoothAdapter = BleGlob.getBluetoothAdapter();
        this.handler = new Handler(Looper.getMainLooper(), this);
    }

    @Override
    public void start(final ScannerCallback callback, long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
        this.callback = callback;
        needProcess.set(true);
        if (callback != null)
            callback.onScanStart();
        handler.sendEmptyMessageDelayed(HANDLE_TIMEOUT, timeoutMillis);
        bluetoothAdapter.startLeScan(bleCallback);
    }

    @Override
    public void stop() {
        handler.sendEmptyMessage(HANDLE_STOP);
    }

    @Override
    public boolean isScanning() {
        return needProcess.get();
    }

    private void stopInternal() {
        if (!needProcess.get())
            return;
        needProcess.set(false);
        if (callback != null)
            callback.onScanCanceled();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            // TODO: 2017/3/27 0027 notify bluetoothAdapter not enabled
            return;
        }
        handler.removeCallbacksAndMessages(null);
        bluetoothAdapter.stopLeScan(bleCallback);
        callback = null;
    }

    private void timeUp() {
        if (!needProcess.get())
            return;
        needProcess.set(false);
        if (callback != null)
            callback.onScanStop();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            // TODO: 2017/3/27 0027 notify bluetoothAdapter not enabled
            return;
        }
        handler.removeCallbacksAndMessages(null);
        bluetoothAdapter.stopLeScan(bleCallback);
        callback = null;
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case HANDLE_STOP:
                stopInternal();
                break;
            case HANDLE_TIMEOUT:
                timeUp();
                break;
            default:
                break;
        }
        return true;
    }

}
