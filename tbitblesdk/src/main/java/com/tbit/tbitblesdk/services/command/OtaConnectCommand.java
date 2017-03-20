package com.tbit.tbitblesdk.services.command;

import com.tbit.tbitblesdk.protocol.Constant;
import com.tbit.tbitblesdk.protocol.Packet;
import com.tbit.tbitblesdk.services.command.callback.ResultCallback;
import com.tbit.tbitblesdk.util.PacketUtil;

/**
 * Created by Salmon on 2017/3/16 0016.
 */

public class OtaConnectCommand extends OtaCommand {

    private static final String TAG = "OtaConnectCommand";
    private Byte[] key;

    public OtaConnectCommand(ResultCallback resultCallback, Byte[] key) {
        super(resultCallback);
        this.key = key;
    }

    @Override
    protected Packet onCreateSendPacket(int sequenceId) {
        return PacketUtil.createPacket(sequenceId, Constant.COMMAND_OTA,
                Constant.VALUE_ON, key);
    }

}
