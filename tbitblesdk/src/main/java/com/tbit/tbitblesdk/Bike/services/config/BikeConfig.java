package com.tbit.tbitblesdk.Bike.services.config;

import com.tbit.tbitblesdk.Bike.services.command.comparator.CommandComparator;
import com.tbit.tbitblesdk.Bike.services.resolver.Resolver;

/**
 * Created by Salmon on 2017/3/20 0020.
 */

public interface BikeConfig {

    Uuid getUuid();

    CommandComparator getComparator();

    Resolver getResolver();
}
