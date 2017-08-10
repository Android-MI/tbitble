package com.tbit.tbitblesdk.Bike.services.resolver;

import com.tbit.tbitblesdk.Bike.model.BikeState;
import com.tbit.tbitblesdk.Bike.model.ControllerState;
import com.tbit.tbitblesdk.Bike.util.StateUpdateHelper;
import com.tbit.tbitblesdk.bluetooth.util.ByteUtil;
import com.tbit.tbitblesdk.user.entity.BControllerState;
import com.tbit.tbitblesdk.user.entity.W206State;

import java.util.Arrays;

import static android.R.attr.data;
import static com.tbit.tbitblesdk.Bike.util.StateUpdateHelper.byteArrayToInt;
import static com.tbit.tbitblesdk.Bike.util.StateUpdateHelper.byteToBitArray;
import static com.tbit.tbitblesdk.Bike.util.StateUpdateHelper.updateBaseStation;
import static com.tbit.tbitblesdk.Bike.util.StateUpdateHelper.updateLocation;
import static com.tbit.tbitblesdk.Bike.util.StateUpdateHelper.updateSignal;
import static com.tbit.tbitblesdk.Bike.util.StateUpdateHelper.updateVoltage;

/**
 * Created by yankaibang on 2017/8/9.
 */

public class W206Resolver implements Resolver<W206State> {

    private final int mSoftVersion;
    private final int mRawControllerStateLength;

    public W206Resolver(int softVersion) {
        mSoftVersion = softVersion;
        if (mSoftVersion <= 3) {
            mRawControllerStateLength = 13;
        } else {
            mRawControllerStateLength = 19;
        }
    }

    @Override
    public void resolveAll(BikeState bikeStates, Byte[] data) {
        if (data == null || data.length == 0)
            return;

        bikeStates.setRawData(data);

        if (data.length >= 10) {
            Byte[] locationData = Arrays.copyOfRange(data, 0, 10);
            resolveLocations(bikeStates, locationData);
        }
        if (data.length >= 13) {
            Byte[] signalData = Arrays.copyOfRange(data, 10, 13);
            updateSignal(bikeStates, signalData);
        }
        if (data.length >= 15) {
            Byte[] batteryData = Arrays.copyOfRange(data, 13, 15);
            updateVoltage(bikeStates, batteryData);
        }
        if (data.length >= 23) {
            Byte[] baseStationData = Arrays.copyOfRange(data, 15, 23);
            updateBaseStation(bikeStates, baseStationData);
        }
        if (data.length >= 23 + mRawControllerStateLength) {
            Byte[] rawControllerState = Arrays.copyOfRange(data, 23, 23 + mRawControllerStateLength);
            resolveControllerState(bikeStates, rawControllerState);
        }
    }

    @Override
    public void resolveControllerState(BikeState bikeState, Byte[] data) {
        if (data == null || data.length < mRawControllerStateLength)
            return;
        ControllerState controllerState = bikeState.getControllerState();
        controllerState.setRawData(data);
    }

    @Override
    public void resolveLocations(BikeState bikeState, Byte[] data) {
        StateUpdateHelper.updateLocation(bikeState, data);
    }

    @Override
    public W206State resolveCustomState(BikeState bikeState) {
        if (bikeState.getControllerState().getRawData().length < mRawControllerStateLength)
            return new W206State();

        if(mSoftVersion <= 3){
            return resolveCustomStateV3(bikeState);
        } else {
            return resolveCustomStateV4(bikeState);
        }
    }

    public W206State resolveCustomStateV3(BikeState bikeState) {
        W206State state = new W206State();
        byte[] originData = ByteUtil.byteArrayToUnBoxed(bikeState.getControllerState().getRawData());

        state.setMovingEi(byteArrayToInt(Arrays.copyOfRange(originData, 4, 5)));
        state.setChargeCount(byteArrayToInt(Arrays.copyOfRange(originData, 5, 7)));
        state.setBattery(byteArrayToInt(Arrays.copyOfRange(originData, 7, 8)));
        state.setControllerTemperature(byteArrayToInt(Arrays.copyOfRange(originData, 8, 9)));
        state.setSingleMileage(byteArrayToInt(Arrays.copyOfRange(originData, 9, 13)));

        int[] errCode = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
        byteToBitArray(originData[1], errCode);
        state.setErrorCode(errCode);

        int[] sysState = bikeState.getSystemState();
        int ctrlValue = (sysState[5] << 1) + sysState[4];
        state.setCtrlState(ctrlValue);
        return state;
    }

    public W206State resolveCustomStateV4(BikeState bikeState) {
        W206State state = resolveCustomStateV3(bikeState);
        byte[] originData = ByteUtil.byteArrayToUnBoxed(bikeState.getControllerState().getRawData());
        state.setTotalMileage(byteArrayToInt(Arrays.copyOfRange(originData, 9, 13)));
        state.setSingleMileage(byteArrayToInt(Arrays.copyOfRange(originData, 13, 17)));
        state.setExtVoltage(byteArrayToInt(Arrays.copyOfRange(originData, 17, 19)));
        return state;
    }
}
