package com.tbit.tbitblesdksample;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
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
import com.tbit.tbitblesdk.OtaListener;
import com.tbit.tbitblesdk.TbitBle;
import com.tbit.tbitblesdk.TbitDebugListener;
import com.tbit.tbitblesdk.TbitListener;
import com.tbit.tbitblesdk.TbitListenerAdapter;
import com.tbit.tbitblesdk.protocol.BikeState;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import me.salmonzhg.easypermission.EasyPermissionHelper;
import me.salmonzhg.easypermission.PermissionListener;

public class Main2Activity extends AppCompatActivity {

    // 022009020
    private static final String TAG = "MainActivity";
    private static final String KEY = "d6 15 61 bc 02 4e 33 70 b1 7b 57 24 60 83 25 81 02 7d b3 56 ab e6 11 1b ce 33 bb c2 32 1e cd f2";
    private static String filesDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/suota/fw_3.img";
    private Handler handler = new Handler(Looper.getMainLooper());
    private EditText editId, editKey, editValue;
    private TextView textLog;
    private StringBuilder logBuilder = new StringBuilder();
    private EasyPermissionHelper helper;
    private EditTextDialog editTextDialog;
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

        initView();
        helper = new EasyPermissionHelper(this);

        helper.checkPermissions(new PermissionListener() {
                                    @Override
                                    public void onAllGranted() {
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
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
                Manifest.permission.READ_EXTERNAL_STORAGE);
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

    public void ota(View view) {
        TbitBle.ota(new File(filesDir), new OtaListener() {
            @Override
            public void onOtaResponse(int code) {
                Log.d(TAG, "onOtaResponse: " + code);
                showLog("onOtaResponse: " + code);
            }

            @Override
            public void onOtaProgress(int progress) {
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
        this.tid = deviceId;
        titleText.setText(String.valueOf(tid));

        showLog("连接开始 : " + deviceId);
//        String key = AesTool.Genkey(deviceId);
        String key = "";
        if (TextUtils.isEmpty(key))
            key = KEY;
        TbitBle.connect(deviceId, key);
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
