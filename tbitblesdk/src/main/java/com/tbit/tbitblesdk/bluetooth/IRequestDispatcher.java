package com.tbit.tbitblesdk.bluetooth;

import com.tbit.tbitblesdk.bluetooth.request.BleRequest;

/**
 * Created by Salmon on 2017/3/23 0023.
 */

public interface IRequestDispatcher {

    void onRequestFinished(BleRequest request);
}
