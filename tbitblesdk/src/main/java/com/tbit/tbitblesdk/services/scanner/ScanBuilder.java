package com.tbit.tbitblesdk.services.scanner;

import com.tbit.tbitblesdk.services.scanner.decorator.FilterNameCallback;
import com.tbit.tbitblesdk.services.scanner.decorator.LogCallback;
import com.tbit.tbitblesdk.services.scanner.decorator.NoneRepeatCallback;

/**
 * Created by Salmon on 2017/3/7 0007.
 */

public class ScanBuilder {

    private ScannerCallback callback;

    public ScanBuilder(ScannerCallback callback) {
        this.callback = callback;
    }

    public ScanBuilder setLogMode(boolean isLog) {
        if (isLog) {
            callback = new LogCallback(callback);
        }
        return this;
    }

    public ScanBuilder setRepeatable(boolean isRepeatable) {
        if (!isRepeatable) {
            callback = new NoneRepeatCallback(callback);
        }
        return this;
    }

    public ScanBuilder setFilter(String filterStr) {
        callback = new FilterNameCallback(filterStr, callback);
        return this;
    }

    public ScannerCallback build() {
        return callback;
    }
}
