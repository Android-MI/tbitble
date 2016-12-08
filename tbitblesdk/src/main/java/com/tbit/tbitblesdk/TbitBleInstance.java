package com.tbit.tbitblesdk;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.tbit.tbitblesdk.protocol.BluEvent;
import com.tbit.tbitblesdk.protocol.Constant;
import com.tbit.tbitblesdk.protocol.ResultCode;
import com.tbit.tbitblesdk.services.BikeBleConnector;
import com.tbit.tbitblesdk.protocol.BikeState;
import com.tbit.tbitblesdk.services.BluetoothIO;
import com.tbit.tbitblesdk.util.ByteUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by Salmon on 2016/12/5 0005.
 */

class TbitBleInstance {
    private static final String TAG = "TbitBleInstance";
    private Context context;
    private Byte[] key;
    private boolean hasVerified = false;
    private String macAddr;
    private TbitListener listener;
    private BluetoothIO bluetoothIO;
    private BikeBleConnector bikeBleConnector;

    TbitBleInstance(Context context) {
        this.context = context;
        EventBus.getDefault().register(this);
        key = new Byte[]{};
        listener = new EmptyListener();
        bluetoothIO = new BluetoothIO(context);
        bikeBleConnector = new BikeBleConnector(bluetoothIO);
    }

    void setListener(TbitListener listener) {
        if (listener == null)
            throw new RuntimeException("listener cannot be null");
        this.listener = listener;
    }

    void connect(String macAddr) {
        this.macAddr = macAddr;
        key = new Byte[]{};
        hasVerified = false;
//        bluetoothIO.setHasVerified(false);

        if (!isMacAddrLegal()) {
            listener.onConnectResponse(ResultCode.MAC_ADDRESS_ILLEGAL, getState());
            return;
        }

        scan();
    }

    void verify(String keyStr) {
        this.key = resolve(keyStr);
        if (!isKeyLegal()) {
            listener.onVerifyResponse(ResultCode.KEY_ILLEGAL, getState());
            return;
        }
        boolean result = bikeBleConnector.connect(key);
        if (!result)
            listener.onVerifyResponse(ResultCode.PROCESSING, getState());
    }

    private void verify() {
        bikeBleConnector.connect(key);
    }

    void unlock() {
        boolean result = bikeBleConnector.unlock();
        if (!result)
            listener.onUnlockResponse(ResultCode.PROCESSING, getState());
    }

    void lock() {
        boolean result = bikeBleConnector.lock();
        if (!result)
            listener.onLockResponse(ResultCode.PROCESSING, getState());
    }

    void common(byte commandId, byte key, Byte[] value) {
        boolean result = bikeBleConnector.common(commandId, key, value);
        if (!result)
            listener.onCommonCommandResponse(ResultCode.PROCESSING, getState());
    }

    void reConnect() {
        if (isBluetoothOpened()) {
            listener.onConnectResponse(ResultCode.BLE_NOT_OPENED, getState());
            return;
        }
        bluetoothIO.reconnect();
    }

    void disConnect() {
        bluetoothIO.disconnect();
    }

    void scan() {
        if (!isBluetoothOpened())
            return;
        bluetoothIO.scanAndConnectByMac(macAddr);
    }

    void stopScan() {
        if (!isBluetoothOpened())
            return;
        bluetoothIO.stopScan();
    }

    void destroy() {
        bikeBleConnector.destrop();
        bluetoothIO.close();
        EventBus.getDefault().unregister(this);
    }

    private BikeState getState() {
        return bikeBleConnector.getState();
    }

    private boolean isBluetoothOpened() {
        boolean isOpened = bluetoothIO.isBlueEnable();
        if (!isOpened && listener != null)
            listener.onConnectResponse(ResultCode.BLE_NOT_OPENED, getState());
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

    private Byte[] resolve(String keyStr) {
        Byte[] result = new Byte[]{};
        try {
            result = ByteUtil.stringToBytes(keyStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onScanTimeOut(BluEvent.ScanTimeOut event) {
        listener.onConnectResponse(ResultCode.DEVICE_NOT_FOUNDED, getState());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectionStateChange(BluEvent.ConnectionStateChange event) {
        Log.i(TAG, "onConnectionStateChange: ");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDisconnected(BluEvent.DisConnected event) {
        Log.i(TAG, "onDisconnected: ");
        listener.onDisconnected(ResultCode.DISCONNECTED, getState());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCommonFailedOccurred(BluEvent.CommonFailedReport report) {
        Log.i(TAG, "onCommonFailedOccurred: " + "\nname: " + report.functionName
                + "\nmessage: " + report.message);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDiscovered(BluEvent.DiscoveredSucceed event) {
        Log.i(TAG, "onDiscovered: ");
        bluetoothIO.enableTXNotification();
        if (hasVerified)
            verify();
        listener.onConnectResponse(ResultCode.SUCCEED, getState());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRssiRead(BluEvent.ReadRssi event) {
        Log.i(TAG, "onRssiRead: ");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUartNotSupport(BluEvent.DeviceUartNotSupported event) {
        Log.i(TAG, "onUartNotSupport: ");
        bluetoothIO.disconnect();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVerifySucceed(BluEvent.VerifySucceed event) {
        Log.i(TAG, "onVerifySucceed: ");
        hasVerified = true;
//        bluetoothIO.setHasVerified(true);
        listener.onVerifyResponse(ResultCode.SUCCEED, getState());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVerifyFailed(BluEvent.VerifyFailed event) {
        Log.i(TAG, "onVerifyFailed: ");
        listener.onVerifyResponse(ResultCode.KEY_ILLEGAL, getState());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOta(BluEvent.Ota event) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSendSucceed(BluEvent.WriteData event) {
        if (listener == null)
            return;
        if (!bikeBleConnector.removeFromQueue(event.requestId)) {
            return;
        }
        if (event.state == BluEvent.State.SUCCEED) {
            switch (event.requestId) {
                case Constant.REQUEST_UNLOCK:
                    listener.onUnlockResponse(ResultCode.SUCCEED, getState());
                    break;
                case Constant.REQUEST_LOCK:
                    listener.onLockResponse(ResultCode.SUCCEED, getState());
                    break;
            }
        } else {
            switch (event.requestId) {
                case Constant.REQUEST_UNLOCK:
                    listener.onUnlockResponse(ResultCode.UNLOCK_FAILED, getState());
                    break;
                case Constant.REQUEST_LOCK:
                    listener.onLockResponse(ResultCode.LOCK_FAILED, getState());
                    break;
            }
        }
    }
}
