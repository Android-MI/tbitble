package com.tbit.tbitblesdk.Bike.services.command.callback;

import com.tbit.tbitblesdk.Bike.model.BikeState;

/**
 * Created by Salmon on 2017/3/17 0017.
 */

public interface StateCallback {

    void onStateUpdated(BikeState bikeState);

}
