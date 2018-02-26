package org.molgenis.data.validation.meta.model;

import org.molgenis.data.AbstractSystemRepositoryDecoratorFactory;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.security.meta.RowLevelSecured;
import org.molgenis.data.security.meta.RowLevelSecuredMetadata;
import org.molgenis.data.security.owned.RowLevelSecurityEntityDecorator;
import org.molgenis.data.security.user.UserService;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/**
 * Due to a circular dependency this decorator factory is not stored in molgenis-data.
 */
@Component
public class RowLevelSecurityRepositoryDecoratorFactory
		extends AbstractSystemRepositoryDecoratorFactory<RowLevelSecured, RowLevelSecuredMetadata>
{
	private final DataService dataService;
	private final UserService userService;
	private final MutableAclService mutableAclService;

	public RowLevelSecurityRepositoryDecoratorFactory(DataService dataService,
			RowLevelSecuredMetadata entityTypeMetadata, UserService userService, MutableAclService mutableAclService)
	{
		super(entityTypeMetadata);
		this.dataService = requireNonNull(dataService);
		this.mutableAclService = requireNonNull(mutableAclService);
		this.userService = requireNonNull(userService);
	}

	@Override
	public Repository<RowLevelSecured> createDecoratedRepository(Repository<RowLevelSecured> repository)
	{
		return new RowLevelSecurityEntityDecorator(repository, mutableAclService, dataService, userService);
	}
}
