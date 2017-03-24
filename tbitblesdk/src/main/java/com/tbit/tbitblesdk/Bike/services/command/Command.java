package com.tbit.tbitblesdk.Bike.services.command;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.tbit.tbitblesdk.Bike.ResultCode;
import com.tbit.tbitblesdk.Bike.services.config.BikeConfig;
import com.tbit.tbitblesdk.bluetooth.Code;
import com.tbit.tbitblesdk.bluetooth.RequestDispatcher;
import com.tbit.tbitblesdk.bluetooth.request.BleRequest;
import com.tbit.tbitblesdk.bluetooth.request.BleResponse;
import com.tbit.tbitblesdk.bluetooth.request.WriterRequest;
import com.tbit.tbitblesdk.protocol.Packet;
import com.tbit.tbitblesdk.protocol.callback.ResultCallback;
import com.tbit.tbitblesdk.protocol.dispatcher.PacketResponseListener;
import com.tbit.tbitblesdk.protocol.dispatcher.ReceivedPacketDispatcher;

/**
 * Created by Salmon on 2017/3/14 0014.
 */

public abstract class Command implements Handler.Callback, BleResponse, PacketResponseListener {
    public static final int NOT_EXECUTE_YET = 0;
    public static final int PROCESSING = 1;
    public static final int FINISHED = 2;

    private static final int DEFAULT_COMMAND_TIMEOUT = 10000;
    private static final int HANDLE_TIMEOUT = 0;

    protected Handler handler;
    protected int state;
    protected int retryCount;
    protected CommandHolder commandHolder;
    protected ResultCallback resultCallback;
    protected RequestDispatcher requestDispatcher;
    protected ReceivedPacketDispatcher receivedPacketDispatcher;
    protected BikeConfig bikeConfig;
    private Packet sendPacket;

    public Command(ResultCallback resultCallback) {
        this.retryCount = 0;
        this.handler = new Handler(Looper.getMainLooper(), this);
        this.state = NOT_EXECUTE_YET;
        this.resultCallback = resultCallback;
    }

    public void setBikeConfig(BikeConfig bikeConfig) {
        this.bikeConfig = bikeConfig;
    }

    public void setReceivedPacketDispatcher(ReceivedPacketDispatcher receivedPacketDispatcher) {
        this.receivedPacketDispatcher = receivedPacketDispatcher;
    }

    public void setRequestDispatcher(RequestDispatcher requestDispatcher) {
        this.requestDispatcher = requestDispatcher;
    }

    public boolean process(CommandHolder commandHolder, int sequenceId) {
        if (state != NOT_EXECUTE_YET)
            return false;

        state = PROCESSING;

        this.commandHolder = commandHolder;

        this.sendPacket = onCreateSendPacket(sequenceId);

        handler.sendEmptyMessageDelayed(HANDLE_TIMEOUT, getTimeout());

        sendCommand();

        receivedPacketDispatcher.addPacketResponseListener(this);

        return true;
    }

    protected void sendCommand(){
        BleRequest writeRequest = new WriterRequest(bikeConfig.getUuid().SPS_SERVICE_UUID,
                bikeConfig.getUuid().SPS_RX_UUID, sendPacket.toByteArray(), false, this);
        requestDispatcher.addRequest(writeRequest);
    }

    @Override
    public void onResponse(int resultCode) {
        if (resultCode != Code.REQUEST_SUCCESS && retryCount < getRetryTimes()) {
            retry();
            return;
        }
        switch (resultCode) {
            case Code.BLE_DISABLED:
                response(ResultCode.BLE_NOT_OPENED);
                break;
            case Code.REQUEST_FAILED:
                response(ResultCode.FAILED);
                break;
            case Code.REQUEST_TIMEOUT:
                response(ResultCode.TIMEOUT);
                break;
            case Code.REQUEST_SUCCESS:

                break;
            default:
                response(ResultCode.FAILED);
                break;
        }
    }

    @Override
    public boolean onPacketReceived(Packet packet) {
        if (bikeConfig.getComparator().compare(this, packet)) {
            if (packet.getHeader().isAck())
                onAck(packet);
            else
                onResult(packet);
            return true;
        }
        return false;
    }

    protected void onResult(Packet packet) {

    }

    protected void onAck(Packet packet) {
        if (packet.getHeader().isError())
            onAckFailed();
        else
            onAckSuccess();
    }

    protected void onAckSuccess() {

    }

    protected void onAckFailed() {
        retry();
    }

    public int getState() {
        return state;
    }

    protected void retry() {
        if (!isProcessable())
            return;
        if (retryCount < getRetryTimes()) {
            sendCommand();
            retryCount++;
        } else {
            response(ResultCode.FAILED);
        }
    }

    protected void response(int resultCode) {
        if (!isProcessable())
            return;
        onFinish();
        resultCallback.onResult(resultCode);
        commandHolder.onCommandCompleted();
    }

    protected void onFinish() {
        receivedPacketDispatcher.removePacketResponseListener(this);
    }

    protected int getRetryTimes() {
        return DEFAULT_COMMAND_TIMEOUT;
    }

    protected void onTimeout() {
        response(ResultCode.TIMEOUT);
    }

    protected Packet getSendPacket() {
        return this.sendPacket;
    }

    protected abstract Packet onCreateSendPacket(int sequenceId);

    public abstract boolean compare(Packet receivedPacket);

    protected int getTimeout() {
        return DEFAULT_COMMAND_TIMEOUT;
    }

    public void timeout() {
        if (!isProcessable())
            return;
        onTimeout();
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
