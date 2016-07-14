package org.molgenis.data.elasticsearch.transaction;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.elasticsearch.reindex.job.ReindexService;
import org.molgenis.data.reindex.ReindexActionRegisterService;
import org.molgenis.data.transaction.DefaultMolgenisTransactionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReindexTransactionListener extends DefaultMolgenisTransactionListener
{
	private static final Logger LOG = LoggerFactory.getLogger(ReindexTransactionListener.class);

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
		try
		{
			reindexActionRegisterService.storeReindexActions(transactionId);
		}
		catch (Exception ex)
		{
			LOG.error("Error storing reindex actions for transaction id {}", transactionId);
		}
	}

	@Override
	public void rollbackTransaction(String transactionId)
	{
		try
		{
			reindexActionRegisterService.forgetReindexActions(transactionId);
		}
		catch(Exception ex)
		{
			LOG.error("Error forgetting actions for transaction id {}", transactionId);
		}
	}

	@Override
	public void doCleanupAfterCompletion(String transactionId)
	{
		try
		{
			if (reindexActionRegisterService.forgetReindexActions(transactionId))
			{
				rebuildIndexService.rebuildIndex(transactionId);
			}
		}
		catch (Exception ex)
		{
			LOG.error("Error during cleanupAfterCompletion");
		}
	}
}
