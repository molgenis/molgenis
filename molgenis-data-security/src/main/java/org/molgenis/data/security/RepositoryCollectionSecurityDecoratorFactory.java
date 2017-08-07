package org.molgenis.data.security;

import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.security.acl.EntityAclManager;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
public class RepositoryCollectionSecurityDecoratorFactory
{
	private final EntityAclManager entityAclManager;

	RepositoryCollectionSecurityDecoratorFactory(EntityAclManager entityAclManager)
	{
		this.entityAclManager = requireNonNull(entityAclManager);
	}

	public RepositoryCollectionSecurityDecorator create(RepositoryCollection repositoryCollection)
	{
		return new RepositoryCollectionSecurityDecorator(repositoryCollection, entityAclManager);
	}
}
