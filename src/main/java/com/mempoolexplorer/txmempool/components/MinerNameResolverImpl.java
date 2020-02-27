package com.mempoolexplorer.txmempool.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

	private Map<String, String> sanetizedNamesMap = new HashMap<>();

	public MinerNameResolverImpl() {

		// Order is important (i.e. "E2M & BTC.TOP" vs "BTC.TOP")
		minerNames = List.of("AntPool", "BTC.COM", "Huobi", "HuoBi", "poolin.com", "Bitfury", "E2M & BTC.TOP", "slush",
				"bytepool.com", "BTC.TOP", "1THash&58COIN", "www.okex.com", "NovaBlock", "ViaBTC", "Ukrpool.com",
				"SpiderPool", "TTTTTT3333", "taal.com", "bitcoin.com", "MiningCity", "ckpool", "CN/TT");

		sanetizedNamesMap = Map.of("CN/TT", "cn_slash_tt");
	}

	@Override
	public CoinBaseData resolveFrom(String coinBaseField) {

		String ascciFromHex = AsciiUtils.hexToAscii(coinBaseField);

		return getMinerName(coinBaseField, ascciFromHex).map(mn -> new CoinBaseData(ascciFromHex, mn))
				.orElse(new CoinBaseData(ascciFromHex, SysProps.MINER_NAME_UNKNOWN));
	}

	private Optional<String> getMinerName(String coinBaseField, String ascciFromHex) {
		return getMinerNameFromCoinBaseField(coinBaseField).or(() -> getMinerNameFromAscci(ascciFromHex))
				.map(this::sanetize);
	}

	private String sanetize(String minerName) {
		return Optional.ofNullable(sanetizedNamesMap.get(minerName)).orElse(minerName).toLowerCase();
	}

	private Optional<String> getMinerNameFromCoinBaseField(String coinbaseField) {
		// Search for F2pool (discuss fish)
		// Search for "七彩神仙鱼"->"e4b883e5bda9e7a59ee4bb99e9b1bc" or "🐟"->"f09f909f"

		if (coinbaseField.contains("e4b883e5bda9e7a59ee4bb99e9b1bc") || coinbaseField.contains("f09f909f")) {
			return Optional.of("f2pool");
		}
		return Optional.empty();
	}

	private Optional<String> getMinerNameFromAscci(String ascciFromHex) {
		ascciFromHex = ascciFromHex.replaceAll("[^\\x00-\\x7F]", "");// Delete all non asccii chars
		for (String minerName : minerNames) {
			if (ascciFromHex.contains(minerName)) {
				return Optional.of(minerName);
			}
		}

		try {
			int start = ascciFromHex.indexOf(SysProps.MINED_BY_START);
			if (start < 0) {
				return Optional.empty();
			}
			start += SysProps.MINED_BY_START.length();

			// int end = ascciFromHex.length();
			int end = ascciFromHex.indexOf(0, start);// up to first null character
			if (end < 0) {
				return Optional.empty();
			}

			return Optional.of(ascciFromHex.substring(start, end).trim());
		} catch (Exception e) {
			logger.error("Error searching for Miner Name. ", e);
			return Optional.empty();
		}
	}
}
