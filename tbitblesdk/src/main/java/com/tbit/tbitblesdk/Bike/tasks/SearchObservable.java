package com.tbit.tbitblesdk.Bike.tasks;

import android.bluetooth.BluetoothDevice;

import com.tbit.tbitblesdk.Bike.ResultCode;
import com.tbit.tbitblesdk.Bike.tasks.exceptions.ResultCodeThrowable;
import com.tbit.tbitblesdk.Bike.util.BikeUtil;
import com.tbit.tbitblesdk.bluetooth.model.SearchResult;
import com.tbit.tbitblesdk.bluetooth.scanner.Scanner;
import com.tbit.tbitblesdk.bluetooth.scanner.ScannerCallback;
import com.tbit.tbitblesdk.bluetooth.scanner.decorator.LogCallback;
import com.tbit.tbitblesdk.bluetooth.scanner.decorator.NoneRepeatCallback;
import com.tbit.tbitblesdk.bluetooth.util.ByteUtil;

import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;

/**
 * Created by Salmon on 2017/4/11 0011.
 */

public class SearchObservable implements ObservableOnSubscribe<SearchResult>, ScannerCallback {

    private String machineId;
    private String encryptedMachineId;
    private Scanner scanner;
    private ScannerCallback decoratedCallback;
    private ObservableEmitter<SearchResult> emitter;

    public SearchObservable(String machineId, Scanner scanner) {
        this.scanner = scanner;
        this.machineId = machineId;

        encryptMachineId();
        decorateCallback();
    }

    private void encryptMachineId() {
        this.encryptedMachineId = BikeUtil.encryptStr(machineId);
    }

    private void decorateCallback() {
        LogCallback logCallback = new LogCallback(this);
        this.decoratedCallback = new NoneRepeatCallback(logCallback);
    }

    @Override
    public void subscribe(@NonNull ObservableEmitter<SearchResult> e) throws Exception {
        this.emitter = e;
        scanner.start(decoratedCallback, 10000);
    }

    @Override
    public void onScanStart() {

    }

    @Override
    public void onScanStop() {
        emitter.onError(new ResultCodeThrowable("SearchObservable: timeout", ResultCode.DEVICE_NOT_FOUNDED));
    }

    @Override
    public void onScanCanceled() {

    }

    @Override
    public void onDeviceFounded(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
        String dataStr = ByteUtil.bytesToHexStringWithoutSpace(bytes);
        boolean isFound = encryptedMachineId != null && dataStr.contains(encryptedMachineId);
        if (isFound) {
            scanner.stop();
            emitter.onNext(new SearchResult(bluetoothDevice, i, bytes));
        }
    }
}
