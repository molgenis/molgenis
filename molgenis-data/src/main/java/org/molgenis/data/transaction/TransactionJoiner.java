package org.molgenis.data.transaction;

public interface TransactionJoiner
{
	void transactionStarted(String transactionId);

	void commitTransaction(String transactionId);

	void rollbackTransaction(String transactionId);
}
