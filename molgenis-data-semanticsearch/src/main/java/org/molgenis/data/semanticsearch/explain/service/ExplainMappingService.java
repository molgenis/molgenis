package org.molgenis.data.semanticsearch.explain.service;

import org.molgenis.data.semanticsearch.explain.bean.ExplainedMatchCandidate;
import org.molgenis.data.semanticsearch.service.bean.SearchParam;

public interface ExplainMappingService
{
	/**
	 * Explains why the given source attribute is matched to the target attribute using all the information available
	 * including the query terms from the target attribute, user defined queries, ontology terms and their children.
	 *
	 * @param searchParam
	 * @param matchedCandidate
	 * @return {@link ExplainedMatchCandidate<String>}
	 */
	ExplainedMatchCandidate<String> explainMapping(SearchParam searchParam, String matchedCandidate);
}
