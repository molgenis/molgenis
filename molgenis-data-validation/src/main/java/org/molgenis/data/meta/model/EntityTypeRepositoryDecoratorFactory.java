package org.molgenis.data.meta.model;

import org.molgenis.data.AbstractSystemRepositoryDecoratorFactory;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.EntityTypeRepositoryDecorator;
import org.molgenis.data.meta.system.SystemEntityTypeRegistry;
import org.molgenis.data.security.meta.EntityTypeRepositorySecurityDecorator;
import org.molgenis.data.validation.meta.EntityTypeRepositoryValidationDecorator;
import org.molgenis.data.validation.meta.EntityTypeValidator;
import org.molgenis.security.core.MolgenisPermissionService;
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
	private final MolgenisPermissionService permissionService;
	private final EntityTypeValidator entityTypeValidator;

	public EntityTypeRepositoryDecoratorFactory(DataService dataService, EntityTypeMetadata entityTypeMetadata,
			SystemEntityTypeRegistry systemEntityTypeRegistry, MolgenisPermissionService permissionService,
			EntityTypeValidator entityTypeValidator)
	{
		super(entityTypeMetadata);
		this.dataService = requireNonNull(dataService);
		this.systemEntityTypeRegistry = requireNonNull(systemEntityTypeRegistry);
		this.permissionService = requireNonNull(permissionService);
		this.entityTypeValidator = requireNonNull(entityTypeValidator);
	}

	@Override
	public Repository<EntityType> createDecoratedRepository(Repository<EntityType> repository)
	{
		repository = new EntityTypeRepositoryDecorator(repository, dataService);
		repository = new EntityTypeRepositoryValidationDecorator(repository, entityTypeValidator);
		return repository;
	}
}
