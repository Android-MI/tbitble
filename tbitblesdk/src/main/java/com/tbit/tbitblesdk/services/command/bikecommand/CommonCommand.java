package com.tbit.tbitblesdk.services.command.bikecommand;

import com.tbit.tbitblesdk.protocol.Packet;
import com.tbit.tbitblesdk.protocol.ResultCode;
import com.tbit.tbitblesdk.services.command.CommandHolder;
import com.tbit.tbitblesdk.services.command.callback.PacketCallback;
import com.tbit.tbitblesdk.services.command.callback.ResultCallback;
import com.tbit.tbitblesdk.util.PacketUtil;

/**
 * Created by Salmon on 2017/3/17 0017.
 */

public class CommonCommand extends BikeCommand {

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
        packetUnready.setHeadSerialNo((byte) sequenceId);
        return packetUnready;
    }
}
