package com.tbit.tbitblesdk.services;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
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
import com.tbit.tbitblesdk.util.EncryptUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
    private static final int DEFAULT_TIME_OUT = 9 * 1000;
    private Packet receive_packet = new Packet();
    private EventBus bus;
    private BluetoothIO bluetoothIO;
    private Handler handler = new Handler(Looper.getMainLooper());
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

    /**
     * 发送数据
     *
     * @param packet
     */
    private void send(Packet packet) {
        final int sequenceId = packet.getL1Header().getSequenceId();
        requestQueue.add(Integer.valueOf(sequenceId));
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                bus.post(new BluEvent.WriteData(sequenceId, BluEvent.State.FAILED));
//            }
//        }, timeout);
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

    /**
     * 发生连接指令，如果校验成功建立持续连接，不成功则断开
     */
    public void getConnect(String tid) {
        Log.d(TAG, "getConnect: --连接绑定");

        Byte[] value = {48, 48, 48, 48, 48, 48, 48, 48,
                48, 48, 48, 48, 48, 48, 48, 48,
                48, 48, 48, 48, 48, 48, 48, 48,
                48, 48, 48, 48, 48, 48, 48, 48
        };

        String encryptSN = getEncryptSN(tid);
//        String encryptSN = "135790246";
        //设置sn的位数
        value[0] = (byte) encryptSN.length();
        for (int i = 0; i < encryptSN.length(); i++) {
            char c = encryptSN.charAt(i);
            value[i + 1] = (byte) c;
        }

        PacketValue packetValue = new PacketValue();
        packetValue.setCommandId((byte) (0x02));
        packetValue.addData(new PacketValue.DataBean((byte) 0x01, value));
        Packet send_packet = new Packet();
        send_packet.setPacketValue(packetValue, true);
        send_packet.print();
        send(Constant.REQUEST_CONNECT, Constant.COMMAND_CONNECT, Constant.SEND_KEY_CONNECT, value);
    }

    /**
     * 获得加密后的SN码
     */
    public String getEncryptSN(String tid) {
        //从mac地址中获取设备的SN码
//        String addr = application.deviceAddr;
//        String[] temp = addr.split(":");
//        StringBuilder SN = new StringBuilder();
////        for (int i = 0; i < temp.length; i++) {
////            SN.append(temp[temp.length - 1 - i]);
////        }
//        for (int i = temp.length - 1; i >= 0; i--) {
//            SN.append(temp[i]);
//        }
//
//        LogUtil.getInstance().info(TAG, "--解析出的SN为：" + SN.toString().substring(3));
        //对SN进行加密
        String Ciphertext = EncryptUtil.encryptStr(tid);
        Log.d(TAG, "getEncryptSN: --加密后的SN为：" + Ciphertext);
        return Ciphertext;
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

    /**
     * 解析收到的数据
     *
     * @param received
     */
    public void parseReceivedPacket(byte[] received) {
        byte[] data = received;
        receive_packet.append(data);
        Log.i(TAG, "-->>receive_packet value = " + receive_packet.toString());
        int checkResult = receive_packet.checkPacket();
        Log.i(TAG, "-->>Check:" + Integer.toHexString(checkResult));
        receive_packet.print();
        //数据头错误，清空
        if (checkResult == 0x05) {
            receive_packet.clear();
        }
        //发送成功
        else if (checkResult == 0x10) {
            Log.d(TAG, "parseReceivedPacket: " +
                    "checkResult == 0x10  Receive ACK:" + receive_packet.toString());
            parseSysState(data[2]);/*解析系统状态**/
            int sequenceId = receive_packet.getL1Header().getSequenceId();
            bus.post(new BluEvent.WriteData(sequenceId, BluEvent.State.SUCCEED));
            writeTask.setAck(sequenceId);
            receive_packet.clear();
        }
        //ACK错误，需要重发
        else if (checkResult == 0x30) {
            Log.i(TAG, "checkResult == 0x30  Receive ACK:" + receive_packet.toString());
            int sequenceId = receive_packet.getL1Header().getSequenceId();
            bus.post(new BluEvent.WriteData(sequenceId, BluEvent.State.FAILED));
        }
        //接收数据包校验正确
        else if (checkResult == 0) {
            try {
                //获取数据包
                PacketValue packetValue = (PacketValue) receive_packet.getPacketValue().clone();
                resolve(packetValue);
            } catch (CloneNotSupportedException e) {
                Log.d(TAG, "parseReceivedPacket: " + "PacketValue:CloneNotSupportedException");
            }
            //接收终端的消息校验正确，给终端一个反馈
            Log.d(TAG, "parseReceivedPacket: " + "checkResult == 0  Send ACK! Receive Packet:" + receive_packet.toString());

            sendACK(receive_packet, false);
            bus.post(new BluEvent.UpdateBikeState());
            receive_packet.clear();
        }
        //接收数据包校验错误
        else if (checkResult == 0x0b) {
            Log.i(TAG, "checkResult == 0x0b  Receive ACK:" + receive_packet.toString());
            sendACK(receive_packet, true);
            receive_packet.clear();
        }
    }

    /**
     * 解析数据包
     *
     * @param packetValue
     */
    private void resolve(PacketValue packetValue) {
        Byte[] temp = packetValue.toArray();
        StringBuilder sb = new StringBuilder();
        for (byte b : temp) {
            sb.append(String.format("%02X ", b));
        }
        Log.d(TAG, "resolve: " + sb.toString());

        byte command = packetValue.getCommandId();

        List<PacketValue.DataBean> resolveData = packetValue.getData();

        for (PacketValue.DataBean b : resolveData) {
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
                case 2:
                    switch (key) {
                        case 0x02:
                            //用户连接返回
                            Log.i(TAG, "--用户连接");
                            isConnSuccessfulUser(value);
                            break;
                        case 0x03:
                            //管理员连接返回
                            Log.i(TAG, "--用户连接");
                            break;
                        case 0x81:
                            Log.i(TAG, "--电池携带");
                            parseVoltage(value);
                            break;
                        case 0x82:
                            Log.i(TAG, "--校验失败原因");
                            parseVerifyFailed(value);
                            break;
                        case 0x83:
                            Log.i(TAG, "--车辆故障");
                            parseDeviceFault(value);
                            break;
                        case 0x84:
                            Log.i(TAG, "--经纬度");
                            parseLocation(value);
                            break;
                        case 0x85:
                            Log.i(TAG, "--小区基站信息相关");
//                            parseIsTested(value);
                            break;
                        case 0x86:
                            Log.i(TAG, "--GPS+GSM+BAT ");
                            break;
                    }
                    break;
                case 3:
                    break;
                case 4:
                    //查询指令返回的结果处理
                    switch (key) {
                        /**此处待终端,目前测试*/
                        case 0x81: // 81 电池电压
                            parseVoltage(value);
                            break;
                        case 0x82: // 82 车辆轮子转动数
                            parseVerifyFailed(value);
                            break;
                        case 0x83: // 83 当前温度
                            parseDeviceFault(value);
                            break;
                        case 0x84: // 84 当前速度
                            parseLocation(value);
                            break;
                        case 0x85: // 85 解析全部状态
                            Log.d(TAG, "resolve: 解析全部状态");
                            parseAll(value);
                            break;
                        case 0x86: // 86 GPS+GSM+BAT

                            break;
                    }
                    break;
                case 5:
                    switch (key) {
                        case 0x02:
                            //用户连接返回
                            Log.i(TAG, "--用户连接");
                            isConnSuccessfulUser(value);
                            break;
                        case 0x81:
                            Log.i(TAG, "--电池携带");
                            parseVoltage(value);
                            break;
                        case 0x82:
                            Log.i(TAG, "--校验失败原因");
                            parseVerifyFailed(value);
                            break;
                        case 0x83:
                            Log.i(TAG, "--车辆故障");
                            parseDeviceFault(value);
                            break;
                        case 0x84:
                            Log.i(TAG, "--经纬度");
                            parseLocation(value);
                            break;
                        case 0x85:
                            Log.i(TAG, "--小区基站信息相关");
//                            parseIsTested(value);
                            break;
                        case 0x86:
                            Log.i(TAG, "--GPS+GSM+BAT");
                            parseSignal(value);
                            break;
                        case 0xff:
                            Log.i(TAG, "--终端重启原因");
                            break;
                    }
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

    }

    /**
     * 根据返回值，判断硬件那边返回是否进入ota模式成功
     *
     * @param data
     */
    private void isOTASuccessful(Byte[] data) {
        byte dataOne = data[0];
        byte dataTwo = data[1];
        if (dataOne == (byte) 0x00) {
            //提示用户进入ota成功，下载升级文件，发送文件到硬件
            Log.i(TAG, "--进入ota模式成功，发送广播，判断是否需要更新");
            bus.post(new BluEvent.Ota());
        } else if (dataOne == (byte) 0x01) {
            if (dataTwo == (byte) 0x01) {
                //提示用户失败的原因是电量过低
                Log.i(TAG, "--进入ota模式失败，电池电量过低");
            } else {
                //提示用户失败于未知原因
                Log.i(TAG, "--进入ota模式失败，发生未知错误");
            }
        }
    }

    /**
     * 判断硬件与APP是否唯一匹配
     *
     * @param data
     */
    private void isConnSuccessfulUser(Byte[] data) {
        if (data[0] == (byte) 0x01) {
            bus.post(new BluEvent.Verified(BluEvent.State.SUCCEED));

        } else if (data[0] == (byte) 0x00) {
            bus.post(new BluEvent.Verified(BluEvent.State.FAILED));
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
        Log.i(TAG, "经纬度：" + result[0] + " | " + result[1]);
    }

    private void parseAll(Byte[] data) {
        if (data == null || data.length < 15)
            return;
        Byte[] locationData = Arrays.copyOfRange(data, 0, 10);
        Byte[] signalData = Arrays.copyOfRange(data, 11, 13);
        Byte[] batteryData = Arrays.copyOfRange(data, 14, 15);

        parseLocation(locationData);
        parseVoltage(batteryData);
        parseSignal(signalData);

        bus.post(new BluEvent.UpdateBikeState());
    }

    private void parseSignal(Byte[] data) {
        if (data == null || data.length != 3)
            return;
        int[] result = new int[3];
        try {
            for (int i = 0; i < data.length; i++) {
                result[0] = Integer.valueOf(data[i]);
            }
        } catch (Exception e) {
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
        Log.i(TAG, "-->>DeviceFault：" + result);
        bikeState.setDeviceFaltCode(result);
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
        Log.i(TAG, "-->>电量：" + result);
        bikeState.setBattery(result);
    }

    private void parseLog(Byte[] data) {
    }

    /**
     * 解析系统状态，若系统状态有告警则发送告警广播，若系统状态中的各个状态与本地不一致，发送一个心跳包同步数据
     *
     * @param state
     */
    private void parseSysState(Byte state) {
        /*String alarm = "";

        if ((byte) 0xC0 == (byte) ((byte) 0xC0 & state)) {
            Log.d(TAG, "parseSysState: " + "断电告警");
            alarm = "断电告警";
        }
        if ((byte) 0x80 == (byte) ((byte) 0xC0 & state)) {
            Log.d(TAG, "parseSysState: " + "震动告警");
            alarm = "震动告警";
        }
        if ((byte) 0x40 == (byte) ((byte) 0xC0 & state)) {
            Log.d(TAG, "parseSysState: " + "低电告警");
            alarm = "低电告警";
        }
        if ((byte) 0x00 == (byte) ((byte) 0xC0 & state)) {
            Log.d(TAG, "parseSysState: " + "无告警信息");
            alarm = "";
        }*/
        boolean isLocked = (byte) 0x01 == (byte) ((byte) 0x01 & state);
        bikeState.setLocked(isLocked);
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
//                    GattCommand.putExtra(BluetoothLeService.HandleCMD, BluetoothLeService.NUS_WRITE_CHARACTERISTIC);
//                    GattCommand.putExtra(BluetoothLeService.HandleData, sendData);
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
}
