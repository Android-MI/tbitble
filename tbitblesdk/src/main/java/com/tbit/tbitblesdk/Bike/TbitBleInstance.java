package com.tbit.tbitblesdk.Bike;

import android.bluetooth.BluetoothGatt;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.tbit.tbitblesdk.Bike.services.OtaService;
import com.tbit.tbitblesdk.bluetooth.IBleClient;
import com.tbit.tbitblesdk.bluetooth.listener.ConnectStateChangeListener;
import com.tbit.tbitblesdk.protocol.Packet;
import com.tbit.tbitblesdk.Bike.model.BikeState;
import com.tbit.tbitblesdk.bluetooth.BleClient;
import com.tbit.tbitblesdk.Bike.services.command.Command;
import com.tbit.tbitblesdk.Bike.services.command.OtaCommand;
import com.tbit.tbitblesdk.Bike.services.command.OtaConnectCommand;
import com.tbit.tbitblesdk.Bike.services.command.bikecommand.CommonCommand;
import com.tbit.tbitblesdk.Bike.services.command.bikecommand.ConnectCommand;
import com.tbit.tbitblesdk.Bike.services.command.bikecommand.LockCommand;
import com.tbit.tbitblesdk.Bike.services.command.bikecommand.UnlockCommand;
import com.tbit.tbitblesdk.Bike.services.command.bikecommand.UpdateCommand;
import com.tbit.tbitblesdk.protocol.callback.PacketCallback;
import com.tbit.tbitblesdk.protocol.callback.ProgressCallback;
import com.tbit.tbitblesdk.protocol.callback.ResultCallback;
import com.tbit.tbitblesdk.Bike.services.command.callback.StateCallback;
import com.tbit.tbitblesdk.bluetooth.scanner.BikeScanHelper;
import com.tbit.tbitblesdk.bluetooth.scanner.ScanHelper;
import com.tbit.tbitblesdk.bluetooth.scanner.ScannerCallback;
import com.tbit.tbitblesdk.bluetooth.util.ByteUtil;
import com.tbit.tbitblesdk.Bike.util.PacketUtil;
import com.tbit.tbitblesdk.Bike.services.BikeService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;

/**
 * Created by Salmon on 2016/12/5 0005.
 */

class TbitBleInstance implements ConnectStateChangeListener {
    private static final String TAG = "TbitBleInstance";
    private TbitListener listener;
    private TbitDebugListener debugListener;
    private IBleClient bleClient;
    private BikeService bikeService;
    private OtaService otaService;
    private BikeScanHelper bikeScanHelper;
    private ScanHelper scanHelper;

    private String deviceId;
    private Byte[] key;

    private ResultCallback connectResultCallback;
    private StateCallback connectStateCallback;

    TbitBleInstance() {
        EventBus.getDefault().register(this);
        listener = new EmptyListener();
        bleClient = new BleClient();
        bikeService = new BikeService(bleClient);
        bleClient.getListenerManager().addConnectStateChangeListener(this);
    }

    void setListener(TbitListener listener) {
        if (listener == null)
            this.listener = new EmptyListener();
        this.listener = listener;
    }

    void setDebugListener(TbitDebugListener debugListener) {
        this.debugListener = debugListener;
    }

    void connect(String deviceId, String keyStr, ResultCallback resultCallback, StateCallback stateCallback) {
        if (Build.VERSION.SDK_INT < 18) {
            resultCallback.onResult(ResultCode.LOWER_THAN_API_18);
            return;
        }
        if (!isBluetoothEnabled()) {
            resultCallback.onResult(ResultCode.BLE_NOT_OPENED);
            return;
        }
        Byte[] key = resolve(keyStr, 32);
        if (!isDeviceIdLegal(deviceId)) {
            resultCallback.onResult(ResultCode.MAC_ADDRESS_ILLEGAL);
            return;
        }
        if (!isKeyLegal(key)) {
            resultCallback.onResult(ResultCode.KEY_ILLEGAL);
            return;
        }

        this.deviceId = deviceId;
        this.key = key;

        this.connectResultCallback = resultCallback;
        this.connectStateCallback = stateCallback;

        Command connectCommand = new ConnectCommand(resultCallback, stateCallback, key,
                bikeService.getBikeState());
        bikeService.setConnectCommand(connectCommand);

        scanBikeInternal(deviceId);
    }

    void connect(String macAddr, String key) {
        connect(macAddr, key, new ResultCallback() {
            @Override
            public void onResult(int resultCode) {
                listener.onConnectResponse(resultCode);
            }
        }, new StateCallback() {
            @Override
            public void onStateUpdated(BikeState bikeState) {
                listener.onStateUpdated(bikeState);
            }
        });
    }

    void unlock(ResultCallback resultCallback) {
        if (!baseCheck(resultCallback))
            return;
        Command command = new UnlockCommand(resultCallback);
        bikeService.addCommand(command);
    }

    void unlock() {
        unlock(new ResultCallback() {
            @Override
            public void onResult(int resultCode) {
                listener.onUnlockResponse(resultCode);
            }
        });
    }

    void lock(ResultCallback resultCallback) {
        if (!baseCheck(resultCallback))
            return;
        Command command = new LockCommand(resultCallback);
        bikeService.addCommand(command);
    }

    void lock() {
        lock(new ResultCallback() {
            @Override
            public void onResult(int resultCode) {
                listener.onLockResponse(resultCode);
            }
        });
    }

    void update(ResultCallback resultCallback, StateCallback stateCallback) {
        if (!baseCheck(resultCallback))
            return;
        Command command = new UpdateCommand(resultCallback, stateCallback, bikeService.getBikeState());
        bikeService.addCommand(command);
    }

    void update() {
        update(new ResultCallback() {
            @Override
            public void onResult(int resultCode) {
                listener.onUpdateResponse(resultCode);
            }
        }, new StateCallback() {
            @Override
            public void onStateUpdated(BikeState bikeState) {
                listener.onStateUpdated(bikeState);
            }
        });
    }

    void common(Command command) {
        bikeService.addCommand(command);
    }

    void common(byte commandId, byte key, Byte[] value,
                ResultCallback resultCallback, PacketCallback packetCallback) {
        Packet packet = PacketUtil.createPacket(128, commandId, key, value);
        common(new CommonCommand(resultCallback, packetCallback, packet));
    }

    void common(byte commandId, byte key, Byte[] value) {
        common(commandId, key, value, new ResultCallback() {
            @Override
            public void onResult(int resultCode) {
                if (resultCode != ResultCode.SUCCEED)
                    listener.onCommonCommandResponse(resultCode, null);
            }
        }, new PacketCallback() {
            @Override
            public void onPacketReceived(Packet packet) {
                listener.onCommonCommandResponse(ResultCode.SUCCEED, packet.getPacketValue());
            }
        });
    }

    void reConnect(ResultCallback resultCallback, StateCallback stateCallback) {
        if (!isDeviceIdLegal(this.deviceId)) {
            resultCallback.onResult(ResultCode.MAC_ADDRESS_ILLEGAL);
            return;
        }
        if (!isKeyLegal(this.key)) {
            resultCallback.onResult(ResultCode.KEY_ILLEGAL);
            return;
        }
        if (!isBluetoothEnabled()) {
            resultCallback.onResult(ResultCode.BLE_NOT_OPENED);
            return;
        }

        Command connectCommand = new ConnectCommand(resultCallback, stateCallback, key,
                bikeService.getBikeState());
        bikeService.setConnectCommand(connectCommand);

        scanBikeInternal(this.deviceId);
    }

    void reConnect() {
        if (connectResultCallback == null || connectStateCallback == null) {
            return;
        }
        reConnect(this.connectResultCallback, this.connectStateCallback);
    }

    void disConnect() {
//        bikeBleConnector.disConnect();
        bleClient.disconnect();
    }

    BikeState getState() {
//        return bikeBleConnector.getState();
        return bikeService.getBikeState();
    }

    int getBleConnectionState() {
        return bleClient.getConnectionState();
    }

    private void scanBikeInternal(String deviceId) {
        if (bikeScanHelper == null) {
            bikeScanHelper = new BikeScanHelper(bleClient);
        }
        boolean result = bikeScanHelper.scanAndConnect(deviceId);
        if (!result) {
            listener.onConnectResponse(ResultCode.PROCESSING);
        }
    }

    int startScan(ScannerCallback callback, long timeout) {
        if (callback == null)
            return ResultCode.FAILED;
        if (!isBluetoothEnabled()) {
            return ResultCode.BLE_NOT_OPENED;
        }
        if (scanHelper == null) {
            scanHelper = new ScanHelper();
        }
        if (scanHelper.isScanning()) {
            return ResultCode.PROCESSING;
        }
        scanHelper.start(callback, timeout);
        return ResultCode.SUCCEED;
    }

    void stopScan() {
        if (!isBluetoothEnabled())
            return;
        if (scanHelper == null)
            return;
        scanHelper.stop();
    }

    void destroy() {
//        bikeBleConnector.destroy();
        bikeService.destroy();
        bleClient.close();
        if (otaService != null)
            otaService.destroy();
        if (connectResultCallback != null)
            connectResultCallback = null;
        if (connectStateCallback != null)
            connectStateCallback = null;
        EventBus.getDefault().unregister(this);
    }

    void connectiveOta(String deviceId, String keyStr, File file,
                       ResultCallback resultCallback, ProgressCallback progressCallback) {
        if (!isBluetoothEnabled()) {
            resultCallback.onResult(ResultCode.BLE_NOT_OPENED);
            return;
        }
        Byte[] key = resolve(keyStr, 16);

        if (!isDeviceIdLegal(deviceId)) {
            resultCallback.onResult(ResultCode.MAC_ADDRESS_ILLEGAL);
            return;
        }
        if (!isKeyLegal(key)) {
            resultCallback.onResult(ResultCode.KEY_ILLEGAL);
            return;
        }
        if (!isOtaFileLegal(file)) {
            resultCallback.onResult(ResultCode.OTA_FILE_ILLEGAL);
            return;
        }
        Command otaConnectCommand = new OtaConnectCommand(resultCallback, key);

        bikeService.setConnectCommand(otaConnectCommand);

        try {
            OtaFile otaFile = OtaFile.getByFile(file);
            if (otaService != null) {
                otaService.destroy();
            }
            this.otaService = new OtaService(bleClient, otaFile, resultCallback, progressCallback);

            scanBikeInternal(deviceId);
        } catch (IOException e) {
            resultCallback.onResult(ResultCode.OTA_FILE_ILLEGAL);
            e.printStackTrace();
        }
    }

    void ota(File file, ResultCallback resultCallback, ProgressCallback progressCallback) {
        if (!baseCheck(resultCallback))
            return;
        if (!isOtaFileLegal(file)) {
            resultCallback.onResult(ResultCode.OTA_FILE_ILLEGAL);
            return;
        }
        Command otaConnectCommand = new OtaCommand(resultCallback);

        bikeService.addCommand(otaConnectCommand);

        try {
            OtaFile otaFile = OtaFile.getByFile(file);
            if (otaService != null) {
                otaService.destroy();
            }
            this.otaService = new OtaService(bleClient, otaFile, resultCallback, progressCallback);

        } catch (IOException e) {
            resultCallback.onResult(ResultCode.OTA_FILE_ILLEGAL);
            e.printStackTrace();
        }
    }

    private boolean baseCheck(ResultCallback resultCallback) {
        boolean result;
        if (!isBluetoothEnabled()) {
            result = false;
            resultCallback.onResult(ResultCode.BLE_NOT_OPENED);
        } else if (bleClient.getConnectionState() < 3) {
            result = false;
            resultCallback.onResult(ResultCode.DISCONNECTED);
        } else {
            result = true;
        }
        return result;
    }

    private boolean isKeyLegal(Byte[] key) {
        if (key == null || key.length == 0)
            return false;
        return true;
    }

    private boolean isDeviceIdLegal(String deviceID) {
        return !TextUtils.isEmpty(deviceID);
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
        listener.onConnectResponse(ResultCode.BLE_NOT_OPENED);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onScanTimeOut(BluEvent.ScanTimeOut event) {
        listener.onConnectResponse(ResultCode.DEVICE_NOT_FOUNDED);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectionStateChange(BluEvent.ConnectionStateChange event) {
        Log.i(TAG, "onConnectionStateChange: from " + event.status + " to " + event.newState);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDebudLogEvent(BluEvent.DebugLogEvent event) {
        if (debugListener != null) {
            debugListener.onLogStrReceived(event.getKey() + "\n" +event.getLogStr());
        }
    }

    @Override
    public void onConnectionStateChange(int status, int newState) {
        if (newState == BluetoothGatt.STATE_DISCONNECTED) {
            if (this.connectResultCallback != null)
                connectResultCallback.onResult(ResultCode.DISCONNECTED);
        }
    }
}
