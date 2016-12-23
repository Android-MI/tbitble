package com.tbit.tbitblesdk.services;

import android.bluetooth.BluetoothAdapter;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Created by Salmon on 2016/12/23 0023.
 */

public abstract class Scanner {
    protected Handler handler = new Handler(Looper.getMainLooper());
    protected long timeoutMillis = 10000;
    protected String encryptedTid;
    protected ScannerCallback callback;
    protected BluetoothAdapter bluetoothAdapter;
    protected AtomicBoolean needProcessScan = new AtomicBoolean(true);

    abstract void start(String macAddress, ScannerCallback callback);

    abstract void stop();

    public Scanner(BluetoothAdapter bluetoothAdapter) {
        this.bluetoothAdapter = bluetoothAdapter;
    }

    public void setTimeoutMillis(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    public void setMacAddress(String macAddress) {
        this.encryptedTid = encryptStr(macAddress);
    }

    public void setBluetoothAdapter(BluetoothAdapter bluetoothAdapter) {
        this.bluetoothAdapter = bluetoothAdapter;
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

    private static final int MAX_ENCRYPT_COUNT = 95;

    private static char[] szKey = {
            0x35,0x41,0x32,0x42,0x33,0x43,0x36,0x44,0x39,0x45,
            0x38,0x46,0x37,0x34,0x31,0x30};

    protected String encryptStr(String in_str) {
        int count = 0;

        StringBuilder builder = new StringBuilder();
        if (in_str == null || in_str.length() == 0) {
            return null;
        }

        count = in_str.length();
        if (count > MAX_ENCRYPT_COUNT) {
            return null;
        }

        for (int i = 0; i < count; i++) {
            builder.append(szKey[in_str.charAt(i) - 0x2A]);
        }
        return builder.toString();
    }
}
