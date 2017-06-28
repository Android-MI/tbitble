package com.tbit.tbitblesdk.Bike.services;

import com.tbit.tbitblesdk.Bike.model.BikeState;
import com.tbit.tbitblesdk.Bike.util.StateUpdateHelper;
import com.tbit.tbitblesdk.bluetooth.IBleClient;
import com.tbit.tbitblesdk.bluetooth.RequestDispatcher;
import com.tbit.tbitblesdk.bluetooth.debug.BleLog;
import com.tbit.tbitblesdk.bluetooth.util.ByteUtil;
import com.tbit.tbitblesdk.protocol.Packet;
import com.tbit.tbitblesdk.protocol.dispatcher.PacketResponseListener;
import com.tbit.tbitblesdk.protocol.dispatcher.ReceivedPacketDispatcher;

/**
 * author: Salmon
 * date: 2017-06-28 17:04
 * github: https://github.com/billy96322
 * email: salmonzhg@foxmail.com
 */

public class BikeReceivedPacketDispatcher extends ReceivedPacketDispatcher {
    private BikeState bikeState;

    public BikeReceivedPacketDispatcher(IBleClient bleClient, RequestDispatcher requestDispatcher, BikeState bikeState) {
        super(bleClient, requestDispatcher);
        this.bikeState = bikeState;
    }

    @Override
    protected void publishData(byte[] data) {
        BleLog.log("ReceivedDispatcherPublish", ByteUtil.bytesToHexString(data));
        Packet packet = new Packet(data);

        // 更新头部
        StateUpdateHelper.updateSysState(bikeState, packet.getHeader().getSystemState());

        if (!packet.getHeader().isAck()) {

            // 0x09是板间命令，不做应答和解析
            if (packet.getPacketValue().getCommandId() == 0x09) {
                BleLog.log("ReceivedDispatcherPublish", "drop broad command");
                return;
            }

            getAckSender().sendACK(packet.getHeader().getSequenceId(), false);
        }

        for (PacketResponseListener listener : getPacketResponseList()) {
            if (listener.onPacketReceived(packet))
                break;
        }
    }
}
