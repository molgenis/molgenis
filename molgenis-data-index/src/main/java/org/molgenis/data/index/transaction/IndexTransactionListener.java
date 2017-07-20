package org.molgenis.data.index.transaction;

import org.molgenis.data.index.IndexActionRegisterService;
import org.molgenis.data.index.job.IndexJobScheduler;
import org.molgenis.data.transaction.DefaultMolgenisTransactionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

public class IndexTransactionListener extends DefaultMolgenisTransactionListener
{
	private static final Logger LOG = LoggerFactory.getLogger(IndexTransactionListener.class);

	private IndexJobScheduler indexJobScheduler;
	private IndexActionRegisterService indexActionRegisterService;

	public IndexTransactionListener(IndexJobScheduler indexJobScheduler,
			IndexActionRegisterService indexActionRegisterService)
	{
		this.indexJobScheduler = requireNonNull(indexJobScheduler);
		this.indexActionRegisterService = requireNonNull(indexActionRegisterService);
	}

	@Override
	public void commitTransaction(String transactionId)
	{
		try
		{
			indexActionRegisterService.storeIndexActions(transactionId);
		}
		catch (Exception ex)
		{
			LOG.error("Error storing index actions for transaction id {}", transactionId, ex);
		}
	}

	@Override
	public void rollbackTransaction(String transactionId)
	{
		try
		{
			indexActionRegisterService.forgetIndexActions(transactionId);
		}
		catch (Exception ex)
		{
			LOG.error("Error forgetting actions for transaction id {}", transactionId, ex);
		}
	}

	@Override
	public void doCleanupAfterCompletion(String transactionId)
	{
		try
		{
			if (indexActionRegisterService.forgetIndexActions(transactionId))
			{
				indexJobScheduler.scheduleIndexJob(transactionId);
			}
		}
		catch (Exception ex)
		{
			LOG.error("Error during cleanupAfterCompletion", ex);
		}
	}
}
