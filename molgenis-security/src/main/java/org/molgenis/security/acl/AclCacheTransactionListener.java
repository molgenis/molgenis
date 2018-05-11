package org.molgenis.security.acl;

import org.molgenis.data.transaction.DefaultMolgenisTransactionListener;
import org.springframework.security.acls.model.AclCache;

import static java.util.Objects.requireNonNull;

public class AclCacheTransactionListener extends DefaultMolgenisTransactionListener
{
	private final AclCache aclCache;
	private final MutableAclClassService aclClassService;

	public AclCacheTransactionListener(AclCache aclCache, MutableAclClassService aclClassService)
	{
		this.aclCache = requireNonNull(aclCache);
		this.aclClassService = requireNonNull(aclClassService);
	}

	@Override
	public void rollbackTransaction(String transactionId)
	{
		aclCache.clearCache();
		aclClassService.clearCache();
	}
}
