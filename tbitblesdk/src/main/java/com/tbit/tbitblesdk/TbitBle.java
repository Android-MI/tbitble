package com.tbit.tbitblesdk;

import android.content.Context;

import com.tbit.tbitblesdk.protocol.BikeState;
import com.tbit.tbitblesdk.services.command.Command;
import com.tbit.tbitblesdk.services.command.callback.PacketCallback;
import com.tbit.tbitblesdk.services.command.callback.ProgressCallback;
import com.tbit.tbitblesdk.services.command.callback.ResultCallback;
import com.tbit.tbitblesdk.services.command.callback.RssiCallback;
import com.tbit.tbitblesdk.services.command.callback.SimpleCommonCallback;
import com.tbit.tbitblesdk.services.command.callback.StateCallback;
import com.tbit.tbitblesdk.services.scanner.ScannerCallback;

import java.io.File;

/**
 * Created by Salmon on 2016/12/5 0005.
 */

public class TbitBle {

    private static TbitBleInstance instance;

    private TbitBle() {
    }

    public static void initialize(Context context, ProtocolAdapter protocolAdapter) {
        if (instance == null) {
            instance = new TbitBleInstance();
            BleGlob.setContext(context);

            ProtocolInfo.packetCrcTable = protocolAdapter.getPacketCrcTable();
            ProtocolInfo.adKey = protocolAdapter.getAdKey();
            ProtocolInfo.maxEncryptCount = protocolAdapter.getMaxAdEncryptedCount();
        }
    }

    @Deprecated
    public static void setListener(TbitListener listener) {
        checkInstanceNotNull();
        instance.setListener(listener);
    }

    public static void unSetListener() {
        if (instance != null)
            instance.setListener(null);
    }

    public static void setDebugListener(TbitDebugListener listener) {
        checkInstanceNotNull();
        instance.setDebugListener(listener);
    }

    public static void connect(String macAddr, String key) {
        checkInstanceNotNull();
        instance.connect(macAddr, key);
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
                                     SimpleCommonCallback simpleCommonCallback) {
        checkInstanceNotNull();
        instance.common(commandId, key, value, simpleCommonCallback, simpleCommonCallback);
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

    public static void unlock() {
        checkInstanceNotNull();
        instance.unlock();
    }

    public static void lock() {
        checkInstanceNotNull();
        instance.lock();
    }

    public static void update() {
        checkInstanceNotNull();
        instance.update();
    }

    public static void reconnect(ResultCallback resultCallback, StateCallback stateCallback) {
        checkInstanceNotNull();
        instance.reConnect(resultCallback, stateCallback);
    }

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

    public static void readRssi(ResultCallback resultCallback, RssiCallback rssiCallback) {
        checkInstanceNotNull();
        instance.readRssi(resultCallback, rssiCallback);
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
