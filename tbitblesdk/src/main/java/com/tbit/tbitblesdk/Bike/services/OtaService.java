package com.tbit.tbitblesdk.Bike.services;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.tbit.tbitblesdk.bluetooth.BleClient;
import com.tbit.tbitblesdk.bluetooth.IBleClient;
import com.tbit.tbitblesdk.bluetooth.listener.ChangeCharacterListener;
import com.tbit.tbitblesdk.bluetooth.listener.WriteCharacterListener;
import com.tbit.tbitblesdk.bluetooth.listener.WriteDescriptorListener;
import com.tbit.tbitblesdk.Bike.BluEvent;
import com.tbit.tbitblesdk.Bike.OtaFile;
import com.tbit.tbitblesdk.Bike.ResultCode;
import com.tbit.tbitblesdk.protocol.callback.ProgressCallback;
import com.tbit.tbitblesdk.protocol.callback.ResultCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.math.BigInteger;
import java.util.UUID;

/**
 * Created by Salmon on 2017/1/9 0009.
 */

public class OtaService implements Handler.Callback,
        ChangeCharacterListener, WriteDescriptorListener, WriteCharacterListener {
    // ota相关
    public static final UUID SPOTA_SERVICE_UUID = UUID.fromString("0000fef5-0000-1000-8000-00805f9b34fb");
    public static final UUID SPOTA_MEM_DEV_UUID = UUID.fromString("8082caa8-41a6-4021-91c6-56f9b954cc34");
    public static final UUID SPOTA_GPIO_MAP_UUID = UUID.fromString("724249f0-5eC3-4b5f-8804-42345af08651");
    public static final UUID SPOTA_MEM_INFO_UUID = UUID.fromString("6c53db25-47a1-45fe-a022-7c92fb334fd4");
    public static final UUID SPOTA_PATCH_LEN_UUID = UUID.fromString("9d84b9a3-000c-49d8-9183-855b673fda31");
    public static final UUID SPOTA_PATCH_DATA_UUID = UUID.fromString("457871e8-d516-4ca1-9116-57d0b17b9cb2");
    public static final UUID SPOTA_SERV_STATUS_UUID = UUID.fromString("5f78df94-798c-46f5-990a-b3eb6a065c88");
    public static final UUID SPOTA_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public static final int END_SIGNAL = 0xfe000000;
    public static final int REBOOT_SIGNAL = 0xfd000000;

    private static final int HANDLE_UPDATE_DELAY = 1;
    private static final int UPDATE_DELAY_TIMEOUT = 3000;
    private static final String TAG = "OtaHelper";
    private static final int MAX_RETRY_COUNT = 3;
    boolean lastBlock = false;
    boolean lastBlockSent = false;
    boolean preparedForLastBlock = false;
    boolean endSignalSent = false;
    boolean rebootsignalSent = false;
    private IBleClient bleClient;
    private int retryCount = 0;
    private Step step = Step.MemDev;
    private OtaFile otaFile;
    private Handler handler;

    int chunkCounter = -1;
    int blockCounter = 0;

    private ResultCallback resultCallback;
    private ProgressCallback progressCallback;

    public OtaService(IBleClient bleClient, OtaFile file, ResultCallback resultCallback,
                      ProgressCallback progressCallback) {
        EventBus.getDefault().register(this);
        this.bleClient = bleClient;
        this.otaFile = file;
        this.handler = new Handler(Looper.getMainLooper(), this);
        this.resultCallback = resultCallback;
        this.progressCallback = progressCallback;
        this.bleClient.getListenerManager().addWriteCharacterListener(this);
        this.bleClient.getListenerManager().addChangeCharacterListener(this);
        this.bleClient.getListenerManager().addWriteDescriptorListener(this);
        setBlockSize();
    }

    private void setBlockSize() {
        this.otaFile.setFileBlockSize(240);
    }

    private void doOnWrite(UUID characterUuid, int status) {
        Log.d(TAG, "onCharacteristicWrite: " + characterUuid + " status: " + status);
        if (status == BluetoothGatt.GATT_SUCCESS) {
            retryCount = 0;
            if (SPOTA_GPIO_MAP_UUID.equals(characterUuid)) {
                step = Step.PatchLength;
                dispatch();
            }
            // Step 4 callback: set the patch length, default 240
            else if (SPOTA_PATCH_LEN_UUID.equals(characterUuid)) {
                step = Step.WriteData;
                dispatch();
            } else if (SPOTA_MEM_DEV_UUID.equals(characterUuid)) {

            } else if (SPOTA_PATCH_DATA_UUID.equals(characterUuid) && chunkCounter != -1) {
                sendBlock();
            }
        } else {
//            if (retryCount > MAX_RETRY_COUNT) {
//                notifyFailed();
//                return;
//            }
//            retryCount++;
            notifyFailed();
        }
    }

    private void doOnChanged(byte[] byteValue) {
        int value = new BigInteger(byteValue).intValue();
        String stringValue = String.format("%#10x", value);
        Log.d(TAG, "onCharacteristicChanged" + stringValue);

        int error = -1;
        int memDevValue = -1;
        // Set memtype callback
        if (stringValue.trim().equals("0x10")) {
            this.step = Step.GpioMap;
            dispatch();
        }
        // Successfully sent a block, send the next one
        else if (stringValue.trim().equals("0x2")) {
            this.step = Step.WriteData;
            dispatch();
            final float progress = ((float) (blockCounter + 1) / (float) otaFile.getNumberOfBlocks()) * 100;
            progressCallback.onProgress((int) progress);
        } else if (stringValue.trim().equals("0x3") || stringValue.trim().equals("0x1")) {
            memDevValue = value;
        } else {
            error = Integer.parseInt(stringValue.trim().replace("0x", ""));
        }
//        if (step >= 0 || error >= 0 || memDevValue >= 0) {
//            dispatch();
//        }
        if (error > 0) {
            int resolvedErrorCode;
            switch (error) {
                case 11:
                    resolvedErrorCode = ResultCode.OTA_FAILED_IMAGE_BANK;
                    break;
                case 12:
                    resolvedErrorCode = ResultCode.OTA_FAILED_IMAGE_HEADER;
                    break;
                case 13:
                    resolvedErrorCode = ResultCode.OTA_FAILED_IMAGE_SIZE;
                    break;
                case 14:
                    resolvedErrorCode = ResultCode.OTA_FAILED_PRODUCT_HEADER;
                    break;
                case 15:
                    resolvedErrorCode = ResultCode.OTA_FAILED_SAME_IMAGE;
                    break;
                case 16:
                    resolvedErrorCode = ResultCode.OTA_FAILED_TO_READ_FROM_EXTERNAL_MEM;
                    break;
                default:
                    resolvedErrorCode = error;
                    break;
            }
            notifyFailed(resolvedErrorCode);
        }
    }

    public void update() {
        handler.sendEmptyMessageDelayed(HANDLE_UPDATE_DELAY, UPDATE_DELAY_TIMEOUT);
    }

    private void doUpdate() {
        bleClient.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
        bleClient.setCharacteristicNotification(OtaService.SPOTA_SERVICE_UUID,
                OtaService.SPOTA_SERV_STATUS_UUID, SPOTA_DESCRIPTOR_UUID, true);
    }

    private void start() {
        reset();
        this.step = Step.MemDev;
        dispatch();
    }

    private void reset() {
        this.retryCount = 0;
        this.step = Step.MemDev;

        this.lastBlock = false;
        this.lastBlockSent = false;
        this.preparedForLastBlock = false;
        this.endSignalSent = false;
        this.blockCounter = 0;
        this.chunkCounter = -1;
    }

    private boolean setSuotaMemDev() {
        int memType = (0x13 << 24) | 0x00;
        Log.d(TAG, "setSpotaMemDev: " + String.format("%#10x", new Object[]{Integer.valueOf(memType)}));
        return bleClient.write(SPOTA_SERVICE_UUID, SPOTA_MEM_DEV_UUID, intToUINT32Byte(memType), true);
    }

    private boolean setSpotaGpioMap() {
        int MISO_GPIO = 0x05;
        int MOSI_GPIO = 0x06;
        int CS_GPIO = 0x03;
        int SCK_GPIO = 0x00;
        int memInfo = (MISO_GPIO << 24) | (MOSI_GPIO << 16) | (CS_GPIO << 8) | SCK_GPIO;
        Log.d(TAG, "setSpotaGpioMap: " + String.format("%#10x", new Object[]{Integer.valueOf(memInfo)}));
        return bleClient.write(SPOTA_SERVICE_UUID, SPOTA_GPIO_MAP_UUID, intToUINT32Byte(memInfo), true);
    }

    private boolean setPatchLength() {
        int blocksize = otaFile.getFileBlockSize();
//		int blocksizeLE = (blocksize & 0xFF) << 8 | ((blocksize & 0xFF00) >> 8);
        if (lastBlock) {
            blocksize = this.otaFile.getNumberOfBytes() % otaFile.getFileBlockSize();
            preparedForLastBlock = true;
        }
        Log.d(TAG, "setPatchLength: " + blocksize + " - " + String.format("%#4x", new Object[]{Integer.valueOf(blocksize)}));
        return bleClient.write(SPOTA_SERVICE_UUID, SPOTA_PATCH_LEN_UUID, intToUNINT16Byte(blocksize), true);
    }

    public float sendBlock() {
        //float progress = 0;
        final float progress = ((float) (blockCounter + 1) / (float) otaFile.getNumberOfBlocks()) * 100;
        if (!lastBlockSent) {
            //progress = ((float) (blockCounter + 1) / (float) file.getNumberOfBlocks()) * 100;
            //sendProgressUpdate((int) progress);
            Log.d(TAG, "Sending block " + (blockCounter + 1) + " of " + otaFile.getNumberOfBlocks());
            byte[][] block = otaFile.getBlock(blockCounter);

            //for (int i = 0; i < block.length; i++) {
            int i = ++chunkCounter;
            boolean lastChunk = false;
            if (chunkCounter == block.length - 1) {
                chunkCounter = -1;
                lastChunk = true;
            }
            byte[] chunk = block[i];

            int chunkNumber = (blockCounter * otaFile.getChunksPerBlockCount()) + i + 1;
            final String message = "Sending chunk " + chunkNumber + " of " + otaFile.getTotalChunkCount() + " (with " + chunk.length + " bytes)";
            //activity.log(message);
            String systemLogMessage = "Sending block " + (blockCounter + 1) + ", chunk " + (i + 1) + ", blocksize: " + block.length + ", chunksize " + chunk.length;
            Log.d(TAG, systemLogMessage);

            boolean r = bleClient.write(SPOTA_SERVICE_UUID, SPOTA_PATCH_DATA_UUID, chunk, false);
            Log.d(TAG, "writeCharacteristic: " + r);
            //}

            if (lastChunk) {

                // SUOTA
                if (!lastBlock) {
                    blockCounter++;
                } else {
                    lastBlockSent = true;
                }
                if (blockCounter + 1 == otaFile.getNumberOfBlocks()) {
                    lastBlock = true;
                }
            }
        }
        return progress;
    }

    private void sendEndSignal() {
        Log.d(TAG, "sendEndSignal");
        bleClient.write(SPOTA_SERVICE_UUID, SPOTA_MEM_DEV_UUID, intToUINT32Byte(END_SIGNAL), true);
        endSignalSent = true;
    }

    private void sendRebootSignal() {
        Log.d(TAG, "sendRebootSignal");
        bleClient.write(SPOTA_SERVICE_UUID, SPOTA_MEM_DEV_UUID, intToUINT32Byte(REBOOT_SIGNAL), true);
        rebootsignalSent = true;
    }

    private void writeData() {
        if (!lastBlock) {
            sendBlock();
        } else {
            if (!preparedForLastBlock) {
                setPatchLength();
            } else if (!lastBlockSent) {
                sendBlock();
            } else if (!endSignalSent) {
                sendEndSignal();
            } else {
                notifySucceed();
            }
        }
    }

    private void notifyFailed(int errCode) {
        resultCallback.onResult(errCode);
    }

    private void notifyFailed() {
        notifyFailed(ResultCode.OTA_WRITE_FAILED);
    }

    private void notifySucceed() {
//        bluetoothIO.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_BALANCED);
        sendRebootSignal();
        resultCallback.onResult(ResultCode.SUCCEED);
    }

    private void dispatch() {
        Log.d(TAG, "dispatching: " + step);
        boolean result = true;
        switch (step) {
            case MemDev:
                setSuotaMemDev();
                break;
            case GpioMap:
                setSpotaGpioMap();
                break;
            case PatchLength:
                setPatchLength();
                break;
            case WriteData:
                writeData();
                result = true;
                break;
        }
        if (result) {
            retryCount = 0;
        } else {
            if (retryCount < MAX_RETRY_COUNT) {
                Log.d(TAG, "dispatch: " + step + " failed");
                retryCount++;
                OtaService.this.dispatch();
            } else {
                notifyFailed();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBleNotOpened(BluEvent.OtaStart otaStart) {
        update();
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case HANDLE_UPDATE_DELAY:
                doUpdate();
                break;
        }
        return true;
    }

    @Override
    public void onCharacterChange(BluetoothGattCharacteristic characteristic, byte[] value) {
        if (!SPOTA_SERVICE_UUID.equals(characteristic.getService().getUuid()))
            return;
        doOnChanged(value);
    }

    @Override
    public void onDescriptorWrite(BluetoothGattDescriptor descriptor, int status) {
        Log.d(TAG, "onDescriptorWrite" + status + ": "
                + descriptor.getCharacteristic().getUuid() + " || " + status);
        if (OtaService.SPOTA_SERV_STATUS_UUID.equals(descriptor.getCharacteristic().getUuid())) {
            start();
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGattCharacteristic characteristic, int status, byte[] value) {
        if (!SPOTA_SERVICE_UUID.equals(characteristic.getService().getUuid()))
            return;

        doOnWrite(characteristic.getUuid(), status);
    }

    public enum Step {
        MemDev, GpioMap, PatchLength, WriteData
    }

    private byte[] intToUINT32Byte(int value) {
        byte[] result = new byte[4];
        result[0] = (byte)(value & 0xFF);
        result[1] = (byte)((value >> 8) & 0xFF);
        result[2] = (byte)((value >> 16) & 0xFF);
        result[3] = (byte)((value >> 24) & 0xFF);
        return result;
    }

    private byte[] intToUNINT16Byte(int value) {
        byte[] result = new byte[2];
        result[0] = (byte)(value & 0xFF);
        result[1] = (byte)((value >> 8) & 0xFF);
        return result;
    }

    public void destroy() {
        bleClient.getListenerManager().removeChangeCharacterListener(this);
        bleClient.getListenerManager().removeWriteCharacterListener(this);
        bleClient.getListenerManager().removeWriteDescriptorListener(this);
        handler.removeCallbacksAndMessages(null);
        if (otaFile != null)
            otaFile.close();
        EventBus.getDefault().unregister(this);
    }
}
