package com.mempoolexplorer.txmempool.controllers.entities;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CandidateBlockHistogram {

	List<SatVByteHistogramElement> histogramList = new ArrayList<>();

}
