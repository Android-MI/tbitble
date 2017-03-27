package com.tbit.tbitblesdk.protocol.dispatcher;

import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.tbit.tbitblesdk.bluetooth.IBleClient;
import com.tbit.tbitblesdk.bluetooth.listener.ChangeCharacterListener;
import com.tbit.tbitblesdk.bluetooth.util.ByteUtil;
import com.tbit.tbitblesdk.protocol.Packet;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Salmon on 2017/3/23 0023.
 */

public class ReceivedPacketDispatcher implements ChangeCharacterListener, Handler.Callback {

    private static final int HEAD_LENGTH = 8;
    private static final Byte HEAD_FLAG = new Byte((byte) 0xAA);
    private static final int PACKET_LENGTH_INDEX = 5;

    private IBleClient bleClient;
    private List<Byte> receivedData = Collections.synchronizedList(new LinkedList<Byte>());
    private List<PacketResponseListener> packetResponseList = new LinkedList<>();
    private Handler handler;

    private UUID serviceUuid;
    private UUID characterUuid;

    public ReceivedPacketDispatcher(IBleClient bleClient) {
        this.bleClient = bleClient;
        this.handler = new Handler(Looper.myLooper(), this);
        bleClient.getListenerManager().addChangeCharacterListener(this);
    }

    public void setServiceUuid(UUID serviceUuid) {
        this.serviceUuid = serviceUuid;
    }

    public void setCharacterUuid(UUID characterUuid) {
        this.characterUuid = characterUuid;
    }

    public void addPacketResponseListener(PacketResponseListener packetResponseListener) {
        packetResponseList.add(0,packetResponseListener);
    }

    public void removePacketResponseListener(PacketResponseListener packetResponseListener) {
        packetResponseList.remove(packetResponseListener);
    }

    public void destroy() {
        packetResponseList.clear();
        bleClient.getListenerManager().removeChangeCharacterListener(this);
    }

    public void finish() {
        bleClient.getListenerManager().removeChangeCharacterListener(this);
    }

    private void tryResolve() {
        //0xAA才是数据包的头
        if (!receivedData.get(0).equals(HEAD_FLAG)) {
            Iterator<Byte> iterator = receivedData.iterator();
            while (iterator.hasNext())
            {
                if (!iterator.next().equals(HEAD_FLAG)) {
                    iterator.remove();
                } else {
                    break;
                }
            }
        }

        // 等待头长度足够
        if (receivedData.size() < HEAD_LENGTH)
            return;

        // 数据包长度
        int dataPacketLen = receivedData.get(PACKET_LENGTH_INDEX) & 0xFF;

        // 数据包长度不足
        if (receivedData.size() - HEAD_LENGTH < dataPacketLen)
            return;

        // 数据包长度足够，取出数据包
        int totalLength = HEAD_LENGTH + dataPacketLen;
        byte[] data = new byte[totalLength];
        for (int i = 0; i < totalLength; i++) {
            data[i] = receivedData.remove(0);
        }
        // 发布数据
        publishData(data);
    }

    private void publishData(byte[] data) {
        Packet packet = new Packet(data);
        for (PacketResponseListener listener : packetResponseList) {
            if (listener.onPacketReceived(packet))
                break;
        }
    }

    @Override
    public void onCharacterChange(BluetoothGattCharacteristic characteristic, final byte[] value) {
        if (serviceUuid != null && !serviceUuid.equals(characteristic.getService().getUuid()))
            return;
        if (characterUuid != null && !characteristic.equals(characteristic.getUuid()))
            return;
        handler.post(new Runnable() {
            @Override
            public void run() {
                Byte[] data = ByteUtil.byteArrayToBoxed(value);
                receivedData.addAll(Arrays.asList(data));
                tryResolve();
            }
        });
    }

    @Override
    public boolean handleMessage(Message msg) {
        return true;
    }
}
