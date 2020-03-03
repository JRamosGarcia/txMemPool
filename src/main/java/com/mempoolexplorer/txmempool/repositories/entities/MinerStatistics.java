package com.mempoolexplorer.txmempool.repositories.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
@Document(collection = "minerStatistics")
public class MinerStatistics {

	@Id
	private String minerName;
	private Long totalLostRewardBT;
	private Long totalLostRewardCB;
	private Integer numBlocksMined;

}
