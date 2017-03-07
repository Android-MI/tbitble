package com.tbit.tbitblesdk.services.scanner;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.tbit.tbitblesdk.protocol.BluEvent;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Salmon on 2017/3/7 0007.
 */

public class ScanDecorator {

    public static Scanner getInstance(BluetoothAdapter bluetoothAdapter) {
        Scanner scanner;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            scanner = new AndroidLScanner(bluetoothAdapter);
        } else {
            scanner = new BelowAndroidLScanner(bluetoothAdapter);
        }
        return scanner;
    }

    public static abstract class BaseScanner implements Scanner {
        protected ScannerCallback callback;
        protected Scanner scanner;

        public BaseScanner(Scanner scanner) {
            this.scanner = scanner;
        }

        protected void setCallback(ScannerCallback callback) {
            this.callback = callback;
        }

        @Override
        public void start(ScannerCallback callback) {

        }

        @Override
        public void stop() {
            scanner.stop();
        }
    }

    public static class TimeoutScanner extends BaseScanner {
        private static final int HANDLE_TIMEOUT = 0;
        private AtomicBoolean needProcessScan = new AtomicBoolean(true);
        private TimeoutHandler handler;
        private long timeoutMillis = 10000;

        public TimeoutScanner(long timeoutMillis, Scanner scanner) {
            super(scanner);
            this.timeoutMillis = timeoutMillis;
            handler = new TimeoutHandler(this);
        }

        @Override
        public void start(final ScannerCallback callback) {
            setCallback(callback);
            handler.removeCallbacksAndMessages(null);
            needProcessScan.set(true);
            handler.sendEmptyMessageDelayed(HANDLE_TIMEOUT, timeoutMillis);
            scanner.start(new ScannerCallback() {
                @Override
                public void onScanStop() {
                    needProcessScan.set(false);
                    callback.onScanStop();
                }

                @Override
                public void onDeviceFounded(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
                    if (needProcessScan.get())
                        callback.onDeviceFounded(bluetoothDevice, i, bytes);
                }
            });
        }

        @Override
        public void stop() {
            needProcessScan.set(false);
            handler.removeCallbacksAndMessages(null);
            scanner.stop();
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

    public static class DebugScanner extends BaseScanner {
        private Map<String, Integer> results = new ConcurrentHashMap<>();
        private EventBus bus = EventBus.getDefault();
        private StringBuilder sb;

        public DebugScanner(Scanner scanner) {
            super(scanner);
        }

        @Override
        public void start(final ScannerCallback callback) {
            setCallback(callback);
            results.clear();
            printLogStart();
            scanner.start(new ScannerCallback() {
                @Override
                public void onScanStop() {
                    printLogScannedLog();
                    printLogTimeout();
                    callback.onScanStop();
                }

                @Override
                public void onDeviceFounded(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
                    results.put(bluetoothDevice.getAddress(), i);
                    callback.onDeviceFounded(bluetoothDevice, i, bytes);
                }
            });
        }

        @Override
        public void stop() {
            printLogStop();
            scanner.stop();
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

        protected void printLogStop() {
            bus.post(new BluEvent.DebugLogEvent("Scan Stop", "Scan Stop"));
        }
    }

    public static class NoneRepeatScanner extends BaseScanner {
        private List<String> addressList = Collections.synchronizedList(new ArrayList<String>());

        public NoneRepeatScanner(Scanner scanner) {
            super(scanner);
        }

        @Override
        public void start(final ScannerCallback callback) {
            setCallback(callback);
            scanner.start(new ScannerCallback() {
                @Override
                public void onScanStop() {
                    callback.onScanStop();
                }

                @Override
                public void onDeviceFounded(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
                    String address = bluetoothDevice.getAddress();
                    if (!addressList.contains(address)) {
                        addressList.add(address);
                        callback.onDeviceFounded(bluetoothDevice, i, bytes);
                    }
                }
            });
        }
    }
}
