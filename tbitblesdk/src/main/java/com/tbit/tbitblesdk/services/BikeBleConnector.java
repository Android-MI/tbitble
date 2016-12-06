package com.tbit.tbitblesdk.services;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import com.tbit.tbitblesdk.BluEvent;
import com.tbit.tbitblesdk.protocol.Constant;
import com.tbit.tbitblesdk.Event;
import com.tbit.tbitblesdk.protocol.Packet;
import com.tbit.tbitblesdk.protocol.PacketValue;
import com.tbit.tbitblesdk.util.ByteUtil;
import com.tbit.tbitblesdk.util.EncryptUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Salmon on 2016/12/6 0006.
 */

public class BikeBleConnector {
    private static final String TAG = "BikeBleConnector";
    private boolean isRunning = false;
    private sendThread mSendThread;
    private Packet send_packet = new Packet();
    private Packet receive_packet = new Packet();
    private int resent_cnt = 0;
    private EventBus bus;
    private byte[] receiveData = null;
    private byte[] head = new byte[8];
    private byte[] readTemp = null;
    private ReadDataThread readDataThread;
    private BluetoothIO bluetoothIO;
    private Handler handler = new Handler(Looper.getMainLooper());

    public BikeBleConnector(BluetoothIO bluetoothIO) {
        this.bus = EventBus.getDefault();
        this.bluetoothIO = bluetoothIO;
    }

    public void start() {
        isRunning = true;
        readDataThread = new ReadDataThread();
        readDataThread.start();
    }

    public void stop() {
        isRunning = false;
    }

    /**
     * 发送数据
     *
     * @param packet
     */
    private void send(Packet packet) {
        final byte[] data = packet.toByteArray();//获得发送的数据包
        mSendThread = new sendThread(data);//开始发送数据
        mSendThread.start();
    }

    public void onUpdateStatus(boolean status) {
        mSendThread.updateStatus(status);
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

        send_packet.setPacketValue(packetValue, true);
        send_packet.print();
        send(Constant.REQUEST_CONNECT, Constant.COMMAND_CONNECT, Constant.SEND_KEY_CONNECT, value);
        resent_cnt = 3;
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
        send_packet.setHeadSerialNo(requestCode);
        send_packet.setPacketValue(packetValue, true);
        send_packet.print();
        send(send_packet);
        resent_cnt = 3;
    }

    public void connect(Byte[] key) {
        PacketValue packetValue = new PacketValue();
        packetValue.setCommandId((byte) (0x02));
        packetValue.addData(new PacketValue.DataBean((byte)0x01, key));

        send_packet.setPacketValue(packetValue, true);
        send_packet.print();
        send(Constant.REQUEST_CONNECT, Constant.COMMAND_CONNECT, Constant.SEND_KEY_CONNECT, key);
        resent_cnt = 3;
    }

    // 心跳包，同步系统状态
    public void sendHeartbeat() {
        Log.d(TAG, "sendHeartbeat: " + "--发送心跳包");
        send(Constant.REQUEST_HEART_BEAT, Constant.COMMAND_HEART_BEAT,
                (byte) 0x01, null);
    }

    // 是否打开日志输入，打开，终端有日志则会自动发送日志到APP
    public void setLog(boolean open) {
        Log.d(TAG, "setLog: " + "--发送日志 " + open);
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
//        receiveTimerThread.setTimeOut(500).setStatus(TimerThread.RESTART);
        int checkResult = receive_packet.checkPacket();
        Log.i(TAG, "-->>Check:" + Integer.toHexString(checkResult));
        receive_packet.print();
        //数据头错误，清空
        if (checkResult == 0x05) {
            receive_packet.clear();
        }
        //发送成功
        else if (checkResult == 0x10) {
//            sendTimerThread.setStatus(TimerThread.STOP);
//            receiveTimerThread.setStatus(TimerThread.STOP);
            Log.d(TAG, "parseReceivedPacket: " +
                    "checkResult == 0x10  Receive ACK:" + receive_packet.toString());
//            if (BluetoothControlFragment.isStateRefreshNeeded) {
//                parseSysState(data[2]);/*解析系统状态**/
//            }
            int sequenceId = receive_packet.getL1Header().getSequenceId();
            bus.post(new Event.SendSuccess(sequenceId));
            receive_packet.clear();
        }
        //ACK错误，需要重发
        else if (checkResult == 0x30) {
//            sendTimerThread.setStatus(TimerThread.STOP);
//            receiveTimerThread.setStatus(TimerThread.STOP);
            Log.i(TAG, "checkResult == 0x30  Receive ACK:" + receive_packet.toString());
            if (0 < resent_cnt--) {
                Log.i(TAG, "checkResult == 0x30  Resent Packet!");
                send(send_packet);
            } else {
//                if (mPacketCallBack != null) {
//                    mPacketCallBack.onSendFailure();
//                }
            }
            receive_packet.clear();
        }
        //接收数据包校验正确
        else if (checkResult == 0) {
//            receiveTimerThread.setStatus(TimerThread.STOP);
//            if (BluetoothControlFragment.isStateRefreshNeeded) {
//                parseSysState(data[2]);/*解析系统状态**/
//            }
            try {
                //获取数据包
                PacketValue packetValue = (PacketValue) receive_packet.getPacketValue().clone();
                resolve(packetValue);
            } catch (CloneNotSupportedException e) {
                Log.d(TAG, "parseReceivedPacket: " + "PacketValue:CloneNotSupportedException");
            }
            //接收终端的消息校验正确，给终端一个反馈
            Log.d(TAG, "parseReceivedPacket: " + "checkResult == 0  Send ACK!");
            Log.d(TAG, "parseReceivedPacket: " + "checkResult == 0  Receive Packet:" + receive_packet.toString());

            sendACK(receive_packet, false);
            receive_packet.clear();
        }
        //接收数据包校验错误
        else if (checkResult == 0x0b) {
//            receiveTimerThread.setStatus(TimerThread.STOP);
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
                            Log.i(TAG, "--用户连接返回");
                            isConnSuccessfulUser(value);
                            break;
                        case 0x03:
                            //管理员连接返回
                            Log.i(TAG, "--用户连接返回");
                            break;
                        case 0x81:
                            Log.i(TAG, "--电池携带返回");
                            parseVoltage(value);
                            break;
                        case 0x82:
                            Log.i(TAG, "--轮径携带返回");
                            parseWheelSpeed(value);
                            break;
                        case 0x83:
                            Log.i(TAG, "--温度携带返回");
                            parseTemperature(value);
                            break;
                        case 0x84:
                            Log.i(TAG, "--速度携带返回");
                            parseSpeed(value);
                            break;
                        case 0x85:
                            Log.i(TAG, "--是否终测过携带返回");
                            parseIsTested(value);
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
                            parseWheelSpeed(value);
                            break;
                        case 0x83: // 83 当前温度
                            parseTemperature(value);
                            break;
                        case 0x84: // 84 当前速度
                            parseSpeed(value);
                            break;
                        case 0x85: // 85
                            parseIsTested(value);
                            break;
                    }
                    break;
                case 5:
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
            Log.i(TAG, "--硬件与APP匹配成功");
            bus.post(new BluEvent.VerifySucceed());

        } else if (data[0] == (byte) 0x00) {
            Log.i(TAG, "--硬件与APP匹配不成功，请确保正确的硬件设备与APP搭配使用");
            bus.post(new BluEvent.VerifyFailed());
        }

    }

    private void parseWheelSpeed(Byte[] data) {
        //只返回一个字节的数据
        Log.i(TAG, "-->>parseWheelSpeed 轮子转数：" + data[data.length - 1]);
//        EventBus.getDefault().post(new Event.BleMileageUpdate(data[data.length - 1]));
    }

    private void parseIsTested(Byte[] data) {
        //只返回一个字节的数据
        Log.i(TAG, "-->>parseIsTested 是否终测过：" + ((1 == data[data.length - 1]) ? "终测过" : "未终测过"));
    }

    private void parseSpeed(Byte[] data) {
        //只返回一个字节的数据
        Log.i(TAG, "-->>parseSpeed 速度为：" + data[data.length - 1] + "km/h");
//        EventBus.getDefault().post(new Event.BleSpeedUpdate(data[data.length - 1]));
    }

    private void parseTemperature(Byte[] data) {
        //只返回一个字节的数据
        Log.i(TAG, "-->>parseTemperature 终端温度为：" + data[data.length - 1] + "℃");
    }

    private void parseVoltage(Byte[] data) {
        for (byte b : data) {
            //只返回一个字节的数据
            Log.i(TAG, "-->>parseVoltage 电池电量为：" + b + "%");
//            EventBus.getDefault().post(new Event.BleVoltageUpdate(b));
        }
    }

    /**
     * 日志输出
     *
     * @param data
     */
    private void parseLog(Byte[] data) {
//        LogUtil.getInstance().info(TAG, "--收到的日志信息" + new String(data));
//        MobclickAgent.reportError(getApplicationContext(), "--终端发送的数据：" + new String(data));
    }

    /**
     * 解析系统状态，若系统状态有告警则发送告警广播，若系统状态中的各个状态与本地不一致，发送一个心跳包同步数据
     *
     * @param state
     */
    private void parseSysState(Byte state) {
        String alarm = "";

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
        }
    }

    private void sendACK(Packet rPacket, boolean error) {
        Packet.L1Header l1Header = new Packet.L1Header();
        l1Header.setLength((short) 0);
        l1Header.setACK(true);
        l1Header.setError(error);
        l1Header.setSequenceId(rPacket.getL1Header().getSequenceId());
        l1Header.setCRC16((short) 0);
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
                    try {
                        Thread.sleep(50L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
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
//        sendTimerThread.setStatus(TimerThread.STOP);
        Log.i(TAG, "Send ACK:" + send_packet.toString());
    }

    public void setReadTemp(byte[] value) {
        this.readTemp = ByteUtil.byteMerger(readTemp, value);//拼接缓存
    }

    public class sendThread extends Thread {
        byte[] mData;
        boolean SEND_OVER = true;//发送完全标志位，存在拆包问题

        sendThread(byte[] data) {
            mData = data;
        }

        public void updateStatus(boolean status) {
            SEND_OVER = status;
        }

        public void run() {
            final int packLength = 20;//每个数据包的长度最长为20字节
            int lastLength = mData.length;//数据的总长度
            Log.d(TAG, "--send_data_total_size: " + lastLength);
            byte[] sendData;
            int sendIndex = 0;
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (lastLength > 0 && isRunning) {
                Log.i(TAG, "--while xun huan");
                //此包的长度小于20字节，不用拆包，直接发送
                if (lastLength <= packLength) {
                    Log.i(TAG, "--不用拆包" + lastLength);
                    sendData = Arrays.copyOfRange(mData, sendIndex, sendIndex + lastLength);
                    sendIndex += lastLength;
                    lastLength = 0;
//                    SEND_OVER = true;
                } else {
                    //拆包发送
                    sendData = Arrays.copyOfRange(mData, sendIndex, sendIndex + packLength);
                    sendIndex += packLength;
                    lastLength -= packLength;
                    Log.i(TAG, "--拆包" + lastLength);
                }

                int count = 0;
                do {
                    try {
                        count++;
                        Thread.sleep(500L);
                        Log.i(TAG, "--do while 循环还在");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } while (!SEND_OVER && isRunning && count < 5);
                if (count >= 5) {
                    break;
                }
                //向蓝牙终端发送数据
                StringBuilder builder = new StringBuilder();
                for (byte b : sendData) {
                    builder.append(String.format("%02X ", b));
                }
                Log.i("dataComeGo", "--sendData= " + builder.toString());
                final byte[] resultData = sendData;
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        bluetoothIO.writeRXCharacteristic(resultData);
                    }
                });

                SEND_OVER = false;
            }
        }
    }

    class ReadDataThread extends Thread {
        boolean wait = true;
        boolean wait2 = true;

        public void setWait() {
            wait = true;
            wait2 = true;
        }

        @Override
        public void run() {
            super.run();
            while (isRunning) {
                try {
//                    Log.d(TAG, "run: reading data...");
                    if (readTemp != null && readTemp.length != 0) {
                        Log.d(TAG, "readTemp: " + ByteUtil.bytesToHexString(readTemp));
                        StringBuilder builder = new StringBuilder();
                        for (byte b : readTemp) {
                            builder.append(String.format("%02X ", b));
                        }
                        Log.i("dataComeGo", "--receiveData= " + builder.toString());
                        for (int i = 0; i < readTemp.length; i++) {
                            if (readTemp[i] == (byte) 0xAA) {
                                Log.i(TAG, "--找到头");
                                if (readTemp.length - i >= 8) {
                                    Log.i(TAG, "--头的长度够了");
                                    //可以拼接头
                                    System.arraycopy(readTemp, i, head, 0, 8);//把数据复制到head
                                    int len = head[5] & 0xFF;  //4 5角标为数据长度  这里存在小问题，后面研究
                                    if (len <= readTemp.length - 8) {
                                        //后面接着的数据达到len的长度，直接取出来
                                        receiveData = ByteUtil.subBytes(readTemp, i, i + 8 + len);//将完整的数据包截取出来
                                        Log.d(TAG, "=======================================");
                                        for (byte b : receiveData) {
                                            Log.i(TAG, "--receiveData: " + b);
                                        }
                                        parseReceivedPacket(receiveData);//发送指令
                                        Log.i(TAG, "--readTemp length" + readTemp.length);
                                        readTemp = ByteUtil.subBytes(readTemp, i + 8 + len, readTemp.length - (i + 8 + len));//清除已经发送的部分
                                        Log.i(TAG, "--readTemp length" + readTemp.length);
                                        break;
                                    } else {
                                        //后面缓存的数据不够len的长度，等待
                                        Log.d(TAG, "--readTemp 等待数据包");
                                        if (!wait) {
                                            //不等待了，把前面的头和数据丢掉
                                            readTemp = null;
                                            wait = true;
                                        }
                                        SystemClock.sleep(3000);
                                        wait = false;
                                    }

                                } else {
                                    Log.d(TAG, "--readTemp 等待数据包");
                                    //头不够长，等待头
                                    if (!wait2) {
                                        //不等待了，把前面的头
                                        readTemp = null;
                                        wait2 = true;
                                    }
                                    SystemClock.sleep(3000);
                                    wait2 = false;
                                }
                            }
                        }
                    }
                    SystemClock.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
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
}
