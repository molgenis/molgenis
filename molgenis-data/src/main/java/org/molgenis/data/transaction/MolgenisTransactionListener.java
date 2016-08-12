package org.molgenis.data.transaction;

/**
 * Interface that can be implemented by classes that want to join transactions.
 * <p>
 * A MolgenisTransactionListener must bootstrap itself by the MolgenisTransactionManager
 */
public interface MolgenisTransactionListener
{
	void transactionStarted(String transactionId);

	void commitTransaction(String transactionId);

	void afterCommitTransaction(String transactionId);

	void rollbackTransaction(String transactionId);

	void doCleanupAfterCompletion(String transactionId);

}
