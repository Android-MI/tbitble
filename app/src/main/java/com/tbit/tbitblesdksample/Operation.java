package com.tbit.tbitblesdksample;

/**
 * Created by Salmon on 2017/4/19 0019.
 */

public class Operation {

    public static final int OPERATION_CONNECT = 0;
    public static final int OPERATION_UNLOCK = 1;
    public static final int OPERATION_LOCK = 2;
    public static final int OPERATION_UPDATE = 3;
    public static final int OPERATION_SET_DEFENCE = 4;
    public static final int OPERATION_UNSET_DEFENCE = 5;
    public static final int OPERATION_OTA = 6;
    public static final int OPERATION_BATTERY_UNLOCK = 7;
    public static final int OPERATION_BATTERY_LOCK = 8;
    public static final int OPERATION_CONNECT_OTA = 9;
    public static final int OPERATION_FIND_ON = 10;
    public static final int OPERATION_FIND_OFF = 11;
    public static final int OPERATION_DISCONNECT = 12;
    public static final int OPERATION_SEARCH = 13;

    private String name;

    private int code;

    public Operation(String name, int code) {

        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
