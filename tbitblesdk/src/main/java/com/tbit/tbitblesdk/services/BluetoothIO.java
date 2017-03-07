package com.tbit.tbitblesdk.services;

import android.annotation.TargetApi;
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

import com.tbit.tbitblesdk.ScanResponse;
import com.tbit.tbitblesdk.protocol.BluEvent;
import com.tbit.tbitblesdk.services.scanner.Scanner;
import com.tbit.tbitblesdk.services.scanner.ScannerCallback;
import com.tbit.tbitblesdk.util.ByteUtil;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;

/**
 * Created by Salmon on 2016/12/6 0006.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BluetoothIO {
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_SCANNING = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;
    public static final int STATE_SERVICES_DISCOVERED = 4;
    private static final String TAG = "BluetoothIO";
    private int connectionState = STATE_DISCONNECTED;
    private Context context;
    private EventBus bus;
    private BluetoothManager bluetoothManager;
    private BluetoothGatt bluetoothGatt;
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
//                printServices(gatt);
                bluetoothGatt = gatt;
                bus.post(new BluEvent.DiscoveredSucceed());
            } else {
                refreshDeviceCache(gatt);
                disconnectInside();
                bus.post(new BluEvent.CommonFailedReport("onServicesDiscovered",
                        "status : " + status));
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

            bus.post(new BluEvent.ChangeCharacteristic(BluEvent.CharState.WRITE,
                    characteristic.getService().getUuid(), characteristic.getUuid(),
                    characteristic.getValue(), status));
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            bus.post(new BluEvent.ChangeCharacteristic(BluEvent.CharState.CHANGE,
                    characteristic.getService().getUuid(), characteristic.getUuid(),
                    characteristic.getValue(), BluetoothGatt.GATT_SUCCESS));
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

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            bus.post(new BluEvent.ChangeDescriptor(BluEvent.CharState.WRITE,
                    descriptor.getCharacteristic().getUuid(), status));
            super.onDescriptorWrite(gatt, descriptor, status);
        }
    };

    public BluetoothIO(Context context) {
        super();
        this.context = context.getApplicationContext();
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bus = EventBus.getDefault();
    }

    public void scanAndConnectByMac(Scanner scanner) {
        isAutoReconnectEnable = true;
        connectionState = STATE_SCANNING;
        disconnectInside();
        scanner.start(new ScannerCallback() {
            @Override
            public void onScanStop() {
                connectionState = STATE_DISCONNECTED;
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

    public void scan(ScanResponse response) {

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

    public BluetoothGatt getBluetoothGatt() {
        return bluetoothGatt;
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bluetoothGatt = device.connectGatt(context, autoConnect, callback, BluetoothDevice.TRANSPORT_LE);
        } else {
            bluetoothGatt = device.connectGatt(context, autoConnect, callback);
        }
//        refreshDeviceCache(bluetoothGatt);
//        bluetoothGatt.connect();
    }

    public void disconnect() {
        isAutoReconnectEnable = false;
        disconnectInside();
    }

    private void disconnectInside() {
        if (bluetoothGatt == null) {
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

//    public void reconnect() {
//        isAutoReconnectEnable = true;
//        scanAndConnectByMac(lastConnectedDeviceMac);
//    }

//    private void tryAutoReconnect() {
//        if (isAutoReconnectEnable /*&& hasVerified*/) {
//            autoReconnect();
//        } else {
//            bus.post(new BluEvent.DisConnected());
//        }
//    }

//    private void autoReconnect() {
//        isAutoReconnectEnable = true;
//        disconnectInside();
//        scanner.start(new ScannerCallback() {
//            @Override
//            public void onScanStop() {
//                stopScanInternal();
//                bus.post(new BluEvent.DisConnected());
//            }
//
//            @Override
//            public void onDeviceFounded(final BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
//                connect(bluetoothDevice, false, coreGattCallback);
//            }
//        });
//    }

    public void close() {
        disconnectInside();
    }

    // Clears the device cache. After uploading new hello4 the DFU target will have other services than before.
    private boolean refreshDeviceCache(BluetoothGatt gatt) {
        try {
            BluetoothGatt localBluetoothGatt = gatt;
            Method localMethod = localBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
            if (localMethod != null) {
                boolean bool = ((Boolean) localMethod.invoke(localBluetoothGatt, new Object[0])).booleanValue();
                return bool;
            }
        } catch (Exception localException) {
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

    public boolean setCharacteristicNotification(UUID service, UUID character, UUID descriptor, boolean enable) {
        BluetoothGattCharacteristic characteristic = getCharacter(service, character);
        if (characteristic == null) {
            Log.e(TAG, String.format("characteristic not exist!"));
            return false;
        }

        if (!isCharacteristicNotifyable(characteristic)) {
            Log.e(TAG, String.format("characteristic not notifyable!"));
            return false;
        }

        if (bluetoothGatt == null) {
            Log.e(TAG, String.format("ble gatt null"));
            return false;
        }

        if (!bluetoothGatt.setCharacteristicNotification(characteristic, enable)) {
            Log.e(TAG, String.format("setCharacteristicNotification failed"));
            return false;
        }

        BluetoothGattDescriptor gattDescriptor = characteristic.getDescriptor(descriptor);

        if (descriptor == null) {
            Log.e(TAG, String.format("getDescriptor for notify null!"));
            return false;
        }

        byte[] value = (enable ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);

        if (!gattDescriptor.setValue(value)) {
            Log.e(TAG, String.format("setValue for notify descriptor failed!"));
            return false;
        }

        if (!bluetoothGatt.writeDescriptor(gattDescriptor)) {
            Log.e(TAG, String.format("writeDescriptor for notify failed"));
            return false;
        }
        return true;
    }

    private BluetoothGattCharacteristic getCharacter(UUID service, UUID character) {
        BluetoothGattCharacteristic characteristic = null;
        if (bluetoothGatt != null) {
            BluetoothGattService gattService = bluetoothGatt.getService(service);
            if (gattService != null) {
                characteristic = gattService.getCharacteristic(character);
            }
        }
        return characteristic;
    }

    private boolean isCharacteristicNotifyable(BluetoothGattCharacteristic characteristic) {
        return characteristic != null && (characteristic.getProperties()
                & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
    }

    public boolean requestConnectionPriority(int connectionPriority) {
        if (bluetoothGatt == null) {
            Log.e(TAG, "gatt is null");
            return false;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.e(TAG, "requestConnectionPriority need above android M" );
            return false;
        }
        return bluetoothGatt.requestConnectionPriority(connectionPriority);
    }

    public boolean write(UUID serviceUUID, UUID characteristicUUID, byte[] value, boolean withResponse) {
        Log.d(TAG, "writeRXCharacteristic: " + ByteUtil.bytesToHexString(value));
        if (!isConnected()) {
            Log.d(TAG, "writeRXCharacteristic: no connected!");
            return false;
        }
        if (bluetoothGatt == null) {
            Log.d(TAG, "writeRXCharacteristic: bluetoothGatt == null");
            return false;
        }
        BluetoothGattService service = bluetoothGatt.getService(serviceUUID);
        if (service == null) {
            bus.post(new BluEvent.DeviceUartNotSupported(serviceUUID + " - service not exist!"));
            return false;
        }
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUUID);
        if (characteristic == null) {
            bus.post(new BluEvent.DeviceUartNotSupported(characteristicUUID + " - characteristic not exist!"));
            return false;
        }
        characteristic.setValue(value);
        characteristic.setWriteType(withResponse ? BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT :
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        boolean status = bluetoothGatt.writeCharacteristic(characteristic);
        if (status) {
            Log.d(TAG, "--写入成功！");
        } else {
            Log.d(TAG, "--写入失败！");
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
