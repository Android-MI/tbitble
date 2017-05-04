package com.tbit.tbitblesdk.user.entity;

import com.tbit.tbitblesdk.bluetooth.util.ByteUtil;

import java.util.Arrays;

import static com.tbit.tbitblesdk.Bike.util.StateUpdateHelper.byteArrayToInt;
import static com.tbit.tbitblesdk.Bike.util.StateUpdateHelper.byteToBitArray;

/**
 * Created by Salmon on 2017/4/27 0027.
 */

public class BControllerState {

    private int[] status2 = new int[]{0, 0, 0, 0, 0, 0, 0, 0};

    private int[] status3 = new int[]{0, 0, 0, 0, 0, 0, 0, 0};

    private int[] status4 = new int[]{0, 0, 0, 0, 0, 0, 0, 0};

    // 总里程，单位是 KM
    private int totalMillage;
    // 单次里程，单位是 0.1KM
    private int singleMillage;
    // 湿度
    private int humidity;
    // 电压，单位是 0.1V
    private int voltage;
    // 外接电压
    private int extVoltage;
    // 速度双字节
    private int speed;
    // 运行电流
    private int movingEi;

    public static BControllerState resolve(Byte[] data) {
        BControllerState controllerState = new BControllerState();
        if (data == null || data.length != 19)
            return controllerState;

        byte[] originData = ByteUtil.byteArrayToUnBoxed(data);

        int[] status2 = controllerState.getStatus2();
        Byte b2 = data[1];
        byteToBitArray(b2, status2);

        int[] status3 = controllerState.getStatus3();
        Byte b3 = data[2];
        byteToBitArray(b3, status3);

        int[] status4 = controllerState.getStatus4();
        Byte b4 = data[3];
        byteToBitArray(b4, status4);

        controllerState.setMovingEi(byteArrayToInt(Arrays.copyOfRange(originData, 4, 5)));

        controllerState.setSpeed(byteArrayToInt(Arrays.copyOfRange(originData, 5, 7)));

        controllerState.setVoltage(byteArrayToInt(Arrays.copyOfRange(originData, 7, 8)));

        controllerState.setHumidity(byteArrayToInt(Arrays.copyOfRange(originData, 8, 9)));

        controllerState.setTotalMillage(byteArrayToInt(Arrays.copyOfRange(originData, 9, 13)));

        controllerState.setSingleMillage(byteArrayToInt(Arrays.copyOfRange(originData, 13, 17)));

        controllerState.setExtVoltage(byteArrayToInt(Arrays.copyOfRange(originData, 17, 19)));

        return controllerState;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int[] getStatus2() {
        return status2;
    }

    public void setStatus2(int[] status2) {
        this.status2 = status2;
    }

    public int[] getStatus3() {
        return status3;
    }

    public void setStatus3(int[] status3) {
        this.status3 = status3;
    }

    public int[] getStatus4() {
        return status4;
    }

    public void setStatus4(int[] status4) {
        this.status4 = status4;
    }

    public int getMovingEi() {
        return movingEi;
    }

    public void setMovingEi(int movingEi) {
        this.movingEi = movingEi;
    }

    public int getTotalMillage() {
        return totalMillage;
    }

    public void setTotalMillage(int totalMillage) {
        this.totalMillage = totalMillage;
    }

    public int getSingleMillage() {
        return singleMillage;
    }

    public void setSingleMillage(int singleMillage) {
        this.singleMillage = singleMillage;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public int getVoltage() {
        return voltage;
    }

    public void setVoltage(int voltage) {
        this.voltage = voltage;
    }

    public int getExtVoltage() {
        return extVoltage;
    }

    public void setExtVoltage(int extVoltage) {
        this.extVoltage = extVoltage;
    }

    @Override
    public String toString() {
        return "BControllerState{" +
                "status2=" + Arrays.toString(status2) +
                ", status3=" + Arrays.toString(status3) +
                ", status4=" + Arrays.toString(status4) +
                ", totalMillage=" + totalMillage +
                ", singleMillage=" + singleMillage +
                ", humidity=" + humidity +
                ", voltage=" + voltage +
                ", extVoltage=" + extVoltage +
                ", speed=" + speed +
                ", movingEi=" + movingEi +
                '}';
    }
}
