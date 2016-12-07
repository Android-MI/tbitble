package com.tbit.tbitblesdk;

import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import com.tbit.tbitblesdk.listener.Writer;
import com.tbit.tbitblesdk.protocol.Packet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Salmon on 2016/12/7 0007.
 */

public class WriteTask extends AsyncTask<Void, byte[], Void> {
    private static final String TAG = "WriteTask";
    private static final int packLength = 20;//每个数据包的长度最长为20字节
    private Packet currentData;
    private List<Packet> dataQueue = new ArrayList<>();
    private boolean SEND_OVER = true;//发送完全标志位，存在拆包问题
    private Writer writer;

    public WriteTask(Writer writer) {
        this.writer = writer;
    }

    public void addData(Packet data) {
        dataQueue.add(data);
    }

    public void updateStatus(boolean status) {
        SEND_OVER = status;
    }

    @Override
    protected Void doInBackground(Void... voids) {

        while (!isCancelled()) {
            SystemClock.sleep(500l);
            if (dataQueue.size() == 0)
                continue;
            currentData = dataQueue.remove(0);
            processData();
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(byte[]... values) {
        writer.write(values[0]);
    }

    private void processData() {
        if (currentData == null)
            return;
        byte[] realData = currentData.toByteArray();
        int lastLength = realData.length;//数据的总长度
        Log.d(TAG, "--send_data_total_size: " + lastLength);
        byte[] sendData;
        int sendIndex = 0;
        SystemClock.sleep(100l);
        while (lastLength > 0) {
            //此包的长度小于20字节，不用拆包，直接发送
            if (lastLength <= packLength) {
                Log.i(TAG, "--不用拆包" + lastLength);
                sendData = Arrays.copyOfRange(realData, sendIndex, sendIndex + lastLength);
                sendIndex += lastLength;
                lastLength = 0;
//                    SEND_OVER = true;
            } else {
                //拆包发送
                sendData = Arrays.copyOfRange(realData, sendIndex, sendIndex + packLength);
                sendIndex += packLength;
                lastLength -= packLength;
                Log.i(TAG, "--拆包" + lastLength);
            }

            int count = 0;
            do {
                count++;
                SystemClock.sleep(500l);
                Log.i(TAG, "--do while 循环还在");
            } while (!SEND_OVER && count < 5);
            if (count >= 5) {
                break;
            }
            //向蓝牙终端发送数据
            StringBuilder builder = new StringBuilder();
            for (byte b : sendData) {
                builder.append(String.format("%02X ", b));
            }
            Log.i(TAG, "--sendData= " + builder.toString());
            final byte[] resultData = sendData;
            publishProgress(resultData);
            SEND_OVER = false;
        }
    }
}
