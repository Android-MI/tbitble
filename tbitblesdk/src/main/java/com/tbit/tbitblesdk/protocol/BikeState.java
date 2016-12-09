package com.tbit.tbitblesdk.protocol;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

/**
 * Created by Salmon on 2016/12/5 0005.
 */

public class BikeState implements Parcelable {

    private float battery;
    private boolean isLocked;
    private double[] location = new double[]{0, 0};
    private int verifyFailedCode;
    private int deviceFaltCode;
    private int[] signal = new int[]{0, 0, 0};

    public float getBattery() {
        return battery;
    }

    public void setBattery(float battery) {
        this.battery = battery;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public double[] getLocation() {
        return location;
    }

    public void setLocation(double[] location) {
        this.location = location;
    }

    public int getVerifyFailedCode() {
        return verifyFailedCode;
    }

    public void setVerifyFailedCode(int verifyFailedCode) {
        this.verifyFailedCode = verifyFailedCode;
    }

    public int getDeviceFaltCode() {
        return deviceFaltCode;
    }

    public void setDeviceFaltCode(int deviceFaltCode) {
        this.deviceFaltCode = deviceFaltCode;
    }

    public int[] getSignal() {
        return signal;
    }

    public void setSignal(int[] signal) {
        this.signal = signal;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(this.battery);
        dest.writeByte(this.isLocked ? (byte) 1 : (byte) 0);
        dest.writeDoubleArray(this.location);
        dest.writeInt(this.verifyFailedCode);
        dest.writeInt(this.deviceFaltCode);
        dest.writeIntArray(this.signal);
    }

    public BikeState() {
    }

    protected BikeState(Parcel in) {
        this.battery = in.readFloat();
        this.isLocked = in.readByte() != 0;
        this.location = in.createDoubleArray();
        this.verifyFailedCode = in.readInt();
        this.deviceFaltCode = in.readInt();
        this.signal = in.createIntArray();
    }

    public static final Parcelable.Creator<BikeState> CREATOR = new Parcelable.Creator<BikeState>() {
        @Override
        public BikeState createFromParcel(Parcel source) {
            return new BikeState(source);
        }

        @Override
        public BikeState[] newArray(int size) {
            return new BikeState[size];
        }
    };

    @Override
    public String toString() {
        return "BikeState{" +
                "battery=" + battery +
                ", isLocked=" + isLocked +
                ", location=" + Arrays.toString(location) +
                ", verifyFailedCode=" + verifyFailedCode +
                ", deviceFaltCode=" + deviceFaltCode +
                ", signal=" + Arrays.toString(signal) +
                '}';
    }
}
