package com.tbit.tbitblesdk.Bike.tasks.exceptions;

/**
 * Created by Salmon on 2017/4/11 0011.
 */

public class ResultCodeThrowable extends Throwable {

    private int resultCode;

    public ResultCodeThrowable(String message, int resultCode) {
        super(message);
        this.resultCode = resultCode;
    }

    public int getResultCode() {
        return resultCode;
    }
}
