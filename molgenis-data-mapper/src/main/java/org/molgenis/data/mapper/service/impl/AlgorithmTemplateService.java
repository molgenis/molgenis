package org.molgenis.data.mapper.service.impl;

import java.util.Map;
import java.util.stream.Stream;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedQueryString;

/**
 * Find suitable algorithm templates for provided attribute matches returned from {@see SemanticSearchService}.
 */
public interface AlgorithmTemplateService
{
	/**
	 * @param attrMatches
	 *            attribute matches returned from {@see SemanticSearchService}.
	 * @return algorithm templates that can be rendered using the given source and target
	 */
	Stream<AlgorithmTemplate> find(Map<AttributeMetaData, Iterable<ExplainedQueryString>> attrMatches);
}
