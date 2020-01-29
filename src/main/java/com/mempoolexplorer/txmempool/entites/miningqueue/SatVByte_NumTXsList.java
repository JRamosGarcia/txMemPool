package com.mempoolexplorer.txmempool.entites.miningqueue;

import java.util.ArrayList;
import java.util.List;

public class SatVByte_NumTXsList {
	private List<SatVByte_NumTXs> satVByteNumTXsList = new ArrayList<>();

	private int maxSize;

	public SatVByte_NumTXsList(int maxSize) {
		this.maxSize = maxSize;
	}

	public boolean addTx(int satVByte) {
		int size = satVByteNumTXsList.size();
		if (size >= maxSize) {
			return false;
		} else if (size == 0) {
			addNewPair(satVByte);
		} else {
			SatVByte_NumTXs pair = satVByteNumTXsList.get(satVByteNumTXsList.size() - 1);
			if (pair.getSatVByte() == satVByte) {
				pair.setNumTxs(pair.getNumTxs() + 1);
			} else {
				addNewPair(satVByte);
			}
		}
		return true;
	}

	private void addNewPair(int satVByte) {
		SatVByte_NumTXs pair = new SatVByte_NumTXs(satVByte, 1);
		satVByteNumTXsList.add(pair);

	}
}
