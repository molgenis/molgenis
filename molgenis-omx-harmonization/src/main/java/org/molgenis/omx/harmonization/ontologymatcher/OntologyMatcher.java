package org.molgenis.omx.harmonization.ontologymatcher;

import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;

public interface OntologyMatcher
{
	void deleteDocumentByIds(String documentType, List<String> documentIds);

	void match(Integer selectedCatalogue, List<Integer> cataloguesToMatch) throws DatabaseException;

	Double matchPercentage();

	boolean isRunning();

	boolean checkExistingMappings(String dataSetIdentifier, Database db) throws DatabaseException;
}
