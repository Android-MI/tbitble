package com.tbit.tbitblesdksample;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.tbit.tbitblesdk.TbitBle;
import com.tbit.tbitblesdk.TbitDebugListener;
import com.tbit.tbitblesdk.TbitListener;
import com.tbit.tbitblesdk.protocol.BikeState;
import com.tbit.tbitblesdk.protocol.Packet;
import com.tbit.tbitblesdk.protocol.PacketValue;
import com.tbit.tbitblesdk.services.command.Command;
import com.tbit.tbitblesdk.services.command.callback.PacketCallback;
import com.tbit.tbitblesdk.services.command.callback.ResultCallback;
import com.tbit.tbitblesdksample.aes.AesTool;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import me.salmonzhg.easypermission.EasyPermissionHelper;
import me.salmonzhg.easypermission.PermissionListener;

public class MainActivity extends AppCompatActivity {

    // 022009020
    private static final String TAG = "MainActivity";
    private static final String KEY = "d6 15 61 bc 02 4e 33 70 b1 7b 57 24 60 83 25 81 02 7d b3 56 ab e6 11 1b ce 33 bb c2 32 1e cd f2";
    private Handler handler = new Handler(Looper.getMainLooper());
    private EditText editId, editKey, editValue;
    private TextView textLog;
    private StringBuilder logBuilder = new StringBuilder();
    private EasyPermissionHelper helper;
    private EditTextDialog editTextDialog;
    private DateFormat format = new SimpleDateFormat("HH:mm:ss");
    TbitListener listener = new TbitListener() {
        @Override
        public void onConnectResponse(int resultCode) {
            showLog("onConnectResponse: " + resultCode);
        }

        @Override
        public void onUnlockResponse(int resultCode) {
            showLog("onUnlockResponse: " + resultCode);
        }

        @Override
        public void onLockResponse(int resultCode) {
            showLog("onLockResponse: " + resultCode);
        }

        @Override
        public void onUpdateResponse(int resultCode) {
            showLog("onUpdateResponse: " + resultCode);
        }

        @Override
        public void onStateUpdated(BikeState state) {
            showLog("onStateUpdated: " + state.toString());
            //也可以这样
            Log.d(TAG, "onStateUpdated: " + TbitBle.getState());
        }

        @Override
        public void onDisconnected(int resultCode) {
//            showLog("onDisconnected: " + resultCode);
        }

        @Override
        public void onCommonCommandResponse(int resultCode, PacketValue packetValue) {

        }

    };

    TbitDebugListener debugListener = new TbitDebugListener() {
        @Override
        public void onLogStrReceived(String logStr) {
            showLog(logStr);
        }
    };
    private View facView, originView;
    private CheckBox facCheckBox;
    private Button autoLockButton, autoUnlockButton, autoUpdateButton, autoConnectButton;
    private View.OnClickListener facButtonListener;
    private AutoTask autoTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        helper = new EasyPermissionHelper(this);

        helper.checkPermissions(new PermissionListener() {
                                    @Override
                                    public void onAllGranted() {
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                showSetting();
                                                TbitBle.initialize(MainActivity.this, new MyProtocolAdapter());
                                                TbitBle.setListener(listener);
                                                TbitBle.setDebugListener(debugListener);
                                            }
                                        });
                                    }

                                    @Override
                                    public void atLeastOneDenied(List<String> list, List<String> list1) {

                                    }
                                }, Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CAMERA);
    }

    private void initView() {
        originView = findViewById(R.id.linear_origin);
        textLog = (TextView) findViewById(R.id.text_log);
        facCheckBox = (CheckBox) findViewById(R.id.checkbox_fac);
//        editId = (EditText) findViewById(R.id.edit_id);
//        editKey = (EditText) findViewById(R.id.edit_key);
//        editValue = (EditText) findViewById(R.id.edit_value);

        facCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                    showFac();
                else
                    hideFac();
            }
        });
    }

    private void showSetting() {
        LocationManager locationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent intent = new Intent();
            intent.setAction(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            MainActivity.this.startActivity(intent);
            Toast.makeText(MainActivity.this, "请开启", Toast.LENGTH_LONG).show();
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
        showLog("开锁按下");
        TbitBle.commonCommand((byte) 0x03, (byte) 0x02, new Byte[]{0x00},
                new ResultCallback() {
                    @Override
                    public void onResult(int resultCode) {
                        showLog("开锁回应： " + resultCode);
                    }
                }, new PacketCallback() {
                    @Override
                    public void onPacketReceived(Packet packet) {

                    }
                });
    }

    public void lock(View view) {
        showLog("关锁按下");
        TbitBle.commonCommand((byte)0x03, (byte)0x02, new Byte[]{0x01},
                new ResultCallback() {
                    @Override
                    public void onResult(int resultCode) {
                        showLog("关锁回应： " + resultCode);
                    }
                }, new PacketCallback() {
                    @Override
                    public void onPacketReceived(Packet packet) {

                    }
                });

    }

    public void common(View view) {
    }

    public void powerUnlock(View view) {
        showLog("开电池锁按下");
        TbitBle.commonCommand((byte)0x03, (byte)0x05, new Byte[]{0x01},
                new ResultCallback() {
                    @Override
                    public void onResult(int resultCode) {
                        showLog("开电池锁回应： " + resultCode);
                    }
                }, new PacketCallback() {
                    @Override
                    public void onPacketReceived(Packet packet) {

                    }
                });
    }

    public void powerLock(View view) {
        showLog("关电池锁按下");
        TbitBle.commonCommand((byte)0x03, (byte)0x05, new Byte[]{0x00},
                new ResultCallback() {
                    @Override
                    public void onResult(int resultCode) {
                        showLog("关电池锁回应： " + resultCode);
                    }
                }, new PacketCallback() {
                    @Override
                    public void onPacketReceived(Packet packet) {

                    }
                });
    }

    public void setDefence(View view) {
        showLog("设防按下");
        TbitBle.commonCommand((byte)0x03, (byte)0x01, new Byte[]{0x01},
                new ResultCallback() {
                    @Override
                    public void onResult(int resultCode) {
                        showLog("设防回应： " + resultCode);
                    }
                }, new PacketCallback() {
                    @Override
                    public void onPacketReceived(Packet packet) {

                    }
                });
    }

    public void unSetDefence(View view) {
        showLog("撤防按下");
        TbitBle.commonCommand((byte)0x03, (byte)0x01, new Byte[]{0x00},
                new ResultCallback() {
                    @Override
                    public void onResult(int resultCode) {
                        showLog("开锁回应： " + resultCode);
                    }
                }, new PacketCallback() {
                    @Override
                    public void onPacketReceived(Packet packet) {

                    }
                });
    }

    public void findCarOn(View view) {
        showLog("开一键寻车按下");
        TbitBle.commonCommand((byte)0x03, (byte)0x04, new Byte[]{0x01},
                new ResultCallback() {
                    @Override
                    public void onResult(int resultCode) {
                        showLog("开寻车回应： " + resultCode);
                    }
                }, new PacketCallback() {
                    @Override
                    public void onPacketReceived(Packet packet) {

                    }
                });
    }

    public void findCarOff(View view) {
        showLog("关一键寻车按下");
        TbitBle.commonCommand((byte)0x03, (byte)0x04, new Byte[]{0x00},
                new ResultCallback() {
                    @Override
                    public void onResult(int resultCode) {
                        showLog("关寻车回应： " + resultCode);
                    }
                }, new PacketCallback() {
                    @Override
                    public void onPacketReceived(Packet packet) {

                    }
                });
    }

    public void reconnect(View view) {
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

    public void switchFac(View view) {
        facCheckBox.setChecked(!facCheckBox.isChecked());
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        helper.onDestroy();
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

    private void showFac() {
        originView.setVisibility(View.GONE);
        if (facView == null) {
            ViewStub stub = (ViewStub) findViewById(R.id.stub_fac_mode);
            facView = stub.inflate();
            autoConnectButton = (Button) facView.findViewById(R.id.button_auto_connect);
            autoLockButton = (Button) facView.findViewById(R.id.button_auto_lock);
            autoUnlockButton = (Button) facView.findViewById(R.id.button_auto_unlock);
            autoUpdateButton = (Button) facView.findViewById(R.id.button_auto_update);
            initFacButtonListener();
            autoConnectButton.setOnClickListener(facButtonListener);
            autoLockButton.setOnClickListener(facButtonListener);
            autoUnlockButton.setOnClickListener(facButtonListener);
            autoUpdateButton.setOnClickListener(facButtonListener);
        } else {
            facView.setVisibility(View.VISIBLE);
        }
    }

    private void hideFac() {
        if (autoTask != null)
            autoTask.cancel(false);
        facView.setVisibility(View.GONE);
        originView.setVisibility(View.VISIBLE);
    }

    private void initFacButtonListener() {
        facButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (autoTask != null)
                    autoTask.cancel(false);
                switch (view.getId()) {
                    case R.id.button_auto_connect:
                        autoTask = new AutoTask(Action.CONNECT);
                        break;
                    case R.id.button_auto_lock:
                        autoTask = new AutoTask(Action.LOCK);
                        break;
                    case R.id.button_auto_unlock:
                        autoTask = new AutoTask(Action.UNLOCK);
                        break;
                    case R.id.button_auto_update:
                        autoTask = new AutoTask(Action.UPDATE);
                        break;
                }
                autoTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        };
    }

    private String getTime() {
        return format.format(new Date());
    }

    private void showInputDialog() {
        if (editValue == null) {
            editTextDialog = new EditTextDialog();
            editTextDialog.setTitle("设置设备编号")
                    .setInputType(InputType.TYPE_CLASS_NUMBER)
                    .setEditTextListener(new EditTextDialog.EditTextListener() {
                        @Override
                        public void onConfirm(String editString) {
                            connectInside(editString);
                            editTextDialog.dismissAllowingStateLoss();
                        }

                        @Override
                        public void onCancel() {
                            editTextDialog.dismissAllowingStateLoss();
                        }

                        @Override
                        public void onNeutral() {
                            editTextDialog.dismissAllowingStateLoss();
                        }
                    })
                    .setCancelable(false);
        }

        editTextDialog.show(getSupportFragmentManager(), null);
    }

    private void connectInside(String deviceId) {
        showLog("连接开始 : " + deviceId);
        String key = "";
        key = AesTool.Genkey(deviceId);
        if (TextUtils.isEmpty(key))
            key = KEY;
        TbitBle.connect(deviceId, key);
    }

    public enum Action {
        CONNECT, LOCK, UNLOCK, UPDATE
    }

    class AutoTask extends AsyncTask<Void, Void, Void> {
        private Action action;

        public AutoTask(Action action) {
            this.action = action;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            while (!isCancelled()) {
                publishProgress();

                SystemClock.sleep(10 * 1000);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
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
        }
    }
}
