package org.molgenis.data.meta.model;

import org.molgenis.data.AbstractSystemRepositoryDecoratorFactory;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.EntityTypeDependencyResolver;
import org.molgenis.data.meta.PackageRepositoryDecorator;
import org.molgenis.data.validation.meta.PackageRepositoryValidationDecorator;
import org.molgenis.data.validation.meta.PackageValidator;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/**
 * Due to a circular dependency this decorator factory is not stored in molgenis-data.
 */
@Component
public class PackageRepositoryDecoratorFactory
		extends AbstractSystemRepositoryDecoratorFactory<Package, PackageMetadata>
{
	private final DataService dataService;
	private final EntityTypeDependencyResolver entityTypeDependencyResolver;
	private final PackageValidator packageValidator;

	public PackageRepositoryDecoratorFactory(PackageMetadata packageMetadata, DataService dataService,
			EntityTypeDependencyResolver entityTypeDependencyResolver, PackageValidator packageValidator)
	{
		super(packageMetadata);
		this.dataService = requireNonNull(dataService);
		this.entityTypeDependencyResolver = requireNonNull(entityTypeDependencyResolver);
		this.packageValidator = requireNonNull(packageValidator);
	}

	@Override
	public Repository<Package> createDecoratedRepository(Repository<Package> repository)
	{
		repository = new PackageRepositoryDecorator(repository, dataService, entityTypeDependencyResolver);
		return new PackageRepositoryValidationDecorator(repository, packageValidator);
	}
}
