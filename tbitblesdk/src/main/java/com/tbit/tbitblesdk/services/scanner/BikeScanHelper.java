package com.tbit.tbitblesdk.services.scanner;

import android.bluetooth.BluetoothAdapter;
import android.text.TextUtils;

import com.tbit.tbitblesdk.services.BikeCallback;
import com.tbit.tbitblesdk.services.BluetoothIO;
import com.tbit.tbitblesdk.util.BikeUtil;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Salmon on 2017/3/8 0008.
 */

public class BikeScanHelper {
    public static final String DEFAULT_DEVICE_NAME = "[TBIT_WA-205]";
    private BluetoothIO bluetoothIO;
    private EventBus bus;
    private ScannerCallback callback;
    private String machineId;
    private Scanner scanner;
    private BikeCallback bikeCallback;
    private String encryptedMachineId;

    public BikeScanHelper(BluetoothAdapter bluetoothAdapter, BluetoothIO bluetoothIO) {
        this.bluetoothIO = bluetoothIO;
        this.bus = EventBus.getDefault();
        scanner = ScanHelper.getScanner(bluetoothAdapter, ScanHelper.DEFAULT_SCAN_TIMEOUT);
        initCallback();
    }

    public boolean scanAndConnect(String machineId) {
        if (scanner.isScanning())
            return false;
        if (!TextUtils.equals(this.machineId, machineId)) {
            this.machineId = machineId;
            this.encryptedMachineId = BikeUtil.encryptStr(machineId);
        }
        bluetoothIO.disconnect();
        bikeCallback.setMachineId(machineId);
        scanner.start(callback);
        return true;
    }

    public void setTimeout(long timeout) {
        scanner.setTimeout(timeout);
    }

    public void stop() {
        scanner.stop();
    }

    private void initCallback() {
        bikeCallback = new BikeCallback(scanner, bluetoothIO);
        ScanBuilder builder = new ScanBuilder(bikeCallback);
        this.callback = builder
//                .setFilter(DEFAULT_DEVICE_NAME)
                .setRepeatable(false)
                .setLogMode(true)
                .build();
    }

}
