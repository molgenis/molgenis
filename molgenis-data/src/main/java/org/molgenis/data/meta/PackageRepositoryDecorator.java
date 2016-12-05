package org.molgenis.data.meta;

import com.google.common.collect.Lists;
import com.google.common.collect.TreeTraverser;
import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.google.common.collect.Lists.reverse;
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

	private void deletePackage(Package package_)
	{
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