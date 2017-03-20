package com.tbit.tbitblesdk.services.command;

import android.util.Log;

import com.tbit.tbitblesdk.protocol.BluEvent;
import com.tbit.tbitblesdk.protocol.Constant;
import com.tbit.tbitblesdk.protocol.Packet;
import com.tbit.tbitblesdk.protocol.PacketValue;
import com.tbit.tbitblesdk.protocol.ResultCode;
import com.tbit.tbitblesdk.services.command.bikecommand.BikeCommand;
import com.tbit.tbitblesdk.services.command.callback.ResultCallback;
import com.tbit.tbitblesdk.util.PacketUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Created by Salmon on 2017/3/17 0017.
 */

public class OtaCommand extends BikeCommand {

    private static final String TAG = "OtaCommand";
    protected EventBus bus;
    public OtaCommand(ResultCallback resultCallback) {
        super(resultCallback);
        this.bus = EventBus.getDefault();
    }

    @Override
    protected void onResult(Packet receivedPacket) {
        PacketValue packetValue = receivedPacket.getPacketValue();
        List<PacketValue.DataBean> resolvedData = packetValue.getData();

        PacketValue.DataBean bean = resolvedData.get(0);
        Byte[] data = bean.value;
        byte dataOne = data[0];
        byte dataTwo = data[1];
        if (dataOne == (byte) 0x00) {
            //进入ota成功，下载升级文件，发送文件到硬件
            Log.i(TAG, "--进入ota模式成功");
            bus.post(new BluEvent.OtaStart());
        } else if (dataOne == (byte) 0x01) {
            if (dataTwo == (byte) 0x01) {
                //电量过低
                resultCallback.onResult(ResultCode.OTA_FAILED_LOW_POWER);
                Log.i(TAG, "--进入ota模式失败，电池电量过低");
            } else if (dataTwo == (byte) 0x02) {
                //密钥错误
                Log.i(TAG, "--进入ota模式失败，密钥错误");
                resultCallback.onResult(ResultCode.OTA_FAILED_ERR_KEY);
            } else {
                //未知原因
                Log.i(TAG, "--进入ota模式失败，发生未知错误");
                resultCallback.onResult(ResultCode.OTA_FAILED_UNKNOWN);
            }
        }
    }

    @Override
    protected Packet onCreateSendPacket(int sequenceId) {
        return PacketUtil.createPacket(sequenceId, Constant.COMMAND_OTA, Constant.VALUE_ON, null);
    }

    @Override
    public boolean compare(Packet receivedPacket) {
        return PacketUtil.compareCommandId(receivedPacket, getSendPacket())
                && PacketUtil.checkPacketValueContainKey(receivedPacket, 0x02);
    }
    
}
