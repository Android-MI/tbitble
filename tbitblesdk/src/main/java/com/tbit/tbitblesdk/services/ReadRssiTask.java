package com.tbit.tbitblesdk.services;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.tbit.tbitblesdk.protocol.BluEvent;
import com.tbit.tbitblesdk.protocol.ResultCode;
import com.tbit.tbitblesdk.services.command.callback.ResultCallback;
import com.tbit.tbitblesdk.services.command.callback.RssiCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by Salmon on 2017/4/7 0007.
 */

public class ReadRssiTask implements Handler.Callback {
    private ResultCallback resultCallback;
    private RssiCallback rssiCallback;
    private BluetoothIO bluetoothIO;
    private Handler handler;
    private boolean isFinished;

    public ReadRssiTask(BluetoothIO bluetoothIO) {
        this.isFinished = false;
        this.bluetoothIO = bluetoothIO;
        this.handler = new Handler(Looper.myLooper(), this);
    }

    public void setResultCallback(ResultCallback resultCallback) {
        this.resultCallback = resultCallback;
    }

    public void setRssiCallback(RssiCallback rssiCallback) {
        this.rssiCallback = rssiCallback;
    }

    public void start() {
        isFinished = false;
        EventBus.getDefault().register(this);
        if (!bluetoothIO.isConnected()) {
            response(ResultCode.DISCONNECTED, 0);
            return;
        }
        if (!bluetoothIO.readRssi()) {
            response(ResultCode.FAILED, 0);
            return;
        }
        handler.sendEmptyMessageDelayed(0, 5000);
    }

    private void finish() {
        isFinished = true;
        handler.removeCallbacksAndMessages(null);
        resultCallback = null;
        rssiCallback = null;
        EventBus.getDefault().unregister(this);
    }

    private void response(int resultCode, int rssi) {
        if (isFinished)
            return;
        if (resultCallback != null)
            resultCallback.onResult(resultCode);
        if (rssiCallback != null && resultCode == ResultCode.SUCCEED)
            rssiCallback.onRssi(rssi);

        finish();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRssiRead(BluEvent.ReadRssi event) {
        Log.i("ReadRssiTask", "onRssiRead: isSuccess: " + event.isSuccess + " | rssi: " +
            event.rssi);
        if (event.isSuccess)
            response(ResultCode.SUCCEED, event.rssi);
        else
            response(ResultCode.FAILED, 0);
    }

    @Override
    public boolean handleMessage(Message msg) {
        response(ResultCode.TIMEOUT, 0);
        return true;
    }
}
