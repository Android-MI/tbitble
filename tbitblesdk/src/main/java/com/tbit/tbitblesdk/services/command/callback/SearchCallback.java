package com.tbit.tbitblesdk.services.command.callback;

import com.tbit.tbitblesdk.protocol.SearchResult;

/**
 * Created by Salmon on 2017/3/20 0020.
 */

public interface SearchCallback {

    void onDeviceFound(SearchResult searchResult);
}
