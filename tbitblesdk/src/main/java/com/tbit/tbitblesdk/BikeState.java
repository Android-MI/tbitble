package com.tbit.tbitblesdk;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Salmon on 2016/12/5 0005.
 */

public class BikeState implements Parcelable {
    private float battery;
    private boolean isLocked;
    private double[] location;

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(this.battery);
        dest.writeByte(this.isLocked ? (byte) 1 : (byte) 0);
        dest.writeDoubleArray(this.location);
    }

    public BikeState() {
    }

    protected BikeState(Parcel in) {
        this.battery = in.readFloat();
        this.isLocked = in.readByte() != 0;
        this.location = in.createDoubleArray();
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
}
