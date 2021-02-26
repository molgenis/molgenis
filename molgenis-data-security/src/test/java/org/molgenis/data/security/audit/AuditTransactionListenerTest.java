package org.molgenis.data.security.audit;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.molgenis.data.security.audit.AuditTransactionListener.TRANSACTION_FAILURE;

import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.audit.AuditEventPublisher;
import org.molgenis.data.transaction.TransactionManager;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = AuditTransactionListenerTest.Config.class)
class AuditTransactionListenerTest extends AbstractMockitoSpringContextTests {

  @Configuration
  static class Config {}

  @Mock private AuditEventPublisher auditEventPublisher;
  @Mock private TransactionManager transactionManager;

  private AuditTransactionListener listener;
  private SecurityContext previousContext;

  @BeforeEach
  void beforeEach() {
    previousContext = SecurityContextHolder.getContext();
    listener = new AuditTransactionListener(transactionManager, auditEventPublisher);
  }

  @AfterEach
  void tearDownAfterEach() {
    SecurityContextHolder.setContext(previousContext);
  }

  @Test
  @WithMockUser("bofke")
  void testRollbackTransaction() {
    listener.rollbackTransaction("id");
    verify(auditEventPublisher)
        .publish("bofke", TRANSACTION_FAILURE, Map.of("transactionId", "id"));
  }

  @Test
  @WithMockSystemUser
  void testRollbackTransactionAsSystem() {
    listener.rollbackTransaction("id");
    verifyNoInteractions(auditEventPublisher);
  }
}
