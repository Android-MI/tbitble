package com.tbit.tbitblesdk;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.tbit.tbitblesdk.protocol.Constant;
import com.tbit.tbitblesdk.protocol.PacketValue;
import com.tbit.tbitblesdk.protocol.ResultCode;
import com.tbit.tbitblesdk.services.BikeBleConnector;
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
    private BikeState state;
    private BikeCallback callback;
    private BluetoothIO bluetoothIO;
    private BikeBleConnector bikeBleConnector;

    TbitBleInstance(Context context) {
        this.context = context;
        EventBus.getDefault().register(this);
        state = new BikeState();
        bluetoothIO = new BluetoothIO(context);
        bikeBleConnector = new BikeBleConnector(bluetoothIO);
        bikeBleConnector.start();
    }

    void connect(String macAddr, BikeCallback callback) {
        this.macAddr = macAddr;
        this.callback = callback;

        if (!isMacAddrLegal()) {
            if (callback != null)
                callback.onResponse(ResultCode.MAC_ADDRESS_ILLEGAL, state);
            return;
        }

        scan();
    }

    void verify(String keyStr, BikeCallback callback) {
        this.callback = callback;
        this.key = resolve(keyStr);
        if (!isKeyLegal()) {
            if (callback != null)
                callback.onResponse(ResultCode.KEY_ILLEGAL, state);
            return;
        }

        bikeBleConnector.connect(key);
    }

    void connect() {
        if (isBluetoothOpened())
            return;
    }

    void disConnect(BikeCallback callback) {
        if (callback != null)
            this.callback = callback;
        bluetoothIO.disconnect();
    }

    void disConnect() {
        disConnect(null);
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
        bikeBleConnector.stop();
        EventBus.getDefault().unregister(this);
    }

    private boolean isBluetoothOpened() {
        boolean isOpened = bluetoothIO.isBlueEnable();;
        if (!isOpened && callback != null)
            callback.onResponse(ResultCode.BLE_NOT_OPENED, state);
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
        if (callback != null)
            callback.onResponse(ResultCode.DEVICE_NOT_FOUNDED, state);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectionStateChange(BluEvent.ConnectionStateChange event) {
        Log.i(TAG, "onConnectionStateChange: ");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDisconnected(BluEvent.DisConnected event) {
        Log.i(TAG, "onDisconnected: ");
        if (callback != null)
            callback.onResponse(ResultCode.SUCCEED, state);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCommonFailedOccurred(BluEvent.CommonFailedReport report) {
        Log.i(TAG, "onCommonFailedOccurred: " + "\nname: " + report.functionName
            + "\nmessage: " + report.message);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDescovered(BluEvent.DiscoveredSucceed event) {
        Log.i(TAG, "onDescovered: ");
        bluetoothIO.enableTXNotification();
        if (callback != null)
            callback.onResponse(ResultCode.SUCCEED, state);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCharacteristicChanged(BluEvent.ChangeCharacteristic event) {
        switch (event.state) {
            case WRITE:
                bikeBleConnector.onUpdateStatus(true);
                break;
            case READ:
                final byte[] rxValue = event.value;
                bikeBleConnector.setReadTemp(rxValue);
                break;
            case CHANGE:

                break;
        }
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
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVerifyFailed(BluEvent.VerifyFailed event) {
        Log.i(TAG, "onVerifyFailed: ");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOta(BluEvent.Ota event) {
        
    }
}
