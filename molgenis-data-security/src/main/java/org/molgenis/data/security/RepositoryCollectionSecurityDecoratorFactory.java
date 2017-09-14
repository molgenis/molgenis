package org.molgenis.data.security;

import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.security.acl.EntityAclManager;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
public class RepositoryCollectionSecurityDecoratorFactory
{
	private final EntityAclManager entityAclManager;
	private final PermissionService permissionService;

	RepositoryCollectionSecurityDecoratorFactory(EntityAclManager entityAclManager, PermissionService permissionService)
	{
		this.entityAclManager = requireNonNull(entityAclManager);
		this.permissionService = requireNonNull(permissionService);
	}

	public RepositoryCollectionSecurityDecorator create(RepositoryCollection repositoryCollection)
	{
		return new RepositoryCollectionSecurityDecorator(repositoryCollection, entityAclManager, permissionService);
	}
}
