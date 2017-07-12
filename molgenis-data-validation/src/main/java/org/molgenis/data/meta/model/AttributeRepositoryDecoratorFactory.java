package org.molgenis.data.meta.model;

import org.molgenis.data.AbstractSystemRepositoryDecoratorFactory;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.AttributeRepositoryDecorator;
import org.molgenis.data.security.acl.EntityAclService;
import org.molgenis.data.security.meta.AttributeRepositorySecurityDecorator;
import org.molgenis.data.validation.meta.AttributeRepositoryValidationDecorator;
import org.molgenis.data.validation.meta.AttributeValidator;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/**
 * Due to a circular dependency this decorator factory is not stored in molgenis-data.
 */
@Component
public class AttributeRepositoryDecoratorFactory
		extends AbstractSystemRepositoryDecoratorFactory<Attribute, AttributeMetadata>
{
	private final DataService dataService;
	private final AttributeValidator attributeValidator;
	private final EntityAclService entityAclService;

	public AttributeRepositoryDecoratorFactory(AttributeMetadata attributeMetadata, DataService dataService,
			AttributeValidator attributeValidator, EntityAclService entityAclService)
	{
		super(attributeMetadata);
		this.dataService = requireNonNull(dataService);
		this.attributeValidator = requireNonNull(attributeValidator);
		this.entityAclService = requireNonNull(entityAclService);
	}

	@Override
	public Repository<Attribute> createDecoratedRepository(Repository<Attribute> repository)
	{
		repository = new AttributeRepositoryDecorator(repository, dataService);
		repository = new AttributeRepositoryValidationDecorator(repository, attributeValidator);
		return new AttributeRepositorySecurityDecorator(repository, entityAclService);
	}
}
