package com.tbit.tbitblesdk.Bike;

import com.tbit.tbitblesdk.Bike.model.ManufacturerAd;

/**
 * Created by Salmon on 2016/12/6 0006.
 */

public class BluEvent {

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


    public static class BleBroadcast {
        public ManufacturerAd manufacturerAd;

        public BleBroadcast(ManufacturerAd manufacturerAd) {
            this.manufacturerAd = manufacturerAd;
        }
    }

}
