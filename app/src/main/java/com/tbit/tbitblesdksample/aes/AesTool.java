package com.tbit.tbitblesdksample.aes;

import com.tbit.tbitblesdk.util.ByteUtil;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Salmon on 2016/12/26 0026.
 */

public class AesTool {
    public static String Genkey(String tid) {
        JavaAesContent aescxt = new JavaAesContent();
        byte[] key = null;
        byte[] data = new byte[32];
        try {
            byte[] temp = tid.getBytes("ascii");
            for(int i = 0; i < temp.length; i++)
                data[i] = temp[i];
            key = "TBIT_WA205-XQBLE".getBytes("ascii");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        int[] keyi = new int[key.length];
        for(int i = 0; i < keyi.length;i++)
        {
            keyi[i] = key[i] >= 0 ? key[i] : 256 + key[i];
        }
        System.out.println();
        int[] datai = new int[data.length];
        for(int i = 0; i < datai.length;i++)
        {
            datai[i] = data[i] >= 0 ? data[i] : 256 + data[i];
        }
        System.out.println();
        aescxt.setKey(keyi, keyi.length);
        int[] out = new int[16];
        JavaAes.AesDecrypt(datai, out, aescxt);
        List<Byte> byteList = new ArrayList<>();
        for(int i = 0; i < out.length; i++)
            byteList.add((byte) out[i]);
        datai = new int[32];
        JavaAes.AesDecrypt(datai, out, aescxt);
        for(int i = 0; i < out.length; i++)
            byteList.add((byte) out[i]);

        int length = byteList.size();
        byte[] bytes = new byte[length];
        for (int j = 0; j < length; j++) {
            bytes[j] = byteList.get(j);
        }

        String gened = ByteUtil.bytesToHexString(bytes);
        System.out.println(gened);
        return gened;
    }
}
