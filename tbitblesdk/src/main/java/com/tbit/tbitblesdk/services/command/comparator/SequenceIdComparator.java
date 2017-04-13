package com.tbit.tbitblesdk.services.command.comparator;

import com.tbit.tbitblesdk.protocol.Packet;
import com.tbit.tbitblesdk.services.command.Command;

/**
 * Created by Salmon on 2017/3/16 0016.
 */

public class SequenceIdComparator implements CommandComparator {

    @Override
    public boolean compare(Command command, Packet receivedPacket) {
        int sendSequenceId = command.getSendPacket().getL1Header().getSequenceId();
        int receivedSequenceId = receivedPacket.getL1Header().getSequenceId();
        return sendSequenceId == receivedSequenceId;
    }
}