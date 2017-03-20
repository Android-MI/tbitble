package com.tbit.tbitblesdk.util;

import com.tbit.tbitblesdk.protocol.Packet;
import com.tbit.tbitblesdk.protocol.PacketValue;

/**
 * Created by Salmon on 2017/3/14 0014.
 */

public class PacketUtil {

    public static Packet createPacket(int requestCode, byte commandId, PacketValue.DataBean... dataBeans) {
        PacketValue packetValue = new PacketValue();
        packetValue.setCommandId(commandId);
        packetValue.addData(dataBeans);
        Packet packet = new Packet();
        packet.setHeadSerialNo(requestCode);
        packet.setPacketValue(packetValue, true);
        return packet;
    }

    public static Packet createPacket(int requestCode, byte commandId, byte key, Byte[] data) {
        return createPacket(requestCode, commandId, new PacketValue.DataBean(key, data));
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

