package com.mempoolexplorer.txmempool.components.containers;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.blocktemplate.BlockTemplateChanges;
import com.mempoolexplorer.txmempool.entites.blocktemplate.BlockTemplate;

public interface BlockTemplateContainer {

	BlockTemplate getBlockTemplate();

	void setBlockTemplate(BlockTemplate bt);

	void refresh(BlockTemplateChanges btc);

	void drop();

}