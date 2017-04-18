package com.tbit.tbitblesdk.Bike.services.command.callback;

import com.tbit.tbitblesdk.Bike.ResultCode;
import com.tbit.tbitblesdk.protocol.Packet;
import com.tbit.tbitblesdk.protocol.PacketValue;
import com.tbit.tbitblesdk.protocol.callback.PacketCallback;
import com.tbit.tbitblesdk.protocol.callback.ResultCallback;

import java.util.List;

/**
 * Created by Salmon on 2017/4/18 0018.
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
                    response(ResultCode.SUCCEED);
                    break;
                case 0x01:
                    response(ResultCode.ILLEGAL_COMMAND);
                    break;
                case 0x02:
                    response(ResultCode.MOTION_STATE);
                    break;
                case 0x03:
                    response(ResultCode.NOT_BINDING);
                    break;
                default:
                    response(ResultCode.FAILED);
            }
        } catch (Exception e) {
            response(ResultCode.FAILED);
            e.printStackTrace();
        }
    }

    @Override
    public void onResult(int resultCode) {
        if (resultCode != 0)
            response(resultCode);
    }

    private void response(int resultCode) {
        if (resultCallback != null)
            resultCallback.onResult(resultCode);
        resultCallback = null;
    }

}