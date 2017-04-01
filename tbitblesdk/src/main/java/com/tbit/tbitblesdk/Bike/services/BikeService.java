package com.tbit.tbitblesdk.Bike.services;

import android.util.Log;

import com.tbit.tbitblesdk.Bike.model.BikeState;
import com.tbit.tbitblesdk.Bike.services.command.Command;
import com.tbit.tbitblesdk.Bike.services.command.CommandHolder;
import com.tbit.tbitblesdk.Bike.services.config.BikeConfig;
import com.tbit.tbitblesdk.Bike.util.PacketUtil;
import com.tbit.tbitblesdk.Bike.util.StateUpdateHelper;
import com.tbit.tbitblesdk.bluetooth.IBleClient;
import com.tbit.tbitblesdk.bluetooth.RequestDispatcher;
import com.tbit.tbitblesdk.bluetooth.listener.ConnectStateChangeListener;
import com.tbit.tbitblesdk.bluetooth.request.WriterRequest;
import com.tbit.tbitblesdk.bluetooth.util.ByteUtil;
import com.tbit.tbitblesdk.protocol.Packet;
import com.tbit.tbitblesdk.protocol.PacketValue;
import com.tbit.tbitblesdk.protocol.dispatcher.EmptyResponse;
import com.tbit.tbitblesdk.protocol.dispatcher.PacketResponseListener;
import com.tbit.tbitblesdk.protocol.dispatcher.ReceivedPacketDispatcher;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Salmon on 2017/3/15 0015.
 */

public class BikeService implements CommandHolder, PacketResponseListener, ConnectStateChangeListener {
    private static final String TAG = "NewBikeConnector";
    private static final int SEQUENCE_ID_START = 128;

    private int sequenceId;
    private BikeState bikeState;
    private IBleClient bleClient;
    private BikeConfig bikeConfig;
    private Command currentCommand;
    private List<Command> commandList;
    private RequestDispatcher requestDispatcher;
    private ReceivedPacketDispatcher receivedPacketDispatcher;

    public BikeService(IBleClient bleClient) {
        this.bleClient = bleClient;
        this.bikeState = new BikeState();
        this.commandList = new LinkedList<>();
        this.sequenceId = SEQUENCE_ID_START;
        this.receivedPacketDispatcher = new ReceivedPacketDispatcher(bleClient);
        this.requestDispatcher = new RequestDispatcher(bleClient);

        this.receivedPacketDispatcher.addPacketResponseListener(this);
        this.bleClient.getListenerManager().addConnectStateChangeListener(this);
    }

    public void setBikeConfig(BikeConfig bikeConfig) {
        this.bikeConfig = bikeConfig;
        this.receivedPacketDispatcher.setServiceUuid(bikeConfig.getUuid().SPS_SERVICE_UUID);
        this.receivedPacketDispatcher.setCharacterUuid(bikeConfig.getUuid().SPS_TX_UUID);
    }

    public void addCommand(Command command) {
        command.setBleClient(bleClient);
        command.setRequestDispatcher(requestDispatcher);
        command.setReceivedPacketDispatcher(receivedPacketDispatcher);
        command.setBikeConfig(bikeConfig);
        this.commandList.add(command);
        notifyCommandAdded();
    }

    public void notifyCommandAdded() {
        if (currentCommand.getState() != Command.FINISHED)
            return;
        if (commandList.size() == 0)
            return;
        Command nextCommand = commandList.remove(0);
        executeCommand(nextCommand);
    }

    private void executeCommand(Command command) {
        currentCommand = command;
        command.process(this, getSequenceId());
    }

    public void destroy() {
        receivedPacketDispatcher.removePacketResponseListener(this);
        bleClient.getListenerManager().addConnectStateChangeListener(this);
        receivedPacketDispatcher.destroy();
    }

    public BikeState getBikeState() {
        return bikeState;
    }

    public void disConnect() {
        bleClient.disconnect();
    }

    private void sendACK(int sequenceId, boolean error) {
        Packet packet = PacketUtil.createAck(sequenceId, error);

        final byte[] data = packet.toByteArray();

        requestDispatcher.addRequest(new WriterRequest(bikeConfig.getUuid().SPS_SERVICE_UUID,
                bikeConfig.getUuid().SPS_RX_UUID, data, false, new EmptyResponse()));

        Log.i(TAG, "Send ACK:" + ByteUtil.bytesToHexString(data));
    }

    @Override
    public void onCommandCompleted() {
        notifyCommandAdded();
    }

    private int getSequenceId() {
        return sequenceId = sequenceId >= 255 ?
                SEQUENCE_ID_START : ++sequenceId;
    }

    @Override
    public boolean onPacketReceived(Packet receivedPacket) {
        Log.i(TAG, "ReceivedPacket Value：" + receivedPacket.toString());

        // BikeService不直接发送指令，所有ack都忽略
        if (receivedPacket.getHeader().isAck())
            return false;

        // 0x09是板间命令，不做应答和解析
        if (receivedPacket.getPacketValue().getCommandId() == 0x09)
            return false;

        int commandId = receivedPacket.getPacketValue().getCommandId();

        if (!(0x04 == commandId &&
                PacketUtil.checkPacketValueContainKey(receivedPacket, 0x85)))
            return false;

        // 接收终端的消息校验正确，给终端应答
        sendACK(receivedPacket.getHeader().getSequenceId(), false);

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

    @Override
    public void onConnectionStateChange(int status, int newState) {

    }
}
