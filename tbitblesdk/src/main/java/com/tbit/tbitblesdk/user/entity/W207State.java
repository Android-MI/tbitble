package com.tbit.tbitblesdk.user.entity;

import com.tbit.tbitblesdk.Bike.model.BikeState;

import java.util.Arrays;

/**
 * Created by Salmon on 2017/5/4 0004.
 */

public class W207State {

    // 纬度坐标，度值
    private double latitudeDegree;

    // 纬度坐标，分值
    private double latitudeMinute;

    // 经度坐标，度值
    private double longitudeDegree;

    // 经度坐标，分值
    private double longitudeMinute;

    // GPS 卫星数
    private int satellite;

    // 行驶形式总里程
    private int totalMileage;

    // 电池剩余电量
    private int battery;

    // 电池充电总循环数
    private int chargeCount;

    // 充电状态
    private boolean charging;

    // 故障码
    private int[] errorCode = new int[]{0, 0, 0, 0, 0, 0};

    //控制器工作状态
    private int ctrlState;

    //脚撑
    private boolean sustained;

    // 定位方式
    private int gpsState = 0;

    private W207State() {
    }

    public static W207State resolve(BikeState bikeState) {
        W207State state = new W207State();

        double[] longitudes = resolveLocations(bikeState.getLocation()[0]);
        double[] latitude = resolveLocations(bikeState.getLocation()[1]);

        state.setLongitudeDegree(longitudes[0]);
        state.setLongitudeMinute(longitudes[1]);
        state.setLatitudeDegree(latitude[0]);
        state.setLatitudeMinute(latitude[1]);

        state.setSatellite(bikeState.getSignal()[1]);

        state.setGpsState(bikeState.getGpsState());

        BControllerState controllerState = BControllerState.resolve(bikeState.getControllerState().getRawData());

        state.setTotalMileage(controllerState.getTotalMillage());
        state.setBattery(controllerState.getVoltage());

        int[] sysState = bikeState.getSystemState();

        state.setCharging(sysState[2] == 1);
        state.setErrorCode(controllerState.getStatus2());

        state.setChargeCount(controllerState.getChargeCount());

        state.setSustained(sysState[7] == 1);

        String res = String.valueOf(sysState[1]) + String.valueOf(sysState[0]);

        int flag = Integer.valueOf(res, 2);

        int ctrlValue = 1;
        switch (flag) {
            case 0:
                ctrlValue = 2;
                break;
            case 1:
                ctrlValue = 1;
                break;
            case 2:
                ctrlValue = 3;
                break;
        }
        state.setCtrlState(ctrlValue);

        return state;
    }

    // 0:degree 1:minute
    private static double[] resolveLocations(double d) {
        double[] locations = new double[]{0, 0};

        try {
            double degree = (double) ((int) (d));

            locations[0] = degree;

            double min = (d % 1) * 60;

            String degreeStr = String.valueOf(min);

            min = Double.parseDouble(degreeStr.substring(0, degreeStr.indexOf(".") + 5));

            locations[1] = min;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return locations;
    }

    public int getGpsState() {
        return gpsState;
    }

    public void setGpsState(int gpsState) {
        this.gpsState = gpsState;
    }

    public boolean isSustained() {
        return sustained;
    }

    public void setSustained(boolean sustained) {
        this.sustained = sustained;
    }

    public double getLatitudeDegree() {
        return latitudeDegree;
    }

    public void setLatitudeDegree(double latitudeDegree) {
        this.latitudeDegree = latitudeDegree;
    }

    public double getLatitudeMinute() {
        return latitudeMinute;
    }

    public void setLatitudeMinute(double latitudeMinute) {
        this.latitudeMinute = latitudeMinute;
    }

    public double getLongitudeDegree() {
        return longitudeDegree;
    }

    public void setLongitudeDegree(double longitudeDegree) {
        this.longitudeDegree = longitudeDegree;
    }

    public double getLongitudeMinute() {
        return longitudeMinute;
    }

    public void setLongitudeMinute(double longitudeMinute) {
        this.longitudeMinute = longitudeMinute;
    }

    public int getSatellite() {
        return satellite;
    }

    public void setSatellite(int satellite) {
        this.satellite = satellite;
    }

    public int getTotalMileage() {
        return totalMileage;
    }

    public void setTotalMileage(int totalMileage) {
        this.totalMileage = totalMileage;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public int getChargeCount() {
        return chargeCount;
    }

    public void setChargeCount(int chargeCount) {
        this.chargeCount = chargeCount;
    }

    public boolean isCharging() {
        return charging;
    }

    public void setCharging(boolean charging) {
        this.charging = charging;
    }

    public int[] getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int[] errorCode) {
        this.errorCode = errorCode;
    }

    public int getCtrlState() {
        return ctrlState;
    }

    public void setCtrlState(int ctrlState) {
        this.ctrlState = ctrlState;
    }

    @Override
    public String toString() {
        return "W207State{" +
                "latitudeDegree=" + latitudeDegree +
                ", latitudeMinute=" + latitudeMinute +
                ", longitudeDegree=" + longitudeDegree +
                ", longitudeMinute=" + longitudeMinute +
                ", satellite=" + satellite +
                ", totalMileage=" + totalMileage +
                ", battery=" + battery +
                ", chargeCount=" + chargeCount +
                ", charging=" + charging +
                ", errorCode=" + Arrays.toString(errorCode) +
                ", ctrlState=" + ctrlState +
                ", sustained=" + sustained +
                ", gpsState=" + gpsState +
                '}';
    }
}
