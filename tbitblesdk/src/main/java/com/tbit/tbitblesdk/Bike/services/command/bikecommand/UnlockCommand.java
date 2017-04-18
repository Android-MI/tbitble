package com.tbit.tbitblesdk.Bike.services.command.bikecommand;

import com.tbit.tbitblesdk.Bike.services.command.Command;
import com.tbit.tbitblesdk.protocol.Packet;
import com.tbit.tbitblesdk.protocol.PacketValue;
import com.tbit.tbitblesdk.Bike.ResultCode;
import com.tbit.tbitblesdk.protocol.callback.ResultCallback;
import com.tbit.tbitblesdk.Bike.util.PacketUtil;

import java.util.List;

/**
 * Created by Salmon on 2017/3/14 0014.
 */

public class UnlockCommand extends Command {

    public UnlockCommand(ResultCallback resultCallback) {
        super(resultCallback);
    }

    @Override
    protected void onResult(Packet resultPacket) {
        PacketValue packetValue = resultPacket.getPacketValue();
        List<PacketValue.DataBean> resolvedData = packetValue.getData();
        // 0x00：成功  0x01：指令非法 0x02：运动状态 0x03：非绑定状态
        int resultCode = ResultCode.FAILED;
        for (PacketValue.DataBean b : resolvedData) {
            int key = b.key & 0xff;
            Byte[] value = b.value;
            if (key == 0x82) {
                switch (value[0]) {
                    case 0x00:
                        resultCode = ResultCode.SUCCEED;
                        break;
                    case 0x01:
                        resultCode = ResultCode.ILLEGAL_COMMAND;
                        break;
                    case 0x02:
                        resultCode = ResultCode.MOTION_STATE;
                        break;
                    case 0x03:
                        resultCode = ResultCode.NOT_BINDING;
                        break;
                }
                break;
            }
        }

        response(resultCode);
    }

    @Override
    protected Packet onCreateSendPacket(int sequenceId) {
        return PacketUtil.createPacket(sequenceId, (byte) 0x03,
                (byte) 0x02, new Byte[]{0x00});
    }

    @Override
    public boolean compare(Packet receivedPacket) {
        return PacketUtil.compareCommandId(receivedPacket, getSendPacket())
                && PacketUtil.checkPacketValueContainKey(receivedPacket, 0x82);
    }

}
