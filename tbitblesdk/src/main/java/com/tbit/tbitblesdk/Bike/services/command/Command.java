package com.tbit.tbitblesdk.Bike.services.command;

import android.bluetooth.BluetoothProfile;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.tbit.tbitblesdk.Bike.ResultCode;
import com.tbit.tbitblesdk.Bike.services.config.BikeConfig;
import com.tbit.tbitblesdk.bluetooth.Code;
import com.tbit.tbitblesdk.bluetooth.IBleClient;
import com.tbit.tbitblesdk.bluetooth.RequestDispatcher;
import com.tbit.tbitblesdk.bluetooth.debug.BleLog;
import com.tbit.tbitblesdk.bluetooth.listener.ConnectStateChangeListener;
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

public abstract class Command implements Handler.Callback, BleResponse, PacketResponseListener, ConnectStateChangeListener {
    private static final String TAG = "Command";
    public static final int NOT_EXECUTE_YET = 0;
    public static final int PROCESSING = 1;
    public static final int FINISHED = 2;

    private static final int DEFAULT_COMMAND_TIMEOUT = 10000;
    private static final int HANDLE_TIMEOUT = 0;

    protected int state;
    protected int retryCount;
    protected Handler handler;
    private ResultCallback resultCallback;
    protected CommandHolder commandHolder;
    protected RequestDispatcher requestDispatcher;
    protected ReceivedPacketDispatcher receivedPacketDispatcher;
    protected BikeConfig bikeConfig;
    protected IBleClient bleClient;
    protected int sequenceId;
    private Packet sendPacket;

    public Command(ResultCallback resultCallback) {
        this.retryCount = 0;
        this.handler = new Handler(Looper.getMainLooper(), this);
        this.state = NOT_EXECUTE_YET;
        this.resultCallback = resultCallback;
    }

    public void setBleClient(IBleClient bleClient) {
        this.bleClient = bleClient;
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

    public void setSequenceId(int sequenceId) {
        this.sequenceId = sequenceId;
    }

    public boolean process(CommandHolder commandHolder) {
        if (state != NOT_EXECUTE_YET)
            return false;

        state = PROCESSING;

        this.commandHolder = commandHolder;

        this.sendPacket = onCreateSendPacket(sequenceId);

        receivedPacketDispatcher.addPacketResponseListener(this);

        bleClient.getListenerManager().addConnectStateChangeListener(this);

        if (bleClient.getConnectionState() < 3) {
            response(ResultCode.DISCONNECTED);
            return true;
        }

        sendCommand();

        startTiming();

        return true;
    }

    protected void startTiming() {
        BleLog.log("StartTiming", "Timeout: " + getTimeout());
        handler.sendEmptyMessageDelayed(HANDLE_TIMEOUT, getTimeout());
    }

    public void cancel() {
        if (isProcessable()) {
            response(ResultCode.CANCELED);
        } else {
            if (resultCallback != null)
                resultCallback.onResult(ResultCode.CANCELED);
            state = FINISHED;
            resultCallback = null;
        }
    }

    protected void sendCommand() {
        BleRequest writeRequest = new WriterRequest(bikeConfig.getUuid().SPS_SERVICE_UUID,
                bikeConfig.getUuid().SPS_RX_UUID, sendPacket.toByteArray(), false, this);
        requestDispatcher.addRequest(writeRequest);
    }

    @Override
    public void onConnectionStateChange(int status, int newState) {
        if (!isProcessable())
            return;
        if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    response(ResultCode.DISCONNECTED);
                }
            });
        }
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
        if (packet.getHeader().isAck()) {
            boolean isMyAck = packet.getHeader().getSequenceId() == getSendPacket().getHeader().getSequenceId();
            if (isMyAck)
                onAck(packet);
            return isMyAck;
        }
        if (bikeConfig.getComparator().compare(this, packet)) {
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
        if (resultCallback != null)
            resultCallback.onResult(resultCode);
        onFinish();
    }

    protected void onFinish() {
        state = FINISHED;
        receivedPacketDispatcher.removePacketResponseListener(this);
        bleClient.getListenerManager().removeConnectStateChangeListener(this);
        commandHolder.onCommandCompleted();
        resultCallback = null;
        commandHolder = null;
    }

    protected int getRetryTimes() {
        return 3;
    }

    protected void onTimeout() {
        response(ResultCode.TIMEOUT);
    }

    public Packet getSendPacket() {
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
