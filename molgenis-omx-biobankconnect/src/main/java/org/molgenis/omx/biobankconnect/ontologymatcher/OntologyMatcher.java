package org.molgenis.omx.biobankconnect.ontologymatcher;

import java.util.List;

import org.molgenis.data.DataService;

public interface OntologyMatcher
{
	void deleteDocumentByIds(String documentType, List<String> documentIds);

	void match(String userName, Integer selectedCatalogue, List<Integer> cataloguesToMatch, Integer sourceDataSetId);

	Integer matchPercentage();

	boolean isRunning();

	boolean checkExistingMappings(String dataSetIdentifier, DataService dataService);
}
