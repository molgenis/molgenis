package org.molgenis.semanticmapper.service.impl;

import java.util.stream.Stream;
import org.molgenis.semanticsearch.explain.bean.ExplainedAttribute;
import org.molgenis.semanticsearch.semantic.Hits;

/**
 * Find suitable algorithm templates for provided attribute matches returned from {@see
 * SemanticSearchService}.
 */
public interface AlgorithmTemplateService {
  /**
   * @param relevantAttributes attribute matches returned from {@see SemanticSearchService}.
   * @return algorithm templates that can be rendered using the given source and target
   */
  Stream<AlgorithmTemplate> find(Hits<ExplainedAttribute> relevantAttributes);
}
