package org.molgenis.omx.biobankconnect.ontologymatcher;

import java.util.List;
import java.util.Map;

import org.molgenis.data.DataService;
import org.molgenis.search.SearchResult;

public interface OntologyMatcher
{
	void deleteDocumentByIds(String documentType, List<String> documentIds);

	void match(String userName, Integer selectedCatalogue, List<Integer> cataloguesToMatch, Integer sourceDataSetId);

	Integer matchPercentage(String currentUserName);

	boolean isRunning();

	boolean checkExistingMappings(String dataSetIdentifier, DataService dataService);

	SearchResult generateMapping(String userName, Integer selectedDataSet, Integer dataSetsToMatch, Integer featureId);

	Map<String, String> updateScript(String userName, OntologyMatcherRequest request);
}
