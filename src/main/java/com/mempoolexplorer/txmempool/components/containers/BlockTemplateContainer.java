package com.mempoolexplorer.txmempool.components.containers;

import com.mempoolexplorer.txmempool.entites.blocktemplate.BlockTemplate;
import com.mempoolexplorer.txmempool.entites.blocktemplate.BlockTemplateChanges;

public interface BlockTemplateContainer {

	BlockTemplate getBlockTemplate();

	void setBlockTemplate(BlockTemplate bt);

	void refresh(BlockTemplateChanges btc);

	void drop();

}