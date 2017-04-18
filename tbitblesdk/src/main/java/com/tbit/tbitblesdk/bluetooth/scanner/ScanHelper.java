package com.tbit.tbitblesdk.bluetooth.scanner;

import android.os.Build;

/**
 * Created by Salmon on 2017/3/8 0008.
 */

public class ScanHelper {

    public static Scanner getScanner() {
        Scanner scanner;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            scanner = new AndroidLScanner();
        } else {
            scanner = new BelowAndroidLScanner();
        }
        return scanner;
    }
}
