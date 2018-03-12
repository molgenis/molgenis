package org.molgenis.security.acl;

import org.molgenis.data.transaction.DefaultMolgenisTransactionListener;
import org.springframework.security.acls.model.AclCache;

import static java.util.Objects.requireNonNull;

public class AclCacheTransactionListener extends DefaultMolgenisTransactionListener
{
	private final AclCache aclCache;

	public AclCacheTransactionListener(AclCache aclCache)
	{
		this.aclCache = requireNonNull(aclCache);
	}

	@Override
	public void rollbackTransaction(String transactionId)
	{
		aclCache.clearCache();
	}
}
