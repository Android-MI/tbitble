package com.tbit.tbitblesdk.protocol.callback;

import com.tbit.tbitblesdk.Bike.model.SearchResult;

/**
 * Created by Salmon on 2017/3/20 0020.
 */

public interface SearchCallback {

    void onDeviceFound(SearchResult searchResult);
}
