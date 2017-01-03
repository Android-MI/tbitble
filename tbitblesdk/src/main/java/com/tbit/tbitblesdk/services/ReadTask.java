package com.tbit.tbitblesdk.services;

import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import com.tbit.tbitblesdk.listener.Reader;
import com.tbit.tbitblesdk.util.ByteUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Salmon on 2016/12/15 0015.
 */

public class ReadTask extends AsyncTask<Void, byte[], Void> {
    private static final String TAG = "ReadTask";
    private static final Byte HEAD_FLAG = new Byte((byte) 0xAA);
    private Reader reader;
    private List<Byte> readTemp = Collections.synchronizedList(new LinkedList<Byte>());

    public ReadTask(Reader reader) {
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
                if (!readTemp.get(0).equals(HEAD_FLAG)) {
                    readTemp.remove(0);
                    continue;
                }
                if (readTemp.size() < 8) {
                    continue;
                }
                //处理
                try {
                    process();
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(byte[]... values) {
        super.onProgressUpdate(values);
        reader.read(values[0]);
    }

    private void process() {
        // 数据包长度
        int dataPacketLen = readTemp.get(5) & 0xFF;
        // 等待数据包长度足够
        for (int i = 0; i < 30; i++) {
            if (readTemp.size() - 8 >= dataPacketLen) {
                break;
            }
            SystemClock.sleep(100L);
        }
        // 数据包长度不足
        if (readTemp.size() - 8 < dataPacketLen) {
            for (int i = 0; i < 8; i++) {
                readTemp.remove(0);
            }
            return;
        }
        // 数据包长度足够，取出数据包
        int totalLength = 8 + dataPacketLen;
        byte[] receiveData = new byte[totalLength];
        for (int i = 0; i < totalLength; i++) {
            receiveData[i] = readTemp.remove(0);
        }
        // 发布数据
        publishProgress(receiveData);
    }

    private void print() {
        int length = readTemp.size();
        byte[] array = new byte[length];
        for (int i = 0; i < length; i++) {
            array[i] = readTemp.get(i);
        }
        Log.i(TAG, "--receiveData= " + ByteUtil.bytesToHexString(array));
    }
}
