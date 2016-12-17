package com.tbit.tbitblesdk.services;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.tbit.tbitblesdk.protocol.BikeState;
import com.tbit.tbitblesdk.protocol.BluEvent;
import com.tbit.tbitblesdk.listener.Reader;
import com.tbit.tbitblesdk.listener.Writer;
import com.tbit.tbitblesdk.protocol.Constant;
import com.tbit.tbitblesdk.protocol.Packet;
import com.tbit.tbitblesdk.protocol.PacketValue;
import com.tbit.tbitblesdk.util.ByteUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Salmon on 2016/12/6 0006.
 */

public class BikeBleConnector implements Reader, Writer {
    private static final String TAG = "BikeBleConnector";
    private static final int DEFAULT_TIME_OUT = 10 * 1000;
    private EventBus bus;
    private BluetoothIO bluetoothIO;
    private Handler handler = new TimeoutHandler(Looper.getMainLooper(), this);
    private WriteTask writeTask;
    private ReadTask readTask;
    private int timeout = DEFAULT_TIME_OUT;
    private BikeState bikeState;
    private boolean isRunning = true;
    private Set<Integer> requestQueue = Collections.synchronizedSet(new HashSet<Integer>());

    public BikeBleConnector(BluetoothIO bluetoothIO) {
        this.bus = EventBus.getDefault();
        this.bluetoothIO = bluetoothIO;
        this.bikeState = new BikeState();
        bus.register(this);
        start();
    }

    private void start() {
        readTask = new ReadTask(this);
        readTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        writeTask = new WriteTask(this);
        writeTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void stop() {
        isRunning = false;
        readTask.cancel(true);
        writeTask.cancel(true);
    }

    public void destroy() {
        stop();
        bus.unregister(this);
    }

    public BikeState getState() {
        return bikeState;
    }

    private void send(Packet packet) {
        final int sequenceId = packet.getL1Header().getSequenceId();
        requestQueue.add(Integer.valueOf(sequenceId));
        handler.removeMessages(sequenceId);
        handler.sendEmptyMessageDelayed(sequenceId, timeout);
        writeTask.addData(packet);
    }

    public boolean removeFromQueue(Integer sequenceId) {
        return requestQueue.remove(sequenceId);
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
        if (this.timeout <= 0)
            this.timeout = DEFAULT_TIME_OUT;
    }

    private void onUpdateStatus(boolean status) {
//        if (writeTask != null)
//            writeTask.setWriteStatus(status);
    }

    public void send(int requestCode, byte commandId, byte key, Byte[] data) {
        send(requestCode, commandId, new PacketValue.DataBean(key, data));
    }

    public void send(int requestCode, byte commandId, PacketValue.DataBean... dataBeans) {
        PacketValue packetValue = new PacketValue();
        packetValue.setCommandId(commandId);
        packetValue.addData(dataBeans);
        Packet send_packet = new Packet();
        send_packet.setHeadSerialNo(requestCode);
        send_packet.setPacketValue(packetValue, true);
        send_packet.print();
        send(send_packet);
    }

    // 校验密钥
    public boolean connect(Byte[] key) {
        if (requestQueue.contains(Constant.REQUEST_CONNECT))
            return false;
        PacketValue packetValue = new PacketValue();
        packetValue.setCommandId((byte) (0x02));
        packetValue.addData(new PacketValue.DataBean((byte) 0x01, key));
        Packet send_packet = new Packet();
        send_packet.setPacketValue(packetValue, true);
        send_packet.print();
        send(Constant.REQUEST_CONNECT, Constant.COMMAND_CONNECT, Constant.SEND_KEY_CONNECT, key);
        return true;
    }

    public boolean unlock() {
        if (requestQueue.contains(Constant.REQUEST_UNLOCK))
            return false;
        send(Constant.REQUEST_UNLOCK, Constant.COMMAND_SETTING, Constant.SETTING_KEY_LOCK,
                new Byte[]{Constant.VALUE_OFF});
        return true;
    }

    public boolean lock() {
        if (requestQueue.contains(Constant.REQUEST_LOCK))
            return false;
        send(Constant.REQUEST_LOCK, Constant.COMMAND_SETTING, Constant.SETTING_KEY_DEFENCE,
                new Byte[]{Constant.VALUE_ON});
        return true;
    }

    public boolean update() {
        if (requestQueue.contains(Constant.REQUEST_UPDATE))
            return false;
        send(Constant.REQUEST_UPDATE, Constant.COMMAND_QUERY, Constant.QUERY_ALL, null);
        return true;
    }

    public boolean common(byte commandId, byte key, Byte[] value) {
        if (requestQueue.contains(Constant.REQUEST_COMMON))
            return false;
        send(Constant.REQUEST_COMMON, commandId, key, value);
        return true;
    }

    // 心跳包，同步系统状态
    public void sendHeartbeat() {
        send(Constant.REQUEST_HEART_BEAT, Constant.COMMAND_HEART_BEAT,
                (byte) 0x01, null);
    }

    // 是否打开日志输入，打开，终端有日志则会自动发送日志到APP
    public void setLog(boolean open) {
        send(Constant.REQUEST_LOG, Constant.COMMAND_LOG, (byte) 0x01,
                new Byte[]{open ? Constant.VALUE_ON : Constant.VALUE_OFF});
    }

    // 预解析
    public void parseReceivedPacket(byte[] received) {
        byte[] data = received;
        Packet receivedPacket = new Packet(data);
        Log.i(TAG, "receivedPacket value：" + receivedPacket.toString());
        int checkResult = receivedPacket.checkPacket();
        Log.i(TAG, "checkResult: " + Integer.toHexString(checkResult));
        receivedPacket.print();
        // 数据头错误，清空
        if (checkResult == 0x05) {

        }
        // 发送成功
        else if (checkResult == 0x10) {
            int sequenceId = receivedPacket.getL1Header().getSequenceId();
            writeTask.setAck(sequenceId);
        }
        // ACK错误
        else if (checkResult == 0x30) {
            int sequenceId = receivedPacket.getL1Header().getSequenceId();
        }
        // 接收数据包校验正确
        else if (checkResult == 0) {
            try {
                parseSysState(data[2]);
                PacketValue packetValue = (PacketValue) receivedPacket.getPacketValue().clone();
                resolve(packetValue);
            } catch (CloneNotSupportedException e) {
                Log.d(TAG, "parseReceivedPacket: " + "PacketValue:CloneNotSupportedException");
            }
            // 接收终端的消息校验正确，给终端应答
            // 0x09是板间命令，不做应答
            if (receivedPacket.getPacketValue().getCommandId() != 0x09)
                sendACK(receivedPacket, false);
        }
        // 接收数据包校验错误
        else if (checkResult == 0x0b) {
            sendACK(receivedPacket, true);
        }
    }

    // 解析数据包
    private void resolve(PacketValue packetValue) {
        Byte[] temp = packetValue.toArray();
        byte command = packetValue.getCommandId();
        List<PacketValue.DataBean> resolvedData = packetValue.getData();
        for (PacketValue.DataBean b : resolvedData) {
            int key = b.key & 0xff;
            Byte[] value = b.value;
            switch (command) {
                case 1:
                    //ota 固件升级
                    switch (key) {
                        case 0x02:
                            //固件升级返回0x02
                            isOTASuccessful(value);
                            break;
                    }
                    break;
                case 3:
                    resolveOperationResponse(key, value);
                    break;
                case 4:
                    //查询指令返回的结果处理
                    resolveQueryResponse(key, value);
                    break;
                case 5:
                    resolveConnectionResponse(key, value);
                    break;
                case 6:
                    break;
                case 7:
                    break;
                case 8:
                    //日志输出
                    parseLog(value);
                    break;
                case 9:
                    break;
                case -2:
                    //0XFE
                    break;
                case -1:
                    //0XFF
                    break;

            }
        }

        if (command == 5) {
            handler.removeMessages(Constant.REQUEST_CONNECT);
            bus.post(new BluEvent.UpdateBikeState());
        }
    }

    // 判断终端是否进入ota模式成功
    private void isOTASuccessful(Byte[] data) {
        byte dataOne = data[0];
        byte dataTwo = data[1];
        if (dataOne == (byte) 0x00) {
            //进入ota成功，下载升级文件，发送文件到硬件
            Log.i(TAG, "--进入ota模式成功");
            bus.post(new BluEvent.Ota());
        } else if (dataOne == (byte) 0x01) {
            if (dataTwo == (byte) 0x01) {
                //电量过低
                Log.i(TAG, "--进入ota模式失败，电池电量过低");
            } else {
                //未知原因
                Log.i(TAG, "--进入ota模式失败，发生未知错误");
            }
        }
    }

    // 判断硬件与APP是否唯一匹配
    private void isConnSuccessfulUser(Byte[] data) {
        int sequence = Constant.REQUEST_CONNECT;
        BluEvent.State state = data[0] == (byte) 0x01 ? BluEvent.State.SUCCEED :
                BluEvent.State.FAILED;
        bus.post(new BluEvent.WriteData(sequence, state));
    }

    private void parseVerifyFailed(Byte[] data) {
        if (data == null || data.length == 0)
            return;
        bikeState.setVerifyFailedCode(data[data.length - 1]);
    }

    private void parseLocation(Byte[] data) {
        double[] result = ByteUtil.getPoint(data);
        bikeState.setLocation(result);
        Log.i(TAG, "--经纬度：" + result[0] + " | " + result[1]);
    }

    private void parseAll(Byte[] data) {
        if (data == null || data.length < 15)
            return;
        Byte[] locationData = Arrays.copyOfRange(data, 0, 10);
        Byte[] signalData = Arrays.copyOfRange(data, 10, 13);
        Byte[] batteryData = Arrays.copyOfRange(data, 13, 15);

        parseLocation(locationData);
        parseVoltage(batteryData);
        parseSignal(signalData);

        handler.removeMessages(Constant.REQUEST_UPDATE);
        bus.post(new BluEvent.WriteData(Constant.REQUEST_UPDATE, BluEvent.State.SUCCEED));
        bus.post(new BluEvent.UpdateBikeState());
    }

    private void parseSignal(Byte[] data) {
        if (data == null || data.length != 3)
            return;
        int[] result = new int[3];
        try {
            for (int i = 0; i < data.length; i++) {
                result[i] = Integer.valueOf(data[i]);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            return;
        }
        bikeState.setSignal(result);
    }

    private void parseDeviceFault(Byte[] data) {
        int result = 0;
        try {
            String s = ByteUtil.bytesToHexString(data);
            s = s.replace(" ", "");
            result = Integer.parseInt(s, 16);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i(TAG, "--设备故障：" + result);
        bikeState.setDeviceFaultCode(result);
    }

    private void parseVoltage(Byte[] data) {
        int result = 0;
        try {
            String s = ByteUtil.bytesToHexString(data);
            s = s.replace(" ", "");
            result = Integer.parseInt(s, 16);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i(TAG, "--电量：" + result);
        bikeState.setBattery(result);
    }

    private void parseLog(Byte[] data) {
    }

    // 解析系统状态
    private void parseSysState(Byte state) {
        int[] result = new int[8];

        result[0] = bitResolver(state, 0x01);
        result[1] = bitResolver(state, 0x02);
        result[2] = bitResolver(state, 0x04);
        result[3] = bitResolver(state, 0x08);
        result[4] = bitResolver(state, 0x10);
        result[5] = bitResolver(state, 0x20);
        result[6] = bitResolver(state, 0x40);
        result[7] = bitResolver(state, 0x80);

        bikeState.setSystemState(result);
    }

    private int bitResolver(Byte state, int flag) {
        boolean isFlagged = (state & flag) == flag;
        return isFlagged ? 1 : 0;
    }

    private void resolveOperationResponse(int key, Byte[] value) {
        Log.d(TAG, "resolveOperationResponse: " + ByteUtil.bytesToHexString(value));
        int sequence = -1;
        BluEvent.State state = value[0] == 0 ? BluEvent.State.SUCCEED :
                BluEvent.State.FAILED;
        bikeState.setOperateFaultCode(value[0]);
        switch (key) {
            case 0x81:
                sequence = Constant.REQUEST_LOCK;
                break;
            case 0x82:
                sequence = Constant.REQUEST_UNLOCK;
                break;
            case 0x83:
                break;
            case 0x84:
                break;
        }
        if (sequence == -1)
            return;
        handler.removeMessages(sequence);
        bus.post(new BluEvent.WriteData(sequence, state));
        if (state == BluEvent.State.FAILED) {
            bus.post(new BluEvent.UpdateBikeState());
        }
    }

    private void resolveConnectionResponse(int key, Byte[] value) {
        switch (key) {
            case 0x02:
                //用户连接返回
                isConnSuccessfulUser(value);
                break;
            case 0x81:
                parseVoltage(value);
                break;
            case 0x82:
                parseVerifyFailed(value);
                break;
            case 0x83:
                parseDeviceFault(value);
                break;
            case 0x84:
                parseLocation(value);
                break;
            case 0x85:
//              parseIsTested(value);
                break;
            case 0x86:
                parseSignal(value);
                break;
            case 0xff:
                break;
        }
    }

    private void resolveQueryResponse(int key, Byte[] value) {
        switch (key) {
            case 0x85: // 85 解析全部状态
                Log.d(TAG, "resolve: 解析全部状态");
                parseAll(value);
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
        Packet send_packet = new Packet();
        send_packet.setL1Header(l1Header);
        send_packet.setPacketValue(null, false);
        send_packet.print();

        final byte[] data = send_packet.toByteArray();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final int packLength = 20;
                int lastLength = data.length;
                byte[] sendData;
                int sendIndex = 0;
                while (lastLength > 0 && isRunning) {
                    if (lastLength <= packLength) {
                        sendData = Arrays.copyOfRange(data, sendIndex, sendIndex + lastLength);
                        sendIndex += lastLength;
                        lastLength = 0;
                    } else {
                        sendData = Arrays.copyOfRange(data, sendIndex, sendIndex + packLength);
                        sendIndex += packLength;
                        lastLength -= packLength;
                    }
                    SystemClock.sleep(50L);
                    final byte[] resultData = sendData;
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            bluetoothIO.writeRXCharacteristic(resultData);
                        }
                    });
                }
            }
        }).start();
        Log.i(TAG, "Send ACK:" + send_packet.toString());
    }

    private void setReadTemp(byte[] value) {
        readTask.setData(value);
    }

    public void writeData(byte[] data) {
        boolean status = bluetoothIO.writeRXCharacteristic(data);
        writeTask.setWriteStatus(status);
    }

    public void runOnMainThread(Runnable runnable) {
        if (isMainThread()) {
            runnable.run();
        } else {
            handler.post(runnable);
        }
    }

    private boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
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
        handler.removeMessages(sequenceId);
        bus.post(new BluEvent.WriteData(sequenceId, BluEvent.State.FAILED));
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCharacteristicChanged(BluEvent.ChangeCharacteristic event) {
        Log.d(TAG, "onCharacteristicChanged: " + event.state.name() + "\n" +
                ByteUtil.bytesToHexString(event.value));
        switch (event.state) {
            case WRITE:
                onUpdateStatus(true);
                break;
            case CHANGE:
                setReadTemp(event.value);
                break;
            case READ:
                break;
        }
    }

    static class TimeoutHandler extends Handler {
        WeakReference<BikeBleConnector> connectorReference;

        public TimeoutHandler(Looper looper, BikeBleConnector connector) {
            super(looper);
            this.connectorReference = new WeakReference<>(connector);
        }

        @Override
        public void handleMessage(Message msg) {
            final BikeBleConnector connector = connectorReference.get();
            if (connector == null)
                return;
            connector.bus.post(new BluEvent.WriteData(msg.what, BluEvent.State.FAILED));
        }
    }
}
