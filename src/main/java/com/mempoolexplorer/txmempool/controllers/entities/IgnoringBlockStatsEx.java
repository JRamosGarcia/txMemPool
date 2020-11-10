package com.mempoolexplorer.txmempool.controllers.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mempoolexplorer.txmempool.entites.IgnoringBlock;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class IgnoringBlockStatsEx extends IgnoringBlockStats {
        @JsonProperty("mInCBn")
        private int minedAndInCandidateBlockTxs;
        @JsonProperty("mInCBw")
        private int minedAndInCandidateBlockWeight;
        @JsonProperty("minCBf")
        private long minedAndInCandidateBlockFees;
        @JsonProperty("nmInCBn")
        private int notMinedButInCandidateBlockDataTxs;
        @JsonProperty("nmInCBw")
        private int notMinedButInCandidateBlockDataWeight;
        @JsonProperty("nmInCBf")
        private long notMinedButInCandidateBlockDataFees;
        @JsonProperty("mInmnInCBn")
        private int minedInMempoolButNotInCandidateBlockDataTxs;
        @JsonProperty("mInmnInCBw")
        private int minedInMempoolButNotInCandidateBlockDataWeight;
        @JsonProperty("mInmnInCBf")
        private long minedInMempoolButNotInCandidateBlockDataFees;
        @JsonProperty("mnInMemn")
        private int minedButNotInMemPoolDataTxs;
        @JsonProperty("mnInMemw")
        private int minedButNotInMemPoolDataTxsWeight;
        @JsonProperty("mnInMemf")
        private long minedButNotInMemPoolDataTxsFees;

        public IgnoringBlockStatsEx(IgnoringBlock igBlock) {
                super(igBlock);
                this.minedAndInCandidateBlockTxs = igBlock.getMinedAndInMemPoolData().getNumTxs().orElse(-1);
                this.minedAndInCandidateBlockWeight = igBlock.getMinedAndInMemPoolData().getTotalWeight().orElse(-1);
                this.minedAndInCandidateBlockFees = igBlock.getMinedAndInMemPoolData().getTotalBaseFee().orElse(-1L);

                this.notMinedButInCandidateBlockDataTxs = igBlock.getNotMinedButInCandidateBlockData().getNumTxs()
                                .orElse(-1);
                this.notMinedButInCandidateBlockDataWeight = igBlock.getNotMinedButInCandidateBlockData()
                                .getTotalWeight().orElse(-1);
                this.notMinedButInCandidateBlockDataFees = igBlock.getNotMinedButInCandidateBlockData()
                                .getTotalBaseFee().orElse(-1L);

                this.minedInMempoolButNotInCandidateBlockDataTxs = igBlock.getMinedInMempoolButNotInCandidateBlockData()
                                .getNumTxs().orElse(-1);
                this.minedInMempoolButNotInCandidateBlockDataWeight = igBlock
                                .getMinedInMempoolButNotInCandidateBlockData().getTotalWeight().orElse(-1);
                this.minedInMempoolButNotInCandidateBlockDataFees = igBlock
                                .getMinedInMempoolButNotInCandidateBlockData().getTotalBaseFee().orElse(-1L);

                this.minedButNotInMemPoolDataTxs = igBlock.getMinedButNotInMemPoolData().getNumTxs().orElse(-1);
                this.minedButNotInMemPoolDataTxsWeight = igBlock.getMinedButNotInMemPoolData().getTotalWeight()
                                .orElse(-1);
                this.minedButNotInMemPoolDataTxsFees = igBlock.getMinedButNotInMemPoolData().getTotalBaseFee()
                                .orElse(-1L);
        }

}
