package com.tbit.tbitblesdk.Bike.services.command.comparator;

import com.tbit.tbitblesdk.protocol.Packet;
import com.tbit.tbitblesdk.Bike.services.command.Command;

/**
 * Created by Salmon on 2017/3/16 0016.
 */

public interface CommandComparator {

    boolean compare(Command command, Packet receivedPacket);

}
