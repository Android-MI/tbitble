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

    void connect(String macAddr, String key) {
        this.macAddr = macAddr;
        this.key = resolve(key);
        if (!isMacAddrLegal()) {
            listener.onConnectResponse(ResultCode.MAC_ADDRESS_ILLEGAL);
            return;
        }
        if (!isKeyLegal()) {
            listener.onConnectResponse(ResultCode.KEY_ILLEGAL);
            return;
        }
        scan();
    }

    private void verify() {
        bikeBleConnector.connect(key);
    }

    void unlock() {
        if (!bluetoothIO.isConnected()) {
            listener.onUnlockResponse(ResultCode.DISCONNECTED);
            return;
        }
        boolean result = bikeBleConnector.unlock();
        if (!result)
            listener.onUnlockResponse(ResultCode.PROCESSING);
    }

    void lock() {
        if (!bluetoothIO.isConnected()) {
            listener.onLockResponse(ResultCode.DISCONNECTED);
            return;
        }
        boolean result = bikeBleConnector.lock();
        if (!result)
            listener.onLockResponse(ResultCode.PROCESSING);
    }

    void update() {
        if (!bluetoothIO.isConnected()) {
            listener.onUpdateResponse(ResultCode.DISCONNECTED);
            return;
        }
        boolean result = bikeBleConnector.update();
        if (!result)
            listener.onUpdateResponse(ResultCode.PROCESSING);
    }

    void common(byte commandId, byte key, Byte[] value) {
        if (!bluetoothIO.isConnected()) {
            listener.onCommonCommandResponse(ResultCode.DISCONNECTED);
            return;
        }
        boolean result = bikeBleConnector.common(commandId, key, value);
        if (!result)
            listener.onCommonCommandResponse(ResultCode.PROCESSING);
    }

    void reConnect() {
        if (!isBluetoothOpened()) {
            listener.onConnectResponse(ResultCode.BLE_NOT_OPENED);
            return;
        }
        bluetoothIO.reconnect();
    }

    void disConnect() {
        bluetoothIO.disconnect();
    }

    BikeState getState() {
        return bikeBleConnector.getState();
    }

    int getBleConnectionState() {
        return bluetoothIO.getConnectionState();
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
        bikeBleConnector.destroy();
        bluetoothIO.close();
        EventBus.getDefault().unregister(this);
    }

    private boolean isBluetoothOpened() {
        boolean isOpened = bluetoothIO.isBlueEnable();
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
        listener.onConnectResponse(ResultCode.DEVICE_NOT_FOUNDED);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectionStateChange(BluEvent.ConnectionStateChange event) {
        Log.i(TAG, "onConnectionStateChange: ");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDisconnected(BluEvent.DisConnected event) {
        Log.i(TAG, "onDisconnected: ");
        listener.onDisconnected(ResultCode.DISCONNECTED);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCommonFailedOccurred(BluEvent.CommonFailedReport report) {
        Log.i(TAG, "onCommonFailedOccurred: " + "\nname: " + report.functionName
                + "\nmessage: " + report.message);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDiscovered(BluEvent.DiscoveredSucceed event) {
        Log.i(TAG, "onDiscovered: ");
        boolean result = bluetoothIO.enableTXNotification();
        if (result)
            verify();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRssiRead(BluEvent.ReadRssi event) {
        Log.i(TAG, "onRssiRead: ");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUartNotSupport(BluEvent.DeviceUartNotSupported event) {
        Log.i(TAG, "onUartNotSupport: ");
        bluetoothIO.disconnect();
        listener.onConnectResponse(ResultCode.BLE_NOT_SUPPORTED);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOta(BluEvent.Ota event) {

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
                    listener.onUpdateResponse(ResultCode.SUCCEED);
                    break;
            }
        } else {
            switch (event.requestId) {
                case Constant.REQUEST_UNLOCK:
                    listener.onUnlockResponse(ResultCode.UNLOCK_FAILED);
                    break;
                case Constant.REQUEST_LOCK:
                    listener.onLockResponse(ResultCode.LOCK_FAILED);
                    break;
                case Constant.REQUEST_UPDATE:
                    listener.onUpdateResponse(ResultCode.UPDATE_FAILED);
                    break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStateUpdated(BluEvent.UpdateBikeState event) {
        listener.onStateUpdated(getState());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVerified(BluEvent.Verified event) {
        Log.d(TAG, "onVerified: ");
        if (event.state == BluEvent.State.SUCCEED) {
            listener.onConnectResponse(ResultCode.SUCCEED);
        } else {
            listener.onConnectResponse(ResultCode.KEY_ILLEGAL);
        }
    }
}
