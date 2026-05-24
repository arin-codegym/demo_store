package com.quochuy.ai.rag.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class RagRetrieveResult {
	private boolean matched;
	private double score;
	private List<RagChunk> chunks;
}
