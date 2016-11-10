package org.molgenis.data.meta;

import com.google.common.collect.Lists;
import com.google.common.collect.TreeTraverser;
import org.molgenis.data.*;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageMetadata;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.google.common.collect.Lists.reverse;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

public class PackageRepositoryDecorator implements Repository<Package>
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
	public Set<RepositoryCapability> getCapabilities()
	{
		return decoratedRepo.getCapabilities();
	}

	@Override
	public void close() throws IOException
	{
		decoratedRepo.close();
	}

	@Override
	public String getName()
	{
		return decoratedRepo.getName();
	}

	@Override
	public Set<QueryRule.Operator> getQueryOperators()
	{
		return decoratedRepo.getQueryOperators();
	}

	public EntityType getEntityType()
	{
		return decoratedRepo.getEntityType();
	}

	@Override
	public long count()
	{
		return decoratedRepo.count();
	}

	@Override
	public Query<Package> query()
	{
		return decoratedRepo.query();
	}

	@Override
	public long count(Query<Package> q)
	{
		return decoratedRepo.count(q);
	}

	@Override
	public Stream<Package> findAll(Query<Package> q)
	{
		return decoratedRepo.findAll(q);
	}

	@Override
	public Iterator<Package> iterator()
	{
		return decoratedRepo.iterator();
	}

	@Override
	public void forEachBatched(Fetch fetch, Consumer<List<Package>> consumer, int batchSize)
	{
		decoratedRepo.forEachBatched(fetch, consumer, batchSize);
	}

	@Override
	public Package findOne(Query<Package> q)
	{
		return decoratedRepo.findOne(q);
	}

	@Override
	public Package findOneById(Object id)
	{
		return decoratedRepo.findOneById(id);
	}

	@Override
	public Package findOneById(Object id, Fetch fetch)
	{
		return decoratedRepo.findOneById(id, fetch);
	}

	@Override
	public Stream<Package> findAll(Stream<Object> ids)
	{
		return decoratedRepo.findAll(ids);
	}

	@Override
	public Stream<Package> findAll(Stream<Object> ids, Fetch fetch)
	{
		return decoratedRepo.findAll(ids, fetch);
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		return decoratedRepo.aggregate(aggregateQuery);
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

	@Transactional
	@Override
	public void delete(Package entity)
	{
		deletePackage(entity);
	}

	@Transactional
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
		if (isSystemPackage(package_))
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
		if (isSystemPackage(package_))
		{
			throw new MolgenisDataException(format("Deleting system package [%s] is not allowed", package_.getName()));
		}
	}

	private static Stream<Package> getPackageTreeTraversal(Package package_)
	{
		return StreamSupport.stream(new PackageTreeTraverser().postOrderTraversal(package_).spliterator(), false);
	}

	private static boolean isSystemPackage(Package package_)
	{
		return package_.getName().equals(PACKAGE_SYSTEM) || (package_.getRootPackage() != null && package_
				.getRootPackage().getName().equals(PACKAGE_SYSTEM));
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