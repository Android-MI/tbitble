package com.tbit.tbitblesdk.services.command.comparator;

import com.tbit.tbitblesdk.protocol.Packet;
import com.tbit.tbitblesdk.services.command.Command;

/**
 * Created by Salmon on 2017/3/16 0016.
 */

public interface CommandComparator {

    boolean compare(Command command, Packet receivedPacket);

}
