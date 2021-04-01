package org.molgenis.data.security.audit;

import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserIsUser;
import static org.molgenis.security.core.utils.SecurityUtils.getActualUsername;

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

  private final AuditEventPublisher auditEventPublisher;

  public AuditTransactionListener(
      TransactionManager transactionManager, AuditEventPublisher auditEventPublisher) {
    requireNonNull(transactionManager).addTransactionListener(this);
    this.auditEventPublisher = requireNonNull(auditEventPublisher);
  }

  @Override
  public void rollbackTransaction(String transactionId) {
    if (currentUserIsUser()) {
      auditEventPublisher.publish(getActualUsername(), TRANSACTION_FAILURE, emptyMap());
    }
  }
}
