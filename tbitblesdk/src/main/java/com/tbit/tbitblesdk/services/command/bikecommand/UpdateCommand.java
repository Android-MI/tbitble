package com.tbit.tbitblesdk.services.command.bikecommand;

import com.tbit.tbitblesdk.protocol.BikeState;
import com.tbit.tbitblesdk.protocol.Constant;
import com.tbit.tbitblesdk.protocol.Packet;
import com.tbit.tbitblesdk.protocol.PacketValue;
import com.tbit.tbitblesdk.protocol.ResultCode;
import com.tbit.tbitblesdk.services.command.callback.ResultCallback;
import com.tbit.tbitblesdk.services.command.callback.StateCallback;
import com.tbit.tbitblesdk.util.PacketUtil;

import java.util.List;

/**
 * Created by Salmon on 2017/3/15 0015.
 */

public class UpdateCommand extends UpdatableCommand {

    public UpdateCommand(ResultCallback resultCallback, StateCallback stateCallback,
                         BikeState bikeState) {
        super(resultCallback, stateCallback, bikeState);
    }

    @Override
    protected void onResult(Packet resultPacket) {
        PacketValue packetValue = resultPacket.getPacketValue();
        List<PacketValue.DataBean> resolvedData = packetValue.getData();
        for (PacketValue.DataBean b : resolvedData) {
            int key = b.key & 0xff;
            Byte[] value = b.value;
            if (key == 0x85) {
                parseAll(value);
                resultCallback.onResult(ResultCode.SUCCEED);
                stateCallback.onStateUpdated(bikeState);
                break;
            }
        }
    }

    @Override
    protected Packet onCreateSendPacket(int sequenceId) {
        return PacketUtil.createPacket(sequenceId,
                Constant.COMMAND_QUERY, Constant.QUERY_ALL, null);
    }

    @Override
    public boolean compare(Packet receivedPacket) {
        return PacketUtil.compareCommandId(receivedPacket, getSendPacket())
                && PacketUtil.checkPacketValueContainKey(receivedPacket, 0x85);
    }
}
