package com.tbit.tbitblesdk.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by Salmon on 2017/4/1 0001.
 */

public class FileUtil {

    public static byte[] file2byte(String filePath) throws IOException {
        File file = new File(filePath);
        return file2byte(file);
    }

    public static byte[] file2byte(File file) throws IOException {
        byte[] buffer = null;
        FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        int n;
        while ((n = fis.read(b)) != -1) {
            bos.write(b, 0, n);
        }
        fis.close();
        bos.close();
        buffer = bos.toByteArray();

        return buffer;
    }
}
