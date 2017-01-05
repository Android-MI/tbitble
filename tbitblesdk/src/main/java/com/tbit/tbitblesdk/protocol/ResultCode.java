package com.tbit.tbitblesdk.protocol;

/**
 * Created by Salmon on 2016/12/5 0005.
 */

public class ResultCode {
    // 操作成功
    public static final int SUCCEED = 0;
    // 操作失败
    public static final int FAILED = -1;
    // 手机蓝牙未开启
    public static final int BLE_NOT_OPENED = -1001;
    // 设备不支持蓝牙BLE或非指定终端
    public static final int BLE_NOT_SUPPORTED = -1002;
    // 权限错误
    public static final int PERMISSION_DENIED = -1003;
    // 未连接或连接已断开
    public static final int DISCONNECTED = -1004;
    // 该指令正在发送中，请稍后发送
    public static final int PROCESSING = -1005;
    // 设备编号不合法
    public static final int MAC_ADDRESS_ILLEGAL = -2001;
    // 未找到设备
    public static final int DEVICE_NOT_FOUNDED = -2002;
    // 密钥不正确(包括密钥规格不正确和无法通过校验两种可能)
    public static final int KEY_ILLEGAL = -2003;
    // 连接超时
    public static final int CONNECT_TIME_OUT = -2004;
    // 解锁失败
//    public static final int UNLOCK_FAILED = -3001;
    // 上锁失败
//    public static final int LOCK_FAILED = -3002;
    // 更新状态失败
//    public static final int UPDATE_FAILED = -3003;
}
