package com.tbit.tbitblesdksample.aes;

public class JavaAesContent {
	public static final int N_COL = 4;
	public static final int N_ROW = 4;
	public static final int N_MAX_ROUNDS = 14;
	public static final int N_BLOCK = N_ROW * N_COL;
	private int rnd;
	private int[] ksch;
	
	public JavaAesContent(){
		rnd = 0;
		ksch = new int[(N_MAX_ROUNDS + 1) * N_BLOCK];
	}

	public int getRnd() {
		return rnd;
	}

	public void setRnd(int rnd) {
		this.rnd = rnd;
	}

	public int[] getKsch() {
		return ksch;
	}

	public void setKsch(int[] ksch) {
		this.ksch = ksch;
	}
	
	private void block_copy_nn(int[] d, int[] s, int len){
		for(int i = 0; i < len; i++)
			d[i] = s[i];
	}
	
	public int setKey(int[] key, int keylen){
		int cc, rc, hi;

	    switch( keylen )
	    {
	    case 16:
	    case 24:
	    case 32:
	        break;
	    default: 
	        rnd = 0; 
	        return -1;
	    }
	    block_copy_nn(ksch, key, keylen);
	    hi = (keylen + 28) << 2;
	    rnd = (hi >> 4) - 1;
	    AesCalFuntion.F2 f2 = new AesCalFuntion.F2();
	    for( cc = keylen, rc = 1; cc < hi; cc += 4 )
	    {   
	    	int tt, t0, t1, t2, t3;

	        t0 = ksch[cc - 4];
	        t1 = ksch[cc - 3];
	        t2 = ksch[cc - 2];
	        t3 = ksch[cc - 1];
	        if( cc % keylen == 0 )
	        {
	            tt = t0;
	            t0 = JavaAes.s_box(t1) ^ rc;
	            t1 = JavaAes.s_box(t2);
	            t2 = JavaAes.s_box(t3);
	            t3 = JavaAes.s_box(tt);
	            
	            rc = f2.GetValue(rc);
	        }
	        else if( keylen > 24 && cc % keylen == 16 )
	        {
	            t0 = JavaAes.s_box(t0);
	            t1 = JavaAes.s_box(t1);
	            t2 = JavaAes.s_box(t2);
	            t3 = JavaAes.s_box(t3);
	        }
	        tt = cc - keylen;
	        ksch[cc + 0] = ksch[tt + 0] ^ t0;
	        ksch[cc + 1] = ksch[tt + 1] ^ t1;
	        ksch[cc + 2] = ksch[tt + 2] ^ t2;
	        ksch[cc + 3] = ksch[tt + 3] ^ t3;
	    }
	    return 0;
	}
}
