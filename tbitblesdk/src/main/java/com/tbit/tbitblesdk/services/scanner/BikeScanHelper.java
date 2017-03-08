package com.tbit.tbitblesdk.services.scanner;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import com.tbit.tbitblesdk.protocol.BluEvent;
import com.tbit.tbitblesdk.services.BluetoothIO;

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
    private BikeScanner bikeScanner;

    public BikeScanHelper(BluetoothAdapter bluetoothAdapter, BluetoothIO bluetoothIO) {
        this.bluetoothIO = bluetoothIO;
        this.bus = EventBus.getDefault();
        Scanner originScanner = ScanHelper.getScanner(bluetoothAdapter, ScanHelper.DEFAULT_SCAN_TIMEOUT);
        bikeScanner = new BikeScanner(originScanner);
        initCallback();
    }

    public void scanAndConnect(String machineId) {
        this.machineId = machineId;
        bikeScanner.setMachineId(machineId);
        bikeScanner.start(callback);
    }

    public void setTimeout(long timeout) {
        bikeScanner.setTimeout(timeout);
    }

    public void stop() {
        bikeScanner.stop();
    }

    private void initCallback() {
        ScannerCallback scannerCallback = new ScannerCallbackAdapter() {

            @Override
            public void onScanStop() {
                bus.post(new BluEvent.ScanTimeOut());
            }

            @Override
            public void onScanCanceled() {
                bus.post(new BluEvent.ScanTimeOut());
            }

            @Override
            public void onDeviceFounded(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
                bluetoothIO.connect(bluetoothDevice, false);
            }
        };
        ScanBuilder builder = new ScanBuilder(scannerCallback);
        this.callback = builder.setLogMode(true)
                .setRepeatable(false)
                .setFilter(DEFAULT_DEVICE_NAME)
                .build();
    }
}
