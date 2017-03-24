package com.tbit.tbitblesdk.Bike;

import com.tbit.tbitblesdk.Bike.model.BikeState;
import com.tbit.tbitblesdk.protocol.PacketValue;

/**
 * Created by Salmon on 2016/12/7 0007.
 */

public interface TbitListener {
    void onConnectResponse(int resultCode);
    void onUnlockResponse(int resultCode);
    void onLockResponse(int resultCode);
    void onUpdateResponse(int resultCode);
    void onStateUpdated(BikeState state);
    void onDisconnected(int resultCode);
    void onCommonCommandResponse(int resultCode, PacketValue packetValue);
}
