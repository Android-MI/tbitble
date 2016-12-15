package com.tbit.tbitblesdksample;

import com.tbit.tbitblesdk.TbitListener;
import com.tbit.tbitblesdk.protocol.BikeState;

/**
 * Created by Salmon on 2016/12/15 0015.
 */

public abstract class TbitListenerAdapter implements TbitListener {

    @Override
    public void onConnectResponse(int resultCode) {

    }

    @Override
    public void onUnlockResponse(int resultCode) {

    }

    @Override
    public void onLockResponse(int resultCode) {

    }

    @Override
    public void onUpdateResponse(int resultCode) {

    }

    @Override
    public void onStateUpdated(BikeState state) {

    }

    @Override
    public void onDisconnected(int resultCode) {

    }

    @Override
    public void onCommonCommandResponse(int resultCode) {

    }
}
