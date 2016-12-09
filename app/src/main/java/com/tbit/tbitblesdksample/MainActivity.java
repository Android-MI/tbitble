package com.tbit.tbitblesdksample;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.tbit.tbitblesdk.TbitBle;
import com.tbit.tbitblesdk.TbitListener;
import com.tbit.tbitblesdk.protocol.BikeState;

import java.util.List;

import me.salmonzhg.easypermission.EasyPermission;
import me.salmonzhg.easypermission.PermissionListener;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String KEY = "d6 15 61 bc 02 4e 33 70 b1 7b 57 24 60 83 25 81 02 7d b3 56 ab e6 11 1b ce 33 bb c2 32 1e cd f2";
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

            //也可以这样
            //Log.d(TAG, "onStateUpdated: " + TbitBle.getState());
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
    private Handler handler = new Handler(Looper.getMainLooper());

    private EditText editId, editKey, editValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        editId = (EditText) findViewById(R.id.edit_id);
//        editKey = (EditText) findViewById(R.id.edit_key);
//        editValue = (EditText) findViewById(R.id.edit_value);

        EasyPermission.initialize(this);
        EasyPermission.checkPermissions(new PermissionListener() {
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
                Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    private void showSetting() {
        Intent intent = new Intent();
        intent.setAction(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        MainActivity.this.startActivity(intent);
        Toast.makeText(MainActivity.this, "请开启", Toast.LENGTH_LONG).show();
    }

    public void connect(View view) {
        TbitBle.connect("EE:AA:FF:AA:AA:FF", KEY);
    }

    public void unlock(View view) {
        TbitBle.unlock();
    }

    public void lock(View view) {
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
        TbitBle.reconnect();
    }

    public void update(View view) {
        TbitBle.update();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TbitBle.destroy();
    }
}
