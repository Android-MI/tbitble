package com.tbit.tbitblesdk.Bike.tasks;


import com.tbit.tbitblesdk.Bike.ResultCode;
import com.tbit.tbitblesdk.Bike.services.BikeService;
import com.tbit.tbitblesdk.Bike.services.command.Command;
import com.tbit.tbitblesdk.Bike.services.config.BikeConfig;
import com.tbit.tbitblesdk.Bike.tasks.exceptions.ResultCodeThrowable;
import com.tbit.tbitblesdk.bluetooth.RequestDispatcher;
import com.tbit.tbitblesdk.bluetooth.debug.BleLog;
import com.tbit.tbitblesdk.bluetooth.model.SearchResult;
import com.tbit.tbitblesdk.bluetooth.scanner.Scanner;
import com.tbit.tbitblesdk.protocol.callback.ResultCallback;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * Created by Salmon on 2017/4/11 0011.
 */

public class BikeConnectHelper {
    private static final String TAG = "BikeConnectHelper";

    private static final int STATE_CONNECT = 0;
    private static final int STATE_DISCONNECT = 1;

    private int state;
    private BikeService bikeService;
    private Scanner scanner;
    private RequestDispatcher requestDispatcher;
    private CompositeDisposable compositeDisposable;

    public BikeConnectHelper(BikeService bikeService, Scanner scanner, RequestDispatcher requestDispatcher) {
        this.bikeService = bikeService;
        this.scanner = scanner;
        this.requestDispatcher = requestDispatcher;
        this.compositeDisposable = new CompositeDisposable();
    }

    public void connect(String deviceId, final ResultCallback resultCallback, final Command command) {
        state = STATE_CONNECT;
        // 搜索设备
        Observable.create(new SearchObservable(deviceId, scanner))
                // 连接设备
                .flatMap(new Function<SearchResult, ObservableSource<SearchResult>>() {
                    @Override
                    public ObservableSource<SearchResult> apply(@NonNull SearchResult searchResult) throws Exception {
                        return Observable.create(new ConnectObservable(requestDispatcher, searchResult));
                    }
                })
                // 解析设备广播数据包，获得设备配置信息
                .flatMap(new Function<SearchResult, ObservableSource<BikeConfig>>() {
                    @Override
                    public ObservableSource<BikeConfig> apply(@NonNull SearchResult searchResult) throws Exception {
                        return Observable.create(new ResolveAdObservable(searchResult.getBroadcastData()));
                    }
                })
                // setNotification
                .flatMap(new Function<BikeConfig, ObservableSource<BikeConfig>>() {
                    @Override
                    public ObservableSource<BikeConfig> apply(@NonNull BikeConfig bikeConfig) throws Exception {
                        return Observable.create(new SetNotificationObservable(requestDispatcher, bikeConfig));
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                // 初始化 BikeService 或 通知错误
                .subscribe(new Consumer<BikeConfig>() {
                    @Override
                    public void accept(@NonNull BikeConfig bikeConfig) throws Exception {
                        if (state == STATE_DISCONNECT)
                            return;
                        bikeService.setBikeConfig(bikeConfig);
                        bikeService.addCommand(command);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        if (state == STATE_DISCONNECT)
                            return;
                        if (throwable instanceof ResultCodeThrowable) {
                            resultCallback.onResult(((ResultCodeThrowable) throwable).getResultCode());
                        } else {
                            throwable.printStackTrace();
                            resultCallback.onResult(ResultCode.FAILED);
                        }
                        BleLog.log("BikeConnectHelper", throwable.getMessage());
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {

                    }
                }, new Consumer<Disposable>() {
                    @Override
                    public void accept(@NonNull Disposable disposable) throws Exception {
                        compositeDisposable.add(disposable);
                    }
                });

    }

    public void disConnect() {
        if (scanner.isScanning())
            scanner.stop();
        state = STATE_DISCONNECT;
    }

    public void destroy() {
        this.compositeDisposable.dispose();
    }

}
