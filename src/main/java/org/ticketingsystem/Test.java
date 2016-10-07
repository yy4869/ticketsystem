package org.ticketingsystem;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		long a = 1L << 63 | ((1L << 63) - 1);
		System.out.println(Long.bitCount(a));
		long b = 1L << 3 | 1L << 7;
		System.out.println(b+" "+Long.lowestOneBit(b));
	}

}
