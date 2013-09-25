package org.molgenis.omx.search;

import java.util.List;

import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.tupletable.TableException;

/**
 * Indexes DataSet matrices.
 * 
 * @author erwin
 * 
 */
public interface DataSetsIndexer
{
	void index() throws DatabaseException, TableException;

	void index(List<Integer> datasetIds) throws TableException;

	void indexNew() throws DatabaseException, TableException;

	/**
	 * Check if there is an indexing job running
	 * 
	 * @return
	 */
	public boolean isIndexingRunning();
}
