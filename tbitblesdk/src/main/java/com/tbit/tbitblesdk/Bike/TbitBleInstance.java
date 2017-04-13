package com.tbit.tbitblesdk.Bike;

import android.bluetooth.BluetoothGatt;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.tbit.tbitblesdk.Bike.model.BikeState;
import com.tbit.tbitblesdk.Bike.services.BikeService;
import com.tbit.tbitblesdk.Bike.services.OtaService;
import com.tbit.tbitblesdk.Bike.services.command.Command;
import com.tbit.tbitblesdk.Bike.services.command.OtaCommand;
import com.tbit.tbitblesdk.Bike.services.command.OtaConnectCommand;
import com.tbit.tbitblesdk.Bike.services.command.bikecommand.CommonCommand;
import com.tbit.tbitblesdk.Bike.services.command.bikecommand.ConnectCommand;
import com.tbit.tbitblesdk.Bike.services.command.bikecommand.LockCommand;
import com.tbit.tbitblesdk.Bike.services.command.bikecommand.UnlockCommand;
import com.tbit.tbitblesdk.Bike.services.command.bikecommand.UpdateCommand;
import com.tbit.tbitblesdk.Bike.services.command.callback.StateCallback;
import com.tbit.tbitblesdk.Bike.tasks.BikeConnectHelper;
import com.tbit.tbitblesdk.Bike.util.BikeUtil;
import com.tbit.tbitblesdk.Bike.util.PacketUtil;
import com.tbit.tbitblesdk.bluetooth.BleClient;
import com.tbit.tbitblesdk.bluetooth.BleGlob;
import com.tbit.tbitblesdk.bluetooth.IBleClient;
import com.tbit.tbitblesdk.bluetooth.RequestDispatcher;
import com.tbit.tbitblesdk.bluetooth.listener.ConnectStateChangeListener;
import com.tbit.tbitblesdk.bluetooth.scanner.ScanHelper;
import com.tbit.tbitblesdk.bluetooth.scanner.Scanner;
import com.tbit.tbitblesdk.bluetooth.scanner.ScannerCallback;
import com.tbit.tbitblesdk.bluetooth.util.ByteUtil;
import com.tbit.tbitblesdk.protocol.Packet;
import com.tbit.tbitblesdk.protocol.callback.PacketCallback;
import com.tbit.tbitblesdk.protocol.callback.ProgressCallback;
import com.tbit.tbitblesdk.protocol.callback.ResultCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;

/**
 * Created by Salmon on 2016/12/5 0005.
 */

class TbitBleInstance implements ConnectStateChangeListener, Handler.Callback {
    private static final String TAG = "TbitBleInstance";
    private TbitListener listener;
    private TbitDebugListener debugListener;
    private IBleClient bleClient;
    private Scanner scanner;
    private BikeService bikeService;
    private OtaService otaService;
    private BikeConnectHelper bikeConnectHelper;
    private RequestDispatcher requestDispatcher;

    private String deviceId;
    private Byte[] key;

    private ResultCallback connectResultCallback;
    private StateCallback connectStateCallback;

    private Handler handler;

    TbitBleInstance() {
        EventBus.getDefault().register(this);
        listener = new EmptyListener();

        handler = new Handler(Looper.getMainLooper(), this);

        bleClient = new BleClient();
        scanner = ScanHelper.getScanner();
        requestDispatcher = new RequestDispatcher(bleClient);
        bikeService = new BikeService(bleClient, requestDispatcher);
        bikeConnectHelper = new BikeConnectHelper(bikeService, scanner, requestDispatcher);

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
        if (!BleGlob.isBluetoothEnabled()) {
            resultCallback.onResult(ResultCode.BLE_NOT_OPENED);
            return;
        }
        Byte[] key = BikeUtil.resolveKey(keyStr, 32);
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
        bikeConnectHelper.connect(deviceId, resultCallback, connectCommand);
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
        if (!BleGlob.isBluetoothEnabled()) {
            resultCallback.onResult(ResultCode.BLE_NOT_OPENED);
            return;
        }

        Command connectCommand = new ConnectCommand(resultCallback, stateCallback, key,
                bikeService.getBikeState());
        bikeConnectHelper.connect(deviceId, resultCallback, connectCommand);
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

    int startScan(ScannerCallback callback, long timeout) {
        if (callback == null)
            return ResultCode.FAILED;
        if (!BleGlob.isBluetoothEnabled()) {
            return ResultCode.BLE_NOT_OPENED;
        }
        if (scanner.isScanning()) {
            return ResultCode.PROCESSING;
        }
        scanner.start(callback, timeout);
        return ResultCode.SUCCEED;
    }

    void stopScan() {
        if (!BleGlob.isBluetoothEnabled())
            return;
        if (scanner.isScanning())
            scanner.stop();
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
        if (!BleGlob.isBluetoothEnabled()) {
            resultCallback.onResult(ResultCode.BLE_NOT_OPENED);
            return;
        }
        Byte[] key = BikeUtil.resolveKey(keyStr, 16);

        if (!isDeviceIdLegal(deviceId)) {
            resultCallback.onResult(ResultCode.MAC_ADDRESS_ILLEGAL);
            return;
        }
        if (!isKeyLegal(key)) {
            resultCallback.onResult(ResultCode.KEY_ILLEGAL);
            return;
        }
        if (!BikeUtil.isOtaFileLegal(file)) {
            resultCallback.onResult(ResultCode.OTA_FILE_ILLEGAL);
            return;
        }

        try {
            OtaFile otaFile = OtaFile.getByFile(file);
            if (otaService != null) {
                otaService.destroy();
            }
            this.otaService = new OtaService(bleClient, otaFile);

            Command otaConnectCommand = new OtaConnectCommand(otaService, key, resultCallback, progressCallback);

            bikeConnectHelper.connect(deviceId, resultCallback, otaConnectCommand);

        } catch (IOException e) {
            resultCallback.onResult(ResultCode.OTA_FILE_ILLEGAL);
            e.printStackTrace();
        }
    }

    void ota(File file, ResultCallback resultCallback, ProgressCallback progressCallback) {
        if (!baseCheck(resultCallback))
            return;
        if (!BikeUtil.isOtaFileLegal(file)) {
            resultCallback.onResult(ResultCode.OTA_FILE_ILLEGAL);
            return;
        }

        try {
            OtaFile otaFile = OtaFile.getByFile(file);
            if (otaService != null) {
                otaService.destroy();
            }
            this.otaService = new OtaService(bleClient, otaFile);

            Command otaConnectCommand = new OtaCommand(otaService, resultCallback, progressCallback);

            bikeService.addCommand(otaConnectCommand);
        } catch (IOException e) {
            resultCallback.onResult(ResultCode.OTA_FILE_ILLEGAL);
            e.printStackTrace();
        }
    }

    private boolean baseCheck(ResultCallback resultCallback) {
        boolean result;
        if (!BleGlob.isBluetoothEnabled()) {
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDebudLogEvent(BluEvent.DebugLogEvent event) {
        if (debugListener != null) {
            debugListener.onLogStrReceived(event.getKey() + "\n" + event.getLogStr());
        }
    }

    @Override
    public void onConnectionStateChange(int status, int newState) {
        if (newState == BluetoothGatt.STATE_DISCONNECTED) {
            if (this.connectResultCallback != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onDisconnected(ResultCode.DISCONNECTED);
                    }
                });
            }
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        return true;
    }
}
