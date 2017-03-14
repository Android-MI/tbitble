package com.tbit.tbitblesdk;

import android.bluetooth.le.ScanCallback;
import android.content.Context;

import com.tbit.tbitblesdk.protocol.BikeState;
import com.tbit.tbitblesdk.services.scanner.Scanner;
import com.tbit.tbitblesdk.services.scanner.ScannerCallback;

import java.io.File;

/**
 * Created by Salmon on 2016/12/5 0005.
 */

public class TbitBle {

    private static TbitBleInstance instance;

    private TbitBle() {
    }

    public static void initialize(Context context) {
        if (instance == null) {
            instance = new TbitBleInstance(context.getApplicationContext());
        }
    }

    public static void setListener(TbitListener listener) {
        checkInstanceNotNull();
        instance.setListener(listener);
    }

    public static void unSetListener() {
        if (instance != null)
            instance.setListener(null);
    }

    public static void setDebugListener(TbitDebugListener listener) {
        checkInstanceNotNull();
        instance.setDebugListener(listener);
    }

    public static void connect(String macAddr, String key) {
        checkInstanceNotNull();
        instance.connect(macAddr, key);
    }

    public static int startScan(ScannerCallback callback, long timeout) {
        checkInstanceNotNull();
        return instance.startScan(callback, timeout);
    }

    public static void stopScan() {
        checkInstanceNotNull();
        instance.stopScan();
    }

    public static void commonCommand(byte commandId, byte key, Byte[] value) {
        checkInstanceNotNull();
        instance.common(commandId, key, value);
    }

    public static void unlock() {
        checkInstanceNotNull();
        instance.unlock();
    }

    public static void lock() {
        checkInstanceNotNull();
        instance.lock();
    }

    public static void update() {
        checkInstanceNotNull();
        instance.update();
    }

    public static void reconnect() {
        checkInstanceNotNull();
        instance.reConnect();
    }

    public static int getBleConnectionState() {
        checkInstanceNotNull();
        return instance.getBleConnectionState();
    }

    public static BikeState getState() {
        checkInstanceNotNull();
        return instance.getState();
    }

    public static void disConnect() {
        checkInstanceNotNull();
        instance.disConnect();
    }

    public static void ota(File file, OtaListener otaListener) {
        checkInstanceNotNull();
        instance.ota(file, otaListener);
    }

    public static void connectiveOta(String machineNo, String key, File file, OtaListener listener) {
        checkInstanceNotNull();
        instance.connectiveOta(machineNo, key, file, listener);
    }

    public static void destroy() {
        checkInstanceNotNull();
        instance.destroy();
        instance = null;
    }

    public static boolean hasInitialized() {
        return instance != null;
    }

    private static void checkInstanceNotNull() {
        if (instance == null)
            throw new RuntimeException("have you 'initialize' on TbitBle ? ");
    }
}
