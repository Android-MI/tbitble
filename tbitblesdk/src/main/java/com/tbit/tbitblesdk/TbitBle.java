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

    public static void connect(String macAddr, BikeCallback callback) {
        checkInstanceNotNull();
        instance.connect(macAddr, callback);
    }

    public static void verify(String key, BikeCallback callback) {
        checkInstanceNotNull();
        instance.verify(key, callback);
    }

    public static void disConnect(BikeCallback callback) {
        checkInstanceNotNull();
        instance.disConnect(callback);
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
