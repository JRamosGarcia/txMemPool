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
		minerNames = List.of("AntPool", "BTC.COM", "edf1", "Huobi", "poolin.com", "taaltech", "hash933123", "Bitfury",
				"tb20190602f1", "E2M & BTC.TOP", "wxd19850920", "slush", "bytepool.com", "BTC.TOP", "1THash&58COIN",
				"www.okex.com", "shenwei2001", "xmtg", "bzp111", "fak500", "NovaBlock", "zhundongall2020", "123zay",
				"szwwucaiwan8", "wanxinrr", "ViaBTC", "giantfinex3", "ghf19817498586", "xmtg", "dazahui", "fak800",
				"sss102", "amlakparsa35", "ddd563002772");
	}

	@Override
	public CoinBaseData resolveFrom(String coinBaseField) {

		String ascciFromHex = AsciiUtils.hexToAscii(coinBaseField);
		String minerName = getMinerNameFrom(ascciFromHex);

		return new CoinBaseData(ascciFromHex, minerName);
	}

	private String getMinerNameFrom(String ascciFromHex) {
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
			start += SysProps.MINER_NAME_UNKNOWN.length();

			int end = ascciFromHex.indexOf("\\", start);
			if (end < 0) {
				return SysProps.MINER_NAME_UNKNOWN;
			}

			return ascciFromHex.substring(start, end);
		} catch (Exception e) {
			logger.error("Error searching for Miner Name. ", e);
			return SysProps.MINER_NAME_UNKNOWN;
		}
	}
}
