package org.molgenis.security.acl;

import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.acls.model.AclCache;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.verify;

public class AclCacheTransactionListenerTest extends AbstractMockitoTest
{
	@Mock
	private AclCache aclCache;
	private AclCacheTransactionListener aclCacheTransactionListener;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		aclCacheTransactionListener = new AclCacheTransactionListener(aclCache);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testAclCacheTransactionListener()
	{
		new AclCacheTransactionListener(null);
	}

	@Test
	public void testRollbackTransaction()
	{
		aclCacheTransactionListener.rollbackTransaction("transactionId");
		verify(aclCache).clearCache();
	}
}