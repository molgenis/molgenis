package org.molgenis.data.security.audit;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.security.audit.AuthenticationUtils.getUsername;
import static org.molgenis.data.security.audit.AuthenticationUtils.isUser;

import java.util.Map;
import org.molgenis.audit.AuditEventPublisher;
import org.molgenis.data.transaction.TransactionListener;
import org.molgenis.data.transaction.TransactionManager;
import org.springframework.stereotype.Component;

@Component
public class AuditTransactionListener implements TransactionListener {

  static final String TRANSACTION_FAILURE = "TRANSACTION_FAILURE";
  static final String TRANSACTION_SUCCESS = "TRANSACTION_SUCCESS";
  static final String TRANSACTION_ID = "transactionId";

  private final AuditEventPublisher auditEventPublisher;

  public AuditTransactionListener(
      TransactionManager transactionManager, AuditEventPublisher auditEventPublisher) {
    requireNonNull(transactionManager).addTransactionListener(this);
    this.auditEventPublisher = requireNonNull(auditEventPublisher);
  }

  @Override
  public void rollbackTransaction(String transactionId) {
    if (isUser()) {
      auditEventPublisher.publish(
          getUsername(), TRANSACTION_FAILURE, Map.of(TRANSACTION_ID, transactionId));
    }
  }

  @Override
  public void afterCommitTransaction(String transactionId) {
    if (isUser()) {
      auditEventPublisher.publish(
          getUsername(), TRANSACTION_SUCCESS, Map.of(TRANSACTION_ID, transactionId));
    }
  }
}
