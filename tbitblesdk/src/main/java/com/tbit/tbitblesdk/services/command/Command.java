package com.tbit.tbitblesdk.services.command;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.tbit.tbitblesdk.protocol.Packet;

/**
 * Created by Salmon on 2017/3/14 0014.
 */

public abstract class Command implements Handler.Callback {
    public static final int NOT_EXECUTE_YET = 0;
    public static final int PROCESSING = 1;
    public static final int FINISHED = 2;

    private static final int DEFAULT_COMMAND_TIMEOUT = 10000;
    private static final int HANDLE_TIMEOUT = 0;

    protected CommandHolder commandHolder;
    protected Handler handler;
    protected int state;
    protected int retryCount;
    private Packet sendPacket;
    private Packet resultPacket;

    public Command() {
        this.retryCount = 0;
        this.handler = new Handler(Looper.getMainLooper(), this);
        this.state = NOT_EXECUTE_YET;
    }

    public boolean execute(CommandHolder commandHolder, int sequenceId) {
        if (state != NOT_EXECUTE_YET)
            return false;

        state = PROCESSING;

        this.commandHolder = commandHolder;

        this.sendPacket = onCreateSendPacket(sequenceId);

        if (commandHolder.getBluetoothState() < 3) {
            onDisconnected();
        } else {
            handler.sendEmptyMessageDelayed(HANDLE_TIMEOUT, getTimeout());
            commandHolder.sendCommand(sendPacket);
        }

        return true;
    }

    public void result(Packet resultPacket) {
        if (!isProcessable())
            return;

        this.resultPacket = resultPacket;

        onResult(resultPacket);

        notifyCommandFinished();
    }

    public void ack(boolean isSucceed) {
        if (!isProcessable())
            return;

        if (isSucceed) {
            onAckSucceed();
        } else {
            onAckFailed();
        }
    }

    public int getState() {
        return state;
    }

    protected void retry() {
        if (!isProcessable())
            return;
        if (retryCount < getRetryTimes()) {
            commandHolder.sendCommand(sendPacket);
            retryCount++;
        } else {
            failed();
        }
    }

    protected void notifyCommandFinished() {
        if (!isProcessable())
            return;
        commandHolder.onCommandCompleted();
    }

    protected void onAckSucceed(){}

    protected void onAckFailed() {
        retry();
    }

    protected void onResult(Packet receivedPacket){}

    protected int getRetryTimes() {
        return DEFAULT_COMMAND_TIMEOUT;
    }

    protected void onTimeout() {
        notifyCommandFinished();
    }

    protected void onFailed() {
        notifyCommandFinished();
    }

    protected void onDisconnected() {
        notifyCommandFinished();
    }

    protected abstract Packet onCreateSendPacket(int sequenceId);

    public abstract boolean compare(Packet receivedPacket);

    /**
     * finish被调用之后，不能再次被使用
     */
    public void finish() {
        state = FINISHED;
        commandHolder = null;
    }

    public Packet getSendPacket() {
        return this.sendPacket;
    }

    protected int getTimeout() {
        return DEFAULT_COMMAND_TIMEOUT;
    }

    protected Packet getResultPacket() {
        return this.resultPacket;
    }

    public void timeout() {
        if (!isProcessable())
            return;
        onTimeout();
    }

    private void failed() {
        if (!isProcessable())
            return;
        onFailed();
    }

    protected boolean isFinished() {
        return state == FINISHED;
    }

    protected boolean isProcessable() {
        return state == PROCESSING;
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (!isProcessable())
            return true;
        switch (msg.what) {
            case HANDLE_TIMEOUT:
                timeout();
                break;
        }
        return true;
    }
}
