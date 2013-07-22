package org.molgenis.omx.ontologyMatcher.lucene;

import java.util.Set;

import org.molgenis.framework.db.DatabaseException;

public interface LuceneMatcher
{
	void match(Integer selectedCatalogue, Set<Integer> cataloguesToMatch) throws DatabaseException;

	void matchPercentage();

	boolean isRunning();
}
