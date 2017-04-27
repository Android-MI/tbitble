package com.tbit.tbitblesdksample;

import android.os.AsyncTask;
import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by Salmon on 2017/4/19 0019.
 */

public class SaveLogTask extends AsyncTask<Void, Void, Void> {
    private String logStr;
    private DateFormat format = new SimpleDateFormat("MMdd_HH-mm-ss");

    private String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/TbitBle/";
    public SaveLogTask(String logStr) {
        this.logStr = logStr;
    }

    @Override
    protected Void doInBackground(Void... params) {
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        filePath = filePath + "/" + getTime() + ".txt";
        file = new File(filePath);
        FileWriter output = null;
        BufferedWriter bf = null;
        try {
            file.createNewFile();
            output = new FileWriter(file);
            bf = new BufferedWriter(output);
            bf.write(logStr);
            bf.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bf != null) {
                try {
                    bf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    private String getTime() {
        return format.format(new Date());
    }
}
