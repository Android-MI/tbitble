package com.tbit.tbitblesdk.protocol;

/**
 * Created by Kenny on 2016/2/23 17:01.
 * Desc：
 */
public class Constant {

    public static final byte VALUE_ON  = 0x01;
    public static final byte VALUE_OFF = 0x00;
    public static final byte VALUE_FAIL = 0x00;
    public static final byte VALUE_SUCCESS = 0x01;
    public static final byte VALUE_AUTO = 0x00;
    public static final byte VALUE_MANUAL = 0x01;
    public static final byte VALUE_PHONE_CONTROL = 0x00;
    public static final byte VALUE_SHRANK_CONTROL =  0x01;

    public static final byte COMMAND_UPDATE          = (byte) 0x01;
    public static final byte COMMAND_CONNECT         = (byte) 0x02;
    public static final byte COMMAND_SETTING         = (byte) 0x03;
    public static final byte COMMAND_QUERY           = (byte) 0x04;
    public static final byte COMMAND_BIND            = (byte) 0x05;
    public static final byte COMMAND_LOG             = (byte) 0x08;
    public static final byte COMMAND_HEART_BEAT      = (byte) 0x09;
    public static final byte COMMAND_RESTART         = (byte) 0xfc;
    public static final byte COMMAND_FACTORY_RESET   = (byte) 0xfd;

    // 0x02 1 终端连接是否成功
    // 0x81 1 终端电池电压
    // 0x82 4 终端车辆轮子转动数
    // 0x83 1 终端当前温度
    // 0x84 1 终端当前速度
    // 0x87 1 终端重启原因
    public static final byte SEND_KEY_CONNECT        = (byte) 0x01;
    public static final byte RESP_KEY_CONNECT_RESP   = (byte) 0x02;
    public static final byte RESP_KEY_VOLTAGE        = (byte) 0x81;
    public static final byte RESP_KEY_WHEEL          = (byte) 0x82;
    public static final byte RESP_KEY_TEMPRETURE     = (byte) 0x83;
    public static final byte RESP_KEY_SPEED          = (byte) 0x84;
    public static final byte RESP_KEY_TESTED         = (byte) 0x85;
    public static final byte RESP_KEY_RESTART_COUNT  = (byte) 0x86;
    public static final byte RESP_KEY_RESTART_REASON = (byte) 0x87;

    // 0x01 0x01 设防/撤防
    // 0x02 0x01 寻车功能打开/关闭 0x00 关闭 0x01 打开
    // 0x03 0x01 控制模式切换 0x00 手机控制模式 0x01 手柄控制模式
    // 0x04 0x01 设置终端布防 RSSI值 布防位置 RSSI值
    // 0x05 0x01 震动灵敏度设置 震动灵敏级别（1-5）其中 1 震动最灵敏
    // 0x06 0x01 回家模式开关 0x00 关闭 0x01 打开
    // 0x07 0x01 蓝牙设防撤防模式开关 0x00 自动模式 0x01 手动模式
    // 0x08 0x01 静音设防开关 0x00 关闭 0x01 打开
    // 0x09 0x01 一键启动开关 0x00 关闭 0x01 打开
    // 0x0A 0x01 设置终端外接电源电压 外接电源电压值，电压档位分别为 12V、36V、48V、60V、64V、72V
    public static final byte SEND_KEY_DEFENCE              = 0x01;
    public static final byte SEND_KEY_FIND                 = 0x02;
    public static final byte SEND_KEY_CONTROL              = 0x03;
    public static final byte SEND_KEY_RSSI                 = 0x04;
    public static final byte SEND_KEY_SENSITIVITY          = 0x05;
    public static final byte SEND_KEY_HOME_MODE            = 0x06;
    public static final byte SEND_KEY_AUTO_DEFENCE         = 0x07;
    public static final byte SEND_KEY_SILENCE              = 0x08;
    public static final byte SEND_KEY_FAST_START           = 0x09;
    public static final byte SEND_KEY_TERMINAL_VOLTAGE     = 0x0A;
    public static final byte SEND_KEY_QUERY_VOLTAGE        = 0x01;
    public static final byte SEND_KEY_QUERY_WHEEL_TERN     = 0x02;
    public static final byte SEND_KEY_QUERY_TEMPERATURE    = 0x03;
    public static final byte SEND_KEY_QUERY_SPEED          = 0x04;
    public static final byte SEND_KEY_QUERY_TESTED         = 0x05;
    public static final byte SEND_KEY_QUERY_RESTART_COUNT  = 0x06;
    public static final byte SEND_KEY_QUERY_RESTART_REASON = 0x07;

    public static final int REQUEST_IGNORE = 100;
    public static final int REQUEST_CONNECT = 1;
    public static final int REQUEST_SET_DEFENCE = 2;
//    public static final int REQUEST_CANCEL_DEFENCE = 3;
    public static final int REQUEST_FIND_CAR = 4;
    public static final int REQUEST_HOME_MODE = 5;
    public static final int REQUEST_SILENCE = 6;
    public static final int REQUEST_FAST_START = 7;
    public static final int REQUEST_CONTROL = 8;
    public static final int REQUEST_AUTO_DEFENCE = 9;
    public static final int REQUEST_RSSI = 10;
    public static final int REQUEST_SENSIBILITY = 11;
    public static final int REQUEST_TERMINAL_VOLTAGE = 12;
    public static final int REQUEST_WHEEL_RADIUS = 13;
    public static final int REQUEST_QUERY_VOLTAGE = 14;
    public static final int REQUEST_QUERY_WHEEL_TERN = 15;
    public static final int REQUEST_QUERY_SPEED = 16;
    public static final int REQUEST_RESTART = 17;
    public static final int REQUEST_RESET = 18;
    public static final int REQUEST_UPDATE = 19;
    public static final int REQUEST_QUERY_TEMPERATURE = 20;
    public static final int REQUEST_QUERY_TESTED = 21;
    public static final int REQUEST_HEART_BEAT = 22;
    public static final int REQUEST_MILEAGE_CALIBRATION = 23;
    public static final int REQUEST_LOG = 24;

    public final static String ACTION_UPDATE_VOLTAGE =
            "com.tbit.ACTION_UPDATE_VOLTAGE";
    public final static String ACTION_UPDATE_SPEED =
            "com.tbit.ACTION_UPDATE_SPEED";
    public final static String ACTION_UPDATE_MILEAGE =
            "com.tbit.ACTION_UPDATE_MILEAGE";
    /**
     * OTA更新
     */
    public final static String ACTION_OTA_UPDATE =
            "com.tbit.ACTION_OTA_UPDATE";
    /**
     * 告警
     */
    public final static String ACTION_ALARM =
            "com.tbit.ACTION_ALARM";
    public final static String EXTRA_DATA =
            "com.tbit.EXTRA_DATA";
    public final static String ACTION_SEND_OK = "com.tbit.ACTION_SEND_OK";
    public final static String ACTION_SEND_FAIL = "com.tbit.ACTION_SEND_FAIL";
    public final static String ACTION_WRITE_OK = "com.tbit.ACTION_WRITE_OK";
    public final static String ACTION_DEVICE_CONN_OK = "com.tbit.ACTION_DEVICE_CONN_OK";
    public final static String ACTION_DEVICE_CONN_PASS = "com.tbit.ACTION_DEVICE_CONN_PASS";
    public final static String ACTION_DEVICE_CONN_FAIL = "com.tbit.ACTION_DEVICE_CONN_FAIL";

    //***********传输层*****************


    //***************应用层*************
    public final static int[] crcTable = {
            0X0000, 0X1189, 0X2312, 0X329B, 0X4624, 0X57AD, 0X6536, 0X74BF, 0X8C48, 0X9DC1, 0XAF5A, 0XBED3, 0XCA6C,
            0XDBE5, 0XE97E, 0XF8F7, 0X1081, 0X0108, 0X3393, 0X221A, 0X56A5, 0X472C, 0X75B7, 0X643E, 0X9CC9, 0X8D40,
            0XBFDB, 0XAE52, 0XDAED, 0XCB64, 0XF9FF, 0XE876, 0X2102, 0X308B, 0X0210, 0X1399, 0X6726, 0X76AF, 0X4434,
            0X55BD, 0XAD4A, 0XBCC3, 0X8E58, 0X9FD1, 0XEB6E, 0XFAE7, 0XC87C, 0XD9F5, 0X3183, 0X200A, 0X1291, 0X0318,
            0X77A7, 0X662E, 0X54B5, 0X453C, 0XBDCB, 0XAC42, 0X9ED9, 0X8F50, 0XFBEF, 0XEA66, 0XD8FD, 0XC974, 0X4204,
            0X538D, 0X6116, 0X709F, 0X0420, 0X15A9, 0X2732, 0X36BB, 0XCE4C, 0XDFC5, 0XED5E, 0XFCD7, 0X8868, 0X99E1,
            0XAB7A, 0XBAF3, 0X5285, 0X430C, 0X7197, 0X601E, 0X14A1, 0X0528, 0X37B3, 0X263A, 0XDECD, 0XCF44, 0XFDDF,
            0XEC56, 0X98E9, 0X8960, 0XBBFB, 0XAA72, 0X6306, 0X728F, 0X4014, 0X519D, 0X2522, 0X34AB, 0X0630, 0X17B9,
            0XEF4E, 0XFEC7, 0XCC5C, 0XDDD5, 0XA96A, 0XB8E3, 0X8A78, 0X9BF1, 0X7387, 0X620E, 0X5095, 0X411C, 0X35A3,
            0X242A, 0X16B1, 0X0738, 0XFFCF, 0XEE46, 0XDCDD, 0XCD54, 0XB9EB, 0XA862, 0X9AF9, 0X8B70, 0X8408, 0X9581,
            0XA71A, 0XB693, 0XC22C, 0XD3A5, 0XE13E, 0XF0B7, 0X0840, 0X19C9, 0X2B52, 0X3ADB, 0X4E64, 0X5FED, 0X6D76,
            0X7CFF, 0X9489, 0X8500, 0XB79B, 0XA612, 0XD2AD, 0XC324, 0XF1BF, 0XE036, 0X18C1, 0X0948, 0X3BD3, 0X2A5A,
            0X5EE5, 0X4F6C, 0X7DF7, 0X6C7E, 0XA50A, 0XB483, 0X8618, 0X9791, 0XE32E, 0XF2A7, 0XC03C, 0XD1B5, 0X2942,
            0X38CB, 0X0A50, 0X1BD9, 0X6F66, 0X7EEF, 0X4C74, 0X5DFD, 0XB58B, 0XA402, 0X9699, 0X8710, 0XF3AF, 0XE226,
            0XD0BD, 0XC134, 0X39C3, 0X284A, 0X1AD1, 0X0B58, 0X7FE7, 0X6E6E, 0X5CF5, 0X4D7C, 0XC60C, 0XD785, 0XE51E,
            0XF497, 0X8028, 0X91A1, 0XA33A, 0XB2B3, 0X4A44, 0X5BCD, 0X6956, 0X78DF, 0X0C60, 0X1DE9, 0X2F72, 0X3EFB,
            0XD68D, 0XC704, 0XF59F, 0XE416, 0X90A9, 0X8120, 0XB3BB, 0XA232, 0X5AC5, 0X4B4C, 0X79D7, 0X685E, 0X1CE1,
            0X0D68, 0X3FF3, 0X2E7A, 0XE70E, 0XF687, 0XC41C, 0XD595, 0XA12A, 0XB0A3, 0X8238, 0X93B1, 0X6B46, 0X7ACF,
            0X4854, 0X59DD, 0X2D62, 0X3CEB, 0X0E70, 0X1FF9, 0XF78F, 0XE606, 0XD49D, 0XC514, 0XB1AB, 0XA022, 0X92B9,
            0X8330, 0X7BC7, 0X6A4E, 0X58D5, 0X495C, 0X3DE3, 0X2C6A, 0X1EF1, 0X0F78,
    };
    //***********传输层*****************
    public static String SEND_OUT = "AA00";
    public static String SEND_RESPONSE = "AA10";
    public static String RESERVE[] = {"00", "01", "10", "11"};
    //first for no err,second for err
    public static String ERR_FLAG[] = {"0", "1"};
    //first for no response,second for response
    public static String ACK_FLAG[] = {"0", "1"};
    //version
    public static String VERSION[] = {"0000", "0001", "0010", "0011", "0100", "0101", "0110", "0111", "1000", "1001", "1010", "1011", "1100", "1101", "1110", "1111"};


    //***************应用层*************
    //first for close ,second for open
    public static String ELECTRIC_DOOR_LOCK[] = {"0", "1"};
    //Disconnect the power,Vibration,low power,no alarm
    public static String ALARM_TYPE[] = {"11", "10", "01", "00"};
    //Silent mode off ,Silent mode on
    public static String SILENT_MODE[] = {"0", "1"};
    //Manual mode,Auto mode
    public static String BLE_BF_CF[] = {"0", "1"};
    //Phone,Handle
    public static String CONTROL_MODE[] = {"0", "1"};
    //CF open,SF open
    public static String SF_CF_OPEN[] = {"0", "1"};
    //固件升级命令，连接命令，设置命令，查询命令，绑定命令，工厂测试命令，Dump stack命令，日志命令，心跳包，测试flash读取命令，系统测试命令
    public static String COMMAND_ID[] = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "FE", "FF"};
    //进入固件升级模式请求，进入固件升级模式返回
    public static String UPDATE_KEY[] = {"01", "02"};
    //用户连接请求，用户连接返回，管理员连接请求，管理员连接返回
    public static String CONNECT_DEVICE[] = {"01", "02", "03", "04"};
    //设置命令key列表：设防/撤防，寻车功能打开/关闭，控制模式切换，设置终端布防RSSI值，震动灵敏度设置，回家模式开关，蓝牙设防撤防模式开关，静音设防开关，一键启动开关
    public static String SETTING_KEY[] = {"01", "02", "03", "04", "05", "06", "07", "08", "09"};
    //与设置key相对应：{撤防，设防}，{关闭，打开}，{手机控制模式，手柄控制模式}，{}，{}，{关闭，打开}，{自动模式，手动模式}，{关闭，打开}，{关闭，打开}
    public static String SETTING_KEY_VALUE[][] = {{"00", "01"}, {"00", "01"}, {"00", "01"}, {"", ""}, {"", ""}, {"00", "01"}, {"00", "01"}, {"00", "01"}, {"00", "01"}};
    //查询命令key列表：查询当前终端电压，查询当前车辆轮子转动数，查询当前终端温度，查询当前终端速度，查询当前终端是否综测过
    public static String QUERY_KEY[] = {"01", "02", "03", "04", "05"};
    //查询命令返回的key列表：返回当前终端电压，返回当前车辆轮子转动数，返回当前终端温度，返回当前终端速度，返回当前终端是否综测过
    public static String RETURN_QUERY_KEY[] = {"81", "82", "83", "84", "85"};

    //****************SharePreferences相关字段名
    public static String SP_RECENTDEVICE = "recentDevice";
    public static String SP_HAS_A1_DEVICE = "has_a1";// 拥有A1设备(连接过蓝牙设备)
}
