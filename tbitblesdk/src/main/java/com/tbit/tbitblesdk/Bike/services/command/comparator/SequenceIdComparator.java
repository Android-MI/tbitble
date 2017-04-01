package com.tbit.tbitblesdk.Bike.services.command.comparator;

import com.tbit.tbitblesdk.protocol.Packet;
import com.tbit.tbitblesdk.Bike.services.command.Command;

/**
 * Created by Salmon on 2017/3/16 0016.
 */

public class SequenceIdComparator implements CommandComparator {

    @Override
    public boolean compare(Command command, Packet receivedPacket) {
        int sendSequenceId = command.getSendPacket().getHeader().getSequenceId();
        int receivedSequenceId = receivedPacket.getHeader().getSequenceId();
        return sendSequenceId == receivedSequenceId;
    }
}
