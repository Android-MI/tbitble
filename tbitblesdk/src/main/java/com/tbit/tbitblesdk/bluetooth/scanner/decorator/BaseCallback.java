package com.tbit.tbitblesdk.bluetooth.scanner.decorator;


import com.tbit.tbitblesdk.bluetooth.scanner.ScannerCallback;

/**
 * Created by Salmon on 2017/3/8 0008.
 */

public abstract class BaseCallback implements ScannerCallback {

    protected ScannerCallback callback;

    public BaseCallback(ScannerCallback callback) {
        this.callback = callback;
    }
}
