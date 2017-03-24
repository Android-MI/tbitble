package com.tbit.tbitblesdk.protocol.dispatcher;

import com.tbit.tbitblesdk.protocol.Packet;

/**
 * Created by Salmon on 2017/3/23 0023.
 */

public interface PacketResponseListener {

    boolean onPacketReceived(Packet packet);

}
