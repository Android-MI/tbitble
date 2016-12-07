package com.tbit.tbitblesdk;

/**
 * Created by Salmon on 2016/12/7 0007.
 */

public interface TbitListener {
    void onConnectResponse(int resultCode, BikeState state);
    void onVerifyResponse(int resultCode, BikeState state);
    void onUnlockResponse(int resultCode, BikeState state);
    void onLockResponse(int resultCode, BikeState state);
    void onStateUpdated(int resultCode, BikeState state);
    void onDisconnected(int resultCode, BikeState state);
}
