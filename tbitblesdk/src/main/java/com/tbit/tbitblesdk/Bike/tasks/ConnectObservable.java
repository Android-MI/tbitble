package com.tbit.tbitblesdk.Bike.tasks;

import com.tbit.tbitblesdk.Bike.ResultCode;
import com.tbit.tbitblesdk.Bike.tasks.exceptions.ResultCodeThrowable;
import com.tbit.tbitblesdk.Bike.util.BikeUtil;
import com.tbit.tbitblesdk.bluetooth.Code;
import com.tbit.tbitblesdk.bluetooth.RequestDispatcher;
import com.tbit.tbitblesdk.bluetooth.model.SearchResult;
import com.tbit.tbitblesdk.bluetooth.request.BleResponse;
import com.tbit.tbitblesdk.bluetooth.request.ConnectRequest;

import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;

/**
 * Created by Salmon on 2017/4/11 0011.
 */

public class ConnectObservable implements ObservableOnSubscribe<SearchResult> {

    private RequestDispatcher requestDispatcher;
    private SearchResult searchResult;

    public ConnectObservable(RequestDispatcher requestDispatcher, SearchResult searchResult) {
        this.requestDispatcher = requestDispatcher;
        this.searchResult = searchResult;
    }

    @Override
    public void subscribe(@NonNull final ObservableEmitter<SearchResult> e) throws Exception {
        requestDispatcher.addRequest(new ConnectRequest(searchResult.getDevice(), new BleResponse() {
            @Override
            public void onResponse(int resultCode) {
                if (resultCode == Code.REQUEST_SUCCESS) {
                    e.onNext(searchResult);
                    e.onComplete();
                } else {
                    int parsedResult = BikeUtil.parseResultCode(resultCode);
                    e.onError(new ResultCodeThrowable("ConnectObservable：errCode: " + resultCode,
                            parsedResult));
                }
            }
        }, 3));
    }
}
