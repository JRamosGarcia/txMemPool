package com.mempoolexplorer.txmempool.components;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.mempoolexplorer.txmempool.utils.SysProps;

@Component
public class MinerNameResolverImpl implements MinerNameResolver {

	private List<String> minerNames = new ArrayList<>();

	public MinerNameResolverImpl() {
		minerNames.add("AntPool");
	}

	@Override
	public String resolveFrom(String coinBaseField) {
		for (String minerName : minerNames) {
			if (coinBaseField.contains(minerName)) {
				return minerName;
			}
		}
		return SysProps.MINER_NAME_UNKNOWN;
	}
}
