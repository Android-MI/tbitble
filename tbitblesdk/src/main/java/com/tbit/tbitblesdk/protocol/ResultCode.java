package com.tbit.tbitblesdk.protocol;

/**
 * Created by Salmon on 2016/12/5 0005.
 */

public class ResultCode {
    public static final int SUCCEED = 0;
    public static final int BLE_NOT_OPENED = -1001;
    public static final int BLE_NOT_SUPPORTED = -1002;
    public static final int PERMISSION_DENIED = -1003;
    public static final int MAC_ADDRESS_ILLEGAL = -2001;
    public static final int DEVICE_NOT_FOUNDED = -2002;
    public static final int KEY_ILLEGAL = -2003;
    public static final int UNLOCK_FAILED = -3001;
    public static final int LOCK_FAILED = -3002;
    public static final int GET_DEVICE_STATE_FAILED = -3003;
}
