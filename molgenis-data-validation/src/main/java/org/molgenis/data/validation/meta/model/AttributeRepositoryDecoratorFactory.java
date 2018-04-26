package org.molgenis.data.validation.meta.model;

import org.molgenis.data.AbstractSystemRepositoryDecoratorFactory;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.AttributeRepositoryDecorator;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.meta.system.SystemEntityTypeRegistry;
import org.molgenis.data.security.meta.AttributeRepositorySecurityDecorator;
import org.molgenis.data.validation.meta.AttributeRepositoryValidationDecorator;
import org.molgenis.data.validation.meta.AttributeValidator;
import org.molgenis.security.core.UserPermissionEvaluator;
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
	private final UserPermissionEvaluator permissionService;
	private final AttributeValidator attributeValidator;

	public AttributeRepositoryDecoratorFactory(AttributeMetadata attributeMetadata,
			SystemEntityTypeRegistry systemEntityTypeRegistry, DataService dataService,
			UserPermissionEvaluator permissionService, AttributeValidator attributeValidator)
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
		repository = new AttributeRepositoryDecorator(repository, dataService);
		repository = new AttributeRepositoryValidationDecorator(repository, attributeValidator);
		return new AttributeRepositorySecurityDecorator(repository, systemEntityTypeRegistry, permissionService);
	}
}
