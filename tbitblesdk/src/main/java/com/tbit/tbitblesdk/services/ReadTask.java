package com.tbit.tbitblesdk.services;

import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import com.tbit.tbitblesdk.listener.Reader;
import com.tbit.tbitblesdk.util.ByteUtil;

/**
 * Created by Salmon on 2016/12/7 0007.
 */

public class ReadTask extends AsyncTask<Void, byte[], Void> {
    private static final String TAG = "ReadTask";
    private byte[] readTemp;
    private boolean wait = true;
    private boolean wait2 = true;
    private byte[] head = new byte[8];
    private Reader reader;

    public ReadTask(Reader reader) {
        this.reader = reader;
    }

    public void setData(byte[] data) {
        this.readTemp = ByteUtil.byteMerger(readTemp, data);//拼接缓存
    }

    @Override
    protected Void doInBackground(Void... voids) {
        while (!isCancelled()) {
            try {
                if (readTemp != null && readTemp.length != 0) {
                    print(readTemp);
                    process();
                }
                SystemClock.sleep(300L);
            } catch (Exception e) {
                readTemp = null;
                e.printStackTrace();
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
        for (int i = 0; i < readTemp.length; i++) {
            if (readTemp[i] == (byte) 0xAA) {
                Log.i(TAG, "-- HEAD");
                if (readTemp.length - i >= 8) {
                    Log.i(TAG, "-- Head length legal");
                    //可以拼接头
                    System.arraycopy(readTemp, i, head, 0, 8);//把数据复制到head
                    int len = head[5] & 0xFF;  //4 5角标为数据长度  这里存在小问题，后面研究
                    if (len <= readTemp.length - 8) {
                        //后面接着的数据达到len的长度，直接取出来
                        byte[] receiveData = ByteUtil.subBytes(readTemp, i, i + 8 + len);//将完整的数据包截取出来
                        publishProgress(receiveData);
                        Log.i(TAG, "--readTemp length" + readTemp.length);
                        readTemp = ByteUtil.subBytes(readTemp, i + 8 + len, readTemp.length - (i + 8 + len));//清除已经发送的部分
                        break;
                    } else {
                        //后面缓存的数据不够len的长度，等待
                        Log.d(TAG, "--readTemp 等待数据包");
                        if (!wait) {
                            //不等待了，把前面的头和数据丢掉
                            readTemp = null;
                            wait = true;
                        }
                        SystemClock.sleep(3000);
                        wait = false;
                    }

                } else {
                    Log.d(TAG, "--readTemp 等待数据包");
                    //头不够长，等待头
                    if (!wait2) {
                        //不等待了，把前面的头
                        readTemp = null;
                        wait2 = true;
                    }
                    SystemClock.sleep(3000);
                    wait2 = false;
                }
            } else {
                readTemp = null;
            }
        }
    }

    private void print(byte[] data) {
        Log.i(TAG, "--receiveData= " + ByteUtil.bytesToHexString(data));
    }
}
