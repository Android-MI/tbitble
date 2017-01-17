package com.tbit.tbitblesdk.protocol;

import android.bluetooth.BluetoothGattDescriptor;

import java.util.UUID;

/**
 * Created by Salmon on 2016/12/6 0006.
 */

public class BluEvent {

    public enum CharState {
        CHANGE, READ, WRITE
    }

    public enum State {
        SUCCEED, FAILED
    }

    public enum OtaState {
        START, FAILED, SUCCEED, UPDATING
    }

    public static class ConnectionStateChange {
        public int status;
        public int newState;

        public ConnectionStateChange(int status, int newState) {
            this.status = status;
            this.newState = newState;
        }
    }

    public static class DisConnected {
    }

    public static class DiscoveredSucceed {
    }

    public static class ChangeCharacteristic {
        public CharState state;
        public UUID serviceUuid;
        public UUID characterUuid;
        public byte[] value;
        public int status;

        public ChangeCharacteristic(CharState state,
                                    UUID serviceUuid,
                                    UUID characterUuid,
                                    byte[] value, int status) {
            this.state = state;
            this.serviceUuid = serviceUuid;
            this.characterUuid = characterUuid;
            this.value = value;
            this.status = status;
        }
    }

    public static class ChangeDescriptor {
        public CharState state;
        public BluetoothGattDescriptor descriptor;
        public int status;

        public ChangeDescriptor(CharState state, BluetoothGattDescriptor descriptor, int status) {
            this.state = state;
            this.descriptor = descriptor;
            this.status = status;
        }
    }

    public static class ReadRssi {
        public int rssi;

        public ReadRssi(int rssi) {
            this.rssi = rssi;
        }
    }

    public static class DeviceUartNotSupported {
        public String message;

        public DeviceUartNotSupported() {
        }

        public DeviceUartNotSupported(String message) {
            this.message = message;
        }
    }

    public static class CommonFailedReport {
        public String functionName;
        public String message;

        public CommonFailedReport(String functionName, String message) {
            this.functionName = functionName;
            this.message = message;
        }
    }

    public static class WriteData {
        public int requestId;
        public State state;

        public WriteData(int requestId, State state) {
            this.requestId = requestId;
            this.state = state;
        }
    }

    public static class ScanTimeOut {
    }

    public static class BleNotOpened {

    }

//    public static class Verified {
//        public State state;
//
//        public Verified(State state) {
//            this.state = state;
//        }
//    }

    public static class DebugLogEvent {
        private String key;
        private String logStr;

        public DebugLogEvent(String key, String logStr) {
            this.key = key;
            this.logStr = logStr;
        }

        public String getLogStr() {
            return logStr;
        }

        public String getKey() {
            return key;
        }
    }

    public static class Ota {
        private OtaState state;
        private int progress;
        private int failedCode;

        public Ota(OtaState state) {
            this.state = state;
        }

        public static Ota getProgressInstance(int progress) {
            Ota ota = new Ota(OtaState.UPDATING);
            ota.progress = progress;
            return ota;
        }

        public static Ota getFailedInstance(int failedCode) {
            Ota ota = new Ota(OtaState.FAILED);
            ota.failedCode = failedCode;
            return ota;
        }

        public OtaState getState() {
            return state;
        }

        public int getProgress() {
            return progress;
        }

        public int getFailedCode() {
            return failedCode;
        }
    }

    public static class UpdateBikeState {

    }

    public static class VersionResponse {
        public int deviceVersion;
        public int firmwareVersion;

        public VersionResponse(int deviceVersion, int firmwareVersion) {
            this.deviceVersion = deviceVersion;
            this.firmwareVersion = firmwareVersion;
        }
    }
}
