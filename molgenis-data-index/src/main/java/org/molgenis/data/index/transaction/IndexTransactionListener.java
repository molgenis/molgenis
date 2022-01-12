package org.molgenis.data.index.transaction;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.index.IndexActionRegisterService;
import org.molgenis.data.index.IndexActionScheduler;
import org.molgenis.data.transaction.TransactionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexTransactionListener implements TransactionListener {
  private static final Logger LOG = LoggerFactory.getLogger(IndexTransactionListener.class);

  private IndexActionScheduler indexActionScheduler;
  private IndexActionRegisterService indexActionRegisterService;

  public IndexTransactionListener(
      IndexActionScheduler indexActionScheduler, IndexActionRegisterService indexActionRegisterService) {
    this.indexActionScheduler = requireNonNull(indexActionScheduler);
    this.indexActionRegisterService = requireNonNull(indexActionRegisterService);
  }

  @Override
  public void commitTransaction(String transactionId) {
    try {
      indexActionRegisterService.storeIndexActions(transactionId);
    } catch (Exception ex) {
      LOG.error("Error storing index actions for transaction id {}", transactionId, ex);
    }
  }

  @Override
  public void rollbackTransaction(String transactionId) {
    try {
      indexActionRegisterService.forgetIndexActions(transactionId);
    } catch (Exception ex) {
      LOG.error("Error forgetting actions for transaction id {}", transactionId, ex);
    }
  }

  @Override
  public void doCleanupAfterCompletion(String transactionId) {
    try {
      if (indexActionRegisterService.forgetIndexActions(transactionId)) {
        indexActionScheduler.scheduleIndexActions(transactionId);
      }
    } catch (Exception ex) {
      LOG.error("Error during cleanupAfterCompletion", ex);
    }
  }
}
