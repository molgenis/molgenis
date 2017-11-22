package org.molgenis.data.meta.model;

import org.molgenis.data.AbstractSystemRepositoryDecoratorFactory;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.PackageRepositoryDecorator;
import org.molgenis.data.validation.meta.PackageRepositoryValidationDecorator;
import org.molgenis.data.validation.meta.PackageValidator;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/**
 * Due to a circular dependency this decorator factory is not stored in molgenis-data.
 *
 * TODO move to molgenis-data-platform module (https://github.com/molgenis/molgenis/issues/6942)
 */
@Component
public class PackageRepositoryDecoratorFactory
		extends AbstractSystemRepositoryDecoratorFactory<Package, PackageMetadata>
{
	private final DataService dataService;
	private final PackageValidator packageValidator;

	public PackageRepositoryDecoratorFactory(PackageMetadata packageMetadata, DataService dataService,
			PackageValidator packageValidator)
	{
		super(packageMetadata);
		this.dataService = requireNonNull(dataService);
		this.packageValidator = requireNonNull(packageValidator);
	}

	@Override
	public Repository<Package> createDecoratedRepository(Repository<Package> repository)
	{
		repository = new PackageRepositoryDecorator(repository, dataService);
		return new PackageRepositoryValidationDecorator(repository, packageValidator);
	}
}
