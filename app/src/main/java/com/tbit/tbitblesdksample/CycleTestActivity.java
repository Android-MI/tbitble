package com.tbit.tbitblesdksample;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.TextUtils;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tbit.tbitblesdk.Bike.TbitBle;
import com.tbit.tbitblesdk.Bike.TbitDebugListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class CycleTestActivity extends AppCompatActivity implements OperationDispatcher.OperationDispatcherListener {

    @Bind(R.id.check_connect)
    AppCompatCheckBox checkConnect;
    @Bind(R.id.check_unlock)
    AppCompatCheckBox checkUnlock;
    @Bind(R.id.check_lock)
    AppCompatCheckBox checkLock;
    @Bind(R.id.check_disconnect)
    AppCompatCheckBox checkDisconnect;
    @Bind(R.id.edit_duration)
    AutoCompleteTextView editDuration;
    @Bind(R.id.edit_cycle)
    AutoCompleteTextView editCycle;
    @Bind(R.id.edit_fail)
    AutoCompleteTextView editFail;
    @Bind(R.id.text_desc)
    TextView textDesc;
    @Bind(R.id.edit_machineNo)
    AutoCompleteTextView editMachineNo;
    @Bind(R.id.button_start)
    Button buttonStart;

    private String deviceId;
    private String key;
    private int duration;
    private int cycle;
    private int maxFail;
    private List<List<Integer>> operations;

    private int failCount;
    private int succeedCount;

    private OperationDispatcher dispatcher;

    private DateFormat format = new SimpleDateFormat("HH:mm:ss");
    private StringBuffer stringBuffer = new StringBuffer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cycle_test);
        ButterKnife.bind(this);

        TbitBle.setDebugListener(new TbitDebugListener() {
            @Override
            public void onLogStrReceived(String logStr) {
                stringBuffer.insert(0, "\n\n")
                        .insert(0, logStr)
                        .insert(0, "\n")
                        .insert(0, getTime());
            }
        });
    }

    private String getTime() {
        return format.format(new Date());
    }

    @OnClick(R.id.button_start)
    public void onClick() {
        deviceId = editMachineNo.getText().toString();
        if (TextUtils.isEmpty(deviceId))
            return;
        try {
            duration = Integer.valueOf(editDuration.getText().toString());
            cycle = Integer.valueOf(editCycle.getText().toString());
            maxFail = Integer.valueOf(editFail.getText().toString());
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return;
        }

        if (duration == 0 || cycle == 0 || maxFail == 0)
            return;

        key = "";

        prepareOperations();

        prepareDispatcher();

        prepareLocalParam();

        dispatcher.start();

        buttonStart.setEnabled(false);
    }

    private void prepareLocalParam() {
        failCount = 0;

        succeedCount = 0;
    }

    private void prepareOperations() {
        List<Integer> singleCycle = new ArrayList<>();
        if (checkConnect.isChecked())
            singleCycle.add(Operation.OPERATION_CONNECT);
        if (checkUnlock.isChecked())
            singleCycle.add(Operation.OPERATION_UNLOCK);
        if (checkLock.isChecked())
            singleCycle.add(Operation.OPERATION_LOCK);
        if (checkDisconnect.isChecked())
            singleCycle.add(Operation.OPERATION_DISCONNECT);

        operations = new ArrayList<>();

        for (int i = 0; i < cycle; i++) {
            List<Integer> single = new ArrayList<>();
            single.addAll(singleCycle);
            operations.add(single);
        }
    }

    private void prepareDispatcher() {
        dispatcher = new OperationDispatcher();

        dispatcher.setKey(key);
        dispatcher.setDuration(duration);
        dispatcher.setDeviceId(deviceId);
        dispatcher.setListener(this);
        dispatcher.setOperations(operations);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TbitBle.disConnect();
        TbitBle.cancelAllCommand();
        TbitBle.setDebugListener(null);
    }

    @Override
    public boolean onFailed() {
        new SaveLogTask(stringBuffer.toString()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        failCount++;
        updateDesc();
        if (failCount >= maxFail) {
            Toast.makeText(this, "测试失败", Toast.LENGTH_LONG).show();
            dispatcher.stop();
            buttonStart.setEnabled(true);
            return false;
        }
        return true;
    }

    @Override
    public void onSucceed() {
        succeedCount++;
        updateDesc();
    }

    private void updateDesc() {
        textDesc.setText("成功次数: " + succeedCount + " | 失败次数: " + failCount);
    }

    @Override
    public void onComplete() {
        dispatcher.stop();
        buttonStart.setEnabled(true);
    }
}
