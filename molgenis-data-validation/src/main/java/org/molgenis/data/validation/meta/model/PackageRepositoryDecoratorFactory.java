package org.molgenis.data.validation.meta.model;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.AbstractSystemRepositoryDecoratorFactory;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.PackageRepositoryDecorator;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.security.meta.PackageRepositorySecurityDecorator;
import org.molgenis.data.validation.meta.PackageRepositoryValidationDecorator;
import org.molgenis.data.validation.meta.PackageValidator;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.stereotype.Component;

/** Due to a circular dependency this decorator factory is not stored in molgenis-data. */
@Component
public class PackageRepositoryDecoratorFactory
    extends AbstractSystemRepositoryDecoratorFactory<Package, PackageMetadata> {
  private final DataService dataService;
  private final PackageValidator packageValidator;
  private final MutableAclService mutableAclService;
  private final UserPermissionEvaluator userPermissionEvaluator;

  public PackageRepositoryDecoratorFactory(
      PackageMetadata packageMetadata,
      DataService dataService,
      PackageValidator packageValidator,
      MutableAclService mutableAclService,
      UserPermissionEvaluator userPermissionEvaluator) {
    super(packageMetadata);
    this.dataService = requireNonNull(dataService);
    this.packageValidator = requireNonNull(packageValidator);
    this.mutableAclService = requireNonNull(mutableAclService);
    this.userPermissionEvaluator = requireNonNull(userPermissionEvaluator);
  }

  @Override
  public Repository<Package> createDecoratedRepository(Repository<Package> repository) {
    repository = new PackageRepositoryDecorator(repository, dataService);
    repository =
        new PackageRepositorySecurityDecorator(
            repository, mutableAclService, userPermissionEvaluator);
    return new PackageRepositoryValidationDecorator(repository, packageValidator);
  }
}
