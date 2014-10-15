package org.molgenis.ontology.search;

import org.molgenis.data.elasticsearch.util.SearchResult;

public interface OmxSemanticSearch
{
	SearchResult generateMapping(String userName, Integer selectedDataSet, Integer dataSetsToMatch, Integer featureId);
}
