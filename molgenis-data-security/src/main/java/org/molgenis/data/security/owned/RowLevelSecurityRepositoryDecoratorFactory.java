package org.molgenis.data.security.owned;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.security.EntityIdentityUtils;
import org.molgenis.security.acl.MutableAclClassService;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/**
 * @see RowLevelSecurityRepositoryDecorator
 */
@Component
public class RowLevelSecurityRepositoryDecoratorFactory
{
	private final UserPermissionEvaluator userPermissionEvaluator;
	private final MutableAclService mutableAclService;
	private final MutableAclClassService mutableAclClassService;

	RowLevelSecurityRepositoryDecoratorFactory(UserPermissionEvaluator userPermissionEvaluator,
			MutableAclService mutableAclService, MutableAclClassService mutableAclClassService)
	{
		this.userPermissionEvaluator = requireNonNull(userPermissionEvaluator);
		this.mutableAclService = requireNonNull(mutableAclService);
		this.mutableAclClassService = requireNonNull(mutableAclClassService);
	}

	public Repository<Entity> createDecoratedRepository(Repository<Entity> repository)
	{
		Repository<Entity> decoratedRepository;
		if (isRowLevelSecured(repository))
		{
			decoratedRepository = new RowLevelSecurityRepositoryDecorator(repository, userPermissionEvaluator,
					mutableAclService);
		}
		else
		{
			decoratedRepository = repository;
		}
		return decoratedRepository;
	}

	private boolean isRowLevelSecured(Repository<Entity> repository)
	{
		String aclClass = EntityIdentityUtils.toType(repository.getEntityType());
		return mutableAclClassService.hasAclClass(aclClass);
	}
}
