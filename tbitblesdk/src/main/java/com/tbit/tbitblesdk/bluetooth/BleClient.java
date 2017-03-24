package com.tbit.tbitblesdk.bluetooth;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Build;
import android.util.Log;

import com.tbit.tbitblesdk.bluetooth.listener.ChangeCharacterListener;
import com.tbit.tbitblesdk.bluetooth.listener.ConnectStateChangeListener;
import com.tbit.tbitblesdk.bluetooth.listener.ReadCharacterListener;
import com.tbit.tbitblesdk.bluetooth.listener.ReadDescriptorListener;
import com.tbit.tbitblesdk.bluetooth.listener.ReadRssiListener;
import com.tbit.tbitblesdk.bluetooth.listener.ServiceDiscoverListener;
import com.tbit.tbitblesdk.bluetooth.listener.WriteCharacterListener;
import com.tbit.tbitblesdk.bluetooth.listener.WriteDescriptorListener;
import com.tbit.tbitblesdk.bluetooth.util.ByteUtil;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;

/**
 * Created by Salmon on 2016/12/6 0006.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BleClient implements IBleClient {
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_SCANNING = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;
    public static final int STATE_SERVICES_DISCOVERED = 4;
    private static final String TAG = "BluetoothIO";
    private int connectionState = STATE_DISCONNECTED;
    private BluetoothGatt bluetoothGatt;
    private ListenerManager listenerManager;

    private BluetoothGattCallback coreGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            for (ConnectStateChangeListener listener : listenerManager.connectStateChangeListeners) {
                listener.onConnectionStateChange(status, newState);
            }
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                connectionState = STATE_CONNECTED;
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                connectionState = STATE_DISCONNECTED;
                gatt.close();
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
            } else {
                refreshDeviceCache(gatt);
                disconnectInternal();
            }
            for (ServiceDiscoverListener listener : listenerManager.serviceDiscoverListeners) {
                listener.onServicesDiscovered(status);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

            for (WriteCharacterListener listener : listenerManager.writeCharacterListeners) {
                listener.onCharacteristicWrite(characteristic, status, characteristic.getValue());
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            for (ChangeCharacterListener listener : listenerManager.changeCharacterListeners) {
                listener.onCharacterChange(characteristic, characteristic.getValue());
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);

            for (ReadRssiListener listener : listenerManager.readRssiListeners) {
                listener.onReadRemoteRssi(rssi, status);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);

            for (WriteDescriptorListener listener : listenerManager.writeDescriptorListeners) {
                listener.onDescriptorWrite(descriptor, status);
            }

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);

            for (ReadCharacterListener listener : listenerManager.readCharacterListeners) {
                listener.onCharacteristicRead(characteristic, status,
                        characteristic.getValue());
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);

            for (ReadDescriptorListener listener : listenerManager.readDescriptorListeners) {
                listener.onDescriptorWrite(descriptor, status);
            }
        }
    };

    public BleClient() {
        listenerManager = new ListenerManager();
    }

    private boolean isConnected() {
        return connectionState >= STATE_CONNECTED;
    }

    @Override
    public int getConnectionState() {
        return connectionState;
    }

    @Override
    public void connect(final BluetoothDevice device, final boolean autoConnect) {
        disconnectInternal();
        Log.i(TAG, "connect name：" + device.getName()
                + " mac:" + device.getAddress()
                + " autoConnect：" + autoConnect);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bluetoothGatt = device.connectGatt(BleGlob.getContext(), autoConnect, coreGattCallback, BluetoothDevice.TRANSPORT_LE);
        } else {
            bluetoothGatt = device.connectGatt(BleGlob.getContext(), autoConnect, coreGattCallback);
        }
//        refreshDeviceCache(bluetoothGatt);
//        bluetoothGatt.connect();
    }

    @Override
    public void disconnect() {
        disconnectInternal();
    }

    private void disconnectInternal() {
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

    @Override
    public void close() {
        listenerManager.removeAll();
        disconnectInternal();
    }

    @Override
    public ListenerManager getListenerManager() {
        return listenerManager;
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

    @Override
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

    @Override
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

    @Override
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
            return false;
        }
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUUID);
        if (characteristic == null) {
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
