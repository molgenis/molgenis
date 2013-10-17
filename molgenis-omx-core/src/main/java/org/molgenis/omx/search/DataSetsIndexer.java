package org.molgenis.omx.search;

import java.util.List;

import org.molgenis.framework.db.DatabaseException;

/**
 * Indexes DataSet matrices.
 * 
 * @author erwin
 * 
 */
public interface DataSetsIndexer
{
	void index() throws DatabaseException;

	void index(List<Integer> datasetIds);

	void indexNew() throws DatabaseException;

	/**
	 * Check if there is an indexing job running
	 * 
	 * @return
	 */
	public boolean isIndexingRunning();
}
