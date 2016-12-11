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

/**
 * Created by Salmon on 2016/12/7 0007.
 */

public class WriteTask extends AsyncTask<Void, byte[], Void> {
    private static final String TAG = "WriteTask";
    private static final int MAX_PACKAGE_LENGTH = 20;//每个数据包的长度最长为20字节
    private Packet currentData;
    private List<Packet> dataQueue = Collections.synchronizedList(new ArrayList<Packet>());
    private boolean isWriteProceed = true;//发送完全标志位，存在拆包问题
    private Writer writer;
    private int currentSequenceId = -1;

    public WriteTask(Writer writer) {
        this.writer = writer;
    }

    public void addData(Packet data) {
        dataQueue.add(data);
    }

    public void setWriteStatus(boolean status) {
        isWriteProceed = status;
    }

    public void setAck(int sequenceId) {
        currentSequenceId = sequenceId;
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
        if (values != null && values[0] != null)
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
                SystemClock.sleep(100);
            }
        }
        if (!isAcked()) {
            publishProgress(new byte[]{});
        }
        currentSequenceId = -1;
    }

    private void doWrite() {
        byte[] realData = currentData.toByteArray();
        int lastLength = realData.length;//数据的总长度
        Log.d(TAG, "--send_data_total_size: " + lastLength);
        byte[] sendData;
        int sendIndex = 0;
        SystemClock.sleep(100l);
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
                SystemClock.sleep(500l);
            } while (!isWriteProceed && count < 5);
            if (count >= 5) {
                break;
            }

            //向蓝牙终端发送数据
            Log.i(TAG, "--sendData= " + ByteUtil.bytesToHexString(sendData));
            publishProgress(sendData);
            isWriteProceed = false;
        }
    }

    private boolean isAcked() {
        return currentData.getL1Header().getSequenceId() == currentSequenceId;
    }
}
