package com.tbit.tbitblesdk.protocol.callback;

import com.tbit.tbitblesdk.protocol.Packet;

/**
 * Created by Salmon on 2017/3/17 0017.
 */

public interface PacketCallback {

    void onPacketReceived(Packet packet);

}
