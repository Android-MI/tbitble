package com.tbit.tbitblesdk;

/**
 * Created by Salmon on 2017/1/9 0009.
 */

public interface OtaListener {
    void onOtaResponse(int code);
    void onOtaProgress(int progress);
}
