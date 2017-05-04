package com.tbit.tbitblesdk.Bike.services.resolver;

import com.tbit.tbitblesdk.Bike.model.BikeState;
import com.tbit.tbitblesdk.Bike.model.ControllerState;

import java.util.Arrays;

import static com.tbit.tbitblesdk.Bike.util.StateUpdateHelper.updateBaseStation;
import static com.tbit.tbitblesdk.Bike.util.StateUpdateHelper.updateLocation;
import static com.tbit.tbitblesdk.Bike.util.StateUpdateHelper.updateSatelliteCount;
import static com.tbit.tbitblesdk.Bike.util.StateUpdateHelper.updateSignal;
import static com.tbit.tbitblesdk.Bike.util.StateUpdateHelper.updateVoltage;

/**
 * Created by Salmon on 2017/4/27 0027.
 */

public class W207Resolver implements Resolver {

    @Override
    public void resolveAll(BikeState bikeStates, Byte[] data) {
        if (data == null || data.length == 0)
            return;

        bikeStates.setRawData(data);

        if (data.length >= 10) {
            Byte[] locationData = Arrays.copyOfRange(data, 0, 10);
            updateLocation(bikeStates, locationData);
        }
        if (data.length >= 11) {
            Byte[] locationData = Arrays.copyOfRange(data, 10, 11);
            updateSatelliteCount(bikeStates, locationData);
        }
        if (data.length >= 14) {
            Byte[] signalData = Arrays.copyOfRange(data, 11, 14);
            updateSignal(bikeStates, signalData);
        }
        if (data.length >= 16) {
            Byte[] batteryData = Arrays.copyOfRange(data, 14, 16);
            updateVoltage(bikeStates, batteryData);
        }
        if (data.length >= 24) {
            Byte[] baseStationData = Arrays.copyOfRange(data, 16, 24);
            updateBaseStation(bikeStates, baseStationData);
        }
        if (data.length >= 44) {
            Byte[] controllerInfoData = Arrays.copyOfRange(data, 24, 44);
            resolveControllerState(bikeStates, controllerInfoData);
        }
    }

    @Override
    public void resolveControllerState(BikeState bikeState, Byte[] data) {
        if (data == null || data.length != 19)
            return;
        ControllerState controllerState = bikeState.getControllerState();

        controllerState.setRawData(data);
    }
}