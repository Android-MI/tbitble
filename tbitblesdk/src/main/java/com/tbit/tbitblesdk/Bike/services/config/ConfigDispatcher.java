package com.tbit.tbitblesdk.Bike.services.config;

import com.tbit.tbitblesdk.Bike.model.ManufacturerAd;

/**
 * author: Salmon
 * date: 2017-06-27 09:57
 * github: https://github.com/billy96322
 * email: salmonzhg@foxmail.com
 */

public interface ConfigDispatcher {

    BikeConfig dispatch(ManufacturerAd manufacturerAd);
}
