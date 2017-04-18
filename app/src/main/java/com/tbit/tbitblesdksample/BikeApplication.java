package com.tbit.tbitblesdksample;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * Created by Salmon on 2017/4/17 0017.
 */

public class BikeApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        LeakCanary.install(this);
    }
}
