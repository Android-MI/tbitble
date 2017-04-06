package com.tbit.tbitblesdk.services;

import android.bluetooth.BluetoothGatt;
import android.os.AsyncTask;
import android.util.Log;

import com.tbit.tbitblesdk.listener.Reader;
import com.tbit.tbitblesdk.listener.Writer;
import com.tbit.tbitblesdk.protocol.BikeState;
import com.tbit.tbitblesdk.protocol.BluEvent;
import com.tbit.tbitblesdk.protocol.ManufacturerAd;
import com.tbit.tbitblesdk.protocol.Packet;
import com.tbit.tbitblesdk.protocol.PacketValue;
import com.tbit.tbitblesdk.services.command.Command;
import com.tbit.tbitblesdk.services.command.CommandHolder;
import com.tbit.tbitblesdk.services.command.comparator.CommandComparator;
import com.tbit.tbitblesdk.services.command.comparator.CommandInsideComparator;
import com.tbit.tbitblesdk.services.config.BikeConfig;
import com.tbit.tbitblesdk.services.config.BikeConfigDispatcher;
import com.tbit.tbitblesdk.services.config.Uuid;
import com.tbit.tbitblesdk.util.ByteUtil;
import com.tbit.tbitblesdk.util.StateUpdateHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Salmon on 2017/3/15 0015.
 */

public class BikeService implements Reader, Writer, CommandHolder {
    private static final String TAG = "NewBikeConnector";
    private static final int SEQUENCE_ID_START = 128;

    private EventBus bus;
    private BluetoothIO bluetoothIO;
    private WriteTask writeTask;
    private ReadTask readTask;
    private BikeState bikeState;
    private List<Command> commandList;
    private Command currentCommand;
    private Command connectCommand;
    private int sequenceId;
    private BikeConfig bikeConfig;

    public BikeService(BluetoothIO bluetoothIO) {
        this.bus = EventBus.getDefault();
        this.bluetoothIO = bluetoothIO;
        this.bikeState = new BikeState();
        this.commandList = new LinkedList<>();
        this.sequenceId = SEQUENCE_ID_START;
        bus.register(this);
        start();
    }

    public void setConnectCommand(Command command) {
        this.connectCommand = command;
    }

    public void addCommand(Command command) {
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
        command.execute(this, getSequenceId());
    }

    private void start() {
        readTask = new ReadTask(this);
        readTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        writeTask = new WriteTask(this);
        writeTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void stop() {
        readTask.cancel(true);
        writeTask.cancel(true);
    }

    public void destroy() {
        stop();
        bus.unregister(this);
    }

    public BikeState getBikeState() {
        return bikeState;
    }

    private void send(Packet packet) {
        writeTask.addData(packet);
    }

    private void onUpdateStatus(boolean status) {
//        if (writeTask != null)
//            writeTask.setWriteStatus(status);
    }

    public void disConnect() {
        bluetoothIO.disconnect();
    }

    // 预解析
    public void parseReceivedPacket(byte[] received) {
        byte[] data = received;
        Packet receivedPacket = new Packet(data);
        Log.i(TAG, "ReceivedPacket Value：" + receivedPacket.toString());
        this.bus.post(new BluEvent.DebugLogEvent("ReceivedPacket Value：",
                receivedPacket.toString()));
        int checkResult = receivedPacket.checkPacket();
        receivedPacket.print();

        if (checkResult == 0x05 || checkResult == 0x10 ||
                checkResult == 0x30) {
            resolveAck(checkResult, receivedPacket);
        }

        // 接收数据包校验正确
        else if (checkResult == 0) {
            // 0x09是板间命令，不做应答和解析
            if (receivedPacket.getPacketValue().getCommandId() == 0x09)
                return;

            // 接收终端的消息校验正确，给终端应答
            sendACK(receivedPacket, false);

            // 从头部更新system_state
            StateUpdateHelper.updateSysState(bikeState, data[2]);

            // 判断是否是当前任务的应答
            boolean needProcess = bikeConfig.getComparator().compare(currentCommand, receivedPacket);

            if (needProcess) {
                currentCommand.result(receivedPacket);
            } else {
                resolve(receivedPacket.getPacketValue());
            }
        }
        // 接收数据包校验错误
        else if (checkResult == 0x0b) {
            sendACK(receivedPacket, true);
        }
    }

    private void resolveAck(int checkResult, Packet receivedPacket) {
        int receivedSequenceId = receivedPacket.getL1Header().getSequenceId();
        int sendSequenceId = currentCommand.getSendPacket().getL1Header().getSequenceId();
        boolean isNeed = receivedSequenceId == sendSequenceId;

        if (isNeed)
            currentCommand.ack(checkResult == 0x10);

        switch (checkResult) {
            case 0x10: // 发送成功
                writeTask.setAck(receivedSequenceId);
                break;
            case 0x30: // ACK错误
                break;
            case 0x05: // 数据头错误，清空
                break;
        }
    }

    // 解析数据包
    private void resolve(PacketValue packetValue) {
        byte command = packetValue.getCommandId();
        List<PacketValue.DataBean> resolvedData = packetValue.getData();
        for (PacketValue.DataBean b : resolvedData) {
            int key = b.key & 0xff;
            Byte[] value = b.value;
            switch (command) {
                case 4:
                    //查询指令返回的结果处理
                    resolveQueryResponse(key, value);
                    break;
            }
        }
    }

    private void resolveQueryResponse(int key, Byte[] value) {
        switch (key) {
            case 0x85: // 85 解析全部状态
                Log.d(TAG, "resolve: 解析全部状态");
                StateUpdateHelper.updateAll(bikeState, value);
                break;
            default:
                break;
        }
    }

    private void sendACK(Packet rPacket, boolean error) {
        Packet.L1Header l1Header = new Packet.L1Header();
        l1Header.setLength((short) 0);
        l1Header.setACK(true);
        l1Header.setError(error);
        l1Header.setSequenceId(rPacket.getL1Header().getSequenceId());
        l1Header.setCRC16((short) 0);
        Packet ackPacket = new Packet();
        ackPacket.setL1Header(l1Header);
        ackPacket.setPacketValue(null, false);

        final byte[] data = ackPacket.toByteArray();
        bluetoothIO.write(bikeConfig.getUuid().SPS_SERVICE_UUID,
                bikeConfig.getUuid().SPS_RX_UUID, data, false);

        Log.i(TAG, "Send ACK:" + ackPacket.toString());
    }

    private void setReadTemp(byte[] value) {
        readTask.setData(value);
    }

    public void writeData(byte[] data) {
        boolean status = bluetoothIO.write(bikeConfig.getUuid().SPS_SERVICE_UUID,
                bikeConfig.getUuid().SPS_RX_UUID, data, false);
        writeTask.setWriteStatus(status);
    }

    @Override
    public void read(byte[] data) {
        parseReceivedPacket(data);
    }

    @Override
    public void write(byte[] data) {
        writeData(data);
    }

    @Override
    public void onWriteAckTimeout(int sequenceId) {
//        bus.post(new BluEvent.WriteData(sequenceId, BluEvent.State.FAILED));
        currentCommand.timeout();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCharacteristicChanged(BluEvent.ChangeCharacteristic event) {
        final int status = event.status;

        if (!bikeConfig.getUuid().SPS_SERVICE_UUID.equals(event.serviceUuid))
            return;

        Log.d(TAG, "onCharacteristicChanged: " + event.state.name() + "\nvalue: " +
                ByteUtil.bytesToHexString(event.value) + "\nstatus: " + status);

        bus.post(new BluEvent.DebugLogEvent("character", "onCharacteristicChanged: " + event.state.name() + "\nvalue: " +
                ByteUtil.bytesToHexString(event.value) + "\nstatus: " + status));

        if (status != BluetoothGatt.GATT_SUCCESS) {
            bus.post(new BluEvent.CommonFailedReport(event.characterUuid + "\nonCharacteristicWrite",
                    "status : " + status));
            return;
        }

        switch (event.state) {
            case WRITE:
                onUpdateStatus(true);
                break;
            case CHANGE:
                setReadTemp(event.value);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDiscovered(BluEvent.DiscoveredSucceed event) {
        Log.i(TAG, "onDiscovered: ");
        Uuid uuid = bikeConfig.getUuid();
        bluetoothIO.setCharacteristicNotification(
                uuid.SPS_SERVICE_UUID, uuid.SPS_TX_UUID, uuid.SPS_NOTIFY_DESCRIPTOR, true);

        this.sequenceId = SEQUENCE_ID_START;

        currentCommand = connectCommand;
        currentCommand.execute(this, sequenceId);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBleBroadcastResponse(BluEvent.BleBroadcast bleBroadcast) {
        ManufacturerAd broadcastData = bleBroadcast.manufacturerAd;
        Log.d(TAG, "Broadcast data: " + broadcastData.toString());
        int[] version = new int[]{broadcastData.getHardwareVersion(), broadcastData.getSoftwareVersion()};
        bikeState.setVersion(version);
        configuration(broadcastData);
    }

    private void configuration(ManufacturerAd broadcastData) {

        bikeConfig = BikeConfigDispatcher.dispatch(broadcastData);
    }

    @Override
    public void sendCommand(Packet packet) {
        send(packet);
    }

    @Override
    public void onCommandCompleted() {
        currentCommand.finish();
        notifyCommandAdded();
    }

    @Override
    public int getBluetoothState() {
        return bluetoothIO.getConnectionState();
    }

    private int getSequenceId() {
        return sequenceId = sequenceId >= 255 ?
                SEQUENCE_ID_START : ++sequenceId;
    }
}
