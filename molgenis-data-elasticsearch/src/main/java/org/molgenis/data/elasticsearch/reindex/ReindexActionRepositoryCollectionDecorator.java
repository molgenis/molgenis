package org.molgenis.data.elasticsearch.reindex;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.elasticsearch.reindex.ReindexActionMetaData.CudType.ADD;
import static org.molgenis.data.elasticsearch.reindex.ReindexActionMetaData.CudType.DELETE;
import static org.molgenis.data.elasticsearch.reindex.ReindexActionMetaData.CudType.UPDATE;

import java.util.Iterator;
import java.util.Set;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.RepositoryCollectionCapability;
import org.molgenis.data.elasticsearch.reindex.ReindexActionMetaData.DataType;
import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.EntityMetaData;

public class ReindexActionRepositoryCollectionDecorator implements RepositoryCollection
{
	private final RepositoryCollection decorated;
	private final ReindexActionRegisterService reindexActionRegisterService;

	public ReindexActionRepositoryCollectionDecorator(RepositoryCollection decorated,
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
		this.reindexActionRegisterService.register(entityMeta, DELETE, DataType.METADATA, null);
		this.decorated.deleteRepository(entityMeta);
	}

	@Override
	public void addAttribute(String entityName, AttributeMetaData attribute)
	{
		this.reindexActionRegisterService
				.register(this.decorated.getRepository(entityName).getEntityMetaData(), UPDATE, DataType.METADATA,
						null);
		this.decorated.addAttribute(entityName, attribute);
	}

	@Override
	public void updateAttribute(EntityMetaData entityMetaData, AttributeMetaData attr, AttributeMetaData updatedAttr)
	{
		this.reindexActionRegisterService.register(entityMetaData, UPDATE, DataType.METADATA, null);
		this.decorated.updateAttribute(entityMetaData, attr, updatedAttr);
	}

	@Override
	public void deleteAttribute(String entityName, String attributeName)
	{
		this.reindexActionRegisterService
				.register(this.decorated.getRepository(entityName).getEntityMetaData(), UPDATE, DataType.METADATA,
						null);
		this.decorated.deleteAttribute(entityName, attributeName);
	}

	@Override
	public Iterator<Repository<Entity>> iterator()
	{
		return this.decorated.iterator();
	}

	@Override
	public Repository<Entity> createRepository(EntityMetaData entityMeta)
	{
		this.reindexActionRegisterService.register(entityMeta, ADD, DataType.METADATA, null);
		return this.decorated.createRepository(entityMeta);
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
	public Repository getRepository(String name)
	{
		return this.decorated.getRepository(name);
	}
}
