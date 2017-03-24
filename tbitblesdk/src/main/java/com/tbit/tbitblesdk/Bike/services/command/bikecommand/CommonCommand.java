package com.tbit.tbitblesdk.Bike.services.command.bikecommand;

import com.tbit.tbitblesdk.Bike.services.command.Command;
import com.tbit.tbitblesdk.protocol.Packet;
import com.tbit.tbitblesdk.Bike.ResultCode;
import com.tbit.tbitblesdk.protocol.callback.PacketCallback;
import com.tbit.tbitblesdk.protocol.callback.ResultCallback;
import com.tbit.tbitblesdk.Bike.util.PacketUtil;

/**
 * Created by Salmon on 2017/3/17 0017.
 */

public class CommonCommand extends Command {

    protected PacketCallback packetCallback;
    private Packet packetUnready;

    public CommonCommand(ResultCallback resultCallback, PacketCallback packetCallback, Packet packet) {
        super(resultCallback);
        this.packetCallback = packetCallback;
        this.packetUnready = packet;
    }

    @Override
    protected void onResult(Packet receivedPacket) {
        resultCallback.onResult(ResultCode.SUCCEED);
        packetCallback.onPacketReceived(receivedPacket);
    }

    @Override
    public boolean compare(Packet receivedPacket) {
        return PacketUtil.compareCommandId(receivedPacket, getSendPacket());
    }

    @Override
    protected Packet onCreateSendPacket(int sequenceId) {
        packetUnready.getHeader().setSequenceId((byte) sequenceId);
        return packetUnready;
    }
}
