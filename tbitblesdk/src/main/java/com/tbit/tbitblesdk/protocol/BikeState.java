package com.tbit.tbitblesdk.protocol;

import java.util.Arrays;

/**
 * Created by Salmon on 2016/12/5 0005.
 */

public class BikeState {
    private float battery;
    private double[] location = new double[]{0, 0};
    private int[] signal = new int[]{0, 0, 0};
    private int verifyFailedCode;
    private int deviceFaultCode;
    private int[] systemState = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
    private int operateFaultCode;

    public int[] getSystemState() {
        return systemState;
    }

    public void setSystemState(int[] systemState) {
        this.systemState = systemState;
    }

    public int getOperateFaultCode() {
        return operateFaultCode;
    }

    public void setOperateFaultCode(int operateFaultCode) {
        this.operateFaultCode = operateFaultCode;
    }

    public float getBattery() {
        return battery;
    }

    public void setBattery(float battery) {
        this.battery = battery;
    }

    public double[] getLocation() {
        return location;
    }

    public void setLocation(double[] location) {
        this.location = location;
    }

    public int[] getSignal() {
        return signal;
    }

    public void setSignal(int[] signal) {
        this.signal = signal;
    }

    public int getVerifyFailedCode() {
        return verifyFailedCode;
    }

    public void setVerifyFailedCode(int verifyFailedCode) {
        this.verifyFailedCode = verifyFailedCode;
    }

    public int getDeviceFaultCode() {
        return deviceFaultCode;
    }

    public void setDeviceFaultCode(int deviceFaultCode) {
        this.deviceFaultCode = deviceFaultCode;
    }

    @Override
    public String toString() {
        return "BikeState{" +
                "battery=" + battery +
                ", location=" + Arrays.toString(location) +
                ", signal=" + Arrays.toString(signal) +
                ", verifyFailedCode=" + verifyFailedCode +
                ", deviceFaultCode=" + deviceFaultCode +
                ", systemState=" + Arrays.toString(systemState) +
                ", operateFaultCode=" + operateFaultCode +
                '}';
    }
}
