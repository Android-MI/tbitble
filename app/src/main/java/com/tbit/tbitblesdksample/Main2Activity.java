package com.tbit.tbitblesdksample;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.tbit.tbitblesdk.TbitBle;
import com.tbit.tbitblesdk.TbitDebugListener;
import com.tbit.tbitblesdk.TbitListener;
import com.tbit.tbitblesdk.TbitListenerAdapter;
import com.tbit.tbitblesdk.protocol.BikeState;
import com.tbit.tbitblesdk.protocol.Packet;
import com.tbit.tbitblesdk.protocol.PacketValue;
import com.tbit.tbitblesdk.services.command.Command;
import com.tbit.tbitblesdk.services.command.callback.ProgressCallback;
import com.tbit.tbitblesdk.services.command.callback.ResultCallback;
import com.tbit.tbitblesdk.services.scanner.ScanBuilder;
import com.tbit.tbitblesdk.services.scanner.ScannerCallback;
import com.tbit.tbitblesdk.services.scanner.decorator.FilterNameCallback;
import com.tbit.tbitblesdk.services.scanner.decorator.LogCallback;
import com.tbit.tbitblesdk.services.scanner.decorator.NoneRepeatCallback;
import com.tbit.tbitblesdk.util.BikeUtil;
import com.tbit.tbitblesdk.util.PacketUtil;
import com.tbit.tbitblesdksample.aes.AesTool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import me.salmonzhg.easypermission.EasyPermissionHelper;
import me.salmonzhg.easypermission.PermissionListener;

public class Main2Activity extends AppCompatActivity {

    // 022009020
    private static final String TAG = "MainActivity";
    private static final String KEY = "d6 15 61 bc 02 4e 33 70 b1 7b 57 24 60 83 25 81 02 7d b3 56 ab e6 11 1b ce 33 bb c2 32 1e cd f2";
    private static String filesDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/suota/fw_27.img";
    private Handler handler = new Handler(Looper.getMainLooper());
    private EditText editId, editKey, editValue;
    private TextView textLog;
    private StringBuilder logBuilder = new StringBuilder();
    private EasyPermissionHelper helper;
    private EditTextDialog connectDialog;
    private EditTextDialog otaDialog;
    private String tid = "";
    private TextView titleText;
    private Button buttonOta;
    private DateFormat format = new SimpleDateFormat("HH:mm:ss");
    TbitListener listener = new TbitListenerAdapter() {
        @Override
        public void onConnectResponse(int resultCode) {
            if (resultCode == 0)
                showLog("连接回应: 成功");
            else
            showLog("连接回应: " + resultCode);
        }

        @Override
        public void onUnlockResponse(int resultCode) {
            if (resultCode == 0)
                showLog("解锁回应: 成功");
            else
                showLog("解锁回应: " + resultCode);
        }

        @Override
        public void onLockResponse(int resultCode) {
            if (resultCode == 0)
                showLog("上锁回应: 成功");
            else
                showLog("上锁回应: " + resultCode);
        }

        @Override
        public void onUpdateResponse(int resultCode) {
            if (resultCode == 0)
                showLog("更新状态回应: 成功");
            else
                showLog("更新状态回应: " + resultCode);
        }

        @Override
        public void onStateUpdated(BikeState state) {
            Log.d(TAG, "最新状态: " + state.toString());
        }

        @Override
        public void onDisconnected(int resultCode) {
            showLog("请按连接");
        }

    };

    TbitDebugListener debugListener = new TbitDebugListener() {
        @Override
        public void onLogStrReceived(String logStr) {
            showLog(logStr);
        }
    };
    private Button autoLockButton, autoUnlockButton, autoUpdateButton, autoConnectButton;
    private View.OnClickListener facButtonListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        checkDateValidity();

        initView();
        helper = new EasyPermissionHelper(this);

        helper.checkPermissions(new PermissionListener() {
                                    @Override
                                    public void onAllGranted() {
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                prepareFile();
                                                showSetting();
                                                TbitBle.initialize(Main2Activity.this);
                                                TbitBle.setListener(listener);
                                                TbitBle.setDebugListener(debugListener);
                                            }
                                        });
                                    }

                                    @Override
                                    public void atLeastOneDenied(List<String> list, List<String> list1) {

                                    }
                                }, Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void checkDateValidity() {
        Calendar calendar = new GregorianCalendar(2017, 2, 31);

        long targetTime = calendar.getTimeInMillis();
        long curTime = System.currentTimeMillis();

        if (curTime > targetTime) {
            throw new RuntimeException("software expired");
        }
    }

    private void prepareFile() {
        File file = new File(filesDir);
        if (file.exists())
            return;
        AssetManager manager = getAssets();
        try {
            InputStream ins = manager.open("fw_27.img");
            String root = Environment.getExternalStorageDirectory().getAbsolutePath() + "/suota";
            file = new File(root);
            if (file == null || !file.exists()) {
                file.mkdir();
            }
            file = new File(filesDir);
            OutputStream os = new FileOutputStream(file);
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            ins.close();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    private void initView() {
        textLog = (TextView) findViewById(R.id.text_log);
        titleText = (TextView) findViewById(R.id.tv_title_tid);
        findViewById(R.id.image_pro).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(Main2Activity.this, MainActivity.class));
            }
        });
        buttonOta = (Button) findViewById(R.id.button_ota);
        buttonOta.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showOtaInputDialog();
                return true;
            }
        });

    }

    private void showSetting() {
        LocationManager locationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent intent = new Intent();
            intent.setAction(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            Main2Activity.this.startActivity(intent);
            Toast.makeText(Main2Activity.this, "请开启", Toast.LENGTH_LONG).show();
        }
    }

    public void connect(View view) {
        new IntentIntegrator(this)
                .setOrientationLocked(false)
                .setCaptureActivity(QRScannerActivity.class)
                .setBarcodeImageEnabled(true)
                .setBeepEnabled(true)
                .initiateScan();
    }

    public void unlock(View view) {
        showLog("解锁按下");
        TbitBle.unlock();
    }

    public void lock(View view) {
        showLog("上锁按下");
        TbitBle.lock();
    }

    public void common(View view) {
        TbitBle.commonCommand(new Command() {
            @Override
            protected Packet onCreateSendPacket(int sequenceId) {
                return PacketUtil.createPacket(sequenceId, (byte) 0x03, (byte) 0x02,
                        new Byte[]{0x00});
            }

            @Override
            public boolean compare(Packet receivedPacket) {
                return false;
            }
        });
    }

    public void reconnect(View view) {
        int state = TbitBle.getBleConnectionState();
        Log.d(TAG, "reconnect: " + state);
        if (state == 1 || state == 2 || state == 3) {
            showLog("连接回应： -1005");
            return;
        }
        showLog("重新连接按下");
        TbitBle.reconnect();
    }

    public void manualConnect(View view) {
        showInputDialog();
    }

    public void update(View view) {
        showLog("更新状态按下");
        TbitBle.update();
    }

    public void disconnect(View view) {
        TbitBle.disConnect();
    }

    public void reset(View view) {
        BluetoothAdapter.getDefaultAdapter().disable();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                BluetoothAdapter.getDefaultAdapter().enable();
            }
        }, 500);
    }

    public static final String DEVICE_NAME = "";


    public void scan(View view) {
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

        // 通过rssi值计算距离
        double distance = BikeUtil.calcDistByRSSI(-55);
    }

    public void ota(View view) {
        TbitBle.ota(new File(filesDir), new ResultCallback() {
            @Override
            public void onResult(int resultCode) {
                Log.d(TAG, "onOtaResponse: " + resultCode);
                showLog("onOtaResponse: " + resultCode);
            }
        }, new ProgressCallback() {
            @Override
            public void onProgress(int progress) {
                Log.d(TAG, "onOtaProgress: " + progress);
//                buttonOta.setText(String.valueOf(progress));
                showLog(progress+"%");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        helper.onDestroy();
    }

    @Override
    public void finish() {
        super.finish();
        TbitBle.destroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        helper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
        helper.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
            } else {
                String resultStr = result.getContents();
                Toast.makeText(this, "请稍等", Toast.LENGTH_LONG).show();
                connectInside(resultStr);
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void showLog(String str) {
        makeLog(str);
        textLog.setText(logBuilder.toString());
    }

    private void makeLog(String log) {
        logBuilder.insert(0, "\n\n")
                .insert(0, log)
                .insert(0, "\n")
                .insert(0, getTime());
    }


    private String getTime() {
        return format.format(new Date());
    }

    private void showInputDialog() {
        if (connectDialog == null) {
            connectDialog = new EditTextDialog();
            connectDialog.setTitle("设置设备编号")
                    .setInputType(InputType.TYPE_CLASS_NUMBER)
                    .setEditTextListener(new EditTextDialog.EditTextListener() {
                        @Override
                        public void onConfirm(String editString) {
                            connectInside(editString);
                            connectDialog.dismissAllowingStateLoss();
                        }

                        @Override
                        public void onCancel() {
                            connectDialog.dismissAllowingStateLoss();
                        }

                        @Override
                        public void onNeutral() {
                            connectDialog.dismissAllowingStateLoss();
                        }
                    })
                    .setCancelable(false);
        }

        connectDialog.show(getSupportFragmentManager(), null);
    }

    private void showOtaInputDialog() {
        if (otaDialog == null) {
            otaDialog = new EditTextDialog();
            otaDialog.setTitle("OTA-升级：设置设备编号")
                    .setInputType(InputType.TYPE_CLASS_NUMBER)
                    .setEditTextListener(new EditTextDialog.EditTextListener() {
                        @Override
                        public void onConfirm(String editString) {
                            otaConnectInside(editString);
                            otaDialog.dismissAllowingStateLoss();
                        }

                        @Override
                        public void onCancel() {
                            otaDialog.dismissAllowingStateLoss();
                        }

                        @Override
                        public void onNeutral() {
                            otaDialog.dismissAllowingStateLoss();
                        }
                    })
                    .setCancelable(false);
        }

        otaDialog.show(getSupportFragmentManager(), null);
    }

    private void connectInside(String deviceId) {
        this.tid = deviceId;
        titleText.setText(String.valueOf(tid));

        showLog("连接开始 : " + deviceId);
        String key = AesTool.Genkey(deviceId);
//        String key = "";
        if (TextUtils.isEmpty(key))
            key = KEY;
        TbitBle.connect(deviceId, key);
    }

    private void otaConnectInside(String deviceId) {
//        this.tid = deviceId;
        titleText.setText(String.valueOf(deviceId));

        showLog("OTA连接开始 : " + deviceId);
        String key = AesTool.Genkey("[WA-205_BLE_OTA]",deviceId);
//        String key = "";
        key = key.substring(0, 48);
        TbitBle.connectiveOta(deviceId, key, new File(filesDir), new ResultCallback() {
            @Override
            public void onResult(int resultCode) {
                Log.d(TAG, "onOtaResponse: " + resultCode);
                showLog("onOtaResponse: " + resultCode);
            }
        }, new ProgressCallback() {
            @Override
            public void onProgress(int progress) {
                Log.d(TAG, "onOtaProgress: " + progress);
//                buttonOta.setText(String.valueOf(progress));
                showLog(progress+"%");
            }
        });
    }

    public enum Action {
        CONNECT, LOCK, UNLOCK, UPDATE
    }

    private void dispatchAction(Action action) {
        switch (action) {
            case CONNECT:
                TbitBle.reconnect();
                break;
            case LOCK:
                TbitBle.lock();
                break;
            case UNLOCK:
                TbitBle.unlock();
                break;
            case UPDATE:
                TbitBle.update();
                break;
        }

        if (TbitBle.hasInitialized()) {
            TbitBle.setListener(null);
        }
    }

//    class AutoTask extends AsyncTask<Void, Void, Void> {
//        private Action action;
//
//        public AutoTask(Action action) {
//            this.action = action;
//        }
//
//        @Override
//        protected Void doInBackground(Void... voids) {
//            while (!isCancelled()) {
//                SystemClock.sleep(60 * 1000);
//                publishProgress();
//            }
//            return null;
//        }
//
//        @Override
//        protected void onProgressUpdate(Void... values) {
//           dispatchAction(action);
//        }
//    }
}
