package com.tbit.tbitblesdk.Bike.services.command;

import android.util.Log;

import com.tbit.tbitblesdk.Bike.ResultCode;
import com.tbit.tbitblesdk.Bike.services.OtaService;
import com.tbit.tbitblesdk.Bike.util.PacketUtil;
import com.tbit.tbitblesdk.protocol.Packet;
import com.tbit.tbitblesdk.protocol.PacketValue;
import com.tbit.tbitblesdk.protocol.callback.ProgressCallback;
import com.tbit.tbitblesdk.protocol.callback.ResultCallback;

import java.util.List;

/**
 * Created by Salmon on 2017/3/17 0017.
 */

public class OtaCommand extends Command implements ResultCallback, ProgressCallback {
    private static final String TAG = "OtaCommand";

    private static final int STATE_COMMAND = 0;
    private static final int STATE_UPDATING = 1;

    protected int timeout;
    protected OtaService otaService;
    protected ProgressCallback progressCallback;
    protected int state;

    public OtaCommand(OtaService otaService, ResultCallback resultCallback, ProgressCallback progressCallback) {
        super(resultCallback);
        this.progressCallback = progressCallback;
        this.otaService = otaService;
        this.timeout = 10000;
        this.state = STATE_COMMAND;
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
            this.state = STATE_COMMAND;
            this.timeout = 120 * 1000;
            this.otaService.update(this, this);
        } else if (dataOne == (byte) 0x01) {
            if (dataTwo == (byte) 0x01) {
                //电量过低
                response(ResultCode.OTA_FAILED_LOW_POWER);
                Log.i(TAG, "--进入ota模式失败，电池电量过低");
            } else if (dataTwo == (byte) 0x02) {
                //密钥错误
                Log.i(TAG, "--进入ota模式失败，密钥错误");
                response(ResultCode.OTA_FAILED_ERR_KEY);
            } else {
                //未知原因
                Log.i(TAG, "--进入ota模式失败，发生未知错误");
                response(ResultCode.OTA_FAILED_UNKNOWN);
            }
        }
    }

    @Override
    protected void onFinish() {
        if (state == STATE_UPDATING)
            otaService.destroy();
        super.onFinish();
    }

    @Override
    protected int getTimeout() {
        return this.timeout;
    }

    @Override
    protected Packet onCreateSendPacket(int sequenceId) {
        return PacketUtil.createPacket(sequenceId, (byte) 0x01, (byte) 0x01, null);
    }

    @Override
    public boolean compare(Packet receivedPacket) {
        return PacketUtil.compareCommandId(receivedPacket, getSendPacket())
                && PacketUtil.checkPacketValueContainKey(receivedPacket, 0x02);
    }

    @Override
    public void onProgress(int progress) {
        if (progressCallback != null)
            progressCallback.onProgress(progress);
    }

    @Override
    public void onResult(int resultCode) {
        response(resultCode);
    }
}
