package org.molgenis.data.security.owned;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.security.user.UserService;
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
	private final UserService userService;

	RowLevelSecurityRepositoryDecoratorFactory(UserPermissionEvaluator userPermissionEvaluator,
			MutableAclService mutableAclService, MutableAclClassService mutableAclClassService, UserService userService)
	{
		this.userPermissionEvaluator = requireNonNull(userPermissionEvaluator);
		this.mutableAclService = requireNonNull(mutableAclService);
		this.mutableAclClassService = requireNonNull(mutableAclClassService);
		this.userService = requireNonNull(userService);
	}

	public Repository<Entity> createDecoratedRepository(Repository<Entity> repository)
	{
		return new RowLevelSecurityRepositoryDecorator(repository, userPermissionEvaluator, mutableAclService,
				mutableAclClassService, userService);
	}
}
