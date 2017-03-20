package com.tbit.tbitblesdk.services.command;

import com.tbit.tbitblesdk.protocol.Packet;

/**
 * Created by Salmon on 2017/3/14 0014.
 */

public interface CommandHolder {

    void sendCommand(Packet packet);

    void onCommandCompleted();

    int getBluetoothState();

}
