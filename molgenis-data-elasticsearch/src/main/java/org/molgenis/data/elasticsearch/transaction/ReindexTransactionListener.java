package org.molgenis.data.elasticsearch.transaction;

import org.molgenis.data.elasticsearch.reindex.job.RebuildIndexService;
import org.molgenis.data.transaction.MolgenisTransactionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReindexTransactionListener implements MolgenisTransactionListener
{
	private RebuildIndexService rebuildIndexService;

	private static final Logger LOG = LoggerFactory.getLogger(ReindexTransactionListener.class);

	public ReindexTransactionListener(RebuildIndexService rebuildIndexService)
	{
		this.rebuildIndexService = rebuildIndexService;
	}

	@Override
	public void transactionStarted(String transactionId)
	{
		// LOG.info("transactionStarted [{}]", transactionId);
	}

	@Override
	public void commitTransaction(String transactionId) {}

	@Override
	public void afterCommitTransaction(String transactionId)
	{

	}

	@Override
	public void rollbackTransaction(String transactionId)
	{
		// LOG.info("rollbackTransaction [{}]", transactionId);
	}

	@Override
	public void doCleanupAfterCompletion(String transactionId)
	{
		rebuildIndexService.rebuildIndex(transactionId);
	}
}
