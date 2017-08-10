package com.tbit.tbitblesdk.user.entity;

import java.util.Arrays;

/**
 * Created by yankaibang on 2017/8/9.
 */

public class W206State {
    // 行驶形式总里程
    private int totalMileage;
    // 行驶形式单次里程
    private int singleMileage;

    private int battery;
    // 车辆外接电压
    private int extVoltage;

    // 电池充电总循环数
    private int chargeCount;

    // 故障码
    private int[] errorCode = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};

    //控制器工作状态
    private int ctrlState;

    // 运行电流
    private int movingEi;

    public int getControllerTemperature() {
        return controllerTemperature;
    }

    public void setControllerTemperature(int controllerTemperature) {
        this.controllerTemperature = controllerTemperature;
    }

    private int controllerTemperature;

    public int getMovingEi() {
        return movingEi;
    }

    public void setMovingEi(int movingEi) {
        this.movingEi = movingEi;
    }

    public int getTotalMileage() {
        return totalMileage;
    }

    public void setTotalMileage(int totalMileage) {
        this.totalMileage = totalMileage;
    }

    public void setSingleMileage(int singleMileage) {
        this.singleMileage = singleMileage;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public void setExtVoltage(int extVoltage) {
        this.extVoltage = extVoltage;
    }

    public int getChargeCount() {
        return chargeCount;
    }

    public void setChargeCount(int chargeCount) {
        this.chargeCount = chargeCount;
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
        return "W206State{" +
                "totalMileage=" + totalMileage +
                ", singleMileage=" + singleMileage +
                ", battery=" + battery +
                ", extVoltage=" + extVoltage +
                ", chargeCount=" + chargeCount +
                ", errorCode=" + Arrays.toString(errorCode) +
                ", ctrlState=" + ctrlState +
                ", movingEi=" + movingEi +
                ", controllerTemperature=" + controllerTemperature +
                '}';
    }
}
