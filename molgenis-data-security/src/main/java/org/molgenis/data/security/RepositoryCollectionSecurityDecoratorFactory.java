package org.molgenis.data.security;

import org.molgenis.data.RepositoryCollection;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
public class RepositoryCollectionSecurityDecoratorFactory
{
	private final MutableAclService mutableAclService;
	private final UserPermissionEvaluator userPermissionEvaluator;

	public RepositoryCollectionSecurityDecoratorFactory(MutableAclService mutableAclService,
			UserPermissionEvaluator userPermissionEvaluator)
	{
		this.mutableAclService = requireNonNull(mutableAclService);
		this.userPermissionEvaluator = requireNonNull(userPermissionEvaluator);
	}

	public RepositoryCollection createDecoratedRepositoryCollection(RepositoryCollection repositoryCollection)
	{
		return new RepositoryCollectionSecurityDecorator(repositoryCollection, mutableAclService,
				userPermissionEvaluator);
	}
}