package com.tbit.tbitblesdk.bluetooth.request;

/**
 * Created by Salmon on 2017/4/10 0010.
 */

public interface RssiResponse extends BleResponse {

    void onRssi(int rssi);
}
