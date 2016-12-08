package com.tbit.tbitblesdksample;

import android.Manifest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.tbit.tbitblesdk.protocol.BikeState;
import com.tbit.tbitblesdk.TbitBle;
import com.tbit.tbitblesdk.TbitListener;
import com.tbit.tbitblesdk.util.ByteUtil;

import java.util.List;

import me.salmonzhg.easypermission.EasyPermission;
import me.salmonzhg.easypermission.PermissionListener;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "asd";
    private static final String KEY = "d6 15 61 bc 02 4e 33 70 b1 7b 57 24 60 83 25 81 02 7d b3 56 ab e6 11 1b ce 33 bb c2 32 1e cd f2";
    TbitListener listener = new TbitListener() {
        @Override
        public void onConnectResponse(int resultCode, BikeState state) {
            Log.d(TAG, "onConnectResponse: " + resultCode);
        }

        @Override
        public void onVerifyResponse(int resultCode, BikeState state) {
            Log.d(TAG, "onVerifyResponse: " + resultCode);
        }

        @Override
        public void onUnlockResponse(int resultCode, BikeState state) {
            Log.d(TAG, "onUnlockResponse: " + resultCode);
        }

        @Override
        public void onLockResponse(int resultCode, BikeState state) {
            Log.d(TAG, "onLockResponse: " + resultCode);
        }

        @Override
        public void onStateUpdated(int resultCode, BikeState state) {
            Log.d(TAG, "onStateUpdated: " + resultCode);
        }

        @Override
        public void onDisconnected(int resultCode, BikeState state) {
            Log.d(TAG, "onDisconnected: " + resultCode);
        }

        @Override
        public void onCommonCommandResponse(int resultCode, BikeState state) {
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
                                                    }
                                                });
                                            }

                                            @Override
                                            public void atLeastOneDenied(List<String> list, List<String> list1) {

                                            }
                                        }, Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    private void testBle() {
        TbitBle.initialize(this);
        TbitBle.setListener(listener);
        TbitBle.connect("EE:AA:FF:AA:AA:FF");
    }

    public void connect(View view) {
        testBle();
    }

    public void verify(View view) {
        TbitBle.verify(KEY);
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
}
