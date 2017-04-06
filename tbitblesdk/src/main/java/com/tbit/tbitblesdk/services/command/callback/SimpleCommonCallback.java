package com.tbit.tbitblesdk.services.command.callback;

import com.tbit.tbitblesdk.protocol.Packet;
import com.tbit.tbitblesdk.protocol.PacketValue;
import com.tbit.tbitblesdk.protocol.ResultCode;

import java.util.List;

/**
 * Created by Salmon on 2017/4/6 0006.
 */

public class SimpleCommonCallback implements ResultCallback, PacketCallback {

    private ResultCallback resultCallback;

    public SimpleCommonCallback(ResultCallback resultCallback) {
        this.resultCallback = resultCallback;
    }

    @Override
    public void onPacketReceived(Packet packet) {
        try {
            PacketValue packetValue = packet.getPacketValue();
            List<PacketValue.DataBean> resolvedData = packetValue.getData();

            Byte[] value = resolvedData.get(0).value;

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
                default:
                    resultCallback.onResult(ResultCode.FAILED);
            }
        } catch (Exception e) {
            resultCallback.onResult(ResultCode.FAILED);
            e.printStackTrace();
        }
    }

    @Override
    public void onResult(int resultCode) {
        if (resultCode != 0)
            resultCallback.onResult(resultCode);
    }

}
