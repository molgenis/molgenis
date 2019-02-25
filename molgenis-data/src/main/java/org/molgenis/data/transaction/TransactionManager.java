package org.molgenis.data.transaction;

import org.springframework.transaction.PlatformTransactionManager;

public interface TransactionManager extends PlatformTransactionManager {

  void addTransactionListener(TransactionListener transactionListener);
}
