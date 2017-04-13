package com.tbit.tbitblesdk.services.old;

import android.bluetooth.BluetoothGatt;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.tbit.tbitblesdk.protocol.BikeState;
import com.tbit.tbitblesdk.protocol.BluEvent;
import com.tbit.tbitblesdk.listener.Reader;
import com.tbit.tbitblesdk.listener.Writer;
import com.tbit.tbitblesdk.protocol.Constant;
import com.tbit.tbitblesdk.protocol.ControllerState;
import com.tbit.tbitblesdk.protocol.Packet;
import com.tbit.tbitblesdk.protocol.PacketValue;
import com.tbit.tbitblesdk.protocol.ResultCode;
import com.tbit.tbitblesdk.services.BluetoothIO;
import com.tbit.tbitblesdk.services.ReadTask;
import com.tbit.tbitblesdk.services.WriteTask;
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
import java.util.UUID;

/**
 * Created by Salmon on 2016/12/6 0006.
 */

public class BikeBleConnector implements Reader, Writer {
    private static final String TAG = "BikeBleConnector";
    private static final int DEFAULT_TIME_OUT = 10 * 1000;

    public UUID SPS_SERVICE_UUID = UUID.fromString("0783b03e-8535-b5a0-7140-a304d2495cb7");
    public UUID SPS_RX_UUID = UUID.fromString("0783b03e-8535-b5a0-7140-a304d2495cba");
    public UUID SPS_TX_UUID = UUID.fromString("0783b03e-8535-b5a0-7140-a304d2495cb8");
    public UUID SPS_CTRL_UUID = UUID.fromString("0783b03e-8535-b5a0-7140-a304d2495cb9");
    public UUID SPS_NOTIFY_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private EventBus bus;
    private BluetoothIO bluetoothIO;
    private Handler handler = new TimeoutHandler(Looper.getMainLooper(), this);
    private WriteTask writeTask;
    private ReadTask readTask;
    private int timeout = DEFAULT_TIME_OUT;
    private BikeState bikeState;
    private Set<Integer> requestQueue = Collections.synchronizedSet(new HashSet<Integer>());
    private Byte[] connectKey;
    private ConnectMode connectMode;

    public enum ConnectMode {
        NORMAL, OTA
    }

    public BikeBleConnector(BluetoothIO bluetoothIO) {
        this.bus = EventBus.getDefault();
        this.bluetoothIO = bluetoothIO;
        this.bikeState = new BikeState();
        this.connectMode = ConnectMode.NORMAL;
        bus.register(this);
        start();
    }

    public void setConnectKey(Byte[] key) {
        this.connectKey = key;
    }

    public void setConnectMode(ConnectMode mode) {
        this.connectMode = mode;
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
    public boolean connect() {
        if (requestQueue.contains(Constant.REQUEST_CONNECT))
            return false;
        PacketValue packetValue = new PacketValue();
        packetValue.setCommandId((byte) (0x02));
        packetValue.addData(new PacketValue.DataBean((byte) 0x01, connectKey));
        Packet send_packet = new Packet();
        send_packet.setPacketValue(packetValue, true);
        send_packet.print();
        send(Constant.REQUEST_CONNECT, Constant.COMMAND_CONNECT, Constant.SEND_KEY_CONNECT, connectKey);
        return true;
    }

    public boolean otaConnect() {
        PacketValue packetValue = new PacketValue();
        packetValue.setCommandId((byte) (0x01));
        packetValue.addData(new PacketValue.DataBean((byte) 0x01, connectKey));
        Packet send_packet = new Packet();
        send_packet.setPacketValue(packetValue, true);
        send_packet.print();
        send(Constant.REQUEST_OTA_CONNECT, Constant.COMMAND_OTA, Constant.VALUE_ON, connectKey);
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
        lockCount = 0;
//        send(Constant.REQUEST_LOCK, Constant.COMMAND_SETTING, Constant.SETTING_KEY_DEFENCE,
//                new Byte[]{Constant.VALUE_ON});

        final int sequenceId = Constant.REQUEST_LOCK;
        requestQueue.add(Integer.valueOf(sequenceId));
        handler.removeMessages(sequenceId);
        handler.sendEmptyMessageDelayed(sequenceId, 15 * 1000);
        doLock();
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

    public void remoteDisconnect() {
        send(Constant.REQUEST_REMOTE, Constant.COMMAND_REMOTE, (byte) 0x01, null);
    }

    public boolean ota() {
//        if (requestQueue.contains(Constant.REQUEST_OTA)) {
//            return false;
//        }
        send(Constant.REQUEST_OTA, Constant.COMMAND_OTA, Constant.VALUE_ON, null);
        return true;
    }

    public void disConnect() {
        bluetoothIO.disconnect();
    }

    private void doLock() {
        PacketValue packetValue = new PacketValue();
        packetValue.setCommandId(Constant.COMMAND_SETTING);
        packetValue.addData(new PacketValue.DataBean(Constant.SETTING_KEY_DEFENCE,
                new Byte[]{Constant.VALUE_ON}));
        Packet send_packet = new Packet();
        send_packet.setHeadSerialNo(Constant.REQUEST_LOCK);
        send_packet.setPacketValue(packetValue, true);
        send_packet.print();
        writeTask.addData(send_packet);
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
            if (sequenceId == Constant.REQUEST_COMMON) {
                handler.removeMessages(Constant.REQUEST_COMMON);
                bus.post(new BluEvent.CommonResponse(ResultCode.SUCCEED, null));
            }
        }
        // ACK错误
        else if (checkResult == 0x30) {
            int sequenceId = receivedPacket.getL1Header().getSequenceId();
            if (sequenceId == Constant.REQUEST_COMMON) {
                handler.removeMessages(Constant.REQUEST_COMMON);
                bus.post(new BluEvent.CommonResponse(ResultCode.FAILED, null));
            }
        }
        // 接收数据包校验正确
        else if (checkResult == 0) {
            // 接收终端的消息校验正确，给终端应答
            // 0x09是板间命令，不做应答
            if (receivedPacket.getPacketValue().getCommandId() != 0x09)
                sendACK(receivedPacket, false);
            try {
                parseSysState(data[2]);
                PacketValue packetValue = (PacketValue) receivedPacket.getPacketValue().clone();
                int sequenceId = receivedPacket.getL1Header().getSequenceId();
                if (sequenceId == Constant.REQUEST_COMMON) {
                    handler.removeMessages(Constant.REQUEST_COMMON);
                    bus.post(new BluEvent.CommonResponse(ResultCode.SUCCEED, packetValue));
                } else {
                    resolve(packetValue);
                }
            } catch (CloneNotSupportedException e) {
                Log.d(TAG, "parseReceivedPacket: " + "PacketValue:CloneNotSupportedException");
            }
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
//        byte dataOne = data[0];
//        byte dataTwo = data[1];
//        if (dataOne == (byte) 0x00) {
//            //进入ota成功，下载升级文件，发送文件到硬件
//            Log.i(TAG, "--进入ota模式成功");
//            bus.post(new BluEvent.Ota(BluEvent.OtaState.START));
//        } else if (dataOne == (byte) 0x01) {
//            if (dataTwo == (byte) 0x01) {
//                //电量过低
//                bus.post(BluEvent.Ota.getFailedInstance(ResultCode.OTA_FAILED_LOW_POWER));
//                Log.i(TAG, "--进入ota模式失败，电池电量过低");
//            } else if (dataTwo == (byte)0x02) {
//                //密钥错误
//                Log.i(TAG, "--进入ota模式失败，密钥错误");
//                bus.post(BluEvent.Ota.getFailedInstance(ResultCode.OTA_FAILED_ERR_KEY));
//            } else {
//                //未知原因
//                Log.i(TAG, "--进入ota模式失败，发生未知错误");
//                bus.post(BluEvent.Ota.getFailedInstance(ResultCode.OTA_FAILED_UNKNOWN));
//            }
//        }
    }

    // 判断硬件与APP是否唯一匹配
    private void isConnSuccessfulUser(Byte[] data) {
        int sequence = Constant.REQUEST_CONNECT;
        BluEvent.State state = data[0] == (byte) 0x01 ? BluEvent.State.SUCCEED :
                BluEvent.State.FAILED;
//        bus.post(new BluEvent.WriteData(sequence, state));
        if (state == BluEvent.State.SUCCEED) {
            bus.post(new BluEvent.WriteData(sequence, BluEvent.State.SUCCEED));
        } else {
            bus.post(new BluEvent.WriteData(sequence, ResultCode.KEY_ILLEGAL));
            bluetoothIO.disconnect();
        }
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
        if (data == null || data.length == 0)
            return;

        if (data.length >= 10) {
            Byte[] locationData = Arrays.copyOfRange(data, 0, 10);
            parseLocation(locationData);
        }
        if (data.length >= 13) {
            Byte[] signalData = Arrays.copyOfRange(data, 10, 13);
            parseSignal(signalData);
        }
        if (data.length >= 15) {
            Byte[] batteryData = Arrays.copyOfRange(data, 13, 15);
            parseVoltage(batteryData);
        }
        if (data.length >= 23) {
            Byte[] baseStationData = Arrays.copyOfRange(data, 15, 23);
            parseBaseStation(baseStationData);
        }
        if (data.length >= 36) {
            Byte[] controllerInfoData = Arrays.copyOfRange(data, 23, 36);
            parseControllerState(controllerInfoData);
        }

        handler.removeMessages(Constant.REQUEST_UPDATE);
        bus.post(new BluEvent.WriteData(Constant.REQUEST_UPDATE, BluEvent.State.SUCCEED));
//        bus.post(new BluEvent.UpdateBikeState());
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

    private void parseControllerState(Byte[] data) {
        if (data == null || data.length != 13)
            return;
        ControllerState controllerState = bikeState.getControllerState();

        byte[] originData = ByteUtil.bytesToBytes(data);

        controllerState.setTotalMillage(byteArrayToInt(Arrays.copyOfRange(originData, 0, 2)));

        controllerState.setSingleMillage(byteArrayToInt(Arrays.copyOfRange(originData, 2, 4)));

        controllerState.setSpeed(byteArrayToInt(Arrays.copyOfRange(originData, 4, 6)));

        Byte originError = data[6];
        int[] error = controllerState.getErrCode();
        error[0] = bitResolver(originError, 0x01);
        error[1] = bitResolver(originError, 0x02);
        error[2] = bitResolver(originError, 0x04);
        error[3] = bitResolver(originError, 0x08);
        error[4] = bitResolver(originError, 0x10);
        error[5] = bitResolver(originError, 0x20);
        error[6] = bitResolver(originError, 0x40);
        error[7] = bitResolver(originError, 0x80);

        controllerState.setVoltage(byteArrayToInt(Arrays.copyOfRange(originData, 7, 9)));

        controllerState.setElectricCurrent(byteArrayToInt(Arrays.copyOfRange(originData, 9, 11)));

        controllerState.setBattery(byteArrayToInt(Arrays.copyOfRange(originData, 11, 13)));
    }

    private void parseBaseStation(Byte[] data) {
        if (data == null || data.length != 8)
            return;
        try {
            byte[] temp = ByteUtil.bytesToBytes(data);
            byte[] mcc = ByteUtil.subBytes(temp, 0, 2);
            byte[] mnc = ByteUtil.subBytes(temp, 2, 1);
            byte[] lac = ByteUtil.subBytes(temp, 3, 2);
            byte[] cell = ByteUtil.subBytes(temp, 5, 3);

            int[] result = new int[4];
            result[0] = byteArrayToInt(mcc);
            result[1] = byteArrayToInt(mnc);
            result[2] = byteArrayToInt(lac);
            result[3] = byteArrayToInt(cell);

            bikeState.setBaseStation(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int byteArrayToInt(byte[] data) {
//        return ByteUtil.bytesToInt(data);
        String s = ByteUtil.bytesToHexString(data);
        s = s.replace(" ", "");
        Log.d(TAG, "byteArrayToInt: " + s);
        return Integer.parseInt(s, 16);
    }

    private void parseDeviceFault(Byte[] data) {
        int result = 0;
        try {
            result = byteArrayToInt(ByteUtil.bytesToBytes(data));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i(TAG, "--设备故障：" + result);
        bikeState.setDeviceFaultCode(result);
    }

    private void parseVoltage(Byte[] data) {
        int result = 0;
        try {
            result = byteArrayToInt(ByteUtil.bytesToBytes(data));
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

    private int lockCount = 0;
    private void resolveOperationResponse(int key, Byte[] value) {
        if (key == 0x81) {
            resolveLockResponse(key, value);
            return;
        }
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
        }
        if (sequence == -1)
            return;
        handler.removeMessages(sequence);
        if (state == BluEvent.State.SUCCEED) {
            bus.post(new BluEvent.WriteData(sequence, state));
        } else {
            int resultCode = ResultCode.FAILED;
            switch (value[0]) {
                case 0x01:
                    resultCode = ResultCode.ILLEGAL_COMMAND;
                    break;
                case 0x02:
                    resultCode = ResultCode.MOTION_STATE;
                    break;
                case 0x03:
                    resultCode = ResultCode.NOT_BINDING;
                    break;
            }
            bus.post(new BluEvent.WriteData(sequence, resultCode));
        }

    }

    private void resolveLockResponse(int key, Byte[] value) {
        Log.d(TAG, "resolveLockResponse: " + ByteUtil.bytesToHexString(value));
        if (!requestQueue.contains(Constant.REQUEST_LOCK))
            return;
        BluEvent.State state = value[0] == 0 ? BluEvent.State.SUCCEED :
                BluEvent.State.FAILED;
        bikeState.setOperateFaultCode(value[0]);
        if (state == BluEvent.State.SUCCEED) {
            bus.post(new BluEvent.WriteData(Constant.REQUEST_LOCK, state));
        } else {
            if (lockCount < 2) {
                lockCount++;
                doLock();
            }
            else {
                int resultCode = ResultCode.FAILED;
                switch (value[0]) {
                    case 0x01:
                        resultCode = ResultCode.ILLEGAL_COMMAND;
                        break;
                    case 0x02:
                        resultCode = ResultCode.MOTION_STATE;
                        break;
                    case 0x03:
                        resultCode = ResultCode.NOT_BINDING;
                        break;
                }
                bus.post(new BluEvent.WriteData(Constant.REQUEST_LOCK, resultCode));
            }
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
                parseBaseStation(value);
                break;
            case 0x86:
                parseSignal(value);
                break;
            case 0x88:
                parseControllerState(value);
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
        Packet ackPacket = new Packet();
        ackPacket.setL1Header(l1Header);
        ackPacket.setPacketValue(null, false);

        final byte[] data = ackPacket.toByteArray();
        bluetoothIO.write(SPS_SERVICE_UUID, SPS_RX_UUID, data, false);
        Log.i(TAG, "Send ACK:" + ackPacket.toString());
    }

    private void setReadTemp(byte[] value) {
        readTask.setData(value);
    }

    public void writeData(byte[] data) {
        boolean status = bluetoothIO.write(SPS_SERVICE_UUID, SPS_RX_UUID, data, false);
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
        final int status = event.status;

        if (!SPS_SERVICE_UUID.equals(event.serviceUuid))
            return;

        Log.d(TAG, "onCharacteristicChanged: " + event.state.name() + "\nvalue: " +
                ByteUtil.bytesToHexString(event.value) + "\nstatus: " + status);

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
        bluetoothIO.setCharacteristicNotification(SPS_SERVICE_UUID, SPS_TX_UUID, SPS_NOTIFY_DESCRIPTOR, true);
        if (connectMode == ConnectMode.NORMAL) {
            this.connect();
        } else if (connectMode == ConnectMode.OTA) {
            this.otaConnect();
        }
    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onVersionResponse(BluEvent.VersionResponse response) {
//        Log.d(TAG, "onVersionResponse: hard: " + response.deviceVersion +
//                " || soft: " + response.firmwareVersion);
//        int[] version = new int[]{response.deviceVersion, response.firmwareVersion};
//        bikeState.setVersion(version);
//        updateVersion(response.deviceVersion, response.firmwareVersion);
//    }

    private void updateVersion(int hardVersion, int softVersion) {
        if (softVersion < 3) {
            SPS_SERVICE_UUID = UUID.fromString("0783b03e-8535-b5a0-7140-a304d2495cb7");
            SPS_TX_UUID = UUID.fromString("0783b03e-8535-b5a0-7140-a304d2495cb8");
            SPS_RX_UUID = UUID.fromString("0783b03e-8535-b5a0-7140-a304d2495cba");
            SPS_NOTIFY_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
        } else if (softVersion >= 3) {
            SPS_SERVICE_UUID = UUID.fromString("0000fef6-0000-1000-8000-00805f9b34fb");
            SPS_TX_UUID = UUID.fromString("0783b03e-8535-b5a0-7140-a304d2495cb8");
            SPS_RX_UUID = UUID.fromString("0783b03e-8535-b5a0-7140-a304d2495cba");
            SPS_NOTIFY_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
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
            if (msg.what == Constant.REQUEST_COMMON) {
//                connector.bus.post(new BluEvent.CommonResponse(ResultCode.FAILED));
            } else if (msg.what == Constant.REQUEST_CONNECT){
                connector.bus.post(new BluEvent.WriteData(msg.what, ResultCode.TIMEOUT));
            } else {
                connector.bus.post(new BluEvent.WriteData(msg.what, BluEvent.State.FAILED));
            }
        }
    }
}