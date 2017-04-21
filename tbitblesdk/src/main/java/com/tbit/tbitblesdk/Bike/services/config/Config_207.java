package com.tbit.tbitblesdk.Bike.services.config;

import com.tbit.tbitblesdk.Bike.services.command.comparator.CommandComparator;
import com.tbit.tbitblesdk.Bike.services.command.comparator.CommandInsideComparator;

import java.util.UUID;

/**
 * Created by Salmon on 2017/4/21 0021.
 */

public class Config_207 implements BikeConfig {
    private Uuid uuid;
    private CommandComparator commandComparator;

    public Config_207() {
        uuid = new Uuid();
        uuid.SPS_SERVICE_UUID = UUID.fromString("0000fef6-0000-1000-8000-00805f9b34fb");
        uuid.SPS_TX_UUID = UUID.fromString("0783b03e-8535-b5a0-7140-a304d2495cb8");
        uuid.SPS_RX_UUID = UUID.fromString("0783b03e-8535-b5a0-7140-a304d2495cba");
        uuid.SPS_NOTIFY_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
        uuid.SPS_CTRL_UUID = UUID.fromString("0783b03e-8535-b5a0-7140-a304d2495cb9");

        commandComparator = new CommandInsideComparator();
    }

    @Override
    public Uuid getUuid() {
        return uuid;
    }

    @Override
    public CommandComparator getComparator() {
        return commandComparator;
    }
}
