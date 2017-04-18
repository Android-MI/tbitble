package com.tbit.tbitblesdk.Bike.services.command.bikecommand;

import com.tbit.tbitblesdk.Bike.ResultCode;
import com.tbit.tbitblesdk.Bike.services.command.Command;
import com.tbit.tbitblesdk.protocol.callback.ResultCallback;

/**
 * Created by Salmon on 2017/3/17 0017.
 */

public abstract class AckOnlyCommand extends Command {

    public AckOnlyCommand(ResultCallback resultCallback) {
        super(resultCallback);
    }

    @Override
    protected void onAckSuccess() {
        response(ResultCode.SUCCEED);
    }
}
