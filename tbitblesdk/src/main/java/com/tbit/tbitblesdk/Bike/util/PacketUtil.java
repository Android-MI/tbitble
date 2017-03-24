package com.tbit.tbitblesdk.bike.util;

import com.tbit.tbitblesdk.protocol.Constant;
import com.tbit.tbitblesdk.protocol.CrcUtil;
import com.tbit.tbitblesdk.protocol.Packet;
import com.tbit.tbitblesdk.protocol.PacketHeader;
import com.tbit.tbitblesdk.protocol.PacketValue;

/**
 * Created by Salmon on 2017/3/14 0014.
 */

public class PacketUtil {

    public static Packet createPacket(int requestCode, byte commandId, byte key, Byte[] data) {
        return createPacket(requestCode, commandId, new PacketValue.DataBean(key, data));
    }

    public static Packet createPacket(int requestCode, byte commandId, PacketValue.DataBean... dataBeans) {
        PacketValue packetValue = new PacketValue();
        packetValue.setCommandId(commandId);
        packetValue.addData(dataBeans);

        short valueLength = (short) packetValue.getSize();
        byte crc16 = (byte) CrcUtil.crcTable(Constant.crcTable, packetValue.toArray());

        PacketHeader header = new PacketHeader.HeaderBuilder()
                .setLength(valueLength)
                .setSystemState((byte) 0x00)
                .setAck(false)
                .setError(false)
                .setCRC16(crc16)
                .build();


        Packet newPacket = new Packet(header, packetValue);

        return newPacket;
    }

    public static Packet createAck(int sequenceId, boolean error) {
        PacketHeader packetHeader = new PacketHeader.HeaderBuilder()
                .setAck(true)
                .setError(error)
                .setSequenceId((byte) sequenceId)
                .setSystemState((byte) 0x00)
                .setLength((short) 0)
                .setCRC16((short) 0)
                .build();

        return new Packet(packetHeader);
    }

    public static Packet createAck(int sequenceId) {
       return createAck(sequenceId, false);
    }

    public static boolean compareCommandId(Packet packet1, Packet packet2) {
        int commandId1 = packet1.getPacketValue().getCommandId();
        int commandId2 = packet2.getPacketValue().getCommandId();
        return commandId1 == commandId2;
    }

    public static boolean checkPacketValueContainKey(Packet packet, int key) {
        boolean result = false;
        for (PacketValue.DataBean b : packet.getPacketValue().getData()) {
            int packetKey = b.key & 0xff;
            if (packetKey == key)
                result = true;
        }
        return  result;
    }

}

