package com.tbit.tbitblesdk.protocol;

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
        public UUID characterUuid;
        public int status;

        public ChangeDescriptor(CharState state, UUID characterUuid, int status) {
            this.state = state;
            this.characterUuid = characterUuid;
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
        public int failCode;

        public WriteData(int requestId, State state) {
            this.requestId = requestId;
            this.state = state;
        }

        public WriteData(int requestId, int failCode) {
            this.requestId = requestId;
            this.state = State.FAILED;
            this.failCode = failCode;
        }
    }

    public static class CommonResponse {
        public PacketValue packetValue;
        public int code;

        public CommonResponse(int code, PacketValue packetValue) {
            this.packetValue = packetValue;
            this.code = code;
        }

        public CommonResponse(int code) {
            this.code = code;
        }
    }

    public static class ScanTimeOut {
    }

    public static class BleNotOpened {
    }

    public static class OtaStart {
    }

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


    public static class UpdateBikeState {

    }


    public static class BleBroadcast {
        public ManufacturerAd manufacturerAd;

        public BleBroadcast(ManufacturerAd manufacturerAd) {
            this.manufacturerAd = manufacturerAd;
        }
    }

}
