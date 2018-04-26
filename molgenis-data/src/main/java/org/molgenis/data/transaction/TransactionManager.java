package org.molgenis.data.transaction;

import org.springframework.transaction.PlatformTransactionManager;

public interface TransactionManager extends PlatformTransactionManager
{
	String TRANSACTION_ID_RESOURCE_NAME = "transactionId";

	void addTransactionListener(TransactionListener transactionListener);
}
