package com.tbit.tbitblesdk;

import android.content.Context;

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

    public static void connect(String macAddr) {
        checkInstanceNotNull();
        instance.connect(macAddr);
    }

    public static void verify(String key) {
        checkInstanceNotNull();
        instance.verify(key);
    }

    public static void unlock() {
        checkInstanceNotNull();
        instance.unlock();
    }

    public static void lock() {
        checkInstanceNotNull();
        instance.lock();
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

    private static void checkInstanceNotNull() {
        if (instance == null)
            throw new RuntimeException("have you 'initialize' on TbitBle ? ");
    }
}
