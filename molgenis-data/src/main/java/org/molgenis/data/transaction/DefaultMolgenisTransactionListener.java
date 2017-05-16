package org.molgenis.data.transaction;

public abstract class DefaultMolgenisTransactionListener implements TransactionListener
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
