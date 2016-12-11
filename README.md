# tbitble   [![](https://jitpack.io/v/billy96322/tbitble.svg)](https://jitpack.io/#billy96322/tbitble)

## 版本号
1.0
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
    // 解锁失败
    public static final int UNLOCK_FAILED = -3001;
    // 上锁失败
    public static final int LOCK_FAILED = -3002;
    // 更新状态失败
    public static final int UPDATE_FAILED = -3003;
```

### 使用
##### 关于权限
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
##### 初始化

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
##### 操作
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

##### 直接可以获得的参数
```
// 获得当前蓝牙连接状态
TbitBle.getBleConnectionState();
// 获得最后一次更新的车辆状态信息(需要更新请执行更新操作并等待相应回调)
TbitBle.getState()
```
##### 蓝牙状态信息
```
public static final int STATE_DISCONNECTED = 0;
public static final int STATE_SCANNING = 1;
public static final int STATE_CONNECTING = 2;
public static final int STATE_CONNECTED = 3;
public static final int STATE_SERVICES_DISCOVERED = 4;
```
##### 结果回调

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
        public void onCommonCommandResponse(int resultCode) {
            Log.d(TAG, "onCommonCommandResponse: " + resultCode);
        }
    };
```
上述回调方法分别会对应相应的操作，需要注意
* 请求更新状态的时候，会得到请求成功的回调**onUpdateResponse**，但是状态更新需要等到**onStateUpdated**才获得最新的状态。
* **onConnectResponse**方法可能会被多次回调，相应的逻辑在这里要做判断避免重复被执行

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
        compile 'com.github.billy96322:tbitble:0.1'
    }
```

