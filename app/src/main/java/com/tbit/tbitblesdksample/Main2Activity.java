package com.tbit.tbitblesdksample;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.tbit.tbitblesdk.Bike.TbitBle;
import com.tbit.tbitblesdk.Bike.TbitDebugListener;
import com.tbit.tbitblesdk.Bike.model.BikeState;
import com.tbit.tbitblesdk.Bike.services.command.callback.SimpleCommonCallback;
import com.tbit.tbitblesdk.Bike.services.command.callback.StateCallback;
import com.tbit.tbitblesdk.Bike.util.BikeUtil;
import com.tbit.tbitblesdk.bluetooth.scanner.ScanBuilder;
import com.tbit.tbitblesdk.bluetooth.scanner.ScannerCallback;
import com.tbit.tbitblesdk.bluetooth.scanner.decorator.FilterNameCallback;
import com.tbit.tbitblesdk.bluetooth.scanner.decorator.LogCallback;
import com.tbit.tbitblesdk.bluetooth.scanner.decorator.NoneRepeatCallback;
import com.tbit.tbitblesdk.protocol.callback.ProgressCallback;
import com.tbit.tbitblesdk.protocol.callback.ResultCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import me.salmonzhg.easypermission.EasyPermissionHelper;
import me.salmonzhg.easypermission.PermissionListener;

import static com.tbit.tbitblesdksample.Operation.OPERATION_BATTERY_LOCK;
import static com.tbit.tbitblesdksample.Operation.OPERATION_BATTERY_UNLOCK;
import static com.tbit.tbitblesdksample.Operation.OPERATION_CONNECT;
import static com.tbit.tbitblesdksample.Operation.OPERATION_CONNECT_OTA;
import static com.tbit.tbitblesdksample.Operation.OPERATION_DISCONNECT;
import static com.tbit.tbitblesdksample.Operation.OPERATION_FIND_OFF;
import static com.tbit.tbitblesdksample.Operation.OPERATION_FIND_ON;
import static com.tbit.tbitblesdksample.Operation.OPERATION_LOCK;
import static com.tbit.tbitblesdksample.Operation.OPERATION_OTA;
import static com.tbit.tbitblesdksample.Operation.OPERATION_SEARCH;
import static com.tbit.tbitblesdksample.Operation.OPERATION_SET_DEFENCE;
import static com.tbit.tbitblesdksample.Operation.OPERATION_UNLOCK;
import static com.tbit.tbitblesdksample.Operation.OPERATION_UNSET_DEFENCE;
import static com.tbit.tbitblesdksample.Operation.OPERATION_UPDATE;

public class Main2Activity extends AppCompatActivity {

    public static final String DEVICE_NAME = "";
    private static final String TAG = "MainActivity";
    private Handler handler = new Handler(Looper.getMainLooper());
    private TextView textLog;
    private StringBuilder logBuilder = new StringBuilder();
    private StringBuilder detailLogBuilder = new StringBuilder();
    private EasyPermissionHelper helper;
    private EditTextDialog connectDialog;
    private EditTextDialog otaDialog;
    private String tid = "";
    private DateFormat format = new SimpleDateFormat("HH:mm:ss");
    TbitDebugListener debugListener = new TbitDebugListener() {
        @Override
        public void onLogStrReceived(String logStr) {
            Log.d(TAG, logStr);
            detailLogBuilder.insert(0, "\n\n")
                    .insert(0, logStr)
                    .insert(0, "\n")
                    .insert(0, getTime());
        }
    };
    private RecyclerView operationRecycler;
    private List<Operation> operationList;
    private OperationAdapter operationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        initOperations();

        initView();

        helper = new EasyPermissionHelper(this);

        helper.checkPermissions(new PermissionListener() {
                                    @Override
                                    public void onAllGranted() {
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                showSetting();
                                                TbitBle.initialize(Main2Activity.this, new MyProtocolAdapter());
                                                TbitBle.setDebugListener(debugListener);
                                            }
                                        });
                                    }

                                    @Override
                                    public void atLeastOneDenied(List<String> list, List<String> list1) {
                                        Toast.makeText(Main2Activity.this, "请同意全部权限", Toast.LENGTH_SHORT)
                                                .show();
                                        finish();
                                    }
                                }, Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save_log:
                new SaveLogTask(detailLogBuilder.toString()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                detailLogBuilder = new StringBuilder();
                break;
            case R.id.action_cycleTest:
                startActivity(new Intent(this, CycleTestActivity.class));
                break;
        }
        return true;
    }

    private void initOperations() {
        operationList = new ArrayList<>();

        operationList.add(new Operation("连接", 0));
        operationList.add(new Operation("解锁", 1));
        operationList.add(new Operation("上锁", 2));
        operationList.add(new Operation("更新", 3));
        operationList.add(new Operation("设防", 4));
        operationList.add(new Operation("撤防", 5));
        operationList.add(new Operation("OTA", 6));
        operationList.add(new Operation("开电池锁", 7));
        operationList.add(new Operation("关电池锁", 8));
        operationList.add(new Operation("工厂OTA", 9));
        operationList.add(new Operation("寻车开", 10));
        operationList.add(new Operation("寻车关", 11));
        operationList.add(new Operation("断开", 12));
//        operationList.add(new Operation("搜索", 13));
    }

    private void initView() {
        textLog = (TextView) findViewById(R.id.text_log);

        operationRecycler = (RecyclerView) findViewById(R.id.recycler_operations);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);

        operationAdapter = new OperationAdapter(operationList, this);

        operationAdapter.setOprationListener(new OperationAdapter.OperationListener() {
            @Override
            public void onOperationClick(int operationCode) {
                dispatchOperation(operationCode);
            }
        });

        operationRecycler.setLayoutManager(layoutManager);

        operationRecycler.setAdapter(operationAdapter);
    }

    private void dispatchOperation(int operationCode) {
        switch (operationCode) {
            case OPERATION_CONNECT:
                showInputDialog();
                break;
            case OPERATION_UNLOCK:
                showLog("解锁按下");
                TbitBle.unlock(new ResultCallback() {
                    @Override
                    public void onResult(int resultCode) {
                        if (resultCode == 0)
                            showLog("解锁回应: 成功");
                        else
                            showLog("解锁回应: " + resultCode);
                    }
                });
                break;
            case OPERATION_LOCK:
                showLog("上锁按下");
                TbitBle.lock(new ResultCallback() {
                    @Override
                    public void onResult(int resultCode) {
                        if (resultCode == 0)
                            showLog("上锁回应: 成功");
                        else
                            showLog("上锁回应: " + resultCode);
                    }
                });
                break;
            case OPERATION_UPDATE:
                showLog("更新状态按下");
                TbitBle.update(new ResultCallback() {
                    @Override
                    public void onResult(int resultCode) {
                        if (resultCode == 0)
                            showLog("更新状态回应: 成功");
                        else
                            showLog("更新状态回应: " + resultCode);
                    }
                }, new StateCallback() {
                    @Override
                    public void onStateUpdated(BikeState bikeState) {
                        showLog("最新状态: " + bikeState.toString());
                    }
                });
                break;
            case OPERATION_SET_DEFENCE:
                showLog("通用-设防按下");
                TbitBle.commonCommand((byte) 0x03, (byte) 0x01, new Byte[]{0x01},
                        new SimpleCommonCallback(new ResultCallback() {
                            @Override
                            public void onResult(int resultCode) {
                                showLog("设防回应： " + resultCode);
                            }
                        }));
                break;
            case OPERATION_UNSET_DEFENCE:
                showLog("通用-撤防按下");
                TbitBle.commonCommand((byte) 0x03, (byte) 0x01, new Byte[]{0x00},
                        new SimpleCommonCallback(new ResultCallback() {
                            @Override
                            public void onResult(int resultCode) {
                                showLog("撤防回应： " + resultCode);
                            }
                        }));
                break;
            case OPERATION_OTA:

//                TbitBle.ota(new File(""), new ResultCallback() {
//                    @Override
//                    public void onResult(int resultCode) {
//                        Log.d(TAG, "onOtaResponse: " + resultCode);
//                        showLog("onOtaResponse: " + resultCode);
//                    }
//                }, new ProgressCallback() {
//                    @Override
//                    public void onProgress(int progress) {
//                        Log.d(TAG, "onOtaProgress: " + progress);
//                        showLog(progress+"%");
//                    }
//                });
                break;
            case OPERATION_BATTERY_UNLOCK:
                showLog("通用-开电池锁按下");
                TbitBle.commonCommand((byte) 0x03, (byte) 0x05, new Byte[]{0x01},
                        new SimpleCommonCallback(new ResultCallback() {
                            @Override
                            public void onResult(int resultCode) {
                                showLog("开电池锁回应： " + resultCode);
                            }
                        }));
                break;
            case OPERATION_BATTERY_LOCK:
                showLog("通用-关电池锁按下");
                TbitBle.commonCommand((byte) 0x03, (byte) 0x05, new Byte[]{0x00},
                        new SimpleCommonCallback(new ResultCallback() {
                            @Override
                            public void onResult(int resultCode) {
                                showLog("关电池锁回应： " + resultCode);
                            }
                        }));
                break;
            case OPERATION_CONNECT_OTA:
                showOtaInputDialog();
                break;
            case OPERATION_FIND_ON:
                showLog("通用-开寻车按下");
                TbitBle.commonCommand((byte) 0x03, (byte) 0x04, new Byte[]{0x01},
                        new SimpleCommonCallback(new ResultCallback() {
                            @Override
                            public void onResult(int resultCode) {
                                showLog("开寻车回应： " + resultCode);
                            }
                        }));
                break;
            case OPERATION_FIND_OFF:
                showLog("通用-关寻车按下");
                TbitBle.commonCommand((byte) 0x03, (byte) 0x04, new Byte[]{0x00},
                        new SimpleCommonCallback(new ResultCallback() {
                            @Override
                            public void onResult(int resultCode) {
                                showLog("关寻车回应： " + resultCode);
                            }
                        }));
                break;
            case OPERATION_DISCONNECT:
                showLog("断开按下");
                TbitBle.disConnect();
                break;
            case OPERATION_SEARCH:

                break;
        }
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

    public void reset(View view) {
        BluetoothAdapter.getDefaultAdapter().disable();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                BluetoothAdapter.getDefaultAdapter().enable();
            }
        }, 500);
    }

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
                    showLog("扫描到设备: " + bluetoothDevice.getAddress() + " | " + machineId);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        helper.onDestroy();
    }

    @Override
    public void finish() {
        super.finish();
        if (TbitBle.hasInitialized())
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

    private void showLog(String str) {
        makeLog(str);
        textLog.setText(logBuilder.toString());
    }

    private void makeLog(String log) {
        logBuilder.insert(0, "\n\n")
                .insert(0, log)
                .insert(0, "\n")
                .insert(0, getTime());
        detailLogBuilder.insert(0, "\n\n")
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
        getSupportActionBar().setTitle(String.valueOf(tid));

        showLog("连接开始 : " + deviceId);
        TbitBle.connect(deviceId, "", new ResultCallback() {
            @Override
            public void onResult(int resultCode) {
                if (resultCode == 0)
                    showLog("连接回应: 成功");
                else
                    showLog("连接回应: " + resultCode);
            }
        }, new StateCallback() {
            @Override
            public void onStateUpdated(BikeState bikeState) {
                showLog("状态更新: " + bikeState.toString());
            }
        });
    }

    private void otaConnectInside(String deviceId) {
        getSupportActionBar().setTitle(String.valueOf(tid));

        showLog("OTA连接开始 : " + deviceId);
        TbitBle.connectiveOta(deviceId, "", new File(""), new ResultCallback() {
            @Override
            public void onResult(int resultCode) {
                showLog("onOtaResponse: " + resultCode);
            }
        }, new ProgressCallback() {
            @Override
            public void onProgress(int progress) {
                Log.d(TAG, "onOtaProgress: " + progress);
                showLog(progress + "%");
            }
        });
    }

}
