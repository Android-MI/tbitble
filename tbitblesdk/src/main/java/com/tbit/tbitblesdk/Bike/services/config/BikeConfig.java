package com.tbit.tbitblesdk.bike.services.config;

import com.tbit.tbitblesdk.bike.services.command.comparator.CommandComparator;

/**
 * Created by Salmon on 2017/3/20 0020.
 */

public interface BikeConfig {

    Uuid getUuid();

    CommandComparator getComparator();

}
