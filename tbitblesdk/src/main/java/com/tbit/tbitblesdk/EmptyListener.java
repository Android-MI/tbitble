package com.tbit.tbitblesdk;

/**
 * Created by Salmon on 2016/12/7 0007.
 */

public class EmptyListener implements TbitListener {
    @Override
    public void onConnectResponse(int resultCode, BikeState state) {

    }

    @Override
    public void onVerifyResponse(int resultCode, BikeState state) {

    }

    @Override
    public void onUnlockResponse(int resultCode, BikeState state) {

    }

    @Override
    public void onLockResponse(int resultCode, BikeState state) {

    }

    @Override
    public void onStateUpdated(int resultCode, BikeState state) {

    }

    @Override
    public void onDisconnected(int resultCode, BikeState state) {

    }
}
