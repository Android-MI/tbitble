package com.tbit.tbitblesdk.services.scanner;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;

import com.tbit.tbitblesdk.protocol.BluEvent;
import com.tbit.tbitblesdk.protocol.ManufacturerAd;
import com.tbit.tbitblesdk.protocol.ParsedAd;
import com.tbit.tbitblesdk.services.BluetoothIO;
import com.tbit.tbitblesdk.util.BikeUtil;
import com.tbit.tbitblesdk.util.ByteUtil;

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
        ScannerCallback scannerCallback = new ScannerCallbackAdapter() {

            @Override
            public void onScanStop() {
                bus.post(new BluEvent.ScanTimeOut());
            }

            @Override
            public void onScanCanceled() {
//                bus.post(new BluEvent.ScanTimeOut());
            }

            @Override
            public void onDeviceFounded(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
                String dataStr = ByteUtil.bytesToHexStringWithoutSpace(bytes);
                boolean isFound = encryptedMachineId != null && dataStr.contains(encryptedMachineId);
                if (isFound) {
                    stop();
                    if (callback != null) {
                        publishVersion(bytes);
                        callback.onDeviceFounded(bluetoothDevice, i, bytes);
                    }
                    bluetoothIO.connect(bluetoothDevice, false);
                }
            }
        };
        ScanBuilder builder = new ScanBuilder(scannerCallback);
        this.callback = builder
//                .setFilter(DEFAULT_DEVICE_NAME)
                .setRepeatable(false)
                .setLogMode(true)
                .build();
    }

    protected void publishVersion(byte[] bytes) {
        try {
            ParsedAd ad = ParsedAd.parseData(bytes);
            byte[] data = ad.getManufacturer();
            ManufacturerAd manufacturerAd = ManufacturerAd.resolveManufacturerAd(data);
            final int hard = manufacturerAd.getHardware();
            final int firm = manufacturerAd.getSoftware();
            EventBus.getDefault().post(new BluEvent.VersionResponse(hard, firm));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
