package com.tbit.tbitblesdk.services.scanner;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.tbit.tbitblesdk.BleGlob;
import com.tbit.tbitblesdk.protocol.BluEvent;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Salmon on 2017/3/3 0003.
 */

public class BelowAndroidLScanner implements Scanner {
    private static final int HANDLE_STOP = 0;
    private static final int HANDLE_TIMEOUT = 1;

    private ScannerCallback callback;
    private BluetoothAdapter bluetoothAdapter;
    private ScanHandler handler;
    private AtomicBoolean needProcess = new AtomicBoolean(false);
    private long timeoutMillis;

    private BluetoothAdapter.LeScanCallback bleCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice bluetoothDevice, final int i, final byte[] bytes) {
            if (callback != null)
                callback.onDeviceFounded(bluetoothDevice, i, bytes);
        }
    };

    public BelowAndroidLScanner() {
        this(Long.MAX_VALUE);
    }

    public BelowAndroidLScanner(long timeoutMillis) {
        this.bluetoothAdapter = BleGlob.getBluetoothAdapter();
        this.timeoutMillis = timeoutMillis;
        this.handler = new ScanHandler(this);
    }

    @Override
    public void start(final ScannerCallback callback) {
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
    public void setTimeout(long timeout) {
        this.timeoutMillis = timeout;
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
            EventBus.getDefault().post(new BluEvent.BleNotOpened());
            return;
        }
        handler.removeCallbacksAndMessages(null);
        bluetoothAdapter.stopLeScan(bleCallback);
    }

    private void timeUp() {
        if (!needProcess.get())
            return;
        needProcess.set(false);
        if (callback != null)
            callback.onScanStop();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            EventBus.getDefault().post(new BluEvent.BleNotOpened());
            return;
        }
        handler.removeCallbacksAndMessages(null);
        bluetoothAdapter.stopLeScan(bleCallback);
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
                case HANDLE_TIMEOUT:
                    scanner.timeUp();
                    break;
                default:
                    break;
            }
        }
    }
}