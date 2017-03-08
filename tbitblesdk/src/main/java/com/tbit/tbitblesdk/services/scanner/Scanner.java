package com.tbit.tbitblesdk.services.scanner;

/**
 * Created by Salmon on 2017/3/3 0003.
 */

public interface Scanner {

    void start(ScannerCallback callback);

    void stop();

    void setTimeout(long timeout);

    boolean isScanning();
}
