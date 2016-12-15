package com.tbit.tbitblesdk.services;

import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import com.tbit.tbitblesdk.listener.Writer;
import com.tbit.tbitblesdk.protocol.Packet;
import com.tbit.tbitblesdk.util.ByteUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Salmon on 2016/12/7 0007.
 */

public class WriteTask extends AsyncTask<Void, byte[], Void> {
    private static final String TAG = "WriteTask";
    private static final int MAX_PACKAGE_LENGTH = 20;//每个数据包的长度最长为20字节
    private Packet currentData;
    private List<Packet> dataQueue = Collections.synchronizedList(new ArrayList<Packet>());
    private AtomicBoolean isWriteProceed = new AtomicBoolean(true);//发送完全标志位，存在拆包问题
    private AtomicInteger currentSequenceId = new AtomicInteger(-1);
    private Writer writer;

    public WriteTask(Writer writer) {
        this.writer = writer;
    }

    public void addData(Packet data) {
        dataQueue.add(data);
    }

    public void setWriteStatus(boolean status) {
        isWriteProceed.set(status);
    }

    public void setAck(int sequenceId) {
        currentSequenceId.set(sequenceId);
    }

    @Override
    protected Void doInBackground(Void... voids) {

        while (!isCancelled()) {
            if (dataQueue.size() == 0)
                continue;
            currentData = dataQueue.remove(0);
            process();
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(byte[]... values) {
        if (values != null && values[0] != null && values[0].length != 0)
            writer.write(values[0]);
        else
            writer.onWriteAckTimeout(currentData.getL1Header().getSequenceId());
    }

    private void process() {
        if (currentData == null)
            return;
        Log.d(TAG, "process: now processing " +
                ByteUtil.bytesToHexString(currentData.toByteArray()));
        for (int i = 0; i < 3; i++) {
            if (isAcked())
                break;
            doWrite();
            for (int j = 0; j < 30; j++) {
                if (isAcked())
                    break;
                SystemClock.sleep(100L);
            }
        }
        if (!isAcked()) {
            publishProgress(new byte[]{});
        }
        currentSequenceId.set(-1);
    }

    private void doWrite() {
        isWriteProceed.set(true);
        byte[] realData = currentData.toByteArray();
        int lastLength = realData.length;//数据的总长度
        Log.d(TAG, "--send_data_total_size: " + lastLength);
        byte[] sendData;
        int sendIndex = 0;
        SystemClock.sleep(100L);
        while (lastLength > 0) {
            //此包的长度小于20字节，不用拆包，直接发送
            if (lastLength <= MAX_PACKAGE_LENGTH) {
                Log.i(TAG, "--不用拆包" + lastLength);
                sendData = Arrays.copyOfRange(realData, sendIndex, sendIndex + lastLength);
                sendIndex += lastLength;
                lastLength = 0;
//                    SEND_OVER = true;
            } else {
                //拆包发送
                sendData = Arrays.copyOfRange(realData, sendIndex, sendIndex + MAX_PACKAGE_LENGTH);
                sendIndex += MAX_PACKAGE_LENGTH;
                lastLength -= MAX_PACKAGE_LENGTH;
                Log.i(TAG, "--拆包" + lastLength);
            }

            int count = 0;
            do {
                count++;
                SystemClock.sleep(100L);
            } while (!isWriteProceed.get() && count < 10);
            if (count >= 10) {
                break;
            }

            //向蓝牙终端发送数据
            Log.i(TAG, "--sendData= " + ByteUtil.bytesToHexString(sendData));
            isWriteProceed.set(false);
            publishProgress(sendData);
        }
    }

    private boolean isAcked() {
        return currentData.getL1Header().getSequenceId() == currentSequenceId.get();
    }
}
