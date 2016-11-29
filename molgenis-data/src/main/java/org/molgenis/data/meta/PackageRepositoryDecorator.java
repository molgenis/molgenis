package org.molgenis.data.meta;

import com.google.common.collect.Lists;
import com.google.common.collect.TreeTraverser;
import org.molgenis.data.*;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageMetadata;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.google.common.collect.Lists.reverse;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;

public class PackageRepositoryDecorator extends AbstractRepositoryDecorator<Package>
{
	private final Repository<Package> decoratedRepo;
	private final DataService dataService;
	private final EntityTypeDependencyResolver entityTypeDependencyResolver;

	public PackageRepositoryDecorator(Repository<Package> decoratedRepo, DataService dataService,
			EntityTypeDependencyResolver entityTypeDependencyResolver)
	{
		this.decoratedRepo = requireNonNull(decoratedRepo);
		this.dataService = requireNonNull(dataService);
		this.entityTypeDependencyResolver = requireNonNull(entityTypeDependencyResolver);
	}

	@Override
	protected Repository<Package> delegate()
	{
		return decoratedRepo;
	}

	@Override
	public void update(Package entity)
	{
		validateUpdateAllowed(entity);
		decoratedRepo.update(entity);
	}

	@Override
	public void update(Stream<Package> entities)
	{
		decoratedRepo.update(entities.filter(entity ->
		{
			validateUpdateAllowed(entity);
			return true;
		}));
	}

	@Override
	public void delete(Package entity)
	{
		deletePackage(entity);
	}

	@Override
	public void delete(Stream<Package> entities)
	{
		entities.forEach(this::deletePackage);
	}

	@Override
	public void deleteById(Object id)
	{
		deletePackage(findOneById(id));
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		findAll(ids).forEach(this::deletePackage);
	}

	@Override
	public void deleteAll()
	{
		forEach(this::deletePackage);
	}

	@Override
	public void add(Package entity)
	{
		validateAddAllowed(entity);
		decoratedRepo.add(entity);
	}

	@Override
	public Integer add(Stream<Package> entities)
	{
		return decoratedRepo.add(entities.filter(entity ->
		{
			validateAddAllowed(entity);
			return true;
		}));
	}

	private void validateAddAllowed(Package package_)
	{
		Entity existingEntity = findOneById(package_.getIdValue(), new Fetch().field(PackageMetadata.FULL_NAME));
		if (existingEntity != null)
		{
			throw new MolgenisDataException(format("Adding existing package [%s] is not allowed",
					package_.getString(EntityTypeMetadata.FULL_NAME)));
		}
	}

	private static void validateUpdateAllowed(Package package_)
	{
		if (MetaUtils.isSystemPackage(package_))
		{
			throw new MolgenisDataException(format("Updating system package [%s] is not allowed", package_.getName()));
		}
	}

	private void deletePackage(Package package_)
	{
		validateDeleteAllowed(package_);

		// recursively delete sub packages
		getPackageTreeTraversal(package_).forEach(this::deletePackageAndContents);
	}

	private void deletePackageAndContents(Package package_)
	{
		// delete entities in package
		Repository<EntityType> entityRepo = getEntityRepository();
		List<EntityType> entityTypes = Lists.newArrayList(package_.getEntityTypes());
		entityRepo.delete(reverse(entityTypeDependencyResolver.resolve(entityTypes)).stream());

		// delete row from package table
		decoratedRepo.delete(package_);
	}

	/**
	 * Deleting a package is allowed if:
	 * <ul>
	 * <li>This package and descending packages do not contain a system package</li>
	 * <li>This package and descending packages do not contain a system package_</li>
	 * <li>User has {@link org.molgenis.security.core.Permission#WRITEMETA} permission on all entities in this package
	 * and descending packages</li>
	 * <li>There are no dependencies to entities in this package and descendant packages from entities in other packages
	 * </li>
	 * </ul>
	 *
	 * @param package_ package
	 */
	private static void validateDeleteAllowed(Package package_)
	{
		// This package and descending packages do not contain a system package
		if (MetaUtils.isSystemPackage(package_))
		{
			throw new MolgenisDataException(format("Deleting system package [%s] is not allowed", package_.getName()));
		}
	}

	private static Stream<Package> getPackageTreeTraversal(Package package_)
	{
		return StreamSupport.stream(new PackageTreeTraverser().postOrderTraversal(package_).spliterator(), false);
	}

	private Repository<EntityType> getEntityRepository()
	{
		return dataService.getRepository(ENTITY_TYPE_META_DATA, EntityType.class);
	}

	private static class PackageTreeTraverser extends TreeTraverser<Package>
	{
		@Override
		public Iterable<Package> children(@Nonnull Package packageEntity)
		{
			return packageEntity.getChildren();
		}
	}
}