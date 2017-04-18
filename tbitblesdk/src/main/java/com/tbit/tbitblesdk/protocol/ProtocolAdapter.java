package com.tbit.tbitblesdk.protocol;

/**
 * Created by Salmon on 2017/4/12 0012.
 */

public abstract class ProtocolAdapter {

    public abstract int[] getPacketCrcTable();

    public abstract char[] getAdKey();

    public abstract int getMaxAdEncryptedCount();

}
