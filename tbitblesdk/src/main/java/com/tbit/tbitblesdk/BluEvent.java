package com.tbit.tbitblesdk;

/**
 * Created by Salmon on 2016/12/6 0006.
 */

public class BluEvent {

    public enum CharState {
        CHANGE, READ, WRITE
    }

    public static class ConnectionStateChange {
        int status;
        int newState;

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
        byte[] value;
        CharState state;

        public ChangeCharacteristic(CharState state,
                                    byte[] value) {
            this.state = state;
            this.value = value;
        }
    }

    public static class ReadRssi {
        int rssi;

        public ReadRssi(int rssi) {
            this.rssi = rssi;
        }
    }

    public static class DeviceUartNotSupported {
        String message;

        public DeviceUartNotSupported() {
        }

        public DeviceUartNotSupported(String message) {
            this.message = message;
        }
    }

    public static class CommonFailedReport {
        String functionName;
        String message;

        public CommonFailedReport(String functionName, String message) {
            this.functionName = functionName;
            this.message = message;
        }
    }

    public static class SendSuccess {
        public int requestId;

        public SendSuccess(int requestId) {
            this.requestId = requestId;
        }
    }

    public static class SendFailed {
        public int requestId;

        public SendFailed(int requestId) {
            this.requestId = requestId;
        }
    }

    public static class ScanTimeOut {
    }

    public static class VerifySucceed {
    }

    public static class VerifyFailed {
        int failCode;

        public VerifyFailed() {

        }

        public VerifyFailed(int failCode) {
            this.failCode = failCode;
        }
    }

    public static class Ota {
    }
}
