package com.tbit.tbitblesdk;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.tbit.tbitblesdk.protocol.BluEvent;
import com.tbit.tbitblesdk.protocol.Constant;
import com.tbit.tbitblesdk.services.OtaConnector;
import com.tbit.tbitblesdk.protocol.OtaFile;
import com.tbit.tbitblesdk.protocol.ResultCode;
import com.tbit.tbitblesdk.services.BikeBleConnector;
import com.tbit.tbitblesdk.protocol.BikeState;
import com.tbit.tbitblesdk.services.BluetoothIO;
import com.tbit.tbitblesdk.services.scanner.BikeScanHelper;
import com.tbit.tbitblesdk.services.scanner.ScanHelper;
import com.tbit.tbitblesdk.services.scanner.ScannerCallback;
import com.tbit.tbitblesdk.util.ByteUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Created by Salmon on 2016/12/5 0005.
 */

class TbitBleInstance {
    private static final String TAG = "TbitBleInstance";
    private Context context;
    private Byte[] key;
    private String macAddr;
    private TbitListener listener;
    private OtaListener otaListener;
    private TbitDebugListener debugListener;
    private BluetoothIO bluetoothIO;
    private BikeBleConnector bikeBleConnector;
    private OtaConnector otaConnector;
    private boolean isConnectResponse = false;
    private ConnectTimeoutHandler timeoutHandler;
    private BikeScanHelper bikeScanHelper;
    private ScanHelper scanHelper;
    private BluetoothAdapter bluetoothAdapter;

    TbitBleInstance(Context context) {
        this.context = context;
        EventBus.getDefault().register(this);
        key = new Byte[]{};
        listener = new EmptyListener();
        otaListener = new EmptyListener.EmptyOtaListener();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothIO = new BluetoothIO(context);
        bikeBleConnector = new BikeBleConnector(bluetoothIO);
        timeoutHandler = new ConnectTimeoutHandler(Looper.getMainLooper(), this);
    }

    void setListener(TbitListener listener) {
        if (listener == null)
            this.listener = new EmptyListener();
        this.listener = listener;
    }

    void setDebugListener(TbitDebugListener debugListener) {
        this.debugListener = debugListener;
    }

    void connect(String macAddr, String key) {
        if (Build.VERSION.SDK_INT < 18) {
            listener.onConnectResponse(ResultCode.LOWER_THAN_API_18);
            return;
        }
        this.macAddr = macAddr;
        this.key = resolve(key, 32);
        if (!isMacAddrLegal()) {
            listener.onConnectResponse(ResultCode.MAC_ADDRESS_ILLEGAL);
            return;
        }
        if (!isKeyLegal()) {
            listener.onConnectResponse(ResultCode.KEY_ILLEGAL);
            return;
        }
        bikeBleConnector.setConnectKey(this.key);
        bikeBleConnector.setConnectMode(BikeBleConnector.ConnectMode.NORMAL);
        scanInternal();
    }

    void unlock() {
        if (!isBluetoothOpened())
            return;
        if (!bluetoothIO.isConnected()) {
            listener.onUnlockResponse(ResultCode.DISCONNECTED);
            return;
        }
        boolean result = bikeBleConnector.unlock();
        if (!result)
            listener.onUnlockResponse(ResultCode.PROCESSING);
    }

    void lock() {
        if (!isBluetoothOpened())
            return;
        if (!bluetoothIO.isConnected()) {
            listener.onLockResponse(ResultCode.DISCONNECTED);
            return;
        }
        boolean result = bikeBleConnector.lock();
        if (!result)
            listener.onLockResponse(ResultCode.PROCESSING);
    }

    void update() {
        if (!isBluetoothOpened())
            return;
        if (!bluetoothIO.isConnected()) {
            listener.onUpdateResponse(ResultCode.DISCONNECTED);
            return;
        }
        boolean result = bikeBleConnector.update();
        if (!result)
            listener.onUpdateResponse(ResultCode.PROCESSING);
    }

    void common(byte commandId, byte key, Byte[] value) {
        if (!isBluetoothOpened())
            return;
        if (!bluetoothIO.isConnected()) {
            listener.onCommonCommandResponse(ResultCode.DISCONNECTED, null);
            return;
        }
        boolean result = bikeBleConnector.common(commandId, key, value);
        if (!result)
            listener.onCommonCommandResponse(ResultCode.PROCESSING, null);
    }

    void reConnect() {
        if (bikeScanHelper == null) {
            listener.onConnectResponse(ResultCode.KEY_ILLEGAL);
            return;
        }
        if (!isMacAddrLegal() || !isKeyLegal()) {
            listener.onConnectResponse(ResultCode.MAC_ADDRESS_ILLEGAL);
            return;
        }
        scanInternal();
    }

    void disConnect() {
        bikeBleConnector.disConnect();
    }

    BikeState getState() {
        return bikeBleConnector.getState();
    }

    int getBleConnectionState() {
        return bluetoothIO.getConnectionState();
    }

    private void scanInternal() {
        if (!isBluetoothOpened())
            return;
        resetTimeout();
        if (bikeScanHelper == null) {
            bikeScanHelper = new BikeScanHelper(bluetoothAdapter, bluetoothIO);
        }
        boolean result = bikeScanHelper.scanAndConnect(macAddr);
        if (!result) {
            listener.onConnectResponse(ResultCode.PROCESSING);
        }
    }

    int startScan(ScannerCallback callback, long timeout) {
        if (callback == null)
            return ResultCode.FAILED;
        if (!bluetoothAdapter.isEnabled()) {
            return ResultCode.BLE_NOT_OPENED;
        }
        if (scanHelper == null) {
            scanHelper = new ScanHelper(bluetoothAdapter);
        }
        if (scanHelper.isScanning()) {
            return ResultCode.PROCESSING;
        }
        scanHelper.start(callback, timeout);
        return ResultCode.SUCCEED;
    }

    void stopScan() {
        if (!isBluetoothOpened())
            return;
        if (scanHelper == null)
            return;
        scanHelper.stop();
    }

    void destroy() {
        bikeBleConnector.destroy();
        bluetoothIO.close();
        if (otaConnector != null)
            otaConnector.destroy();
        EventBus.getDefault().unregister(this);
    }

    void connectiveOta(String machineNo, String key, File file, OtaListener otaListener) {
        if (!isBluetoothOpened())
            return;
        this.macAddr = machineNo;
        this.key = resolve(key, 16);
        this.otaListener = otaListener == null ? new EmptyListener.EmptyOtaListener() :
                otaListener;

        if (!isMacAddrLegal()) {
            otaListener.onOtaResponse(ResultCode.MAC_ADDRESS_ILLEGAL);
            return;
        }
        if (!isKeyLegal()) {
            otaListener.onOtaResponse(ResultCode.KEY_ILLEGAL);
            return;
        }
        bikeBleConnector.setConnectKey(this.key);
        if (!isOtaFileLegal(file)) {
            this.otaListener.onOtaResponse(ResultCode.OTA_FILE_ILLEGAL);
            return;
        }
        try {
            OtaFile otaFile = OtaFile.getByFile(file);
            if (otaConnector != null) {
                otaConnector.destroy();
            }
            this.otaConnector = new OtaConnector(bluetoothIO, otaFile);
            this.bikeBleConnector.setConnectMode(BikeBleConnector.ConnectMode.OTA);
            scanInternal();
        } catch (IOException e) {
            otaListener.onOtaResponse(ResultCode.OTA_FILE_ILLEGAL);
            e.printStackTrace();
        }
    }

    void ota(File file, OtaListener otaListener) {
        if (!isBluetoothOpened())
            return;
        this.otaListener = otaListener == null ? new EmptyListener.EmptyOtaListener() :
                otaListener;
        if (!isOtaFileLegal(file)) {
            this.otaListener.onOtaResponse(ResultCode.OTA_FILE_ILLEGAL);
            return;
        }
        try {
            OtaFile otaFile = OtaFile.getByFile(file);
            if (otaConnector != null) {
                otaConnector.destroy();
            }
            this.otaConnector = new OtaConnector(bluetoothIO, otaFile);
            boolean result = bikeBleConnector.ota();
            if (!result)
                this.otaListener.onOtaResponse(ResultCode.PROCESSING);
        } catch (IOException e) {
            otaListener.onOtaResponse(ResultCode.OTA_FILE_ILLEGAL);
            e.printStackTrace();
        }
    }

    private void resetTimeout() {
        isConnectResponse = false;
//        timeoutHandler.removeCallbacksAndMessages(null);
//        timeoutHandler.sendEmptyMessageDelayed(0, 11 * 1000);
    }

    private boolean isBluetoothOpened() {
        boolean isOpened = bluetoothAdapter.isEnabled();
        if (!isOpened && listener != null)
            listener.onConnectResponse(ResultCode.BLE_NOT_OPENED);
        return isOpened;
    }

    private boolean isKeyLegal() {
        if (key == null || key.length == 0)
            return false;
        return true;
    }

    private boolean isMacAddrLegal() {
        return !TextUtils.isEmpty(macAddr);
    }

    private Byte[] resolve(String keyStr, int length) {
        length = length * 2;
        Byte[] result = new Byte[]{};
        keyStr = keyStr.replace(" ", "");
        if (keyStr.length() != length)
            return result;
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < length; j+=2) {
            sb.append(keyStr.substring(j, j+2));
            if (j == length -2)
                continue;
            sb.append(" ");
        }
        try {
            result = ByteUtil.stringToBytes(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private boolean isOtaFileLegal(File file) {
        if (file == null)
            return false;
        if (!file.exists()) {
            return false;
        }
        String filename = file.getName();
        if (TextUtils.isEmpty(filename))
            return false;
        int dot = filename.lastIndexOf('.');
        if ((dot >-1) && (dot < (filename.length() - 1))) {
            String extName = filename.substring(dot + 1);
            if (!TextUtils.equals(extName, "img")) {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBleNotOpened(BluEvent.BleNotOpened event) {
        isConnectResponse = true;
        listener.onConnectResponse(ResultCode.BLE_NOT_OPENED);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onScanTimeOut(BluEvent.ScanTimeOut event) {
        isConnectResponse = true;
        listener.onConnectResponse(ResultCode.DEVICE_NOT_FOUNDED);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectionStateChange(BluEvent.ConnectionStateChange event) {
        Log.i(TAG, "onConnectionStateChange: from " + event.status + " to " + event.newState);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDisconnected(BluEvent.DisConnected event) {
        Log.i(TAG, "onDisconnected: ");
        if (!isConnectResponse) {
            isConnectResponse = true;
            listener.onConnectResponse(ResultCode.DISCONNECTED);
        } else {
            listener.onDisconnected(ResultCode.DISCONNECTED);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCommonFailedOccurred(BluEvent.CommonFailedReport report) {
        Log.e(TAG, "onCommonFailedOccurred: " + "\nname: " + report.functionName
                + "\nmessage: " + report.message);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRssiRead(BluEvent.ReadRssi event) {
        Log.i(TAG, "onRssiRead: ");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUartNotSupport(BluEvent.DeviceUartNotSupported event) {
        Log.i(TAG, "onUartNotSupport: " + event.message);
        isConnectResponse = true;
        bluetoothIO.disconnect();
        listener.onConnectResponse(ResultCode.BLE_NOT_SUPPORTED);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOta(BluEvent.Ota event) {
        Log.d(TAG, "onOta: " + event.getState());
        if (otaConnector == null) {
            otaListener.onOtaResponse(ResultCode.OTA_FILE_ILLEGAL);
        } else {
            switch (event.getState()) {
                case START:
                    otaConnector.update();
                    break;
                case UPDATING:
                    otaListener.onOtaProgress(event.getProgress());
                    break;
                case FAILED:
                    otaListener.onOtaResponse(event.getFailedCode());
                    break;
                case SUCCEED:
                    otaListener.onOtaResponse(ResultCode.SUCCEED);
                    break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWriteDone(BluEvent.WriteData event) {
        if (!bikeBleConnector.removeFromQueue(event.requestId)) {
            return;
        }
        if (event.state == BluEvent.State.SUCCEED) {
            switch (event.requestId) {
                case Constant.REQUEST_UNLOCK:
                    listener.onUnlockResponse(ResultCode.SUCCEED);
                    break;
                case Constant.REQUEST_LOCK:
                    listener.onLockResponse(ResultCode.SUCCEED);
                    break;
                case Constant.REQUEST_UPDATE:
                    EventBus.getDefault().post(new BluEvent.UpdateBikeState());
                    listener.onUpdateResponse(ResultCode.SUCCEED);
                    break;
                case Constant.REQUEST_CONNECT:
                    if (!isConnectResponse) {
                        listener.onConnectResponse(ResultCode.SUCCEED);
                        isConnectResponse = true;
                    }
                    break;
            }
        } else {
            switch (event.requestId) {
                case Constant.REQUEST_UNLOCK:
                    if (event.failCode != 0)
                        listener.onUnlockResponse(event.failCode);
                    else
                        listener.onUnlockResponse(ResultCode.FAILED);
                    break;
                case Constant.REQUEST_LOCK:
                    if (event.failCode != 0)
                        listener.onLockResponse(event.failCode);
                    else
                        listener.onLockResponse(ResultCode.FAILED);
                    break;
                case Constant.REQUEST_UPDATE:
                    listener.onUpdateResponse(ResultCode.FAILED);
                    break;
                case Constant.REQUEST_CONNECT:
                    if (!isConnectResponse) {
                        if (event.failCode != 0)
                            listener.onConnectResponse(event.failCode);
                        else
                            listener.onConnectResponse(ResultCode.KEY_ILLEGAL);
                        isConnectResponse = true;
                    }
                    break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCommonResponse(BluEvent.CommonResponse response) {
        if (!bikeBleConnector.removeFromQueue(Constant.REQUEST_COMMON)) {
            return;
        }
        listener.onCommonCommandResponse(response.code, response.packetValue);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStateUpdated(BluEvent.UpdateBikeState event) {
        listener.onStateUpdated(getState());
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDebudLogEvent(BluEvent.DebugLogEvent event) {
        if (debugListener != null) {
            debugListener.onLogStrReceived(event.getKey() + "\n" +event.getLogStr());
        }
    }

    static class ConnectTimeoutHandler extends Handler {
        WeakReference<TbitBleInstance> instanceReference;

        public ConnectTimeoutHandler(Looper looper, TbitBleInstance instance) {
            super(looper);
            this.instanceReference = new WeakReference<>(instance);
        }

        @Override
        public void handleMessage(Message msg) {
            final TbitBleInstance instance = instanceReference.get();
            if (instance == null)
                return;
            if (!instance.isConnectResponse) {
                instance.isConnectResponse = true;
                instance.listener.onConnectResponse(ResultCode.TIMEOUT);
                instance.disConnect();
            }
        }
    }
}
