package com.tbit.tbitblesdk.Bike.services.command.bikecommand;

import com.tbit.tbitblesdk.Bike.model.BikeState;
import com.tbit.tbitblesdk.protocol.Packet;
import com.tbit.tbitblesdk.protocol.PacketValue;
import com.tbit.tbitblesdk.Bike.ResultCode;
import com.tbit.tbitblesdk.protocol.callback.ResultCallback;
import com.tbit.tbitblesdk.Bike.services.command.callback.StateCallback;
import com.tbit.tbitblesdk.Bike.util.PacketUtil;

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
                stateCallback.onStateUpdated(bikeState);
                response(ResultCode.SUCCEED);
                break;
            }
        }
    }

    @Override
    protected Packet onCreateSendPacket(int sequenceId) {
        return PacketUtil.createPacket(sequenceId,
                (byte) 0x04, (byte) 0x05, null);
    }

    @Override
    public boolean compare(Packet receivedPacket) {
        return PacketUtil.compareCommandId(receivedPacket, getSendPacket())
                && PacketUtil.checkPacketValueContainKey(receivedPacket, 0x85);
    }
}
