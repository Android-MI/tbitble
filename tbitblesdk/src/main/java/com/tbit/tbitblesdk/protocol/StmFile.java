package com.tbit.tbitblesdk.protocol;

import com.tbit.tbitblesdk.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by Salmon on 2017/4/1 0001.
 */

public class StmFile {
    private static final int BLOCK_SIZE = 256;
    private static final int MAX_FILE_SIZE = 51200;

    private String path;
    private byte[][] data;

    private StmFile() {
    }

    public static StmFile resolveStmFile(String path) throws StmFileException {
        StmFile stmFile = null;
        try {
            File file = new File(path);
            if (!file.exists())
                throw new StmFileException("file not exists");

            if (file.length() == 0 || file.length() > MAX_FILE_SIZE)
                throw new StmFileException("file size cannot be 0 or bigger than 50kb : " +
                        file.length());

            int fileLength = (int) file.length();
//            if (Looper.myLooper() == Looper.getMainLooper())
//                throw new StmFileException("cannot run on main thread");

            byte[] originData = FileUtil.file2byte(file);
            stmFile = new StmFile();
            stmFile.setPath(path);

            int blockCount = (int) Math.ceil((double) fileLength / (double) BLOCK_SIZE);
            byte[][] data = new byte[blockCount][];

            for (int i = 0, start = 0; start < fileLength; i++) {
                int end = start + ((fileLength - start) >= BLOCK_SIZE ? BLOCK_SIZE :
                        (fileLength - start));
                data[i] = Arrays.copyOfRange(originData, start, end);
                start = end;
            }

            stmFile.setData(data);
        } catch (IOException e) {
            throw new StmFileException(e.getMessage());
        }

        return stmFile;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public byte[][] getData() {
        return data;
    }

    public void setData(byte[][] data) {
        this.data = data;
    }
}
