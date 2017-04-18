package com.tbit.tbitblesdk.Bike.services.command;

import com.tbit.tbitblesdk.Bike.services.OtaService;
import com.tbit.tbitblesdk.protocol.Packet;
import com.tbit.tbitblesdk.protocol.callback.ProgressCallback;
import com.tbit.tbitblesdk.protocol.callback.ResultCallback;
import com.tbit.tbitblesdk.Bike.util.PacketUtil;

/**
 * Created by Salmon on 2017/3/16 0016.
 */

public class OtaConnectCommand extends OtaCommand {

    private Byte[] key;

    public OtaConnectCommand(OtaService otaServices, Byte[]key, ResultCallback resultCallback, ProgressCallback progressCallback) {
        super(otaServices, resultCallback, progressCallback);
        this.key = key;
    }

    @Override
    protected Packet onCreateSendPacket(int sequenceId) {
        return PacketUtil.createPacket(sequenceId, (byte) 0x01,
                (byte) 0x01, key);
    }

}
