# tbitble

## 版本号
[![](https://jitpack.io/v/billy96322/tbitble.svg)](https://jitpack.io/#billy96322/tbitble)
> 该版本非最终版本，文中提及的方法名，接口名，以及传递参数有在后续的版本做更改的可能性，以最新版本的文档为准

---
## 最低安卓版本号
18
## 需要用到的权限
```
<uses-permission android:name="android.permission.BLUETOOTH"/>
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
```

## 状态码
```
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
// 低于API18
public static final int LOWER_THAN_API_18 = -1006;
// 设备编号不合法
public static final int MAC_ADDRESS_ILLEGAL = -2001;
// 未找到设备
public static final int DEVICE_NOT_FOUNDED = -2002;
// 密钥不正确(包括密钥规格不正确和无法通过校验两种可能)
public static final int KEY_ILLEGAL = -2003;
// 连接超时
public static final int CONNECT_TIME_OUT = -2004;
// 验证密钥超时
public static final int VERIFICATION_RESPONSE_TIME_OUT = -2005;
// 解锁失败
public static final int UNLOCK_FAILED = -3001;
// 上锁失败
public static final int LOCK_FAILED = -3002;
// 获取终端状态失败
public static final int UPDATE_STATUS_FAILED = -3003;
// 指令非法
public static final int ILLEGAL_COMMAND = -3004;
// 运动状态
public static final int MOTION_STATE = -3005;
// 非绑定状态
public static final int NOT_BINDING = -3006;
// 断开连接
public static final int NOT_CONNECTING = -3007;
// 重启终端失败
public static final int RESTART_FAILED = -3008;
// 恢复原厂失败
public static final int FACTORING_FAILED = -3009;
// OTA升级文件不合法
public static final int OTA_FILE_ILLEGAL = -4001;
// OTA升级失败 - 电量不足
public static final int OTA_FAILED_LOW_POWER = -4002;
// OTA升级失败 - 未知原因
public static final int OTA_FAILED_UNKNOWN = -4003;
// OTA升级失败 - 写入失败
public static final int OTA_WRITE_FAILED = -4004;
// OTA升级失败 - 密钥错误
public static final int OTA_FAILED_ERR_KEY = -4005;
// OTA升级失败 - Invalid image bank
public static final int OTA_FAILED_IMAGE_BANK = -4006;
// OTA升级失败 - Invalid image header
public static final int OTA_FAILED_IMAGE_HEADER = -4007;
// OTA升级失败 - Invalid image size
public static final int OTA_FAILED_IMAGE_SIZE = -4008;
// OTA升级失败 - Invalid product header
public static final int OTA_FAILED_PRODUCT_HEADER = -4009;
// OTA升级失败 - Same Image Error
public static final int OTA_FAILED_SAME_IMAGE = -4010;
// OTA升级失败 - Failed to read from external memory device
public static final int OTA_FAILED_TO_READ_FROM_EXTERNAL_MEM = -4011;
// 连接失败，未知原因
public static final int CONNECT_FAILED_UNKNOWN = -8000;
// 连接失败，密钥非法
public static final int CONNECT_FAILED_ILLEGAL_KEY = -8001;
// 连接失败，数据校验非法
public static final int CONNECT_DATA_VERIFICATION_FAILED = -8002;
// 连接失败，已有设备连接
public static final int CONNECT_COMMAND_NOT_SUPPORT= -8003;
// 连接失败，已有设备连接
public static final int CONNECT_ALREADY_CONNECTED= -8004;
```

### 使用
#### 关于权限
在Android 6.0 及以上系统，除了需要申请文中说明的权限，最好也将该开关打开
```
LocationManager locationManager =
        (LocationManager) getSystemService(Context.LOCATION_SERVICE);
if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
    Intent intent = new Intent();
    intent.setAction(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
    MainActivity.this.startActivity(intent);
    Toast.makeText(MainActivity.this, "请开启", Toast.LENGTH_LONG).show();
}
```
#### 初始化

在程序**入口**的Activity中初始化
```
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        TbitBle.initialize(MainActivity.this);
        TbitBle.setListener(listener);
    }
}
```
**或者**

在Application中进行初始化，与在入口Activity中初始化类似，但需要确保只在主进程被初始化
```
private boolean isMainProcess() {
    int pid = Process.myPid();
    String processNameString = "";
    ActivityManager m = (ActivityManager) getSystemService(
            Context.ACTIVITY_SERVICE);
    for (ActivityManager.RunningAppProcessInfo appProcess :
            m.getRunningAppProcesses()) {
        if (appProcess.pid == pid) {
            processNameString = appProcess.processName;
        }
    }
    return TextUtils.equals(BuildConfig.APPLICATION_ID, processNameString);
}
```
#### 操作
```
// 如果是Android 6.0以上需要在权限被同意之后再做初始化工作以下操作

// 连接命令，参数为设备编号，密钥
TbitBle.connect(deviceId, key);
// 解锁
TbitBle.unlock();
// 上锁
TbitBle.lock();
// 更新状态
TbitBle.update();
// 重新连接
TbitBle.reconnect();
// 断开连接
TbitBle.disConnect();
// 销毁（在退出程序的Activity中的onDestroy中调用）
// 销毁后无法再进行操作，如需要请重新初始化
TbitBle.destroy();
```
以上操作均需要在**主线程**执行

#### 直接可以获得的参数
```
// 获得当前蓝牙连接状态
TbitBle.getBleConnectionState();
// 获得最后一次更新的车辆状态信息(需要更新请执行更新操作并等待相应回调)
TbitBle.getState()
```
#### 蓝牙状态信息
```
public static final int STATE_DISCONNECTED = 0;
public static final int STATE_SCANNING = 1;
public static final int STATE_CONNECTING = 2;
public static final int STATE_CONNECTED = 3;
public static final int STATE_SERVICES_DISCOVERED = 4;
```

#### 车辆状态字段
```
// 电量
private float battery;

// 位置,location[0]是经度，location[1]是纬度
private double[] location = new double[]{0, 0};

// 信号量，数组的三位分别为如下
// GSM：用于标识 GSM 信号强度，范围 0~9
// GPS：用于标识 GPS 信号强度，范围 0~9
// BAT：用于标识定位器后备电池电量，范围 0~9
private int[] signal = new int[]{0, 0, 0};

// 校验失败原因
// 0：未知原因 1：连接密钥非法 2：数据校验失败 3：指令不支持
private int verifyFailedCode;

// 车辆故障 电机类故障 中控类故障 通信类故障 其它故障 详见常用数据类型说明
private int deviceFaultCode;

// 数组每一位的解析如下
// 0 | 0：车辆撤防模式开启 1：车辆设防模式开启
// 1 | 0：车辆处于静止状态 1：车辆处于运动状态
// 2 | 0：车辆锁电机为关闭状态 1：车辆锁电机为打开状态
// 3 | 0：车辆 ACC 为关闭状态 1：车辆 ACC 为打开状态
// 4 | 0：车辆不处于休眠模式 1：车辆处于休眠模式
// 5 | 0：车辆蓝牙处于非连接状态 1：车辆蓝牙处于连接状态
// 6~7 | 11：断电告警  10：震动告警  01：低电告警  00：无告警信息
private int[] systemState = new int[]{0, 0, 0, 0, 0, 0, 0, 0};

// 0x00：成功  0x01：指令非法 0x02：运动状态 0x03：非绑定状态
private int operateFaultCode;
// 0:MCC 1:MNC 2:LAC 3.Cell ID
private int[] baseStation = new int[]{0, 0, 0, 0};
// 版本号 0：硬件版本 1：软件版本
private int[] version = new int[]{0, 0};
// 控制器信息
private ControllerState controllerState;
```

#### 控制器状态字段
```
// 总里程，单位是 KM
private int totalMillage;
// 单次里程，单位是 0.1KM
private int singleMillage;
// 速度，单位是 0.1KM/H
private int speed;
// 电压，单位是 0.1V
private int voltage;
// 电流，单位是 MA
private int electricCurrent;
// 电量，单位是 MAH
private int battery;
// 故障码
// BIT7 电机缺相      0: 电机不故障，1: 电机缺相故障
// BIT6 霍尔故障状态  0: 霍尔无故障，1: 霍尔故障
// BIT5 转把故障状态  0: 转把无故障，1: 转把故障
// BIT4 MOS 故障状态   0：MOS 无故障，1：MOS 故障
// BIT3 欠压状态      0: 不在欠压保护，1: 正在欠压保护
// BIT2 巡航状态      0: 巡航无效，1: 巡航有效
// BIT1 刹车状态      0：刹车无效，1：刹车有效
// BIT0 WALK 状态      0：WALK 无效，1：WALK 有效
private int[] errCode = new int[]{0,0,0,0,0,0,0,0};
```

#### 结果回调

```
TbitListener listener = new TbitListener() {
        @Override
        public void onConnectResponse(int resultCode) {
            Log.d(TAG, "onConnectResponse: " + resultCode);
        }

        @Override
        public void onUnlockResponse(int resultCode) {
            Log.d(TAG, "onUnlockResponse: " + resultCode);
        }

        @Override
        public void onLockResponse(int resultCode) {
            Log.d(TAG, "onLockResponse: " + resultCode);
        }

        @Override
        public void onUpdateResponse(int resultCode) {
            Log.d(TAG, "onUpdateResponse: " + resultCode);
        }

        @Override
        public void onStateUpdated(BikeState state) {
            Log.d(TAG, "onStateUpdated: " + state.toString());

            //也可以这样，下述方式在全局均可调用
            Log.d(TAG, "onStateUpdated: " + TbitBle.getState());
        }

        @Override
        public void onDisconnected(int resultCode) {
            Log.d(TAG, "onDisconnected: " + resultCode);
        }

        @Override
        public void onCommonCommandResponse(int resultCode, PacketValue packetValue) {
            // packetValue 有为null的可能
            if(packetValue == null)
                ...
        }
    };
```
上述回调方法分别会对应相应的操作，需要注意
* 请求更新状态的时候，会得到请求成功的回调**onUpdateResponse**，但是状态更新需要等到**onStateUpdated**才获得最新的状态。
* **onConnectResponse**方法可能会被多次回调，相应的逻辑在这里要做判断避免重复被执行


#### 扫描设备

```
// 最终得到的结果的回调
ScannerCallback scannerCallback = new ScannerCallback() {
    @Override
    public void onScanStart() {
        Log.d(TAG, "onScanStart: ");
    }

    @Override
    public void onScanStop() {
        Log.d(TAG, "onScanStop: ");
    }

    @Override
    public void onScanCanceled() {
        Log.d(TAG, "onScanCanceled: ");
    }

    @Override
    public void onDeviceFounded(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
        String machineId = BikeUtil.resolveMachineIdByAdData(bytes);
        if (!TextUtils.isEmpty(machineId)) {
            showLog("扫描到设备: " + bluetoothDevice.getAddress()+ " | " + machineId);
        }
    }
};

// 添加装饰器

// 方式一：
// 过滤设备名字的装饰器
FilterNameCallback filterNameCallback = new FilterNameCallback(DEVICE_NAME, scannerCallback);
// 确保结果非重复的装饰器
NoneRepeatCallback noneRepeatCallback = new NoneRepeatCallback(filterNameCallback);
// 收集日志的装饰器，这个最好放在最外层包裹
LogCallback logCallback = new LogCallback(noneRepeatCallback);

// 方式二：(与上述效果相同)
ScanBuilder builder = new ScanBuilder(scannerCallback);
ScannerCallback decoratedCallback = builder
        .setFilter(DEVICE_NAME)
        .setRepeatable(false)
        .setLogMode(true)
        .build();

// 开始扫描(目前同一时间仅支持启动一个扫描),返回状态码
int code = TbitBle.startScan(logCallback, 10000);

// 结束当前扫描
TbitBle.stopScan();
```


### 添加依赖到项目
----------------------
在项目根目录的build.gradle添加
```gradle
allprojects {
		repositories {
			...
			maven { url "https://jitpack.io" }
		}
	}
```

 添加依赖

``` gradle
dependencies {
        compile 'com.github.billy96322:tbitble:0.5.3'
    }
```

