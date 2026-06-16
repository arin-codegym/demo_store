package com.quochuy.ai.product;

import com.quochuy.ai.product.dto.ExternalProductContext;
import com.quochuy.ai.product.dto.ExternalProductResearchRequest;

public interface ExternalProductResearchService {
	ExternalProductContext search(ExternalProductResearchRequest request);
}
