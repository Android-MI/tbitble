package com.tbit.tbitblesdksample;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.tbit.tbitblesdk.Bike.ResultCode;
import com.tbit.tbitblesdk.Bike.TbitBle;
import com.tbit.tbitblesdk.Bike.model.BikeState;
import com.tbit.tbitblesdk.Bike.services.command.callback.StateCallback;
import com.tbit.tbitblesdk.protocol.callback.ResultCallback;

import java.util.List;

/**
 * Created by Salmon on 2017/4/20 0020.
 */

public class OperationDispatcher implements Handler.Callback {

    private String deviceId;
    private String key;
    private int duration;
    private List<Integer> curOperation;
    private List<List<Integer>> operations;
    private OperationDispatcherListener listener;

    private Handler handler;

    public OperationDispatcher() {
        this.handler = new Handler(Looper.getMainLooper(), this);
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setOperations(List<List<Integer>> operations) {
        this.operations = operations;
    }

    public void start() {
        dispatch();
    }

    public void setListener(OperationDispatcherListener listener) {
        this.listener = listener;
    }

    private void dispatch() {
        if (operations.size() == 0 && curOperation != null && curOperation.size() == 0) {
            listener.onComplete();
            return;
        }
        if (curOperation == null || curOperation.size() == 0)
            curOperation = operations.remove(0);

        for (int i : curOperation) {
            Log.d("asd", "dispatch: " + i);
        }

        int op = curOperation.remove(0);
        realDispatch(op);
    }

    private void realDispatch(int opCode) {
        switch (opCode) {
            case Operation.OPERATION_CONNECT:
                TbitBle.connect(deviceId, key, new ResultCallback() {
                    @Override
                    public void onResult(int resultCode) {
                        if (resultCode == ResultCode.SUCCEED)
                            next();
                        else
                            notifyFailed();

                    }
                }, new StateCallback() {
                    @Override
                    public void onStateUpdated(BikeState bikeState) {

                    }
                });
                break;

            case Operation.OPERATION_UNLOCK:
                TbitBle.unlock(new ResultCallback() {
                    @Override
                    public void onResult(int resultCode) {
                        if (resultCode == ResultCode.SUCCEED)
                            next();
                        else
                            notifyFailed();
                    }
                });
                break;

            case Operation.OPERATION_LOCK:
                TbitBle.lock(new ResultCallback() {
                    @Override
                    public void onResult(int resultCode) {
                        if (resultCode == ResultCode.SUCCEED)
                            next();
                        else
                            notifyFailed();
                    }
                });
                break;

            case Operation.OPERATION_DISCONNECT:
                TbitBle.disConnect();
                notifySucceed();
                break;
        }
    }

    private void notifySucceed() {
        this.listener.onSucceed();
        next();
    }

    private void notifyFailed() {
        curOperation.clear();
        if (listener.onFailed())
            next();
    }

    private void next() {
        handler.sendEmptyMessageDelayed(0, duration * 1000);
    }

    public void stop() {
        this.handler.removeCallbacksAndMessages(null);
        this.listener = new EmptyListener();
        TbitBle.cancelAllCommand();
        TbitBle.disConnect();
    }

    @Override
    public boolean handleMessage(Message msg) {
        dispatch();
        return true;
    }

    interface OperationDispatcherListener {
        boolean onFailed();

        void onSucceed();

        void onComplete();
    }

    class EmptyListener implements OperationDispatcherListener {

        @Override
        public boolean onFailed() {
            return false;
        }

        @Override
        public void onSucceed() {

        }

        @Override
        public void onComplete() {

        }
    }
}
