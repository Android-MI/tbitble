package com.tbit.tbitblesdksample;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.tbit.tbitblesdk.TbitBle;
import com.tbit.tbitblesdk.TbitListener;
import com.tbit.tbitblesdk.protocol.BikeState;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.salmonzhg.easypermission.EasyPermissionHelper;
import me.salmonzhg.easypermission.PermissionListener;

public class MainActivity extends AppCompatActivity {

    // 022009020
    private static final String TAG = "MainActivity";
    private static final String KEY = "d6 15 61 bc 02 4e 33 70 b1 7b 57 24 60 83 25 81 02 7d b3 56 ab e6 11 1b ce 33 bb c2 32 1e cd f2";
    private Map<String, String> keyMap = new HashMap<>();
    private Handler handler = new Handler(Looper.getMainLooper());
    private EditText editId, editKey, editValue;
    private TextView textLog;
    private String logStr;
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
            //Log.d(TAG, "onStateUpdated: " + TbitBle.getState());
        }

        @Override
        public void onDisconnected(int resultCode) {
            showLog("onDisconnected: " + resultCode);
        }

        @Override
        public void onCommonCommandResponse(int resultCode) {
            Log.d(TAG, "onCommonCommandResponse: " + resultCode);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textLog = (TextView) findViewById(R.id.text_log);
//        editId = (EditText) findViewById(R.id.edit_id);
//        editKey = (EditText) findViewById(R.id.edit_key);
//        editValue = (EditText) findViewById(R.id.edit_value);


        logStr = "";
        initMapData();
        helper = new EasyPermissionHelper(this);

        helper.checkPermissions(new PermissionListener() {
                                    @Override
                                    public void onAllGranted() {
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                showSetting();
                                                TbitBle.initialize(MainActivity.this);
                                                TbitBle.setListener(listener);
                                            }
                                        });
                                    }

                                    @Override
                                    public void atLeastOneDenied(List<String> list, List<String> list1) {

                                    }
                                }, Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CAMERA);
    }

    private void initMapData() {
        keyMap.put("022009029", "90 EB 28 6C 76 92 AB 11 BD C6 D8 A5 C9 4C 08 19 02 7D B3 56 AB E6 11 1B CE 33 BB C2 32 1E CD F2");
        keyMap.put("022009028", "60 DA 69 AD B8 43 F5 56 69 48 C0 33 86 71 B2 6F 02 7D B3 56 AB E6 11 1B CE 33 BB C2 32 1E CD F2");
        keyMap.put("0229027", "D0 EA 52 28 C9 C9 31 D3 2D AA FF CA 96 09 55 31 02 7D B3 56 AB E6 11 1B CE 33 BB C2 32 1E CD F2");
        keyMap.put("022009026", "59 65 83 A5 0E C4 23 F7 40 39 E6 CA EA 40 82 4E 02 7D B3 56 AB E6 11 1B CE 33 BB C2 32 1E CD F2");
        keyMap.put("022009025", "A3 D1 80 97 93 7C 3E 15 A5 55 22 10 0D 4A 31 B6 02 7D B3 56 AB E6 11 1B CE 33 BB C2 32 1E CD F2");
        keyMap.put("135790246", "D6 15 61 BC 02 4E 33 70 B1 7B 57 24 60 83 25 81 02 7D B3 56 AB E6 11 1B CE 33 BB C2 32 1E CD F2");
        keyMap.put("022009021", "18 1B E3 4F BE 0B A3 39 06 4D 6B 9A 8E 73 F8 46 02 7D B3 56 AB E6 11 1B CE 33 BB C2 32 1E CD F2");
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
        showLog("解锁按下");
        TbitBle.unlock();
    }

    public void lock(View view) {
        showLog("上锁按下");
        TbitBle.lock();
    }

    public void common(View view) {
//        String idStr = editId.getText().toString();
//        String keyStr = editKey.getText().toString();
//        String valueStr = editValue.getText().toString();
//        if (TextUtils.isEmpty(idStr) || TextUtils.isEmpty(keyStr) ||
//                TextUtils.isEmpty(valueStr))
//            return;
//        byte id;
//        byte key;
//        Byte[] value;
//        try {
//            id = (byte) Integer.parseInt(idStr, 16);
//            key = (byte) Integer.parseInt(keyStr, 16);
//            value = ByteUtil.stringToBytes(valueStr);
//        } catch (NumberFormatException e) {
//            e.printStackTrace();
//            return;
//        }
//        TbitBle.commonCommand(id, key, value);
    }

    public void reconnect(View view) {
        showLog("更新状态按下");
        TbitBle.update();
    }

    public void update(View view) {
//        TbitBle.update()
//        TbitBle.disConnect();
        showInputDialog();
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
        textLog.setText(logStr);
    }

    private void makeLog(String log) {
        logStr = getTime() + "\n" + log + "\n\n" + logStr;
    }

    private String getTime() {
        return format.format(new Date());
    }

    private void showInputDialog() {
        if (editValue == null) {
            editTextDialog = new EditTextDialog();
            editTextDialog.setTitle("设置设备编号");
            editTextDialog.setInputType(InputType.TYPE_CLASS_NUMBER);
            editTextDialog.setEditTextListener(new EditTextDialog.EditTextListener() {
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
            });
            editTextDialog.setCancelable(false);
        }

        editTextDialog.show(getSupportFragmentManager(), null);
    }

    private void connectInside(String deviceId) {
        showLog("连接开始");
        String key = keyMap.get(deviceId);
        if (TextUtils.isEmpty(key))
            key = KEY;
        TbitBle.connect(deviceId, key);
    }
}
