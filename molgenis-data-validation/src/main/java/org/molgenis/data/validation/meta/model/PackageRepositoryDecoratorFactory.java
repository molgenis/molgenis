package org.molgenis.data.validation.meta.model;

import org.molgenis.data.AbstractSystemRepositoryDecoratorFactory;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.PackageRepositoryDecorator;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.security.meta.PackageRepositorySecurityDecorator;
import org.molgenis.data.validation.meta.PackageRepositoryValidationDecorator;
import org.molgenis.data.validation.meta.PackageValidator;
import org.springframework.security.acls.model.MutableAclService;
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
	private final PackageValidator packageValidator;
	private final MutableAclService mutableAclService;

	public PackageRepositoryDecoratorFactory(PackageMetadata packageMetadata, DataService dataService,
			PackageValidator packageValidator, MutableAclService mutableAclService)
	{
		super(packageMetadata);
		this.dataService = requireNonNull(dataService);
		this.packageValidator = requireNonNull(packageValidator);
		this.mutableAclService = requireNonNull(mutableAclService);
	}

	@Override
	public Repository<Package> createDecoratedRepository(Repository<Package> repository)
	{
		repository = new PackageRepositoryDecorator(repository, dataService);
		repository = new PackageRepositorySecurityDecorator(repository, mutableAclService);
		return new PackageRepositoryValidationDecorator(repository, packageValidator);
	}
}
