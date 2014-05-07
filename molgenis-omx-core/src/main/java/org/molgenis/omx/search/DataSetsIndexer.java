package org.molgenis.omx.search;

import java.util.List;

/**
 * Indexes DataSet matrices.
 * 
 * @author erwin
 * 
 */
public interface DataSetsIndexer
{
	void index();

	void indexDataSets(List<Object> datasetIds);

	void indexProtocols(List<Object> protocolIds);

	/**
	 * Check if there is an indexing job running
	 * 
	 * @return
	 */
	public boolean isIndexingRunning();

	void indexProtocolsSynced(List<Object> protocolIds);
}
