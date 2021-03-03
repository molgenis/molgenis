package org.molgenis.data.security.audit;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.security.audit.AuthenticationUtils.getUsername;
import static org.molgenis.data.security.audit.AuthenticationUtils.isRunByUser;

import java.util.Map;
import org.molgenis.audit.AuditEventPublisher;
import org.molgenis.data.transaction.TransactionListener;
import org.molgenis.data.transaction.TransactionManager;
import org.springframework.stereotype.Component;

/**
 * Publishes audit events with transaction ids so that they can be correlated to other audit events
 * that were published during that transaction.
 */
@Component
public class AuditTransactionListener implements TransactionListener {

  static final String TRANSACTION_FAILURE = "TRANSACTION_FAILURE";
  static final String TRANSACTION_ID = "transactionId";

  private final AuditEventPublisher auditEventPublisher;

  public AuditTransactionListener(
      TransactionManager transactionManager, AuditEventPublisher auditEventPublisher) {
    requireNonNull(transactionManager).addTransactionListener(this);
    this.auditEventPublisher = requireNonNull(auditEventPublisher);
  }

  @Override
  public void rollbackTransaction(String transactionId) {
    if (isRunByUser()) {
      auditEventPublisher.publish(
          getUsername(), TRANSACTION_FAILURE, Map.of(TRANSACTION_ID, transactionId));
    }
  }
}
