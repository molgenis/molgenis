package org.molgenis.data.reindex;

import static java.util.Objects.requireNonNull;

import java.util.Iterator;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.ManageableRepositoryCollection;
import org.molgenis.data.Repository;
import org.molgenis.data.reindex.meta.ReindexActionMetaData.CudType;
import org.molgenis.data.reindex.meta.ReindexActionMetaData.DataType;

/**
 * Decorator around a {@link Repository} that registers changes made to its data with the
 * {@link ReindexActionRegisterService}.
 */
public class ReindexActionRepositoryCollectionDecorator implements ManageableRepositoryCollection
{
	private final ManageableRepositoryCollection decorated;
	private final ReindexActionRegisterService reindexActionRegisterService;

	public ReindexActionRepositoryCollectionDecorator(ManageableRepositoryCollection decorated,
			ReindexActionRegisterService reindexActionRegisterService)
	{
		this.decorated = requireNonNull(decorated);
		this.reindexActionRegisterService = requireNonNull(reindexActionRegisterService);
	}

	@Override
	public String getName()
	{
		return this.decorated.getName();
	}

	public boolean hasRepository(String name)
	{
		return this.decorated.hasRepository(name);
	}

	@Override
	public void deleteRepository(EntityMetaData entityMeta)
	{
		this.decorated.deleteEntityMeta(entityMeta);
		this.reindexActionRegisterService.register(entityMeta, CudType.DELETE, DataType.METADATA, null);
	}

	@Override
	public void addAttribute(String entityFullName, AttributeMetaData attribute)
	{
		this.decorated.addAttribute(entityFullName, attribute);
		this.reindexActionRegisterService.register(entityFullName, CudType.UPDATE, DataType.METADATA, null);
	}

	@Override
	public void updateAttribute(EntityMetaData entityMetaData, AttributeMetaData attr, AttributeMetaData updatedAttr)
	{
		this.reindexActionRegisterService.register(entityMetaData, UPDATE, DataType.METADATA, null);
		this.decorated.updateAttribute(entityMetaData, attr, updatedAttr);
	}

	@Override
	public void deleteAttribute(String entityFullName, String attributeName)
	{
		this.decorated.deleteAttribute(entityFullName, attributeName);
		this.reindexActionRegisterService.register(entityFullName, CudType.UPDATE, DataType.METADATA, null);
	}

	@Override
	public Iterator<Repository<Entity>> iterator()
	{
		return this.decorated.iterator();
	}

	@Override
	public Repository<Entity> addEntityMeta(EntityMetaData entityMeta)
	{
		this.reindexActionRegisterService.register(entityMeta.getName(), CudType.CREATE, DataType.METADATA, null);
		return this.decorated.addEntityMeta(entityMeta);
	}

	@Override
	public Iterable<String> getEntityNames()
	{
		return this.decorated.getEntityNames();
	}

	@Override
	public Set<RepositoryCollectionCapability> getCapabilities()
	{
		return decorated.getCapabilities();
	}

	@Override
	public Repository<Entity> getRepository(EntityMetaData entityMeta)
	{
		return decorated.getRepository(entityMeta);
	}

	@Override
	public <E extends Entity> Repository<E> getRepository(EntityMetaData entityMeta, Class<E> clazz)
	{
		return decorated.getRepository(entityMeta, clazz);
	}

	@Override
	public boolean hasRepository(EntityMetaData entityMeta)
	{
		return decorated.hasRepository(entityMeta);
	}

	@Override
	public Repository<Entity> getRepository(String name)
	{
		return this.decorated.getRepository(name);
	}
}
