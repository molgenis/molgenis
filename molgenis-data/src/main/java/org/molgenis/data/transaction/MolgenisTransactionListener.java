package org.molgenis.data.transaction;

/**
 * Interface that can be implemented by classes that want to join transactions.
 * 
 * A MolgenisTransactionListener must register itself by the MolgenisTransactionManager
 */
public interface MolgenisTransactionListener
{
	void transactionStarted(String transactionId);

	void commitTransaction(String transactionId);

	void rollbackTransaction(String transactionId);
}
