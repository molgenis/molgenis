package org.molgenis.data.elasticsearch.reindex;

import static java.util.Objects.requireNonNull;

import java.util.Iterator;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.ManageableRepositoryCollection;
import org.molgenis.data.Repository;
import org.molgenis.data.elasticsearch.reindex.meta.ReindexActionMetaData.CudType;
import org.molgenis.data.elasticsearch.reindex.meta.ReindexActionMetaData.DataType;

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
	public void deleteEntityMeta(String entityName)
	{
		this.reindexActionRegisterService.register(this.decorated.getRepository(entityName).getEntityMetaData(),
				CudType.DELETE, DataType.METADATA, null);
		this.decorated.deleteEntityMeta(entityName);
	}

	@Override
	public void addAttribute(String entityName, AttributeMetaData attribute)
	{
		this.reindexActionRegisterService.register(this.decorated.getRepository(entityName).getEntityMetaData(),
				CudType.UPDATE,
				DataType.METADATA, null);
		this.decorated.addAttribute(entityName, attribute);
	}

	@Override
	public void deleteAttribute(String entityName, String attributeName)
	{
		this.reindexActionRegisterService.register(this.decorated.getRepository(entityName).getEntityMetaData(),
				CudType.UPDATE, DataType.METADATA, null);
		this.decorated.deleteAttribute(entityName, attributeName);
	}

	@Override
	public void addAttributeSync(String entityName, AttributeMetaData attribute)
	{
		this.reindexActionRegisterService.register(this.decorated.getRepository(entityName).getEntityMetaData(),
				CudType.UPDATE,
				DataType.METADATA, null);
		this.decorated.addAttribute(entityName, attribute);
	}

	@Override
	public Iterator<Repository<Entity>> iterator()
	{
		return this.decorated.iterator();
	}

	@Override
	public Repository<Entity> addEntityMeta(EntityMetaData entityMeta)
	{
		this.reindexActionRegisterService.register(entityMeta, CudType.CREATE,
				DataType.METADATA, null);
		return this.decorated.addEntityMeta(entityMeta);
	}

	@Override
	public Iterable<String> getEntityNames()
	{
		return this.decorated.getEntityNames();
	}

	@Override
	public Repository getRepository(String name)
	{
		return this.decorated.getRepository(name);
	}
}
