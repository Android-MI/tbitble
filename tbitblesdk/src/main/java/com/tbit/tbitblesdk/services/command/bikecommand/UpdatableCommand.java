package com.tbit.tbitblesdk.services.command.bikecommand;


import com.tbit.tbitblesdk.protocol.BikeState;
import com.tbit.tbitblesdk.services.command.callback.ResultCallback;
import com.tbit.tbitblesdk.services.command.callback.StateCallback;
import com.tbit.tbitblesdk.util.StateUpdateHelper;

/**
 * Created by Salmon on 2017/3/15 0015.
 */

public abstract class UpdatableCommand extends BikeCommand {

    protected BikeState bikeState;
    protected StateCallback stateCallback;

    public UpdatableCommand(ResultCallback resultCallback, StateCallback stateCallback, BikeState bikeState) {
        super(resultCallback);
        this.bikeState = bikeState;
        this.stateCallback = stateCallback;
    }

    protected void parseVerifyFailed(Byte[] data) {
        StateUpdateHelper.updateVerifyFailed(bikeState, data);
    }

    protected void parseLocation(Byte[] data) {
        StateUpdateHelper.updateLocation(bikeState, data);
    }

    protected void parseSignal(Byte[] data) {
        StateUpdateHelper.updateSignal(bikeState, data);
    }

    protected void parseControllerState(Byte[] data) {
        StateUpdateHelper.updateControllerState(bikeState, data);
    }

    protected void parseBaseStation(Byte[] data) {
        StateUpdateHelper.updateBaseStation(bikeState, data);
    }

    protected void parseAll(Byte[] data) {
        StateUpdateHelper.updateAll(bikeState, data);
    }

    protected void parseVoltage(Byte[] data) {
        StateUpdateHelper.updateVoltage(bikeState, data);
    }

    protected void parseDeviceFault(Byte[] data) {
        StateUpdateHelper.updateDeviceFault(bikeState, data);
    }
}
