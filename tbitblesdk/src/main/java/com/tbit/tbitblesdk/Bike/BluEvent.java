package com.tbit.tbitblesdk.Bike;

import com.tbit.tbitblesdk.Bike.model.ManufacturerAd;

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
