package org.molgenis.data.security.audit;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.molgenis.data.security.audit.AuditTransactionListener.TRANSACTION_FAILURE;
import static org.molgenis.data.security.audit.SecurityContextTestUtils.withSystemToken;
import static org.molgenis.data.security.audit.SecurityContextTestUtils.withUser;

import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.audit.AuditEventPublisher;
import org.molgenis.data.transaction.TransactionManager;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

class AuditTransactionListenerTest extends AbstractMockitoTest {

  @Mock private AuditEventPublisher auditEventPublisher;
  @Mock private TransactionManager transactionManager;

  private AuditTransactionListener listener;
  private SecurityContext previousContext;

  @BeforeEach
  void beforeEach() {
    previousContext = SecurityContextHolder.getContext();
    SecurityContext testContext = SecurityContextHolder.createEmptyContext();
    SecurityContextHolder.setContext(testContext);

    listener = new AuditTransactionListener(transactionManager, auditEventPublisher);
  }

  @AfterEach
  void tearDownAfterEach() {
    SecurityContextHolder.setContext(previousContext);
  }

  @Test
  void testRollbackTransaction() {
    withUser("bofke");
    listener.rollbackTransaction("id");
    verify(auditEventPublisher)
        .publish("bofke", TRANSACTION_FAILURE, Map.of("transactionId", "id"));
  }

  @Test
  void testRollbackTransactionAsSystem() {
    withSystemToken();
    listener.rollbackTransaction("id");
    verifyNoInteractions(auditEventPublisher);
  }
}
