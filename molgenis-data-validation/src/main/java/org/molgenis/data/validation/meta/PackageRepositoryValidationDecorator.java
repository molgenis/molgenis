package org.molgenis.data.validation.meta;

import static java.util.Objects.requireNonNull;

import java.util.stream.Stream;
import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.model.Package;

/** Validates {@link Package packages} before adding or updating the delegated repository */
public class PackageRepositoryValidationDecorator extends AbstractRepositoryDecorator<Package> {
  private final PackageValidator packageValidator;

  public PackageRepositoryValidationDecorator(
      Repository<Package> delegateRepository, PackageValidator packageValidator) {
    super(delegateRepository);
    this.packageValidator = requireNonNull(packageValidator);
  }

  @Override
  public void add(Package aPackage) {
    packageValidator.validate(aPackage);
    delegate().add(aPackage);
  }

  @Override
  public Integer add(Stream<Package> packageStream) {
    return delegate()
        .add(
            packageStream.filter(
                entityType -> {
                  packageValidator.validate(entityType);
                  return true;
                }));
  }

  @Override
  public void update(Package aPackage) {
    packageValidator.validate(aPackage);
    delegate().update(aPackage);
  }

  @Override
  public void update(Stream<Package> packageStream) {
    delegate()
        .update(
            packageStream.filter(
                entityType -> {
                  packageValidator.validate(entityType);
                  return true;
                }));
  }

  @Override
  public void delete(Package aPackage) {
    packageValidator.validate(aPackage);
    super.delete(aPackage);
  }

  @Override
  public void delete(Stream<Package> packageStream) {
    delegate()
        .delete(
            packageStream.filter(
                aPackage -> {
                  packageValidator.validate(aPackage);
                  return true;
                }));
  }

  @Override
  public void deleteById(Object id) {
    Package aPackage = findOneById(id);
    if (aPackage == null) {
      throw new UnknownEntityException(getEntityType(), id);
    }
    packageValidator.validate(aPackage);
    super.deleteById(id);
  }

  @Override
  public void deleteAll() {
    iterator().forEachRemaining(packageValidator::validate);
    super.deleteAll();
  }

  @Override
  public void deleteAll(Stream<Object> ids) {
    super.deleteAll(
        ids.map(this::findOneById)
            .filter(
                package_ -> {
                  packageValidator.validate(package_);
                  return true;
                })
            .map(Package::getId));
  }
}
