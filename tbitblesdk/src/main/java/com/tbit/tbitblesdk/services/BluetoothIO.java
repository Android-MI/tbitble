package com.tbit.tbitblesdk.services;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.tbit.tbitblesdk.protocol.BluEvent;
import com.tbit.tbitblesdk.util.ByteUtil;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;

/**
 * Created by Salmon on 2016/12/6 0006.
 */

public class BluetoothIO {
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_SCANNING = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;
    public static final int STATE_SERVICES_DISCOVERED = 4;
    public UUID SPS_SERVICE_UUID = UUID.fromString("0783b03e-8535-b5a0-7140-a304d2495cb7");
    public UUID SPS_TX_UUID = UUID.fromString("0783b03e-8535-b5a0-7140-a304d2495cba");
    public UUID SPS_RX_UUID = UUID.fromString("0783b03e-8535-b5a0-7140-a304d2495cb8");
    public UUID SPS_CTRL_UUID = UUID.fromString("0783b03e-8535-b5a0-7140-a304d2495cb9");
    public UUID SPS_NOTIFY_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private static final String TAG = "BluetoothIO";
    private int connectionState = STATE_DISCONNECTED;
    private Context context;
    private EventBus bus;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private Scanner scanner;
    private String lastConnectedDeviceMac;
    private boolean isAutoReconnectEnable = true;
//    private boolean hasVerified = false;
    private Handler handler = new Handler(Looper.getMainLooper());

    private BluetoothGattCallback coreGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            bus.post(new BluEvent.ConnectionStateChange(status, newState));
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                connectionState = STATE_CONNECTED;
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                connectionState = STATE_DISCONNECTED;
                gatt.close();
                bus.post(new BluEvent.DisConnected());
//                tryAutoReconnect();
            } else if (newState == BluetoothGatt.STATE_CONNECTING) {
                connectionState = STATE_CONNECTING;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            connectionState = STATE_SERVICES_DISCOVERED;
            if (status == BluetoothGatt.GATT_SUCCESS) {
                bluetoothGatt = gatt;
                bus.post(new BluEvent.DiscoveredSucceed());
            } else {
                bus.post(new BluEvent.CommonFailedReport("onServicesDiscovered",
                        "status : " + status));
            }
//            printServices(gatt);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (BluetoothGatt.GATT_SUCCESS == status) {
                bus.post(new BluEvent.ChangeCharacteristic(BluEvent.CharState.READ,
                        characteristic.getValue()));
            } else {
                bus.post(new BluEvent.CommonFailedReport("onCharacteristicRead",
                        "status : " + status));
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

            if (BluetoothGatt.GATT_SUCCESS == status) {
                bus.post(new BluEvent.ChangeCharacteristic(BluEvent.CharState.WRITE,
                        characteristic.getValue()));
            } else {
                bus.post(new BluEvent.CommonFailedReport("onCharacteristicWrite",
                        "status : " + status));
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            bus.post(new BluEvent.ChangeCharacteristic(BluEvent.CharState.CHANGE,
                    characteristic.getValue()));
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            if (BluetoothGatt.GATT_SUCCESS == status) {
                bus.post(new BluEvent.ReadRssi(rssi));
            } else {
                bus.post(new BluEvent.CommonFailedReport("onReadRemoteRssi",
                        "status : " + status));
            }
        }

    };

    public BluetoothIO(Context context) {
        super();
        this.context = context.getApplicationContext();
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            scanner = new AndroidLBikeBleScanner(bluetoothAdapter);
        } else {
            scanner = new BikeBleScanner(bluetoothAdapter);
        }
        bus = EventBus.getDefault();
    }

    public void scanAndConnectByMac(String macAddress) {
        isAutoReconnectEnable = true;
        lastConnectedDeviceMac = macAddress;
        connectionState = STATE_SCANNING;
        stopScan();
        disconnectInside();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!isBlueEnable()) {
            bus.post(new BluEvent.BleNotOpened());
            return;
        }
        scanner.setBluetoothAdapter(bluetoothAdapter);
        scanner.start(macAddress, new ScannerCallback() {
            @Override
            public void onScanTimeout() {
                stopScan();
                bus.post(new BluEvent.ScanTimeOut());
            }

            @Override
            public void onDeviceFounded(final BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
//                BluetoothGatt connect = connect(bluetoothDevice, false, coreGattCallback);
//                refreshDeviceCache(connect);
//                connect.connect();
                connect(bluetoothDevice, false, coreGattCallback);
            }
        });
    }

    public void stopScan() {
        connectionState = STATE_DISCONNECTED;
        scanner.stop();
    }

    public boolean isBlueEnable() {
        if (bluetoothAdapter == null)
            return false;
        return bluetoothAdapter.isEnabled();
    }

    public boolean isInScanning() {
        return connectionState == STATE_SCANNING;
    }

    public boolean isConnectingOrConnected() {
        return connectionState >= STATE_CONNECTING;
    }

    public boolean isConnected() {
        return connectionState >= STATE_CONNECTED;
    }

    public int getConnectionState() {
        return connectionState;
    }

    public boolean isServiceDiscovered() {
        return connectionState == STATE_SERVICES_DISCOVERED;
    }

    public void connect(final BluetoothDevice device,
                                 final boolean autoConnect,
                                 BluetoothGattCallback callback) {
        Log.i(TAG, "connect name：" + device.getName()
                + " mac:" + device.getAddress()
                + " autoConnect：" + autoConnect);
        BluetoothGatt bluetoothGatt;
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bluetoothGatt = device.connectGatt(context, autoConnect, callback, BluetoothDevice.TRANSPORT_LE);
        } else {
            bluetoothGatt = device.connectGatt(context, autoConnect, callback);
        }
        refreshDeviceCache(bluetoothGatt);
        bluetoothGatt.connect();
    }

    public void disconnect() {
        isAutoReconnectEnable = false;
        disconnectInside();
    }

    private void disconnectInside() {
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            Log.w(TAG, "--BluetoothAdapter not initialized");
            return;
        }
        try {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            bluetoothGatt = null;
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void reconnect() {
        isAutoReconnectEnable = true;
        scanAndConnectByMac(lastConnectedDeviceMac);
    }

//    public void setHasVerified(boolean hasVerified) {
//        this.hasVerified = hasVerified;
//        if (hasVerified)
//            isAutoReconnectEnable = true;
//    }

    private void tryAutoReconnect() {
        if (isAutoReconnectEnable /*&& hasVerified*/) {
            autoReconnect();
        } else {
            bus.post(new BluEvent.DisConnected());
        }
    }

    private void autoReconnect() {
        isAutoReconnectEnable = true;
        disconnectInside();
        scanner.start(lastConnectedDeviceMac, new ScannerCallback() {
            @Override
            public void onScanTimeout() {
                stopScan();
                bus.post(new BluEvent.DisConnected());
            }

            @Override
            public void onDeviceFounded(final BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
                connect(bluetoothDevice, false, coreGattCallback);
            }
        });
    }

    public void close() {
        stopScan();
        disconnectInside();
    }

    // Clears the device cache. After uploading new hello4 the DFU target will have other services than before.
    private boolean refreshDeviceCache(BluetoothGatt gatt){
        try {
            BluetoothGatt localBluetoothGatt = gatt;
            Method localMethod = localBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
            if (localMethod != null) {
                boolean bool = ((Boolean) localMethod.invoke(localBluetoothGatt, new Object[0])).booleanValue();
                return bool;
            }
        }
        catch (Exception localException) {
            Log.e(TAG, "An exception occured while refreshing device");
        }
        return false;
    }


    public void runOnMainThread(Runnable runnable) {
        if (isMainThread()) {
            runnable.run();
        } else {
            handler.post(runnable);
        }
    }

    private boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }


    public void updateVersion(int hardVersion, int softVersion) {
        if (softVersion >= 3) {
            SPS_SERVICE_UUID = UUID.fromString("0000fef6-0000-1000-8000-00805f9b34fb");
            SPS_TX_UUID = UUID.fromString("0783b03e-8535-b5a0-7140-a304d2495cba");
            SPS_RX_UUID = UUID.fromString("0783b03e-8535-b5a0-7140-a304d2495cb8");
            SPS_NOTIFY_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
        } else {
            SPS_SERVICE_UUID = UUID.fromString("0783b03e-8535-b5a0-7140-a304d2495cb7");
            SPS_TX_UUID = UUID.fromString("0783b03e-8535-b5a0-7140-a304d2495cba");
            SPS_RX_UUID = UUID.fromString("0783b03e-8535-b5a0-7140-a304d2495cb8");
            SPS_NOTIFY_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
        }
    }

    /**
     * Enable TXNotification
     */
    public boolean enableTXNotification() {
        Log.d(TAG, "enableTXNotification: ");
        if (bluetoothGatt == null) {
            bus.post(new BluEvent.DeviceUartNotSupported("bluetoothGatt == null"));
            return false;
        }
        BluetoothGattService RxService = bluetoothGatt.getService(SPS_SERVICE_UUID);
        if (RxService == null) {
            bus.post(new BluEvent.DeviceUartNotSupported("Service"));
            return false;
        }
        BluetoothGattCharacteristic RxChar = RxService.getCharacteristic(SPS_RX_UUID);
        if (RxChar == null) {
            bus.post(new BluEvent.DeviceUartNotSupported("rx"));
            return false;
        }
        bluetoothGatt.setCharacteristicNotification(RxChar, true);

        BluetoothGattDescriptor descriptor = RxChar.getDescriptor(SPS_NOTIFY_DESCRIPTOR);
        if (descriptor == null) {
            bus.post(new BluEvent.CommonFailedReport("enableTXNotification",
                    "--descriptor not found"));
            return false;
        }

        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        bluetoothGatt.writeDescriptor(descriptor);
        return true;
    }

    /**
     * writeRXCharacteristic
     *
     * @param value
     */
    public boolean writeRXCharacteristic(byte[] value) {
        Log.d(TAG, "writeRXCharacteristic: " + ByteUtil.bytesToHexString(value));
        if (!isConnected()) {
            Log.d(TAG, "writeRXCharacteristic: no connected!");
            return false;
        }
        if (bluetoothGatt == null) {
            Log.d(TAG, "writeRXCharacteristic: bluetoothGatt == null");
            return false;
        }
        BluetoothGattService Service = bluetoothGatt.getService(SPS_SERVICE_UUID);
        if (Service == null) {
            bus.post(new BluEvent.DeviceUartNotSupported());
            return false;
        }
        BluetoothGattCharacteristic TxChar = Service.getCharacteristic(SPS_TX_UUID);
        if (TxChar == null) {
            bus.post(new BluEvent.DeviceUartNotSupported());
            return false;
        }
        TxChar.setValue(value);
        boolean status = bluetoothGatt.writeCharacteristic(TxChar);
        if (status) {
            Log.d(TAG, "--指令下发成功！");
        } else {
            Log.d(TAG, "--指令下发失败！");
        }
        return status;
    }

    private void printServices(BluetoothGatt gatt) {
        if (gatt != null) {
            for (BluetoothGattService service : gatt.getServices()) {
                Log.i(TAG, "service: " + service.getUuid());

                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    Log.d(TAG, "    characteristic: " + characteristic.getUuid()
                            + "   ------  value: " + Arrays.toString(characteristic.getValue())
                            + "   ------  properties: " + characteristic.getProperties());

                    for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                        Log.v(TAG, "    descriptor: " + descriptor.getUuid()
                                + "   ------  value: " + Arrays.toString(descriptor.getValue()));
                    }
                }
            }
        }
    }
}
