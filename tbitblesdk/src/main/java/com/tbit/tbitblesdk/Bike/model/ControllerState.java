package com.tbit.tbitblesdk.Bike.model;

import com.tbit.tbitblesdk.bluetooth.util.ByteUtil;

/**
 * Created by Salmon on 2017/2/10 0010.
 */

public class ControllerState {

    private Byte[] rawData = new Byte[]{};

    public Byte[] getRawData() {
        return rawData;
    }

    public void setRawData(Byte[] rawData) {
        this.rawData = rawData;
    }

    @Override
    public String toString() {
        return "ControllerState{" +
                "rawData=" + ByteUtil.bytesToHexString(rawData) +
                '}';
    }
}
