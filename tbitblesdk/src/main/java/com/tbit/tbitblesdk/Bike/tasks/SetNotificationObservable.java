package com.tbit.tbitblesdk.Bike.tasks;

import com.tbit.tbitblesdk.Bike.ResultCode;
import com.tbit.tbitblesdk.Bike.services.config.BikeConfig;
import com.tbit.tbitblesdk.Bike.services.config.Uuid;
import com.tbit.tbitblesdk.Bike.tasks.exceptions.ResultCodeThrowable;
import com.tbit.tbitblesdk.bluetooth.Code;
import com.tbit.tbitblesdk.bluetooth.RequestDispatcher;
import com.tbit.tbitblesdk.bluetooth.request.BleResponse;
import com.tbit.tbitblesdk.bluetooth.request.NotifyRequest;

import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;

/**
 * Created by Salmon on 2017/4/12 0012.
 */

public class SetNotificationObservable implements ObservableOnSubscribe<BikeConfig> {

    private BikeConfig bikeConfig;
    private RequestDispatcher requestDispatcher;

    public SetNotificationObservable(RequestDispatcher requestDispatcher, BikeConfig bikeConfig) {
        this.bikeConfig = bikeConfig;
        this.requestDispatcher = requestDispatcher;
    }

    @Override
    public void subscribe(@NonNull final ObservableEmitter<BikeConfig> e) throws Exception {
        Uuid uuid = bikeConfig.getUuid();
        requestDispatcher.addRequest(new NotifyRequest(new BleResponse() {
            @Override
            public void onResponse(int resultCode) {
                if (resultCode == Code.REQUEST_SUCCESS)
                    e.onNext(bikeConfig);
                else
                    e.onError(new ResultCodeThrowable("SetNotificationObservable: " + resultCode,
                            ResultCode.BLE_NOT_SUPPORTED));
            }
        }, uuid.SPS_SERVICE_UUID, uuid.SPS_TX_UUID, uuid.SPS_NOTIFY_DESCRIPTOR, true));
    }
}
