package com.tbit.tbitblesdk.Bike;

import android.content.Context;

import com.tbit.tbitblesdk.bluetooth.BleGlob;
import com.tbit.tbitblesdk.Bike.model.BikeState;
import com.tbit.tbitblesdk.Bike.services.command.Command;
import com.tbit.tbitblesdk.protocol.ProtocolAdapter;
import com.tbit.tbitblesdk.protocol.ProtocolInfo;
import com.tbit.tbitblesdk.protocol.callback.PacketCallback;
import com.tbit.tbitblesdk.protocol.callback.ProgressCallback;
import com.tbit.tbitblesdk.protocol.callback.ResultCallback;
import com.tbit.tbitblesdk.Bike.services.command.callback.StateCallback;
import com.tbit.tbitblesdk.bluetooth.scanner.ScannerCallback;

import java.io.File;

/**
 * Created by Salmon on 2016/12/5 0005.
 */

public class TbitBle {

    private static TbitBleInstance instance;

    private TbitBle() {
    }

    public static void initialize(Context context, ProtocolAdapter adapter) {
        if (instance == null) {
            instance = new TbitBleInstance();
            BleGlob.setContext(context);

            ProtocolInfo.packetCrcTable = adapter.getPacketCrcTable();
            ProtocolInfo.adKey = adapter.getAdKey();
            ProtocolInfo.maxEncryptCount = adapter.getMaxAdEncryptedCount();
        }
    }

    @Deprecated
    public static void setListener(TbitListener listener) {
        checkInstanceNotNull();
        instance.setListener(listener);
    }

    @Deprecated
    public static void unSetListener() {
        if (instance != null)
            instance.setListener(null);
    }

    public static void setDebugListener(TbitDebugListener listener) {
        checkInstanceNotNull();
        instance.setDebugListener(listener);
    }

    @Deprecated
    public static void connect(String macAddr, String key) {
        checkInstanceNotNull();
        instance.connect(macAddr, key);
    }

    public static void connect(String machineId, String key, ResultCallback resultCallback, StateCallback stateCallback) {
        checkInstanceNotNull();
        instance.connect(machineId, key, resultCallback, stateCallback);
    }

    public static int startScan(ScannerCallback callback, long timeout) {
        checkInstanceNotNull();
        return instance.startScan(callback, timeout);
    }

    public static void stopScan() {
        checkInstanceNotNull();
        instance.stopScan();
    }

    public static void commonCommand(Command command) {
        checkInstanceNotNull();
        instance.common(command);
    }

    public static void commonCommand(byte commandId, byte key, Byte[] value,
                                     ResultCallback resultCallback, PacketCallback packetCallback) {
        checkInstanceNotNull();
        instance.common(commandId, key, value, resultCallback, packetCallback);
    }

    @Deprecated
    public static void commonCommand(byte commandId, byte key, Byte[] value) {
        checkInstanceNotNull();
        instance.common(commandId, key, value);
    }

    @Deprecated
    public static void unlock() {
        checkInstanceNotNull();
        instance.unlock();
    }

    public static void unlock(ResultCallback resultCallback) {
        checkInstanceNotNull();
        instance.unlock(resultCallback);
    }

    @Deprecated
    public static void lock() {
        checkInstanceNotNull();
        instance.lock();
    }

    public static void lock(ResultCallback resultCallback) {
        checkInstanceNotNull();
        instance.lock(resultCallback);
    }

    @Deprecated
    public static void update() {
        checkInstanceNotNull();
        instance.update();
    }

    public static void update(ResultCallback resultCallback, StateCallback stateCallback) {
        checkInstanceNotNull();
        instance.update(resultCallback, stateCallback);
    }

    @Deprecated
    public static void reconnect(ResultCallback resultCallback, StateCallback stateCallback) {
        checkInstanceNotNull();
        instance.reConnect(resultCallback, stateCallback);
    }

    @Deprecated
    public static void reconnect() {
        checkInstanceNotNull();
        instance.reConnect();
    }

    public static int getBleConnectionState() {
        checkInstanceNotNull();
        return instance.getBleConnectionState();
    }

    public static BikeState getState() {
        checkInstanceNotNull();
        return instance.getState();
    }

    public static void disConnect() {
        checkInstanceNotNull();
        instance.disConnect();
    }

    public static void ota(File file, ResultCallback resultCallback,
                           ProgressCallback progressCallback) {
        checkInstanceNotNull();
        instance.ota(file, resultCallback, progressCallback);
    }

    public static void connectiveOta(String machineNo, String key, File file,
                                     ResultCallback resultCallback, ProgressCallback progressCallback) {
        checkInstanceNotNull();
        instance.connectiveOta(machineNo, key, file, resultCallback, progressCallback);
    }

    public static void destroy() {
        checkInstanceNotNull();
        instance.destroy();
        instance = null;
    }

    public static boolean hasInitialized() {
        return instance != null;
    }

    private static void checkInstanceNotNull() {
        if (instance == null)
            throw new RuntimeException("have you 'initialize' on TbitBle ? ");
    }
}
