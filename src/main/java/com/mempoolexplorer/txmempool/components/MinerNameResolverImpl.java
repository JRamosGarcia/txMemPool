package com.mempoolexplorer.txmempool.components;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.mempoolexplorer.txmempool.entites.CoinBaseData;
import com.mempoolexplorer.txmempool.utils.AsciiUtils;
import com.mempoolexplorer.txmempool.utils.SysProps;

@Component
public class MinerNameResolverImpl implements MinerNameResolver {

	private Logger logger = LoggerFactory.getLogger(MinerNameResolverImpl.class);

	private List<String> minerNames = new ArrayList<>();

	public MinerNameResolverImpl() {

		// Order is important (i.e. "E2M & BTC.TOP" vs "BTC.TOP")
		minerNames = List.of("AntPool", "BTC.COM", "Huobi", "HuoBi", "poolin.com", "Bitfury", "E2M & BTC.TOP", "slush",
				"bytepool.com", "BTC.TOP", "1THash&58COIN", "www.okex.com", "NovaBlock", "ViaBTC", "Ukrpool.com",
				"SpiderPool", "TTTTTT3333", "taal.com", "bitcoin.com");
	}

	@Override
	public CoinBaseData resolveFrom(String coinBaseField) {

		String ascciFromHex = AsciiUtils.hexToAscii(coinBaseField);
		String minerName = getMinerNameFrom(ascciFromHex);

		return new CoinBaseData(ascciFromHex, minerName);
	}

	private String getMinerNameFrom(String ascciFromHex) {
		ascciFromHex = ascciFromHex.replaceAll("[^\\x00-\\x7F]", "");
		for (String minerName : minerNames) {
			if (ascciFromHex.contains(minerName)) {
				return minerName;
			}
		}

		try {
			int start = ascciFromHex.indexOf(SysProps.MINED_BY_START);
			if (start < 0) {
				return SysProps.MINER_NAME_UNKNOWN;
			}
			start += SysProps.MINED_BY_START.length();

			// int end = ascciFromHex.length();
			int end = ascciFromHex.indexOf(0, start);// up to first null character
			if (end < 0) {
				return SysProps.MINER_NAME_UNKNOWN;
			}

			return ascciFromHex.substring(start, end).trim();
		} catch (Exception e) {
			logger.error("Error searching for Miner Name. ", e);
			return SysProps.MINER_NAME_UNKNOWN;
		}
	}
}
