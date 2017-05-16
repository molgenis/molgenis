package org.molgenis.data.transaction;

/**
 * Interface that can be implemented by classes that want to join transactions.
 * <p>
 * A TransactionListener must bootstrap itself by the TransactionManager
 */
public interface TransactionListener
{
	void transactionStarted(String transactionId);

	void commitTransaction(String transactionId);

	void afterCommitTransaction(String transactionId);

	void rollbackTransaction(String transactionId);

	void doCleanupAfterCompletion(String transactionId);
}
