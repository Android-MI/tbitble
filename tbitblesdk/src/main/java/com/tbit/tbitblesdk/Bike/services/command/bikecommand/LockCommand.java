package com.tbit.tbitblesdk.Bike.services.command.bikecommand;

import com.tbit.tbitblesdk.protocol.Packet;
import com.tbit.tbitblesdk.protocol.PacketValue;
import com.tbit.tbitblesdk.Bike.ResultCode;
import com.tbit.tbitblesdk.protocol.callback.ResultCallback;
import com.tbit.tbitblesdk.Bike.util.PacketUtil;
import com.tbit.tbitblesdk.Bike.services.command.Command;

import java.util.List;

/**
 * Created by Salmon on 2017/3/15 0015.
 */

public class LockCommand extends Command {

    public LockCommand(ResultCallback resultCallback) {
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
            if (key == 0x81) {
                if (value[0] != 0) {
                    retry();
                    break;
                }
                int resultCode = ResultCode.FAILED;
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
                response(resultCode);
                break;
            }
        }
    }

    @Override
    protected int getRetryTimes() {
        return 3;
    }

    @Override
    protected int getTimeout() {
        return 15000;
    }

    @Override
    protected Packet onCreateSendPacket(int sequenceId) {
        return PacketUtil.createPacket(sequenceId, (byte) 0x03,
                (byte) 0x01, new Byte[]{0x01});
    }

    @Override
    public boolean compare(Packet receivedPacket) {
        return PacketUtil.compareCommandId(receivedPacket, getSendPacket())
                && PacketUtil.checkPacketValueContainKey(receivedPacket, 0x81);
    }

}
