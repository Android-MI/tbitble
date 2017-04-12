package com.tbit.tbitblesdk.bluetooth.request;

import android.bluetooth.BluetoothGatt;

import com.tbit.tbitblesdk.bluetooth.Code;
import com.tbit.tbitblesdk.bluetooth.listener.ReadRssiListener;

/**
 * Created by Salmon on 2017/4/10 0010.
 */

public class RssiRequest extends BleRequest implements ReadRssiListener {
    private RssiResponse rssiResponse;
    public RssiRequest(RssiResponse rssiResponse) {
        super(rssiResponse);

        this.rssiResponse = rssiResponse;
    }

    @Override
    protected void onPrepare() {
        super.onPrepare();
        bleClient.getListenerManager().addReadRssiListener(this);
    }

    @Override
    protected void onRequest() {
        if (!bleClient.readRssi()) {
            response(Code.REQUEST_FAILED);
            return;
        }

        startTiming();
    }

    @Override
    protected int getTimeout() {
        return 3000;
    }

    @Override
    public void onReadRemoteRssi(int rssi, int status) {
        stopTiming();

        if (BluetoothGatt.GATT_SUCCESS == status) {
            rssiResponse.onRssi(rssi);
            response(Code.REQUEST_SUCCESS);
        } else {
            response(Code.REQUEST_FAILED);
        }
    }

    @Override
    protected void onFinish() {
        bleClient.getListenerManager().removeReadRssiListener(this);
    }
}
