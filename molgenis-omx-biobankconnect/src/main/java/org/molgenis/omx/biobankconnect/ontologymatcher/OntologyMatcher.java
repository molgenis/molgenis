package org.molgenis.omx.biobankconnect.ontologymatcher;

import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;

public interface OntologyMatcher
{
	void deleteDocumentByIds(String documentType, List<String> documentIds);

	void match(String userName, Integer selectedCatalogue, List<Integer> cataloguesToMatch, Integer sourceDataSetId)
			throws DatabaseException;

	Integer matchPercentage();

	boolean isRunning();

	boolean checkExistingMappings(String dataSetIdentifier, Database db) throws DatabaseException;
}
