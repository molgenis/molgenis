package org.molgenis.dataexplorer.search;

import java.util.List;

import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.omx.observ.DataSet;

/**
 * Indexes DataSet matrices.
 * 
 * @author erwin
 * 
 */
public interface DataSetsIndexer
{
	void index() throws DatabaseException, TableException;

	void index(List<DataSet> dataSets) throws TableException;

	void indexNew() throws DatabaseException, TableException;

	/**
	 * Check if there is an indexing job running
	 * 
	 * @return
	 */
	public boolean isIndexingRunning();
}
