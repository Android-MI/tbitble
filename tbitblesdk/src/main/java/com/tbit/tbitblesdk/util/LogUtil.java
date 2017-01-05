package com.tbit.tbitblesdk.util;

import com.tbit.tbitblesdk.protocol.BluEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Salmon on 2017/1/4 0004.
 */

public class LogUtil {
    public static void postLog(String title, String content) {
        EventBus.getDefault().post(new BluEvent.DebugLogEvent(title, content));
    }
}
