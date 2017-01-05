package com.tbit.tbitblesdk;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
    private TbitDebugListener debugListener;
    private BluetoothIO bluetoothIO;
    private BikeBleConnector bikeBleConnector;
    private boolean isConnectResponse = false;
    private ConnectTimeoutHandler timeoutHandler;

    TbitBleInstance(Context context) {
        this.context = context;
        EventBus.getDefault().register(this);
        key = new Byte[]{};
        listener = new EmptyListener();
        bluetoothIO = new BluetoothIO(context);
        bikeBleConnector = new BikeBleConnector(bluetoothIO);
        timeoutHandler = new ConnectTimeoutHandler(Looper.getMainLooper(), this);
    }

    void setListener(TbitListener listener) {
        if (listener == null)
            throw new RuntimeException("listener cannot be null");
        this.listener = listener;
    }

    void setDebugListener(TbitDebugListener debugListener) {
        this.debugListener = debugListener;
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
        if (!isMacAddrLegal() || !isKeyLegal()) {
            listener.onConnectResponse(ResultCode.MAC_ADDRESS_ILLEGAL);
            return;
        }
        resetTimeout();
        bluetoothIO.reconnect();
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

    void scan() {
        if (!isBluetoothOpened())
            return;
        resetTimeout();
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

    private void resetTimeout() {
        isConnectResponse = false;
        timeoutHandler.removeCallbacksAndMessages(null);
        timeoutHandler.sendEmptyMessageDelayed(0, 11 * 1000);
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
        keyStr = keyStr.replace(" ", "");
        if (keyStr.length() != 64)
            return result;
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < 64; j+=2) {
            sb.append(keyStr.substring(j, j+2));
            if (j == 64 -2)
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBleNotOpened(BluEvent.BleNotOpened event) {
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
        isConnectResponse = true;
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
        if (isConnectResponse)
            return;
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
        isConnectResponse = true;
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
                    listener.onUnlockResponse(ResultCode.FAILED);
                    break;
                case Constant.REQUEST_LOCK:
                    listener.onLockResponse(ResultCode.FAILED);
                    break;
                case Constant.REQUEST_UPDATE:
                    listener.onUpdateResponse(ResultCode.FAILED);
                    break;
                case Constant.REQUEST_CONNECT:
                    if (!isConnectResponse) {
                        listener.onConnectResponse(ResultCode.KEY_ILLEGAL);
                        isConnectResponse = true;
                    }
                    break;
            }
        }
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
                instance.listener.onConnectResponse(ResultCode.CONNECT_TIME_OUT);
                instance.disConnect();
            }
        }
    }
}
