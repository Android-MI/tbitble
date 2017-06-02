package com.tbit.tbitblesdk.Bike.services.config;

import android.util.Log;

import com.tbit.tbitblesdk.Bike.model.ManufacturerAd;
import com.tbit.tbitblesdk.bluetooth.debug.BleLog;
import com.tbit.tbitblesdk.protocol.ProtocolInfo;

/**
 * Created by Salmon on 2017/3/20 0020.
 */

public class BikeConfigDispatcher {

    public static BikeConfig dispatch(ManufacturerAd manufacturerAd) {

        if (ProtocolInfo.bikeConfig != null) return ProtocolInfo.bikeConfig;

        int hardVersion = manufacturerAd.getHardwareVersion();

        int softVersion = manufacturerAd.getSoftwareVersion();

        // 0xe0 => 1110 0000 高3位代表主板本号
        int mainHardVersion = (hardVersion & 0xe0) >> 5;

        // 0x1f => 0001 1111 低5位代表副板本号
        int subHardVersion = hardVersion & 0x1f;

        BikeConfig result;

        switch (mainHardVersion) {
            case 0: {
                switch (softVersion) {
                    case 0:
                    case 1:
                    case 2:
                        result = new Config_205B();
                        break;
                    case 3:
                    case 4:
                        result = new Config_205D();
                        break;
                    default:
                        result = new Config_205D();
                        break;
                }
                break;
            }
            case 1: {
                switch (subHardVersion) {
                    case 1:
                        result = new Config_206();
                        break;
                    case 2:
                        result = new Config_206();
                        break;
                    case 3:
                        result = new Config_207();
                        break;
                    default:
                        result = new Config_206();
                        break;
                }
            }
                break;
            default:
                result = new Config_206();
                break;
        }

        BleLog.log("BikeConfigDispatcher", result == null ? "null" : result.getClass().getSimpleName());

        return result;
    }
}
