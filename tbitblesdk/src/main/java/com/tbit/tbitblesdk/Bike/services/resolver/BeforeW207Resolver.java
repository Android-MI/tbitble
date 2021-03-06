package com.tbit.tbitblesdk.Bike.services.resolver;

import com.tbit.tbitblesdk.Bike.model.BikeState;
import com.tbit.tbitblesdk.Bike.util.StateUpdateHelper;

/**
 * Created by Salmon on 2017/4/27 0027.
 */

public class BeforeW207Resolver implements Resolver<BikeState> {

    @Override
    public void resolveAll(BikeState bikeState, Byte[] data) {
        StateUpdateHelper.updateAll(bikeState, data);
    }

    @Override
    public void resolveControllerState(BikeState bikeState, Byte[] data) {
        StateUpdateHelper.updateControllerState(bikeState, data);
    }

    @Override
    public void resolveLocations(BikeState bikeState, Byte[] data) {
        StateUpdateHelper.updateLocation(bikeState, data);
    }

    @Override
    public BikeState resolveCustomState(BikeState bikeState) {
        return bikeState;
    }
}
