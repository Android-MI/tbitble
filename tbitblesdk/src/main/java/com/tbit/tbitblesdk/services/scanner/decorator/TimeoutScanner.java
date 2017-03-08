package com.tbit.tbitblesdk.services.scanner.decorator;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.tbit.tbitblesdk.services.scanner.Scanner;
import com.tbit.tbitblesdk.services.scanner.ScannerCallback;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Salmon on 2017/3/8 0008.
 */
@Deprecated
public class TimeoutScanner implements Scanner {
    private ScannerCallback callback;
    private Scanner scanner;
    private static final int HANDLE_TIMEOUT = 0;
    private AtomicBoolean needProcessScan = new AtomicBoolean(true);
    private TimeoutHandler handler;
    private long timeoutMillis = 10000;

    public TimeoutScanner(long timeoutMillis, Scanner scanner) {
        this.scanner = scanner;
        this.timeoutMillis = timeoutMillis;
        handler = new TimeoutHandler(this);
    }

    @Override
    public void start(final ScannerCallback callback) {
        this.callback = callback;
        handler.removeCallbacksAndMessages(null);
        needProcessScan.set(true);
        handler.sendEmptyMessageDelayed(HANDLE_TIMEOUT, timeoutMillis);
        scanner.start(new ScannerCallback() {
            @Override
            public void onScanStart() {

            }

            @Override
            public void onDeviceFounded(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
                if (needProcessScan.get())
                    callback.onDeviceFounded(bluetoothDevice, i, bytes);
            }

            @Override
            public void onScanCanceled() {

            }

            @Override
            public void onScanStop() {
                needProcessScan.set(false);
                callback.onScanStop();
            }

        });
    }

    @Override
    public void stop() {
        needProcessScan.set(false);
        handler.removeCallbacksAndMessages(null);
        scanner.stop();
    }

    @Override
    public void setTimeout(long timeout) {
        scanner.setTimeout(timeout);
    }

    @Override
    public boolean isScanning() {
        return scanner.isScanning();
    }


    private static class TimeoutHandler extends Handler {
        WeakReference<TimeoutScanner> scannerReference;

        public TimeoutHandler(TimeoutScanner timeoutScanner) {
            super(Looper.getMainLooper());
            this.scannerReference = new WeakReference<>(timeoutScanner);
        }

        @Override
        public void handleMessage(Message msg) {
            final TimeoutScanner scanner = scannerReference.get();
            if (scanner == null)
                return;
            switch (msg.what) {
                case HANDLE_TIMEOUT:
                    if (scanner.needProcessScan.get() && scanner.callback != null) {
                        scanner.callback.onScanStop();
                    }
                    scanner.needProcessScan.set(false);
                    break;
                default:
                    break;
            }
        }
    }
}
