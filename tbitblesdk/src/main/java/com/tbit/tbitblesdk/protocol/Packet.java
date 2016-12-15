package com.tbit.tbitblesdk.protocol;

import android.util.Log;


import com.tbit.tbitblesdk.util.ByteUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Kenny on 2016/2/26 18:31.
 * Desc：
 */
public class Packet {
    private static final String TAG = "packet";
    private int mCrc;
    private List<Byte> mPacket = new ArrayList<>();//数据包
    private byte mSequenceId = 1;
    private CRC16 crc16 = new CRC16();
    private int mPacketError;

    public Packet(byte[] value) {
        append(value);
    }

    public Packet() {

    }

    private L1Header genL1Header() {
        int packetvalue_length = this.getPacketValue().toList().size();
        Byte[] packetvalue_bytes = new Byte[packetvalue_length];
        L1Header aL1Header = new L1Header();
        this.getPacketValue().toList().toArray(packetvalue_bytes);

//        int crc = crc16.getCrc(packetvalue_bytes);
//        Log.i(TAG,"--herecrc"+crc);
        byte[] b = new byte[packetvalue_bytes.length];
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            b[i] = packetvalue_bytes[i].byteValue();
            sb.append(b[i]);
            sb.append(" ");
        }
        Log.d(TAG, "-->>>>>>>" + sb.toString());
        int crc = crcTable(b);
        Log.i(TAG, "--herecrc=" + crc + "--short crc=" + (short) crc);
        aL1Header.setSystemState(getSysState());
        aL1Header.setCRC16((short) crc);
        aL1Header.setLength((short) packetvalue_length);
        aL1Header.setSequenceId(mSequenceId);
        aL1Header.setAckError(false, false);
        setL1Header(aL1Header);
        return aL1Header;
    }

    /**
     * 将数据包转换成字节类型准备发送
     *
     * @return
     */
    public byte[] toByteArray() {

        int packet_length = mPacket.size();
        Byte[] packet_bytes = new Byte[packet_length];
        mPacket.toArray(packet_bytes);
        byte[] aPacket = ByteUtil.byteArrayUnBox(packet_bytes);
        return aPacket;
    }

    public void setPacket(byte[] packet) {
        mPacket.clear();
        mPacket.addAll(Arrays.asList(ByteUtil.byteArrayBox(packet)));
    }

    /**
     * 设置大头
     *
     * @param l1header
     */
    public void setL1Header(L1Header l1header) {
        List<Byte> nPacket = new ArrayList<Byte>();
        nPacket.addAll(l1header.toList());//根据之前的数据新构建的头
        //subList(起始（包含），终止（不包含））
        if (mPacket.size() > 8) {
            nPacket.addAll(mPacket.subList(8, mPacket.size()));//取之前构造好的数据包的数据部分添加到新的大头上
        }
        mPacket = nPacket;//包含头的数据包  传输头，装载数据  最后应该要发送的正确的数据包
    }

    public L1Header getL1Header() {
        if (mPacket.size() < 8) {
            return null;
        }
        List<Byte> aL1Header = mPacket.subList(0, 8);
        L1Header nL1Header = new L1Header(aL1Header);
        return nL1Header;
    }

    public void setPacketValue(PacketValue packetValue, boolean genL1) {
        if (mPacket.size() == 0) {
            mPacket.addAll(new L1Header().toList());//加上大头
        }

        List<Byte> nPacket = new ArrayList<Byte>();
        nPacket.addAll(mPacket.subList(0, 8));//组装大头
        if (packetValue == null) {

        } else {
            nPacket.addAll(packetValue.toList());//组装大头加装载数据
        }
        mPacket = nPacket;//将组装后的赋值给即将发送的数据包
        if (genL1) {
            genL1Header();
        }
    }

    public void setHeadSerialNo(int requestCode) {
        mSequenceId = (byte) requestCode;
    }

    public PacketValue getPacketValue() {
        if (mPacket.size() < 10) {
            //没有装载数据
            return null;
        }
        List<Byte> aPacketValue = mPacket.subList(8, mPacket.size());
        PacketValue mPacketValue = new PacketValue(aPacketValue);
        return mPacketValue;
    }

    public void append(byte[] data) {
        Byte[] aData = ByteUtil.byteArrayBox(data);
        mPacket.addAll(Arrays.asList(aData));

        //checkPacket();
    }


    /**
     * 检测数据包格式的正确性
     *
     * @return
     */
    public int checkPacket() {

        if (mPacket.size() > 512) {
            mPacket.clear();
        }
        L1Header aL1Header = getL1Header();
        PacketValue aPacketValue = getPacketValue();
        mPacketError = 0;

        //ACK错误，重发
        if ((aL1Header == null)) {
            mPacketError = 0x03;
            return mPacketError;
        }

        //数据头错误
        if (aL1Header.toList().get(0).byteValue() != (byte) 0xAA) {
            mPacketError = 0x05;
            return mPacketError;
        }
        //收到正确的ack反馈
        if (aL1Header.getAckError() != 0x00) {
            mPacketError = aL1Header.getAckError();
            return mPacketError;
        }
//        Log.i(BluetoothLeService.TAG,"Length:"+Short.toString(aL1Header.getLength()));

        //没有装载数据
        if ((aPacketValue == null)) {
            mPacketError = 0x07;
            return mPacketError;
        }
//        Log.i(BluetoothLeService.TAG,"Size:"+Integer.toString(aPacketValue.toList().size()));
        //大头中的装载数据长度指示器与实际装载的数据长度不一致
        if (aL1Header.getLength() > aPacketValue.toList().size()) {
            mPacketError = 0x09;
            return mPacketError;
        }
        Byte[] aPacketValueBytes = new Byte[aPacketValue.toList().size()];
        aPacketValue.toList().toArray(aPacketValueBytes);
        /////////////////////
        byte[] b = new byte[aPacketValueBytes.length];
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            b[i] = aPacketValueBytes[i].byteValue();
            sb.append(b[i]);
            sb.append(" ");
        }

        int tempcrc = crcTable(b);
        short crc = (short) tempcrc;
        Log.i(TAG, "-->>check" + sb.toString() + "--tempcrc" + tempcrc + "--crc" + crc);
        if (aL1Header.getCRC16() != crc) {
//            crc
            mPacketError = 0x0B;
        } else {
            mPacketError = 0;
        }
        return mPacketError;
    }


    public void clear() {
        mPacket.clear();
    }

    public boolean isChecked() {
        return (mPacketError == 0);
    }

    public void print() {
        StringBuilder strBuilder = new StringBuilder();
        for (byte bb : toByteArray()) {
            strBuilder.append(String.format("%02X ", bb));
        }
        strBuilder.append("\n");
//        Log.i(TAG, strBuilder.toString());
    }

    @Override
    public String toString() {
        StringBuilder strBuilder = new StringBuilder();
        for (byte bb : toByteArray()) {
            strBuilder.append(String.format("%02X ", bb));
        }
        strBuilder.append("\n");
        return strBuilder.toString();
    }

    public static void Print(List<Byte> data) {
        StringBuilder strBuilder = new StringBuilder();
        for (byte bb : data) {
            strBuilder.append(String.format("%02X ", bb));
        }
        strBuilder.append("\n");
        Log.i(TAG, "Print:" + strBuilder.toString());
    }


    //L2Header就不具体分了，与数据一同处理
    static public class L1Header {
        List<Byte> L1Header;
        //起始位（1），版本号（1），系统状态（1），流水号（1），数据长度（2），crc校验（2）
        private Byte[] aL1Header = {(byte) 0xAA, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};

        public L1Header() {
            L1Header = new ArrayList<>(8);
            //L1Header.clear();
        }

        public L1Header(List<Byte> aData) {
            aData.toArray(aL1Header);
        }

        public void setSystemState(byte systemState) {
            byte[] state = ByteUtil.byteToByte(systemState);
            aL1Header[2] = state[0];
        }

        public byte getSystemState() {
            byte state = aL1Header[2];
            Log.i(TAG, "--getSystemState" + state);
            return state;
        }

        public void setLength(short length) {
            byte[] len = ByteUtil.shortToByte(length);
            aL1Header[4] = len[0];
            aL1Header[5] = len[1];
        }

        public short getLength() {
            int value;
            value = aL1Header[4] & 0x000000ff;
            value = value << 8;
            value |= aL1Header[5] & 0x000000ff;
            return (short) value;

        }

        public void setCRC16(short crc16) {
            byte[] crc = ByteUtil.shortToByte(crc16);
            aL1Header[6] = crc[0];
            aL1Header[7] = crc[1];
        }

        public short getCRC16() {
            int value;
            value = aL1Header[6] & 0x000000ff;
            value = value << 8;
            value |= aL1Header[7] & 0x000000ff;
            return (short) value;
        }

        public void setSequenceId(byte sequenceid) {
            byte[] sid = ByteUtil.byteToByte(sequenceid);
//            aL1Header[6] = sid[0];
//            aL1Header[7] = sid[1];
            aL1Header[3] = sid[0];
        }

        public byte getSequenceId() {
            byte value = aL1Header[3];
            return value;
        }

        public void setACK(boolean ack) {
            byte sta = aL1Header[1];
            if (ack == true) {
                sta |= (byte) 0x10;
            } else {
                sta &= ~(byte) 0x10;
            }
            aL1Header[1] = sta;
        }

        public boolean getACK() {
            return ((aL1Header[1] & 0x10) == 0x10);
        }

        public void setError(boolean err) {
            byte sta = aL1Header[1];
            if (err == true) {
                sta |= (byte) 0x20;
            } else {
                sta &= ~(byte) 0x20;
            }
            aL1Header[1] = sta;
        }

        public boolean getError() {
            return ((aL1Header[1] & 0x20) == 0x20);
        }

        public void setAckError(boolean ack, boolean err) {
            setACK(ack);
            setError(err);
        }

        public byte getAckError() {
            return aL1Header[1];
        }

        public List<Byte> toList() {
            L1Header = Arrays.asList(aL1Header);
            return L1Header;
        }
    }

    public class CRC16 {
        private short[] crcTable = new short[256];
        private int gPloy = 0x1021; // 生成多项式

        public CRC16() {
            computeCrcTable();
        }

        private short getCrcOfByte(int aByte) {
            int value = aByte << 8;

            for (int count = 7; count >= 0; count--) {
                if ((value & 0x8000) != 0) { // 高第16位为1，可以按位异或
                    value = (value << 1) ^ gPloy;
                } else {
                    value = value << 1; // 首位为0，左移
                }

            }
            value = value & 0xFFFF; // 取低16位的值
            return (short) value;
        }

        /*
         * 生成0 - 255对应的CRC16校验码
         */
        private void computeCrcTable() {
            for (int i = 0; i < 256; i++) {
                crcTable[i] = getCrcOfByte(i);
            }
        }

        public short getCrc(Byte[] data) {
            int crc = 0;
            int length = data.length;
            for (int i = 0; i < length; i++) {
                crc = ((crc & 0xFF) << 8) ^ crcTable[(((crc & 0xFF00) >> 8) ^ data[i]) & 0xFF];
            }
            crc = crc & 0xFFFF;
            Log.i(TAG, "--crc" + crc);
            return (short) crc;
        }
    }

    private short crcTable(byte[] bytes) {
        int[] table = Constant.crcTable;

//        int crc = 0xffff;
        mCrc = 0xffff;

        for (byte b : bytes) {
            mCrc = (mCrc >>> 8) ^ table[(mCrc ^ b) & 0xff];
        }

//        int a = ~crc;
        Log.i(TAG, "-->>" + Integer.toHexString(~mCrc));
//        Log.i(TAG, "--" + (~mCrc == 57926));
        Log.i(TAG, "-->>crc check table" + ~mCrc + "--" + Integer.toHexString(57926) + "--" + Integer.toHexString(~mCrc));
        return (short) ((~mCrc) & 0xffff);
    }

    /**
     * Get the system state witch APP have saved to local.
     *
     * @return
     */
    public byte getSysState() {

//        MyApplication application = MyApplication.getInstance();
//        Map<Integer, String> map = application.readA1StateData(application.getCurCar().getCarId());
//
////        boolean fastStart = (boolean) SharePreferenceUtil.getInstance().getData(Constant.SP_FAST_START, false);
////        boolean door_lock = (boolean) SharePreferenceUtil.getInstance().getData(Constant.SP_POWER_DOOR_LOCK, false);
////        boolean quiet_lock = (boolean) SharePreferenceUtil.getInstance().getData(Constant.SP_MODE_QUIET_LOCK, false);
////        boolean mode_lock = (boolean) SharePreferenceUtil.getInstance().getData(Constant.SP_MODE_LOCK, false);
////        boolean mode_control = (boolean) SharePreferenceUtil.getInstance().getData(Constant.SP_MODE_CONTROL, false);
////        boolean guard = (boolean) SharePreferenceUtil.getInstance().getData(Constant.SP_GUARD, false);
//
//        byte state = (byte) 0x00;
//        if ("1".equals(map.get(Constant.REQUEST_FAST_START))) {
//            state = (byte) (state | 0x20);
//        }
//        if ("1".equals(map.get(Constant.REQUEST_SILENCE))) {
//            state = (byte) (state | 0x08);
//        }
//        if ("手动".equals(map.get(Constant.REQUEST_AUTO_DEFENCE))) {
//            state = (byte) (state | 0x04);
//        }
//        if ("手柄".equals(map.get(Constant.REQUEST_CONTROL))) {
//            state = (byte) (state | 0x02);
//        }
//        if ("1".equals(map.get(Constant.REQUEST_SET_DEFENCE))) {
//            state = (byte) (state | 0x01);
//        }
//        Log.i(TAG, "--getSysState" + state);

        return (byte) 0x00;
    }
}
