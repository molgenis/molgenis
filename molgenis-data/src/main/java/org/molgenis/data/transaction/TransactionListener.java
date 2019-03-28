package org.molgenis.data.transaction;

/**
 * Interface that can be implemented by classes that want to join transactions.
 *
 * <p>A TransactionListener must bootstrap itself by the TransactionManager
 */
public interface TransactionListener {
  default void transactionStarted(String transactionId) {}

  default void commitTransaction(String transactionId) {}

  default void afterCommitTransaction(String transactionId) {}

  default void rollbackTransaction(String transactionId) {}

  default void doCleanupAfterCompletion(String transactionId) {}
}
