package org.molgenis.data.meta.model;

import org.molgenis.data.AbstractSystemRepositoryDecoratorFactory;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.AttributeRepositoryDecorator;
import org.molgenis.data.meta.system.SystemEntityTypeRegistry;
import org.molgenis.data.validation.meta.AttributeRepositoryValidationDecorator;
import org.molgenis.data.validation.meta.AttributeValidator;
import org.molgenis.security.core.MolgenisPermissionService;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/**
 * Due to a circular dependency this decorator factory is not stored in molgenis-data.
 */
@Component
public class AttributeRepositoryDecoratorFactory
		extends AbstractSystemRepositoryDecoratorFactory<Attribute, AttributeMetadata>
{
	private final SystemEntityTypeRegistry systemEntityTypeRegistry;
	private final DataService dataService;
	private final MolgenisPermissionService permissionService;
	private final AttributeValidator attributeValidator;

	public AttributeRepositoryDecoratorFactory(AttributeMetadata attributeMetadata,
			SystemEntityTypeRegistry systemEntityTypeRegistry, DataService dataService,
			MolgenisPermissionService permissionService, AttributeValidator attributeValidator)
	{
		super(attributeMetadata);
		this.systemEntityTypeRegistry = requireNonNull(systemEntityTypeRegistry);
		this.dataService = requireNonNull(dataService);
		this.permissionService = requireNonNull(permissionService);
		this.attributeValidator = requireNonNull(attributeValidator);
	}

	@Override
	public Repository<Attribute> createDecoratedRepository(Repository<Attribute> repository)
	{
		repository = new AttributeRepositoryDecorator(repository, systemEntityTypeRegistry, dataService,
				permissionService);
		return new AttributeRepositoryValidationDecorator(repository, attributeValidator);
	}
}
