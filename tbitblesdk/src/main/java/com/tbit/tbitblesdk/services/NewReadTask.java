package com.tbit.tbitblesdk.services;

import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import com.tbit.tbitblesdk.listener.Reader;
import com.tbit.tbitblesdk.util.ByteUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Salmon on 2016/12/15 0015.
 */

public class NewReadTask extends AsyncTask<Void, byte[], Void> {
    private static final String TAG = "NewReadTask";
    private Reader reader;
    private List<Byte> readTemp = Collections.synchronizedList(new ArrayList<Byte>());

    public NewReadTask(Reader reader) {
        this.reader = reader;
    }

    public void setData(byte[] data) {
        Byte[] temp = new Byte[data.length];
        int length = temp.length;
        for (int i = 0; i < length; i++) {
            temp[i] = data[i];
        }
        readTemp.addAll(Arrays.asList(temp));
    }

    @Override
    protected Void doInBackground(Void... voids) {
        while (!isCancelled()) {
            if (readTemp.size() != 0) {
                // 过滤
                if (readTemp.get(0).byteValue() == 0xAA) {
                    readTemp.remove(0);
                    continue;
                }
                if (readTemp.size() < 8)
                    continue;

                //处理
                try {
                    process();
                } catch (IndexOutOfBoundsException e) {
                    readTemp.clear();
                    e.printStackTrace();
                }
            }
            SystemClock.sleep(100L);
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(byte[]... values) {
        super.onProgressUpdate(values);
        reader.read(values[0]);
    }

    private void process() {
        Log.d(TAG, "process: start process");
        // 截取头部
        List<Byte> head = readTemp.subList(0, 8);
        // 数据包长度
        int dataPacketLen = head.get(5) & 0xFF;
        // 等待数据包长度足够
        for (int i = 0; i < 30; i++) {
            if (readTemp.size() - 8 >= dataPacketLen) {
                break;
            }
            SystemClock.sleep(100L);
        }
        // 数据包长度不足
        if (readTemp.size() -8 < dataPacketLen) {
            Log.d(TAG, "process: data package length NOT enough");
            for (int i = 0; i < dataPacketLen; i++) {
                readTemp.remove(0);
            }
            return;
        }
        Log.d(TAG, "process: data package length enough");
        // 数据包长度足够，取出数据包
        int totalLength = 8 + dataPacketLen;
        List<Byte> currentDataList = readTemp.subList(0, totalLength);
        byte[] receiveData = new byte[totalLength];
        for (int i = 0; i < totalLength; i++) {
            receiveData[i] = currentDataList.get(i);
        }
        // 发布数据
        publishProgress(receiveData);
        // 清除已经发布的数据
        for (int i = 0; i < totalLength; i++) {
            readTemp.remove(0);
        }
    }

    private void print(List<Byte> data) {
        byte[] array = new byte[data.size()];
        int length = data.size();
        for (int i = 0; i < length; i++) {
            array[i] = data.get(i);
        }
        Log.i(TAG, "--receiveData= " + ByteUtil.bytesToHexString(array));
    }
}
