package com.tbit.tbitblesdk.Bike.services;

import com.tbit.tbitblesdk.Bike.model.BikeState;
import com.tbit.tbitblesdk.Bike.services.command.Command;
import com.tbit.tbitblesdk.Bike.services.command.CommandDispatcher;
import com.tbit.tbitblesdk.Bike.services.config.BikeConfig;
import com.tbit.tbitblesdk.Bike.util.BikeUtil;
import com.tbit.tbitblesdk.Bike.util.PacketUtil;
import com.tbit.tbitblesdk.Bike.util.StateUpdateHelper;
import com.tbit.tbitblesdk.bluetooth.IBleClient;
import com.tbit.tbitblesdk.bluetooth.RequestDispatcher;
import com.tbit.tbitblesdk.bluetooth.listener.ConnectStateChangeListener;
import com.tbit.tbitblesdk.bluetooth.request.RssiRequest;
import com.tbit.tbitblesdk.bluetooth.request.RssiResponse;
import com.tbit.tbitblesdk.protocol.Packet;
import com.tbit.tbitblesdk.protocol.PacketValue;
import com.tbit.tbitblesdk.protocol.callback.ResultCallback;
import com.tbit.tbitblesdk.protocol.callback.RssiCallback;
import com.tbit.tbitblesdk.protocol.dispatcher.PacketResponseListener;
import com.tbit.tbitblesdk.protocol.dispatcher.ReceivedPacketDispatcher;

import java.util.List;

/**
 * Created by Salmon on 2017/3/15 0015.
 */

public class BikeService implements PacketResponseListener, ConnectStateChangeListener {
    private static final String TAG = "BikeService";
    private static final int SEQUENCE_ID_START = 128;

    private int sequenceId;
    private BikeState bikeState;
    private IBleClient bleClient;
    private BikeConfig bikeConfig;
    private CommandDispatcher commandDispatcher;
    private RequestDispatcher requestDispatcher;
    private ReceivedPacketDispatcher receivedPacketDispatcher;

    public BikeService(IBleClient bleClient, RequestDispatcher requestDispatcher) {
        this.bleClient = bleClient;
        this.bikeState = new BikeState();
        this.commandDispatcher = new CommandDispatcher();
        this.sequenceId = SEQUENCE_ID_START;
        this.requestDispatcher = requestDispatcher;
        this.receivedPacketDispatcher = new ReceivedPacketDispatcher(bleClient, requestDispatcher);

        // 此处注册接受包的是为了解析心跳数据
        this.receivedPacketDispatcher.addPacketResponseListener(this);
        this.bleClient.getListenerManager().addConnectStateChangeListener(this);
    }

    public void setBikeConfig(BikeConfig bikeConfig) {
        this.bikeConfig = bikeConfig;
        this.receivedPacketDispatcher.setServiceUuid(bikeConfig.getUuid().SPS_SERVICE_UUID);
        this.receivedPacketDispatcher.setTxUuid(bikeConfig.getUuid().SPS_TX_UUID);
        this.receivedPacketDispatcher.setRxUuid(bikeConfig.getUuid().SPS_RX_UUID);
    }

    public void addCommand(Command command) {
        command.setBleClient(bleClient);
        command.setRequestDispatcher(requestDispatcher);
        command.setReceivedPacketDispatcher(receivedPacketDispatcher);
        command.setBikeConfig(bikeConfig);
        command.setSequenceId(getSequenceId());
        this.commandDispatcher.addCommand(command);
    }

    public void destroy() {
        receivedPacketDispatcher.removePacketResponseListener(this);
        bleClient.getListenerManager().addConnectStateChangeListener(this);
        receivedPacketDispatcher.destroy();
        commandDispatcher.destroy();
    }

    public BikeState getBikeState() {
        return bikeState;
    }

    private int getSequenceId() {
        return sequenceId = sequenceId >= 255 ?
                SEQUENCE_ID_START : ++sequenceId;
    }

    @Override
    public boolean onPacketReceived(Packet receivedPacket) {
        // BikeService不直接发送指令，所有ack都忽略
        if (receivedPacket.getHeader().isAck())
            return false;

        int commandId = receivedPacket.getPacketValue().getCommandId();

        if (!(0x04 == commandId &&
                PacketUtil.checkPacketValueContainKey(receivedPacket, 0x85)))
            return false;

        // 从头部更新system_state
        StateUpdateHelper.updateSysState(bikeState, receivedPacket.getHeader().getSystemState());

        // 解析并且更新数据
        List<PacketValue.DataBean> resolvedData = receivedPacket.getPacketValue().getData();
        for (PacketValue.DataBean b : resolvedData) {
            int key = b.key & 0xff;
            Byte[] value = b.value;
            if (key == 0x85) {
                StateUpdateHelper.updateAll(bikeState, value);
                break;
            }
        }

        return true;
    }

    public void cancelAllCommand() {
        this.commandDispatcher.cancelAll();
    }

    public void readRssi(final ResultCallback resultCallback, final RssiCallback rssiCallback) {
        this.requestDispatcher.addRequest(new RssiRequest(new RssiResponse() {
            @Override
            public void onRssi(int rssi) {
                rssiCallback.onRssiReceived(rssi);
            }

            @Override
            public void onResponse(int resultCode) {
                int parsedResult = BikeUtil.parseResultCode(resultCode);
                resultCallback.onResult(parsedResult);
            }
        }));
    }

    @Override
    public void onConnectionStateChange(int status, int newState) {

    }
}
