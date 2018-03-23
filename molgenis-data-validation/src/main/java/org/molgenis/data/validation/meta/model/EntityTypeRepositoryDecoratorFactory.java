package org.molgenis.data.validation.meta.model;

import org.molgenis.data.AbstractSystemRepositoryDecoratorFactory;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.EntityTypeDependencyResolver;
import org.molgenis.data.meta.EntityTypeRepositoryDecorator;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.system.SystemEntityTypeRegistry;
import org.molgenis.data.security.meta.EntityTypeRepositorySecurityDecorator;
import org.molgenis.data.validation.meta.EntityTypeRepositoryValidationDecorator;
import org.molgenis.data.validation.meta.EntityTypeValidator;
import org.molgenis.security.acl.MutableAclClassService;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/**
 * Due to a circular dependency this decorator factory is not stored in molgenis-data.
 */
@Component
public class EntityTypeRepositoryDecoratorFactory
		extends AbstractSystemRepositoryDecoratorFactory<EntityType, EntityTypeMetadata>
{
	private final DataService dataService;
	private final SystemEntityTypeRegistry systemEntityTypeRegistry;
	private final UserPermissionEvaluator permissionService;
	private final EntityTypeValidator entityTypeValidator;
	private final EntityTypeDependencyResolver entityTypeDependencyResolver;
	private final MutableAclService mutableAclService;
	private final MutableAclClassService mutableAclClassService;

	public EntityTypeRepositoryDecoratorFactory(DataService dataService, EntityTypeMetadata entityTypeMetadata,
			SystemEntityTypeRegistry systemEntityTypeRegistry, UserPermissionEvaluator permissionService,
			EntityTypeValidator entityTypeValidator, EntityTypeDependencyResolver entityTypeDependencyResolver,
			MutableAclService mutableAclService, MutableAclClassService mutableAclClassService)
	{
		super(entityTypeMetadata);
		this.dataService = requireNonNull(dataService);
		this.systemEntityTypeRegistry = requireNonNull(systemEntityTypeRegistry);
		this.permissionService = requireNonNull(permissionService);
		this.entityTypeValidator = requireNonNull(entityTypeValidator);
		this.entityTypeDependencyResolver = requireNonNull(entityTypeDependencyResolver);
		this.mutableAclService = requireNonNull(mutableAclService);
		this.mutableAclClassService = requireNonNull(mutableAclClassService);
	}

	@Override
	public Repository<EntityType> createDecoratedRepository(Repository<EntityType> repository)
	{
		repository = new EntityTypeRepositoryDecorator(repository, dataService, entityTypeDependencyResolver);
		repository = new EntityTypeRepositoryValidationDecorator(repository, entityTypeValidator);
		return new EntityTypeRepositorySecurityDecorator(repository, systemEntityTypeRegistry, permissionService,
				mutableAclService, mutableAclClassService);
	}
}