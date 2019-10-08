package org.molgenis.security.acl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.acls.model.AclCache;

class AclCacheTransactionListenerTest extends AbstractMockitoTest {
  @Mock private AclCache aclCache;
  @Mock private MutableAclClassService mutableAclClassService;
  private AclCacheTransactionListener aclCacheTransactionListener;

  @BeforeEach
  void setUpBeforeMethod() {
    aclCacheTransactionListener = new AclCacheTransactionListener(aclCache, mutableAclClassService);
  }

  @Test
  void testAclCacheTransactionListener() {
    assertThrows(
        NullPointerException.class,
        () -> new AclCacheTransactionListener(null, mutableAclClassService));
  }

  @Test
  void testRollbackTransaction() {
    aclCacheTransactionListener.rollbackTransaction("transactionId");
    verify(aclCache).clearCache();
    verify(mutableAclClassService).clearCache();
  }
}
