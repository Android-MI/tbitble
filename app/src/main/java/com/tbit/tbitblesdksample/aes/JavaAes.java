package com.tbit.tbitblesdksample.aes;

public class JavaAes {
	public static int[] isbox;
	public static int[] gfmul_9;
	public static int[] gfmul_b;
	public static int[] gfmul_d;
	public static int[] gfmul_e;
	public static int[] sb_box;
	
	static {
		
		isbox = GetIsBoxArray(new AesCalFuntion.F1());
		gfmul_9= GetMmDataArray(new AesCalFuntion.F9());
		gfmul_b= GetMmDataArray(new AesCalFuntion.FB());
		gfmul_d= GetMmDataArray(new AesCalFuntion.FD());
		gfmul_e= GetMmDataArray(new AesCalFuntion.FE());
		sb_box = GetSbBoxArray(new AesCalFuntion.F1());
	}
	
	public static int AesDecrypt(int[] in, int[] out, JavaAesContent ctx){
		if( ctx.getRnd() != 0 )
	    {
			int[] s1 = new int[JavaAesContent.N_BLOCK];
			int r;
	        copy_and_key( s1, in, ctx.getKsch(), ctx.getRnd() * JavaAesContent.N_BLOCK );
	        inv_shift_sub_rows( s1 );
	        //printIntArray("inv_shift_sub_rows",s1);
	        for( r = ctx.getRnd() ; --r != 0 ; )
	        {
	        	xor_block( s1, ctx.getKsch(), r * JavaAesContent.N_BLOCK );
	        	//printIntArray("xor_block",s1);
	            inv_mix_sub_columns( s1 );
	            //printIntArray("inv_mix_sub_columns" + r,s1);
	        }
	        copy_and_key( out, s1, ctx.getKsch(), 0);
	    }
	    else
	        return -1;
		return 0;
	}
	
	public static void printIntArray(String title,int[] out){
		System.out.print(title + ":");
		for(int i = 0; i < out.length;i++){
			System.out.print(out[i] + " ");
		}
		System.out.println();
	}
	
	private  static void xor_block( int[] d, int[] s,int index){
		for(int i = 0; i < 16; i++){
			d[i] = (s[i+ index] ^ d[i]);
		}
	}
	
	private static int[] GetIsBoxArray(AesCal w)
	{
		return new int[]{
			    w.GetValue(0x52), w.GetValue(0x09), w.GetValue(0x6a), w.GetValue(0xd5), w.GetValue(0x30), w.GetValue(0x36), w.GetValue(0xa5), w.GetValue(0x38),
			    w.GetValue(0xbf), w.GetValue(0x40), w.GetValue(0xa3), w.GetValue(0x9e), w.GetValue(0x81), w.GetValue(0xf3), w.GetValue(0xd7), w.GetValue(0xfb),
			    w.GetValue(0x7c), w.GetValue(0xe3), w.GetValue(0x39), w.GetValue(0x82), w.GetValue(0x9b), w.GetValue(0x2f), w.GetValue(0xff), w.GetValue(0x87),
			    w.GetValue(0x34), w.GetValue(0x8e), w.GetValue(0x43), w.GetValue(0x44), w.GetValue(0xc4), w.GetValue(0xde), w.GetValue(0xe9), w.GetValue(0xcb),
			    w.GetValue(0x54), w.GetValue(0x7b), w.GetValue(0x94), w.GetValue(0x32), w.GetValue(0xa6), w.GetValue(0xc2), w.GetValue(0x23), w.GetValue(0x3d),
			    w.GetValue(0xee), w.GetValue(0x4c), w.GetValue(0x95), w.GetValue(0x0b), w.GetValue(0x42), w.GetValue(0xfa), w.GetValue(0xc3), w.GetValue(0x4e),
			    w.GetValue(0x08), w.GetValue(0x2e), w.GetValue(0xa1), w.GetValue(0x66), w.GetValue(0x28), w.GetValue(0xd9), w.GetValue(0x24), w.GetValue(0xb2),
			    w.GetValue(0x76), w.GetValue(0x5b), w.GetValue(0xa2), w.GetValue(0x49), w.GetValue(0x6d), w.GetValue(0x8b), w.GetValue(0xd1), w.GetValue(0x25),
			    w.GetValue(0x72), w.GetValue(0xf8), w.GetValue(0xf6), w.GetValue(0x64), w.GetValue(0x86), w.GetValue(0x68), w.GetValue(0x98), w.GetValue(0x16),
			    w.GetValue(0xd4), w.GetValue(0xa4), w.GetValue(0x5c), w.GetValue(0xcc), w.GetValue(0x5d), w.GetValue(0x65), w.GetValue(0xb6), w.GetValue(0x92),
			    w.GetValue(0x6c), w.GetValue(0x70), w.GetValue(0x48), w.GetValue(0x50), w.GetValue(0xfd), w.GetValue(0xed), w.GetValue(0xb9), w.GetValue(0xda),
			    w.GetValue(0x5e), w.GetValue(0x15), w.GetValue(0x46), w.GetValue(0x57), w.GetValue(0xa7), w.GetValue(0x8d), w.GetValue(0x9d), w.GetValue(0x84),
			    w.GetValue(0x90), w.GetValue(0xd8), w.GetValue(0xab), w.GetValue(0x00), w.GetValue(0x8c), w.GetValue(0xbc), w.GetValue(0xd3), w.GetValue(0x0a),
			    w.GetValue(0xf7), w.GetValue(0xe4), w.GetValue(0x58), w.GetValue(0x05), w.GetValue(0xb8), w.GetValue(0xb3), w.GetValue(0x45), w.GetValue(0x06),
			    w.GetValue(0xd0), w.GetValue(0x2c), w.GetValue(0x1e), w.GetValue(0x8f), w.GetValue(0xca), w.GetValue(0x3f), w.GetValue(0x0f), w.GetValue(0x02),
			    w.GetValue(0xc1), w.GetValue(0xaf), w.GetValue(0xbd), w.GetValue(0x03), w.GetValue(0x01), w.GetValue(0x13), w.GetValue(0x8a), w.GetValue(0x6b),
			    w.GetValue(0x3a), w.GetValue(0x91), w.GetValue(0x11), w.GetValue(0x41), w.GetValue(0x4f), w.GetValue(0x67), w.GetValue(0xdc), w.GetValue(0xea),
			    w.GetValue(0x97), w.GetValue(0xf2), w.GetValue(0xcf), w.GetValue(0xce), w.GetValue(0xf0), w.GetValue(0xb4), w.GetValue(0xe6), w.GetValue(0x73),
			    w.GetValue(0x96), w.GetValue(0xac), w.GetValue(0x74), w.GetValue(0x22), w.GetValue(0xe7), w.GetValue(0xad), w.GetValue(0x35), w.GetValue(0x85),
			    w.GetValue(0xe2), w.GetValue(0xf9), w.GetValue(0x37), w.GetValue(0xe8), w.GetValue(0x1c), w.GetValue(0x75), w.GetValue(0xdf), w.GetValue(0x6e),
			    w.GetValue(0x47), w.GetValue(0xf1), w.GetValue(0x1a), w.GetValue(0x71), w.GetValue(0x1d), w.GetValue(0x29), w.GetValue(0xc5), w.GetValue(0x89),
			    w.GetValue(0x6f), w.GetValue(0xb7), w.GetValue(0x62), w.GetValue(0x0e), w.GetValue(0xaa), w.GetValue(0x18), w.GetValue(0xbe), w.GetValue(0x1b),
			    w.GetValue(0xfc), w.GetValue(0x56), w.GetValue(0x3e), w.GetValue(0x4b), w.GetValue(0xc6), w.GetValue(0xd2), w.GetValue(0x79), w.GetValue(0x20),
			    w.GetValue(0x9a), w.GetValue(0xdb), w.GetValue(0xc0), w.GetValue(0xfe), w.GetValue(0x78), w.GetValue(0xcd), w.GetValue(0x5a), w.GetValue(0xf4),
			    w.GetValue(0x1f), w.GetValue(0xdd), w.GetValue(0xa8), w.GetValue(0x33), w.GetValue(0x88), w.GetValue(0x07), w.GetValue(0xc7), w.GetValue(0x31),
			    w.GetValue(0xb1), w.GetValue(0x12), w.GetValue(0x10), w.GetValue(0x59), w.GetValue(0x27), w.GetValue(0x80), w.GetValue(0xec), w.GetValue(0x5f),
			    w.GetValue(0x60), w.GetValue(0x51), w.GetValue(0x7f), w.GetValue(0xa9), w.GetValue(0x19), w.GetValue(0xb5), w.GetValue(0x4a), w.GetValue(0x0d),
			    w.GetValue(0x2d), w.GetValue(0xe5), w.GetValue(0x7a), w.GetValue(0x9f), w.GetValue(0x93), w.GetValue(0xc9), w.GetValue(0x9c), w.GetValue(0xef),
			    w.GetValue(0xa0), w.GetValue(0xe0), w.GetValue(0x3b), w.GetValue(0x4d), w.GetValue(0xae), w.GetValue(0x2a), w.GetValue(0xf5), w.GetValue(0xb0),
			    w.GetValue(0xc8), w.GetValue(0xeb), w.GetValue(0xbb), w.GetValue(0x3c), w.GetValue(0x83), w.GetValue(0x53), w.GetValue(0x99), w.GetValue(0x61),
			    w.GetValue(0x17), w.GetValue(0x2b), w.GetValue(0x04), w.GetValue(0x7e), w.GetValue(0xba), w.GetValue(0x77), w.GetValue(0xd6), w.GetValue(0x26),
			    w.GetValue(0xe1), w.GetValue(0x69), w.GetValue(0x14), w.GetValue(0x63), w.GetValue(0x55), w.GetValue(0x21), w.GetValue(0x0c), w.GetValue(0x7d)  
		};
	}
	
	private static int[] GetSbBoxArray(AesCal w){
		return new int[]{
			    w.GetValue(0x63), w.GetValue(0x7c), w.GetValue(0x77), w.GetValue(0x7b), w.GetValue(0xf2), w.GetValue(0x6b), w.GetValue(0x6f), w.GetValue(0xc5),
			    w.GetValue(0x30), w.GetValue(0x01), w.GetValue(0x67), w.GetValue(0x2b), w.GetValue(0xfe), w.GetValue(0xd7), w.GetValue(0xab), w.GetValue(0x76),
			    w.GetValue(0xca), w.GetValue(0x82), w.GetValue(0xc9), w.GetValue(0x7d), w.GetValue(0xfa), w.GetValue(0x59), w.GetValue(0x47), w.GetValue(0xf0),
			    w.GetValue(0xad), w.GetValue(0xd4), w.GetValue(0xa2), w.GetValue(0xaf), w.GetValue(0x9c), w.GetValue(0xa4), w.GetValue(0x72), w.GetValue(0xc0),
			    w.GetValue(0xb7), w.GetValue(0xfd), w.GetValue(0x93), w.GetValue(0x26), w.GetValue(0x36), w.GetValue(0x3f), w.GetValue(0xf7), w.GetValue(0xcc),
			    w.GetValue(0x34), w.GetValue(0xa5), w.GetValue(0xe5), w.GetValue(0xf1), w.GetValue(0x71), w.GetValue(0xd8), w.GetValue(0x31), w.GetValue(0x15),
			    w.GetValue(0x04), w.GetValue(0xc7), w.GetValue(0x23), w.GetValue(0xc3), w.GetValue(0x18), w.GetValue(0x96), w.GetValue(0x05), w.GetValue(0x9a),
			    w.GetValue(0x07), w.GetValue(0x12), w.GetValue(0x80), w.GetValue(0xe2), w.GetValue(0xeb), w.GetValue(0x27), w.GetValue(0xb2), w.GetValue(0x75),
			    w.GetValue(0x09), w.GetValue(0x83), w.GetValue(0x2c), w.GetValue(0x1a), w.GetValue(0x1b), w.GetValue(0x6e), w.GetValue(0x5a), w.GetValue(0xa0),
			    w.GetValue(0x52), w.GetValue(0x3b), w.GetValue(0xd6), w.GetValue(0xb3), w.GetValue(0x29), w.GetValue(0xe3), w.GetValue(0x2f), w.GetValue(0x84),
			    w.GetValue(0x53), w.GetValue(0xd1), w.GetValue(0x00), w.GetValue(0xed), w.GetValue(0x20), w.GetValue(0xfc), w.GetValue(0xb1), w.GetValue(0x5b),
			    w.GetValue(0x6a), w.GetValue(0xcb), w.GetValue(0xbe), w.GetValue(0x39), w.GetValue(0x4a), w.GetValue(0x4c), w.GetValue(0x58), w.GetValue(0xcf),
			    w.GetValue(0xd0), w.GetValue(0xef), w.GetValue(0xaa), w.GetValue(0xfb), w.GetValue(0x43), w.GetValue(0x4d), w.GetValue(0x33), w.GetValue(0x85),
			    w.GetValue(0x45), w.GetValue(0xf9), w.GetValue(0x02), w.GetValue(0x7f), w.GetValue(0x50), w.GetValue(0x3c), w.GetValue(0x9f), w.GetValue(0xa8),
			    w.GetValue(0x51), w.GetValue(0xa3), w.GetValue(0x40), w.GetValue(0x8f), w.GetValue(0x92), w.GetValue(0x9d), w.GetValue(0x38), w.GetValue(0xf5),
			    w.GetValue(0xbc), w.GetValue(0xb6), w.GetValue(0xda), w.GetValue(0x21), w.GetValue(0x10), w.GetValue(0xff), w.GetValue(0xf3), w.GetValue(0xd2),
			    w.GetValue(0xcd), w.GetValue(0x0c), w.GetValue(0x13), w.GetValue(0xec), w.GetValue(0x5f), w.GetValue(0x97), w.GetValue(0x44), w.GetValue(0x17),
			    w.GetValue(0xc4), w.GetValue(0xa7), w.GetValue(0x7e), w.GetValue(0x3d), w.GetValue(0x64), w.GetValue(0x5d), w.GetValue(0x19), w.GetValue(0x73),
			    w.GetValue(0x60), w.GetValue(0x81), w.GetValue(0x4f), w.GetValue(0xdc), w.GetValue(0x22), w.GetValue(0x2a), w.GetValue(0x90), w.GetValue(0x88),
			    w.GetValue(0x46), w.GetValue(0xee), w.GetValue(0xb8), w.GetValue(0x14), w.GetValue(0xde), w.GetValue(0x5e), w.GetValue(0x0b), w.GetValue(0xdb),
			    w.GetValue(0xe0), w.GetValue(0x32), w.GetValue(0x3a), w.GetValue(0x0a), w.GetValue(0x49), w.GetValue(0x06), w.GetValue(0x24), w.GetValue(0x5c),
			    w.GetValue(0xc2), w.GetValue(0xd3), w.GetValue(0xac), w.GetValue(0x62), w.GetValue(0x91), w.GetValue(0x95), w.GetValue(0xe4), w.GetValue(0x79),
			    w.GetValue(0xe7), w.GetValue(0xc8), w.GetValue(0x37), w.GetValue(0x6d), w.GetValue(0x8d), w.GetValue(0xd5), w.GetValue(0x4e), w.GetValue(0xa9),
			    w.GetValue(0x6c), w.GetValue(0x56), w.GetValue(0xf4), w.GetValue(0xea), w.GetValue(0x65), w.GetValue(0x7a), w.GetValue(0xae), w.GetValue(0x08),
			    w.GetValue(0xba), w.GetValue(0x78), w.GetValue(0x25), w.GetValue(0x2e), w.GetValue(0x1c), w.GetValue(0xa6), w.GetValue(0xb4), w.GetValue(0xc6),
			    w.GetValue(0xe8), w.GetValue(0xdd), w.GetValue(0x74), w.GetValue(0x1f), w.GetValue(0x4b), w.GetValue(0xbd), w.GetValue(0x8b), w.GetValue(0x8a),
			    w.GetValue(0x70), w.GetValue(0x3e), w.GetValue(0xb5), w.GetValue(0x66), w.GetValue(0x48), w.GetValue(0x03), w.GetValue(0xf6), w.GetValue(0x0e),
			    w.GetValue(0x61), w.GetValue(0x35), w.GetValue(0x57), w.GetValue(0xb9), w.GetValue(0x86), w.GetValue(0xc1), w.GetValue(0x1d), w.GetValue(0x9e),
			    w.GetValue(0xe1), w.GetValue(0xf8), w.GetValue(0x98), w.GetValue(0x11), w.GetValue(0x69), w.GetValue(0xd9), w.GetValue(0x8e), w.GetValue(0x94),
			    w.GetValue(0x9b), w.GetValue(0x1e), w.GetValue(0x87), w.GetValue(0xe9), w.GetValue(0xce), w.GetValue(0x55), w.GetValue(0x28), w.GetValue(0xdf),
			    w.GetValue(0x8c), w.GetValue(0xa1), w.GetValue(0x89), w.GetValue(0x0d), w.GetValue(0xbf), w.GetValue(0xe6), w.GetValue(0x42), w.GetValue(0x68),
			    w.GetValue(0x41), w.GetValue(0x99), w.GetValue(0x2d), w.GetValue(0x0f), w.GetValue(0xb0), w.GetValue(0x54), w.GetValue(0xbb), w.GetValue(0x16) 
		};
	}
	
	private static int[] GetMmDataArray(AesCal w)
	{
		return new int[]{
			    w.GetValue(0x00), w.GetValue(0x01), w.GetValue(0x02), w.GetValue(0x03), w.GetValue(0x04), w.GetValue(0x05), w.GetValue(0x06), w.GetValue(0x07),
			    w.GetValue(0x08), w.GetValue(0x09), w.GetValue(0x0a), w.GetValue(0x0b), w.GetValue(0x0c), w.GetValue(0x0d), w.GetValue(0x0e), w.GetValue(0x0f),
			    w.GetValue(0x10), w.GetValue(0x11), w.GetValue(0x12), w.GetValue(0x13), w.GetValue(0x14), w.GetValue(0x15), w.GetValue(0x16), w.GetValue(0x17),
			    w.GetValue(0x18), w.GetValue(0x19), w.GetValue(0x1a), w.GetValue(0x1b), w.GetValue(0x1c), w.GetValue(0x1d), w.GetValue(0x1e), w.GetValue(0x1f),
			    w.GetValue(0x20), w.GetValue(0x21), w.GetValue(0x22), w.GetValue(0x23), w.GetValue(0x24), w.GetValue(0x25), w.GetValue(0x26), w.GetValue(0x27),
			    w.GetValue(0x28), w.GetValue(0x29), w.GetValue(0x2a), w.GetValue(0x2b), w.GetValue(0x2c), w.GetValue(0x2d), w.GetValue(0x2e), w.GetValue(0x2f),
			    w.GetValue(0x30), w.GetValue(0x31), w.GetValue(0x32), w.GetValue(0x33), w.GetValue(0x34), w.GetValue(0x35), w.GetValue(0x36), w.GetValue(0x37),
			    w.GetValue(0x38), w.GetValue(0x39), w.GetValue(0x3a), w.GetValue(0x3b), w.GetValue(0x3c), w.GetValue(0x3d), w.GetValue(0x3e), w.GetValue(0x3f),
			    w.GetValue(0x40), w.GetValue(0x41), w.GetValue(0x42), w.GetValue(0x43), w.GetValue(0x44), w.GetValue(0x45), w.GetValue(0x46), w.GetValue(0x47),
			    w.GetValue(0x48), w.GetValue(0x49), w.GetValue(0x4a), w.GetValue(0x4b), w.GetValue(0x4c), w.GetValue(0x4d), w.GetValue(0x4e), w.GetValue(0x4f),
			    w.GetValue(0x50), w.GetValue(0x51), w.GetValue(0x52), w.GetValue(0x53), w.GetValue(0x54), w.GetValue(0x55), w.GetValue(0x56), w.GetValue(0x57),
			    w.GetValue(0x58), w.GetValue(0x59), w.GetValue(0x5a), w.GetValue(0x5b), w.GetValue(0x5c), w.GetValue(0x5d), w.GetValue(0x5e), w.GetValue(0x5f),
			    w.GetValue(0x60), w.GetValue(0x61), w.GetValue(0x62), w.GetValue(0x63), w.GetValue(0x64), w.GetValue(0x65), w.GetValue(0x66), w.GetValue(0x67),
			    w.GetValue(0x68), w.GetValue(0x69), w.GetValue(0x6a), w.GetValue(0x6b), w.GetValue(0x6c), w.GetValue(0x6d), w.GetValue(0x6e), w.GetValue(0x6f),
			    w.GetValue(0x70), w.GetValue(0x71), w.GetValue(0x72), w.GetValue(0x73), w.GetValue(0x74), w.GetValue(0x75), w.GetValue(0x76), w.GetValue(0x77),
			    w.GetValue(0x78), w.GetValue(0x79), w.GetValue(0x7a), w.GetValue(0x7b), w.GetValue(0x7c), w.GetValue(0x7d), w.GetValue(0x7e), w.GetValue(0x7f),
			    w.GetValue(0x80), w.GetValue(0x81), w.GetValue(0x82), w.GetValue(0x83), w.GetValue(0x84), w.GetValue(0x85), w.GetValue(0x86), w.GetValue(0x87),
			    w.GetValue(0x88), w.GetValue(0x89), w.GetValue(0x8a), w.GetValue(0x8b), w.GetValue(0x8c), w.GetValue(0x8d), w.GetValue(0x8e), w.GetValue(0x8f),
			    w.GetValue(0x90), w.GetValue(0x91), w.GetValue(0x92), w.GetValue(0x93), w.GetValue(0x94), w.GetValue(0x95), w.GetValue(0x96), w.GetValue(0x97),
			    w.GetValue(0x98), w.GetValue(0x99), w.GetValue(0x9a), w.GetValue(0x9b), w.GetValue(0x9c), w.GetValue(0x9d), w.GetValue(0x9e), w.GetValue(0x9f),
			    w.GetValue(0xa0), w.GetValue(0xa1), w.GetValue(0xa2), w.GetValue(0xa3), w.GetValue(0xa4), w.GetValue(0xa5), w.GetValue(0xa6), w.GetValue(0xa7),
			    w.GetValue(0xa8), w.GetValue(0xa9), w.GetValue(0xaa), w.GetValue(0xab), w.GetValue(0xac), w.GetValue(0xad), w.GetValue(0xae), w.GetValue(0xaf),
			    w.GetValue(0xb0), w.GetValue(0xb1), w.GetValue(0xb2), w.GetValue(0xb3), w.GetValue(0xb4), w.GetValue(0xb5), w.GetValue(0xb6), w.GetValue(0xb7),
			    w.GetValue(0xb8), w.GetValue(0xb9), w.GetValue(0xba), w.GetValue(0xbb), w.GetValue(0xbc), w.GetValue(0xbd), w.GetValue(0xbe), w.GetValue(0xbf),
			    w.GetValue(0xc0), w.GetValue(0xc1), w.GetValue(0xc2), w.GetValue(0xc3), w.GetValue(0xc4), w.GetValue(0xc5), w.GetValue(0xc6), w.GetValue(0xc7),
			    w.GetValue(0xc8), w.GetValue(0xc9), w.GetValue(0xca), w.GetValue(0xcb), w.GetValue(0xcc), w.GetValue(0xcd), w.GetValue(0xce), w.GetValue(0xcf),
			    w.GetValue(0xd0), w.GetValue(0xd1), w.GetValue(0xd2), w.GetValue(0xd3), w.GetValue(0xd4), w.GetValue(0xd5), w.GetValue(0xd6), w.GetValue(0xd7),
			    w.GetValue(0xd8), w.GetValue(0xd9), w.GetValue(0xda), w.GetValue(0xdb), w.GetValue(0xdc), w.GetValue(0xdd), w.GetValue(0xde), w.GetValue(0xdf),
			    w.GetValue(0xe0), w.GetValue(0xe1), w.GetValue(0xe2), w.GetValue(0xe3), w.GetValue(0xe4), w.GetValue(0xe5), w.GetValue(0xe6), w.GetValue(0xe7),
			    w.GetValue(0xe8), w.GetValue(0xe9), w.GetValue(0xea), w.GetValue(0xeb), w.GetValue(0xec), w.GetValue(0xed), w.GetValue(0xee), w.GetValue(0xef),
			    w.GetValue(0xf0), w.GetValue(0xf1), w.GetValue(0xf2), w.GetValue(0xf3), w.GetValue(0xf4), w.GetValue(0xf5), w.GetValue(0xf6), w.GetValue(0xf7),
			    w.GetValue(0xf8), w.GetValue(0xf9), w.GetValue(0xfa), w.GetValue(0xfb), w.GetValue(0xfc), w.GetValue(0xfd), w.GetValue(0xfe), w.GetValue(0xff)  
		};
	}
	
	private  static void copy_and_key(int[] d, int[] s, int[] k, int index){
		for(int i = 0; i < 16; i++){
			d[i] = s[i] ^ k[i + index];
		}
	}
	
	private static  int is_box(int i){
		return JavaAes.isbox[i];
	}
	
	public static int s_box(int i){
		return JavaAes.sb_box[i ];
	}
	private  static int gfm_9(int i){
		return JavaAes.gfmul_9[i ];
	}
	private static  int gfm_b(int i){
		return JavaAes.gfmul_b[i ];
	}
	private static  int gfm_d(int i){
		return JavaAes.gfmul_d[i ];
	}
	private static  int gfm_e(int i){
		return JavaAes.gfmul_e[i ];
	}
	
	private static  void inv_shift_sub_rows( int[] st)
	{
		int tt;

	    st[ 0] = is_box(st[ 0]); st[ 4] = is_box(st[ 4]);
	    st[ 8] = is_box(st[ 8]); st[12] = is_box(st[12]);

	    tt = st[13]; st[13] = is_box(st[9]); st[ 9] = is_box(st[5]);
	    st[ 5] = is_box(st[1]); st[ 1] = is_box( tt );

	    tt = st[2]; st[ 2] = is_box(st[10]); st[10] = is_box( tt );
	    tt = st[6]; st[ 6] = is_box(st[14]); st[14] = is_box( tt );

	    tt = st[3]; st[ 3] = is_box(st[ 7]); st[ 7] = is_box(st[11]);
	    st[11] = is_box(st[15]); st[15] = is_box( tt );
	}
	
	private static  void block_copy(int[] d, int[] s){
		for(int i = 0; i < 16; i ++)
			d[i] = s[i];
	} 
	
	private  static  void inv_mix_sub_columns( int[] dt){ 
		int[] st = new int[JavaAesContent.N_BLOCK];
	    block_copy(st, dt);
	    dt[ 0] = is_box(gfm_e(st[ 0]) ^ gfm_b(st[ 1]) ^ gfm_d(st[ 2]) ^ gfm_9(st[ 3]));
	    dt[ 5] = is_box(gfm_9(st[ 0]) ^ gfm_e(st[ 1]) ^ gfm_b(st[ 2]) ^ gfm_d(st[ 3]));
	    dt[10] = is_box(gfm_d(st[ 0]) ^ gfm_9(st[ 1]) ^ gfm_e(st[ 2]) ^ gfm_b(st[ 3]));
	    dt[15] = is_box(gfm_b(st[ 0]) ^ gfm_d(st[ 1]) ^ gfm_9(st[ 2]) ^ gfm_e(st[ 3]));

	    dt[ 4] = is_box(gfm_e(st[ 4]) ^ gfm_b(st[ 5]) ^ gfm_d(st[ 6]) ^ gfm_9(st[ 7]));
	    dt[ 9] = is_box(gfm_9(st[ 4]) ^ gfm_e(st[ 5]) ^ gfm_b(st[ 6]) ^ gfm_d(st[ 7]));
	    dt[14] = is_box(gfm_d(st[ 4]) ^ gfm_9(st[ 5]) ^ gfm_e(st[ 6]) ^ gfm_b(st[ 7]));
	    dt[ 3] = is_box(gfm_b(st[ 4]) ^ gfm_d(st[ 5]) ^ gfm_9(st[ 6]) ^ gfm_e(st[ 7]));

	    dt[ 8] = is_box(gfm_e(st[ 8]) ^ gfm_b(st[ 9]) ^ gfm_d(st[10]) ^ gfm_9(st[11]));
	    dt[13] = is_box(gfm_9(st[ 8]) ^ gfm_e(st[ 9]) ^ gfm_b(st[10]) ^ gfm_d(st[11]));
	    dt[ 2] = is_box(gfm_d(st[ 8]) ^ gfm_9(st[ 9]) ^ gfm_e(st[10]) ^ gfm_b(st[11]));
	    dt[ 7] = is_box(gfm_b(st[ 8]) ^ gfm_d(st[ 9]) ^ gfm_9(st[10]) ^ gfm_e(st[11]));

	    dt[12] = is_box(gfm_e(st[12]) ^ gfm_b(st[13]) ^ gfm_d(st[14]) ^ gfm_9(st[15]));
	    dt[ 1] = is_box(gfm_9(st[12]) ^ gfm_e(st[13]) ^ gfm_b(st[14]) ^ gfm_d(st[15]));
	    dt[ 6] = is_box(gfm_d(st[12]) ^ gfm_9(st[13]) ^ gfm_e(st[14]) ^ gfm_b(st[15]));
	    dt[11] = is_box(gfm_b(st[12]) ^ gfm_d(st[13]) ^ gfm_9(st[14]) ^ gfm_e(st[15]));
	  }
}
