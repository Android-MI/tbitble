package com.tbit.tbitblesdk.Bike.services.resolver;

import com.tbit.tbitblesdk.Bike.model.BikeState;
import com.tbit.tbitblesdk.Bike.util.StateUpdateHelper;

/**
 * Created by Salmon on 2017/4/27 0027.
 */

public class BeforeW207Resolver implements Resolver {

    @Override
    public void resolveAll(BikeState bikeState, Byte[] data) {
        StateUpdateHelper.updateAll(bikeState, data);
    }

    @Override
    public void resolveControllerState(BikeState bikeState, Byte[] data) {
        StateUpdateHelper.updateControllerState(bikeState, data);
    }
}