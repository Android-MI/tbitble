package com.tbit.tbitblesdk.bike.services.command;

import com.tbit.tbitblesdk.protocol.Packet;
import com.tbit.tbitblesdk.protocol.callback.ResultCallback;
import com.tbit.tbitblesdk.bike.util.PacketUtil;

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
        return PacketUtil.createPacket(sequenceId, (byte) 0x01,
                (byte) 0x01, key);
    }

}
