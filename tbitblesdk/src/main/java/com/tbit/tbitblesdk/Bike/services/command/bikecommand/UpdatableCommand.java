package com.tbit.tbitblesdk.Bike.services.command.bikecommand;


import com.tbit.tbitblesdk.Bike.model.BikeState;
import com.tbit.tbitblesdk.Bike.services.command.Command;
import com.tbit.tbitblesdk.protocol.callback.ResultCallback;
import com.tbit.tbitblesdk.Bike.services.command.callback.StateCallback;
import com.tbit.tbitblesdk.Bike.util.StateUpdateHelper;

/**
 * Created by Salmon on 2017/3/15 0015.
 */

public abstract class UpdatableCommand extends Command {

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
