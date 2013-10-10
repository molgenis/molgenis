package org.molgenis.omx.biobankconnect.ontologymatcher;

import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;

public interface OntologyMatcher
{
	void deleteDocumentByIds(String documentType, List<String> documentIds);

	void match(Integer selectedCatalogue, List<Integer> cataloguesToMatch) throws DatabaseException;

	Integer matchPercentage();

	boolean isRunning();

	boolean checkExistingMappings(String dataSetIdentifier, Database db) throws DatabaseException;
}
