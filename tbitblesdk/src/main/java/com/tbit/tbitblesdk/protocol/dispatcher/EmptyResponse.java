package com.tbit.tbitblesdk.protocol.dispatcher;

import com.tbit.tbitblesdk.bluetooth.request.BleResponse;

/**
 * Created by Salmon on 2017/3/24 0024.
 */

public class EmptyResponse implements BleResponse{
    @Override
    public void onResponse(int resultCode) {

    }
}
