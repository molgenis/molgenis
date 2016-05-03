package org.molgenis.data.elasticsearch.reindex.job;

public interface RebuildIndexService
{
	void rebuildIndex(String transactionId);
}
