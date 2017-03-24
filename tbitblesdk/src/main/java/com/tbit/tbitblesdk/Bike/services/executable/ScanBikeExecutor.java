package com.tbit.tbitblesdk.Bike.services.executable;

import com.tbit.tbitblesdk.Bike.BluEvent;
import com.tbit.tbitblesdk.Bike.model.ManufacturerAd;
import com.tbit.tbitblesdk.Bike.model.ParsedAd;
import com.tbit.tbitblesdk.Bike.ResultCode;
import com.tbit.tbitblesdk.Bike.model.SearchResult;
import com.tbit.tbitblesdk.Bike.services.BikeCallback;
import com.tbit.tbitblesdk.protocol.callback.ResultCallback;
import com.tbit.tbitblesdk.protocol.callback.SearchCallback;
import com.tbit.tbitblesdk.bluetooth.scanner.ScanHelper;
import com.tbit.tbitblesdk.bluetooth.scanner.Scanner;
import com.tbit.tbitblesdk.bluetooth.scanner.ScannerCallback;
import com.tbit.tbitblesdk.bluetooth.scanner.decorator.FilterNameCallback;
import com.tbit.tbitblesdk.bluetooth.scanner.decorator.LogCallback;
import com.tbit.tbitblesdk.bluetooth.scanner.decorator.NoneRepeatCallback;
import com.tbit.tbitblesdk.Bike.util.BikeUtil;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Salmon on 2017/3/21 0021.
 */

public class ScanBikeExecutor extends Executor implements SearchCallback {
    private static final String DEFAULT_DEVICE_NAME = "TBIT";
    private Scanner scanner;
    private String deviceId;
    private ResultCallback resultCallback;
    private ScannerCallback scannerCallback;

    public ScanBikeExecutor(String deviceId, ResultCallback resultCallback) {
        this.deviceId = deviceId;
        this.scanner = ScanHelper.getScanner(ScanHelper.DEFAULT_SCAN_TIMEOUT);
        this.resultCallback = resultCallback;

        String encryptedDeviceId = BikeUtil.encryptStr(deviceId);

        BikeCallback bikeCallback = new BikeCallback(encryptedDeviceId, resultCallback, this);
        FilterNameCallback filterNameCallback = new FilterNameCallback(DEFAULT_DEVICE_NAME, bikeCallback, false);
        NoneRepeatCallback noneRepeatCallback = new NoneRepeatCallback(filterNameCallback);
        scannerCallback = new LogCallback(noneRepeatCallback);

    }

    @Override
    protected void onExecute() {
        scanner.start(scannerCallback);
    }

    @Override
    public void onDeviceFound(SearchResult searchResult) {
        try {
            scanner.stop();
            ParsedAd ad = ParsedAd.parseData(searchResult.getBroadcastData());
            byte[] data = ad.getManufacturer();
            ManufacturerAd manufacturerAd = ManufacturerAd.resolveManufacturerAd(data);
            EventBus.getDefault().post(new BluEvent.BleBroadcast(manufacturerAd));
            notifySucceed();
        } catch (Exception e) {
            resultCallback.onResult(ResultCode.BROARCAST_RESOLUTION_FAILED);
            EventBus.getDefault().post(new BluEvent.DebugLogEvent("ScanBikeExecutor", e.getMessage()));
            notifyFailed();
        }
    }
}
