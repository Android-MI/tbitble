package com.tbit.tbitblesdk.Bike.tasks;

import com.tbit.tbitblesdk.Bike.ResultCode;
import com.tbit.tbitblesdk.Bike.model.ManufacturerAd;
import com.tbit.tbitblesdk.Bike.model.ParsedAd;
import com.tbit.tbitblesdk.Bike.services.config.BikeConfig;
import com.tbit.tbitblesdk.Bike.services.config.BikeConfigDispatcher;
import com.tbit.tbitblesdk.Bike.tasks.exceptions.ResultCodeThrowable;

import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;

/**
 * Created by Salmon on 2017/4/12 0012.
 */

public class ResolveAdObservable implements ObservableOnSubscribe<BikeConfig> {

    private byte[] originData;

    public ResolveAdObservable(byte[] originData) {
        this.originData = originData;
    }

    @Override
    public void subscribe(@NonNull ObservableEmitter<BikeConfig> e) throws Exception {
        try {
            ParsedAd ad = ParsedAd.parseData(originData);
            byte[] data = ad.getManufacturer();
            ManufacturerAd manufacturerAd = ManufacturerAd.resolveManufacturerAd(data);
            BikeConfig bikeConfig = BikeConfigDispatcher.dispatch(manufacturerAd);
            e.onNext(bikeConfig);
        } catch (Exception e1) {
            e.onError(new ResultCodeThrowable("ResolveAdObservable: " + e1.getMessage(),
                    ResultCode.BROARCAST_RESOLUTION_FAILED));
        }
    }
}
