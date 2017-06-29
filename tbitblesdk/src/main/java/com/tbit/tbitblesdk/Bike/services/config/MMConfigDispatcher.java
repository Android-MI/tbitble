package com.tbit.tbitblesdk.Bike.services.config;

import com.tbit.tbitblesdk.Bike.model.ManufacturerAd;
import com.tbit.tbitblesdk.bluetooth.debug.BleLog;

/**
 * author: Salmon
 * date: 2017-06-27 10:04
 * github: https://github.com/billy96322
 * email: salmonzhg@foxmail.com
 */

public class MMConfigDispatcher implements ConfigDispatcher {

    @Override
    public BikeConfig dispatch(ManufacturerAd manufacturerAd) {
        int hardVersion = manufacturerAd.getHardwareVersion();

        int softVersion = manufacturerAd.getSoftwareVersion();

        int mainHardVersion = (hardVersion & 0xe0) >> 5;

        int subHardVersion = hardVersion & 0x1f;

        BikeConfig result;

        switch (softVersion) {
            case 0:
                result = new Config_207();
                break;
            case 1:
                result = new Config_207_2();
                break;
            default:
                result = new Config_207_2();
        }

        BleLog.log("BikeConfigDispatcher", result == null ? "null" : result.getClass().getSimpleName());

        return result;
    }
}
