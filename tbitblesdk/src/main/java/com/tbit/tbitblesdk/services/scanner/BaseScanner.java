package com.tbit.tbitblesdk.services.scanner;

import android.bluetooth.BluetoothAdapter;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.tbit.tbitblesdk.protocol.BluEvent;
import com.tbit.tbitblesdk.services.ScannerCallback;

import org.greenrobot.eventbus.EventBus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Salmon on 2017/3/3 0003.
 */

public abstract class BaseScanner extends Scanner {
    private static final String TAG = "Scanner";
    protected Handler handler = new Handler(Looper.getMainLooper());
    protected long timeoutMillis = 10000;
    protected ScannerCallback callback;
    protected Map<String, Integer> results = new ConcurrentHashMap<>();
    protected AtomicBoolean needProcessScan = new AtomicBoolean(true);
    private StringBuilder sb;
    private EventBus bus = EventBus.getDefault();

    public BaseScanner(BluetoothAdapter bluetoothAdapter) {
        super(bluetoothAdapter);
    }

    public void setTimeoutMillis(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    public void reset() {
        removeHandlerMsg();
        needProcessScan.set(true);
    }

    public void removeHandlerMsg() {
        handler.removeCallbacksAndMessages(null);
    }

    protected void runOnMainThread(Runnable runnable) {
        handler.post(runnable);
    }

    protected String bytesToHexString(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

    protected void printLogScannedLog() {
        sb = new StringBuilder();
        sb.append("#####################################\n");
        for (Map.Entry<String, Integer> entry : results.entrySet()) {
            sb.append("mac: " + entry.getKey() + " rssi : " + entry.getValue())
                    .append("\n");
        }
        sb.append("#####################################");
        Log.d(TAG, sb.toString());
        bus.post(new BluEvent.DebugLogEvent("Scan Record", sb.toString()));
    }

    protected void printLogStart() {
        bus.post(new BluEvent.DebugLogEvent("Scan Started", "Scan Started : "));
    }

    protected void printLogTimeout() {
        bus.post(new BluEvent.DebugLogEvent("Scan Timeout", "Scan Timeout : " + timeoutMillis));
    }

    protected void printLogStop() {
        bus.post(new BluEvent.DebugLogEvent("Scan Stop", "Scan Stop"));
    }

    protected void printLogFound() {
        bus.post(new BluEvent.DebugLogEvent("Scan Found", "Scan Found"));
    }
}
