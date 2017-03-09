package com.tbit.tbitblesdk.protocol;

import com.tbit.tbitblesdk.util.ByteUtil;

import java.util.Arrays;

/**
 * Created by Salmon on 2017/3/9 0009.
 */

public class ManufacturerAd {
    private byte[] manuId = new byte[1];
    private byte[] maskId = new byte[1];
    private String machineId = "";
    private byte[] reverse = new byte[1];
    private int hardware;
    private int software;
    private int type;

    public byte[] getManuId() {
        return manuId;
    }

    public void setManuId(byte[] manuId) {
        this.manuId = manuId;
    }

    public byte[] getMaskId() {
        return maskId;
    }

    public void setMaskId(byte[] maskId) {
        this.maskId = maskId;
    }

    public String getMachineId() {
        return machineId;
    }

    public void setMachineId(String machineId) {
        this.machineId = machineId;
    }

    public byte[] getReverse() {
        return reverse;
    }

    public void setReverse(byte[] reverse) {
        this.reverse = reverse;
    }

    public int getHardware() {
        return hardware;
    }

    public void setHardware(int hardware) {
        this.hardware = hardware;
    }

    public int getSoftware() {
        return software;
    }

    public void setSoftware(int software) {
        this.software = software;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public static ManufacturerAd resolveManufacturerAd(byte[] data) {
        ManufacturerAd manufacturerAd = new ManufacturerAd();
        if (data == null || data.length != 12)
            return manufacturerAd;
        manufacturerAd.setManuId(new byte[]{data[0]});
        manufacturerAd.setMaskId(new byte[]{data[1]});
        String machineId = ByteUtil.bytesToHexStringWithoutSpace(Arrays.copyOfRange(data, 2, 8));
        manufacturerAd.setMachineId(machineId);
        manufacturerAd.setReverse(new byte[]{data[8]});
        manufacturerAd.setHardware(data[9]);
        manufacturerAd.setSoftware(data[10]);
        manufacturerAd.setType(data[11]);
        return manufacturerAd;
    }
}
