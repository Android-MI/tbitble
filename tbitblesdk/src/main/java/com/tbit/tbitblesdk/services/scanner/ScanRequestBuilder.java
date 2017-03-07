package com.tbit.tbitblesdk.services.scanner;

import android.bluetooth.BluetoothAdapter;

/**
 * Created by Salmon on 2017/3/7 0007.
 */

public class ScanRequestBuilder {

    private Scanner scanner;

    public ScanRequestBuilder(BluetoothAdapter bluetoothAdapter) {
        scanner = ScanDecorator.getInstance(bluetoothAdapter);
    }

    public ScanRequestBuilder setTimeout(int timeout) {
        if (timeout > 0) {
            scanner = new ScanDecorator.TimeoutScanner(timeout, scanner);
        }
        return this;
    }

    public ScanRequestBuilder setDebugMode(boolean isDebug) {
        if (isDebug) {
            scanner = new ScanDecorator.DebugScanner(scanner);
        }
        return this;
    }

    public ScanRequestBuilder setRepeatable(boolean isRepeatable) {
        if (!isRepeatable) {
            scanner = new ScanDecorator.NoneRepeatScanner(scanner);
        }
        return this;
    }

    public Scanner build() {
        return scanner;
    }
}
