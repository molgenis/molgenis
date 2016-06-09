package org.molgenis.data.elasticsearch.transaction;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.elasticsearch.reindex.job.ReindexService;
import org.molgenis.data.reindex.ReindexActionRegisterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReindexTransactionListener extends DefaultMolgenisTransactionListener
{
	private ReindexService rebuildIndexService;
	private ReindexActionRegisterService reindexActionRegisterService;

	private static final Logger LOG = LoggerFactory.getLogger(DefaultMolgenisTransactionListener.class);

	public ReindexTransactionListener(ReindexService rebuildIndexService,
			ReindexActionRegisterService reindexActionRegisterService)
	{
		this.rebuildIndexService = requireNonNull(rebuildIndexService);
		this.reindexActionRegisterService = requireNonNull(reindexActionRegisterService);
	}

	@Override
	public void doCleanupAfterCompletion(String transactionId)
	{
		try
		{
			rebuildIndexService.rebuildIndex(transactionId);
		}
		catch (Exception ex)
		{
			LOG.error("Error during cleanupAfterCompletion");
		}

	}
}
