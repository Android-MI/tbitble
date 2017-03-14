package com.tbit.tbitblesdk.services;

import android.bluetooth.BluetoothDevice;

import com.tbit.tbitblesdk.protocol.BluEvent;
import com.tbit.tbitblesdk.protocol.ManufacturerAd;
import com.tbit.tbitblesdk.protocol.ParsedAd;
import com.tbit.tbitblesdk.services.scanner.Scanner;
import com.tbit.tbitblesdk.services.scanner.ScannerCallback;
import com.tbit.tbitblesdk.util.BikeUtil;
import com.tbit.tbitblesdk.util.ByteUtil;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Salmon on 2017/3/9 0009.
 */

public class BikeCallback implements ScannerCallback {

    private Scanner scanner;
    private EventBus bus;
    private String encryptedMachineId;
    private BluetoothIO bluetoothIO;
    private String machineId;

    public BikeCallback(Scanner scanner, BluetoothIO bluetoothIO) {
        this.scanner = scanner;
        this.bus = EventBus.getDefault();
        this.bluetoothIO = bluetoothIO;
    }

    public void setMachineId(String machineId) {
        this.machineId = machineId;
        this.encryptedMachineId = BikeUtil.encryptStr(machineId);
    }

    @Override
    public void onScanStart() {

    }

    @Override
    public void onScanStop() {
        bus.post(new BluEvent.ScanTimeOut());
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
            publishVersion(bytes);
            bluetoothIO.connect(bluetoothDevice, false);
        }
    }

    protected void publishVersion(byte[] bytes) {
        try {
            ParsedAd ad = ParsedAd.parseData(bytes);
            byte[] data = ad.getManufacturer();
            ManufacturerAd manufacturerAd = ManufacturerAd.resolveManufacturerAd(data);
            final int hard = manufacturerAd.getHardwareVersion();
            final int firm = manufacturerAd.getSoftwareVersion();
            EventBus.getDefault().post(new BluEvent.VersionResponse(hard, firm));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
