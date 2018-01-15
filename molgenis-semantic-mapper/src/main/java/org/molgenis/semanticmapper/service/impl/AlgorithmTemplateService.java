package org.molgenis.semanticmapper.service.impl;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.semanticsearch.explain.bean.ExplainedAttribute;

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
	Stream<AlgorithmTemplate> find(Map<Attribute, ExplainedAttribute> relevantAttributes);
}
