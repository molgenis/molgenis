package org.molgenis.data.meta;

import com.google.common.collect.TreeTraverser;
import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;

public class PackageRepositoryDecorator extends AbstractRepositoryDecorator<Package>
{
	private final DataService dataService;

	public PackageRepositoryDecorator(Repository<Package> delegateRepository, DataService dataService)
	{
		super(delegateRepository);
		this.dataService = requireNonNull(dataService);
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
		deleteEntityTypesInPackageAndSubPackages(package_);

		// delete rows from package table
		delegate().delete(getPackageTreeTraversal(package_));
	}

	private void deleteEntityTypesInPackageAndSubPackages(Package package_)
	{
		Repository<EntityType> entityRepo = getEntityRepository();
		Stream<EntityType> entityTypesToDelete = getPackageTreeTraversal(package_).flatMap(
				p -> stream(p.getEntityTypes().spliterator(), false));
		entityRepo.delete(entityTypesToDelete);
	}

	private static Stream<Package> getPackageTreeTraversal(Package package_)
	{
		return stream(new PackageTreeTraverser().postOrderTraversal(package_).spliterator(), false);
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