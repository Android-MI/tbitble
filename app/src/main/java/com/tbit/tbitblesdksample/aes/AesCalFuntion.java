package com.tbit.tbitblesdksample.aes;

public class AesCalFuntion{
	
	public static final int WPOLY =  0x011b;
	public static final int BPOLY  =   0x1b;
	public static final int  DPOLY  = 0x8d;
	
	public static class F1 implements AesCal {

		public int GetValue(int x) {
			// TODO Auto-generated method stub
			return x;
		}
	}
	
	public static class F2 implements AesCal {

		public int GetValue(int x) {
			// TODO Auto-generated method stub
			return ((x << 1) ^ (((x >> 7) & 1) * WPOLY));
		}
	}
	
	public static  class F4 implements AesCal {

		public int GetValue(int x) {
			// TODO Auto-generated method stub
			return ((x << 2) ^ (((x >> 6) & 1) * WPOLY) ^ (((x >> 6) & 2) * WPOLY));
		}
	}
	
	public static  class F8 implements AesCal {

		public int GetValue(int x) {
			// TODO Auto-generated method stub
			return ((x << 3) ^ (((x >> 5) & 1) * WPOLY) ^ (((x >> 5) & 2) * WPOLY)
                    ^ (((x >> 5) & 4) * WPOLY));
		}
	}
	
	public  static class D2 implements AesCal {

		public int GetValue(int x) {
			// TODO Auto-generated method stub
			return (((x) >> 1) ^ (((x) & 1) != 0 ? DPOLY : 0));
		}
	}
	
	public static  class F3 implements AesCal {

		public int GetValue(int x) {
			// TODO Auto-generated method stub
			F2 f2 = new F2();
			return (f2.GetValue(x) ^ x);
		}
	}
	
	public  static class F9 implements AesCal {

		public int GetValue(int x) {
			// TODO Auto-generated method stub
			F8 f8 = new F8();
			return (f8.GetValue(x) ^ x);
		}
	}
	
	public static  class FB implements AesCal {

		public int GetValue(int x) {
			// TODO Auto-generated method stub
			F2 f2 = new F2();
			F8 f8 = new F8();
			return (f8.GetValue(x) ^ f2.GetValue(x) ^ x);
		}
	}
	
	public static  class FD implements AesCal {

		public int GetValue(int x) {
			// TODO Auto-generated method stub
			F4 f4 = new F4();
			F8 f8 = new F8();
			return (f8.GetValue(x) ^ f4.GetValue(x) ^ x);
		}
	}
	
	public static  class FE implements AesCal {

		public int GetValue(int x) {
			// TODO Auto-generated method stub
			F2 f2 = new F2();
			F4 f4 = new F4();
			F8 f8 = new F8();
			return (f8.GetValue(x) ^ f4.GetValue(x) ^ f2.GetValue(x));
		}
	}
}