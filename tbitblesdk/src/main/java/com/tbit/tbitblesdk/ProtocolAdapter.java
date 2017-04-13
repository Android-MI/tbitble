package com.tbit.tbitblesdk;

/**
 * Created by Salmon on 2017/4/13 0013.
 */

public abstract class ProtocolAdapter {

    public abstract int[] getPacketCrcTable();

    public abstract char[] getAdKey();

    public abstract int getMaxAdEncryptedCount();

}