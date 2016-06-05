package org.molgenis.data.elasticsearch.transaction;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.elasticsearch.reindex.job.ReindexService;
import org.molgenis.data.reindex.ReindexActionRegisterService;

public class ReindexTransactionListener extends DefaultMolgenisTransactionListener
{
	private ReindexService rebuildIndexService;
	private ReindexActionRegisterService reindexActionRegisterService;

	public ReindexTransactionListener(ReindexService rebuildIndexService,
			ReindexActionRegisterService reindexActionRegisterService)
	{
		this.rebuildIndexService = requireNonNull(rebuildIndexService);
		this.reindexActionRegisterService = requireNonNull(reindexActionRegisterService);
	}

	@Override
	public void commitTransaction(String transactionId)
	{
		reindexActionRegisterService.storeReindexActions(transactionId);
	}

	@Override
	public void rollbackTransaction(String transactionId)
	{
		reindexActionRegisterService.forgetReindexActions(transactionId);
	}

	@Override
	public void doCleanupAfterCompletion(String transactionId)
	{
		rebuildIndexService.rebuildIndex(transactionId);
	}
}
