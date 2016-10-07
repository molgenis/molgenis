package org.molgenis.data.mapper.service.impl;

import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedMatchCandidate;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Find suitable algorithm templates for provided attribute matches returned from {@see SemanticSearchService}.
 */
public interface AlgorithmTemplateService
{
	/**
	 * @param relevantAttributes attribute matches returned from {@see SemanticSearchService}.
	 * @return algorithm templates that can be rendered using the given source and target
	 */
	Stream<AlgorithmTemplate> find(
			Map<AttributeMetaData, ExplainedMatchCandidate<AttributeMetaData>> relevantAttributes);
}
