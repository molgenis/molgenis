package org.molgenis.data.elasticsearch.transaction;

import org.molgenis.data.transaction.MolgenisTransactionListener;

public abstract class DefaultMolgenisTransactionListener implements MolgenisTransactionListener
{
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

	}
}
