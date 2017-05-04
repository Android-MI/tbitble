package com.tbit.tbitblesdk.Bike.services.resolver;

import com.tbit.tbitblesdk.Bike.model.BikeState;

/**
 * Created by Salmon on 2017/4/27 0027.
 */

public interface Resolver {

    void resolveAll(BikeState bikeState, Byte[] data);

    void resolveControllerState(BikeState bikeState, Byte[] data);
}
