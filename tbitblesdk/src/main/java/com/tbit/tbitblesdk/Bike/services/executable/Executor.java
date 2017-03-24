package com.tbit.tbitblesdk.Bike.services.executable;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * Created by Salmon on 2017/3/21 0021.
 */

public abstract class Executor implements Handler.Callback {
    public static final int NOT_EXECUTE_YET = 0;
    public static final int PROCESSING = 1;
    public static final int FINISHED = 2;

    private static final int DEFAULT_COMMAND_TIMEOUT = 10000;
    private static final int HANDLE_TIMEOUT = 0;

    protected Handler handler;
    protected int state;
    protected int retryCount;
    private ExecutorCallback executorCallback;

    public Executor() {
        this.retryCount = 0;
        this.handler = new Handler(Looper.getMainLooper(), this);
        this.state = NOT_EXECUTE_YET;
    }

    public boolean execute(ExecutorCallback executorCallback) {
        if (state != NOT_EXECUTE_YET)
            return false;

        state = PROCESSING;

        this.executorCallback = executorCallback;

        onExecute();

        handler.sendEmptyMessageDelayed(HANDLE_TIMEOUT, getTimeout());

        return true;
    }

    protected abstract void onExecute();

    public int getState() {
        return state;
    }

    protected void retry() {
        if (!isProcessable())
            return;
        if (retryCount < getRetryTimes()) {
            onExecute();
            retryCount++;
        } else {
            notifyFailed();
        }
    }

    protected void notifySucceed() {
        if (!isProcessable())
            return;
        state = FINISHED;
        executorCallback.onExecuteSucceed();
    }

    protected void notifyFailed() {
        if (!isProcessable())
            return;
        state = FINISHED;
        executorCallback.onExecuteFailed();
    }

    protected int getRetryTimes() {
        return DEFAULT_COMMAND_TIMEOUT;
    }

    protected void onTimeout() {
        notifyFailed();
    }

    /**
     * finish被调用之后，不能再次被使用
     */
    public void finish() {
        state = FINISHED;
        executorCallback = null;
    }

    protected int getTimeout() {
        return DEFAULT_COMMAND_TIMEOUT;
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
                onTimeout();
                break;
        }
        return true;
    }
}
