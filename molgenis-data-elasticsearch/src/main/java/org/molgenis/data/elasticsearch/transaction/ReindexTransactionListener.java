package org.molgenis.data.elasticsearch.transaction;

import org.molgenis.data.elasticsearch.reindex.job.ReindexService;

public class ReindexTransactionListener extends DefaultMolgenisTransactionListener
{
	private ReindexService rebuildIndexService;

	public ReindexTransactionListener(ReindexService rebuildIndexService)
	{
		this.rebuildIndexService = rebuildIndexService;
	}

	@Override
	public void doCleanupAfterCompletion(String transactionId)
	{
		// if the transaction was rolled back, so has the insert of the ReindexJob
		rebuildIndexService.rebuildIndex(transactionId);
	}
}
