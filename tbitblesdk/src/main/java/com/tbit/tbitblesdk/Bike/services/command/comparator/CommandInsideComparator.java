package com.tbit.tbitblesdk.Bike.services.command.comparator;

import com.tbit.tbitblesdk.protocol.Packet;
import com.tbit.tbitblesdk.Bike.services.command.Command;

/**
 * Created by Salmon on 2017/3/16 0016.
 */

public class CommandInsideComparator implements CommandComparator {

    @Override
    public boolean compare(Command command, Packet receivedPacket) {
        return command.compare(receivedPacket);
    }
}
