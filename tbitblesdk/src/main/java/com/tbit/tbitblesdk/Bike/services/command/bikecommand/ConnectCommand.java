package com.tbit.tbitblesdk.Bike.services.command.bikecommand;

import android.util.Log;

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

public class ConnectCommand extends UpdatableCommand {

    private Byte[] key;

    public ConnectCommand(ResultCallback resultCallback, StateCallback stateCallback,
                          Byte[] key, BikeState bikeState) {
        super(resultCallback, stateCallback, bikeState);
        this.key = key;
    }

    @Override
    protected void onResult(Packet resultPacket) {
        Log.d("ConnectCommand", "onResult: " + resultPacket.toString());
        PacketValue packetValue = resultPacket.getPacketValue();
        List<PacketValue.DataBean> resolvedData = packetValue.getData();

        // 0x00：成功  0x01：指令非法 0x02：运动状态 0x03：非绑定状态
        int resultCode = ResultCode.FAILED;
        for (PacketValue.DataBean b : resolvedData) {
            int key = b.key & 0xff;
            Byte[] value = b.value;
            switch (key) {
                case 0x02:
                    //用户连接返回
                    if (value[0] == (byte) 0x01) {
                        resultCode = ResultCode.SUCCEED;
                    }
                    break;
                case 0x81:
                    parseVoltage(value);
                    break;
                case 0x82: {
                    switch (value[0]) {
                        case 0x00:
                            resultCode = ResultCode.CONNECT_FAILED_UNKNOWN;
                            break;
                        case 0x01:
                            resultCode = ResultCode.CONNECT_FAILED_ILLEGAL_KEY;
                            break;
                        case 0x02:
                            resultCode = ResultCode.CONNECT_DATA_VERIFICATION_FAILED;
                            break;
                        case 0x03:
                            resultCode = ResultCode.CONNECT_COMMAND_NOT_SUPPORT;
                            break;
                        default:
                            resultCode = ResultCode.FAILED;
                    }
                    break;
                }
                case 0x83:
                    parseDeviceFault(value);
                    break;
                case 0x84:
                    parseLocation(value);
                    break;
                case 0x85:
                    parseBaseStation(value);
                    break;
                case 0x86:
                    parseSignal(value);
                    break;
                case 0x88:
                    parseControllerState(value);
                    break;
                case 0xff:
                    break;
            }
        }

        stateCallback.onStateUpdated(bikeState);
        response(resultCode);
    }

    @Override
    protected Packet onCreateSendPacket(int sequenceId) {
        return PacketUtil.createPacket(sequenceId,
                (byte) 0x02, (byte) 0x01, key);
    }

    @Override
    public boolean compare(Packet receivedPacket) {
        return 0x05 == receivedPacket.getPacketValue().getCommandId()
                && PacketUtil.checkPacketValueContainKey(receivedPacket, 0x02);
    }
}
