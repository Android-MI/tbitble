package com.tbit.tbitblesdk.Bike.services.executable;

import android.bluetooth.BluetoothDevice;

import com.tbit.tbitblesdk.Bike.BluEvent;
import com.tbit.tbitblesdk.bluetooth.BleClient;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by Salmon on 2017/3/22 0022.
 */

public class ConnectBikeExecutor extends Executor {
    private static final String TAG = "ConnectBikeExecutor";
    private BleClient bleClient;
    private BluetoothDevice bluetoothDevice;

    public ConnectBikeExecutor(BleClient bleClient, BluetoothDevice bluetoothDevice) {
        this.bleClient = bleClient;
    }

    @Override
    protected void onExecute() {
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
        this.bleClient.connect(this.bluetoothDevice, false);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDiscovered(BluEvent.DiscoveredSucceed event) {

    }

    @Override
    public void finish() {
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        super.finish();
    }
}
