package com.mempoolexplorer.txmempool.utils;

public class SysProps {

	public static final String NL = System.getProperty("line.separator");
	public static final float BEST_LOAD_FACTOR = 0.75f;// https://javaconceptoftheday.com/initial-capacity-and-load-factor-of-hashmap-in-java/
	public static final int EXPECTED_NUM_TX_IN_BLOCK = 5000;
	public static final int HM_INITIAL_CAPACITY_FOR_BLOCK = (int) ((1.0f + (1.0f - BEST_LOAD_FACTOR))
			* EXPECTED_NUM_TX_IN_BLOCK);// BEST_LOAD_FACTOR*1.25=6250
	public static final float HM_INITIAL_CAPACITY_MULTIPLIER = 1.25f;
	public static final int MAX_BLOCK_WEIGHT = 4000000;// Segwit new size
	public static final int EXPECTED_MAX_ANCESTRY_CHANGES = 200;
	public static final int EXPECTED_MAX_IGNORED_TXS = 200;
}
