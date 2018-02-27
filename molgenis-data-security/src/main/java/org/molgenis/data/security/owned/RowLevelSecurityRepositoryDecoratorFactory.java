package org.molgenis.data.security.owned;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.security.user.UserService;
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
	private final DataService dataService;
	private final UserPermissionEvaluator userPermissionEvaluator;
	private final MutableAclService mutableAclService;
	private final UserService userService;

	RowLevelSecurityRepositoryDecoratorFactory(DataService dataService, UserPermissionEvaluator userPermissionEvaluator,
			MutableAclService mutableAclService, UserService userService)
	{
		this.dataService = requireNonNull(dataService);
		this.userPermissionEvaluator = requireNonNull(userPermissionEvaluator);
		this.mutableAclService = requireNonNull(mutableAclService);
		this.userService = requireNonNull(userService);
	}

	public Repository<Entity> createDecoratedRepository(Repository<Entity> repository)
	{
		return new RowLevelSecurityRepositoryDecorator(repository, dataService, userPermissionEvaluator,
				mutableAclService, userService);
	}
}
