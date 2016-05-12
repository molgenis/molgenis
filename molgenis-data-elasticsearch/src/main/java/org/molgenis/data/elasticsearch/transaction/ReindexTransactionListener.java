package org.molgenis.data.elasticsearch.transaction;

import org.molgenis.data.elasticsearch.reindex.job.RebuildIndexService;
import org.molgenis.data.transaction.MolgenisTransactionListener;

public class ReindexTransactionListener implements MolgenisTransactionListener
{
	private RebuildIndexService rebuildIndexService;

	public ReindexTransactionListener(RebuildIndexService rebuildIndexService)
	{
		this.rebuildIndexService = rebuildIndexService;
	}

	@Override
	public void transactionStarted(String transactionId)
	{
	}

	@Override
	public void commitTransaction(String transactionId)
	{
	}

	@Override
	public void afterCommitTransaction(String transactionId)
	{
	}

	@Override
	public void rollbackTransaction(String transactionId)
	{
	}

	@Override
	public void doCleanupAfterCompletion(String transactionId)
	{
		rebuildIndexService.rebuildIndex(transactionId);
	}
}
