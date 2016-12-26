package com.tbit.tbitblesdk;

import android.content.Context;

import com.tbit.tbitblesdk.protocol.BikeState;

/**
 * Created by Salmon on 2016/12/5 0005.
 */

public class TbitBle {

    private static TbitBleInstance instance;

    private TbitBle() {
    }

    public static void initialize(Context context) {
        if (instance == null) {
            instance = new TbitBleInstance(context);
        }
    }

    public static void setListener(TbitListener listener) {
        checkInstanceNotNull();
        instance.setListener(listener);
    }

    public static void connect(String macAddr, String key) {
        checkInstanceNotNull();
        instance.connect(macAddr, key);
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

    public static void destroy() {
        checkInstanceNotNull();
        instance.destroy();
        instance = null;
    }

    public static void reset() {
        checkInstanceNotNull();
        instance.reset();
    }

    public static boolean hasInitialized() {
        return instance != null;
    }

    private static void checkInstanceNotNull() {
        if (instance == null)
            throw new RuntimeException("have you 'initialize' on TbitBle ? ");
    }
}
