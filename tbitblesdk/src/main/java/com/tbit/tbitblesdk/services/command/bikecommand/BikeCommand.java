package com.tbit.tbitblesdk.services.command.bikecommand;

import com.tbit.tbitblesdk.protocol.ResultCode;
import com.tbit.tbitblesdk.services.command.Command;
import com.tbit.tbitblesdk.services.command.callback.ResultCallback;

/**
 * Created by Salmon on 2017/3/16 0016.
 */

public abstract class BikeCommand extends Command {

    protected ResultCallback resultCallback;

    public BikeCommand(ResultCallback resultCallback) {
        this.resultCallback = resultCallback;
    }

    @Override
    protected void onDisconnected() {
        notifyCommandFinished(ResultCode.DISCONNECTED);
    }

    @Override
    protected void onFailed() {
        notifyCommandFinished(ResultCode.FAILED);
    }

    @Override
    protected void onTimeout() {
        notifyCommandFinished(ResultCode.TIMEOUT);
    }

    @Override
    public void finish() {
        state = FINISHED;
        resultCallback = null;
    }

    protected void notifyCommandFinished(int resultCode) {
        if (!isProcessable())
            return;
        resultCallback.onResult(resultCode);
        commandHolder.onCommandCompleted();
    }
}
