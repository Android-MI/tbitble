package com.tbit.tbitblesdk.services.command.bikecommand;

import com.tbit.tbitblesdk.protocol.Constant;
import com.tbit.tbitblesdk.protocol.Packet;
import com.tbit.tbitblesdk.protocol.PacketValue;
import com.tbit.tbitblesdk.protocol.ResultCode;
import com.tbit.tbitblesdk.services.command.callback.ResultCallback;
import com.tbit.tbitblesdk.util.PacketUtil;

import java.util.List;

/**
 * Created by Salmon on 2017/3/14 0014.
 */

public class UnlockCommand extends BikeCommand {

    public UnlockCommand(ResultCallback resultCallback) {
        super(resultCallback);
    }

    @Override
    protected void onResult(Packet resultPacket) {
        PacketValue packetValue = resultPacket.getPacketValue();
        List<PacketValue.DataBean> resolvedData = packetValue.getData();
        // 0x00：成功  0x01：指令非法 0x02：运动状态 0x03：非绑定状态
        for (PacketValue.DataBean b : resolvedData) {
            int key = b.key & 0xff;
            Byte[] value = b.value;
            if (key == 0x82) {
                switch (value[0]) {
                    case 0x00:
                        resultCallback.onResult(ResultCode.SUCCEED);
                        break;
                    case 0x01:
                        resultCallback.onResult(ResultCode.ILLEGAL_COMMAND);
                        break;
                    case 0x02:
                        resultCallback.onResult(ResultCode.MOTION_STATE);
                        break;
                    case 0x03:
                        resultCallback.onResult(ResultCode.NOT_BINDING);
                        break;
                }
                break;
            }
        }
    }

    @Override
    protected Packet onCreateSendPacket(int sequenceId) {
        return PacketUtil.createPacket(sequenceId, Constant.COMMAND_SETTING,
                Constant.SETTING_KEY_LOCK, new Byte[]{Constant.VALUE_OFF});
    }

    @Override
    public boolean compare(Packet receivedPacket) {
        return PacketUtil.compareCommandId(receivedPacket, getSendPacket())
                && PacketUtil.checkPacketValueContainKey(receivedPacket, 0x82);
    }

}
