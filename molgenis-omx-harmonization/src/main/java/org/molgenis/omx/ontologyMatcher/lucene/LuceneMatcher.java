package org.molgenis.omx.ontologyMatcher.lucene;

import java.util.List;
import java.util.Set;

import org.molgenis.framework.db.DatabaseException;

public interface LuceneMatcher
{
	void deleteDocumentByIds(String documentType, List<String> documentIds);

	void match(Integer selectedCatalogue, Set<Integer> cataloguesToMatch) throws DatabaseException;

	void matchPercentage();

	boolean isRunning();
}
