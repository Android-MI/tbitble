package com.tbit.tbitblesdk.services.command.bikecommand;

import com.tbit.tbitblesdk.services.command.Command;

/**
 * Created by Salmon on 2017/3/17 0017.
 */

public abstract class AckOnlyCommand extends Command {

    public AckOnlyCommand() {
        super();
    }

    @Override
    protected void onAckSucceed() {
        notifyCommandFinished();
    }
}
