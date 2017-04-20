package org.molgenis.data.validation.meta;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.model.Package;

import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Validates {@link Package packages} before adding or updating the delegated repository
 */
public class PackageRepositoryValidationDecorator extends AbstractRepositoryDecorator<Package>
{
	private final Repository<Package> decoratedRepo;
	private final PackageValidator packageValidator;

	public PackageRepositoryValidationDecorator(Repository<Package> decoratedRepo, PackageValidator packageValidator)
	{
		this.decoratedRepo = requireNonNull(decoratedRepo);
		this.packageValidator = requireNonNull(packageValidator);
	}

	@Override
	public Repository<Package> delegate()
	{
		return decoratedRepo;
	}

	@Override
	public void add(Package package_)
	{
		packageValidator.validate(package_);
		decoratedRepo.add(package_);
	}

	@Override
	public Integer add(Stream<Package> packageStream)
	{
		return decoratedRepo.add(packageStream.filter(entityType ->
		{
			packageValidator.validate(entityType);
			return true;
		}));
	}

	@Override
	public void update(Package package_)
	{
		packageValidator.validate(package_);
		decoratedRepo.update(package_);
	}

	@Override
	public void update(Stream<Package> packageStream)
	{
		decoratedRepo.update(packageStream.filter(entityType ->
		{
			packageValidator.validate(entityType);
			return true;
		}));
	}

	@Override
	public void delete(Package package_)
	{
		packageValidator.validate(package_);
		super.delete(package_);
	}

	@Override
	public void delete(Stream<Package> packageStream)
	{
		decoratedRepo.delete(packageStream.filter(package_ ->
		{
			packageValidator.validate(package_);
			return true;
		}));
	}

	@Override
	public void deleteById(Object id)
	{
		Package package_ = findOneById(id);
		if (package_ == null)
		{
			throw new UnknownEntityException(format("Unknown package [%s]", id.toString()));
		}
		packageValidator.validate(package_);
		super.deleteById(id);
	}

	@Override
	public void deleteAll()
	{
		iterator().forEachRemaining(packageValidator::validate);
		super.deleteAll();
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		super.deleteAll(ids.map(this::findOneById).filter(package_ ->
		{
			packageValidator.validate(package_);
			return true;
		}).map(Package::getId));
	}
}
