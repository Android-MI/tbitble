package com.tbit.tbitblesdk.Bike;

import com.tbit.tbitblesdk.Bike.model.BikeState;
import com.tbit.tbitblesdk.protocol.PacketValue;

/**
 * Created by Salmon on 2016/12/7 0007.
 */

public class EmptyListener implements TbitListener {

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
    public void onCommonCommandResponse(int resultCode, PacketValue packetValue) {

    }

    public static class EmptyDebugListener implements TbitDebugListener {

        @Override
        public void onLogStrReceived(String logStr) {

        }
    }
}
