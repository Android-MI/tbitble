package com.tbit.tbitblesdk.services.scanner;

import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;

import com.tbit.tbitblesdk.protocol.BluEvent;
import com.tbit.tbitblesdk.protocol.ParsedAd;

import org.greenrobot.eventbus.EventBus;


/**
 * Created by Salmon on 2017/3/3 0003.
 */

public class BikeScanner implements Scanner {
    private static final int MAX_ENCRYPT_COUNT = 95;
    private static char[] szKey = {
            0x35, 0x41, 0x32, 0x42, 0x33, 0x43, 0x36, 0x44, 0x39, 0x45,
            0x38, 0x46, 0x37, 0x34, 0x31, 0x30};
    private Scanner scanner;
    private String machineId;

    public BikeScanner(String machineId, Scanner scanner) {
        this.machineId = machineId;
        this.scanner = scanner;
    }

    public BikeScanner(Scanner scanner) {
        this.scanner = scanner;
    }

    public void setMachineId(String machineId) {
        this.machineId = machineId;
    }

    @Override
    public void start(final ScannerCallback callback) {
        if (TextUtils.isEmpty(machineId))
            throw new IllegalArgumentException("machineId cannot be null");
        final CharSequence encryptedTid = encryptStr(machineId);
        scanner.start(new ScannerCallback() {
            @Override
            public void onScanStart() {
                callback.onScanStart();
            }

            @Override
            public void onScanStop() {
                callback.onScanStop();
            }

            @Override
            public void onScanCanceled() {
                callback.onScanCanceled();
            }

            @Override
            public void onDeviceFounded(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
                String dataStr = bytesToHexString(bytes);
                boolean isFound = encryptedTid != null && dataStr.contains(encryptedTid);
                if (isFound) {
                    stop();
                    if (callback != null) {
                        publishVersion(bytes);
                        callback.onDeviceFounded(bluetoothDevice, i, bytes);
                    }
                }
            }
        });
    }

    @Override
    public void setTimeout(long timeout) {
        scanner.setTimeout(timeout);
    }

    @Override
    public boolean isScanning() {
        return scanner.isScanning();
    }

    private String bytesToHexString(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

    protected String encryptStr(String in_str) {
        int count = 0;

        StringBuilder builder = new StringBuilder();
        if (in_str == null || in_str.length() == 0) {
            return null;
        }

        count = in_str.length();
        if (count > MAX_ENCRYPT_COUNT) {
            return null;
        }

        for (int i = 0; i < count; i++) {
            builder.append(szKey[in_str.charAt(i) - 0x2A]);
        }
        return builder.toString();
    }

    protected void publishVersion(byte[] bytes) {
        try {
            ParsedAd ad = ParsedAd.parseData(bytes);
            final byte[] manuData = ad.getManufacturer();
            final int hard = manuData[9];
            final int firm = manuData[10];
            EventBus.getDefault().post(new BluEvent.VersionResponse(hard, firm));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        scanner.stop();
    }
}
