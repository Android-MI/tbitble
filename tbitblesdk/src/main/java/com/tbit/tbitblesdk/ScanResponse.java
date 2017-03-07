package com.tbit.tbitblesdk;

import com.tbit.tbitblesdk.services.scanner.ScannerCallback;

/**
 * Created by Salmon on 2017/3/7 0007.
 */

public interface ScanResponse extends ScannerCallback {
    void onError(int errorCode);
}
