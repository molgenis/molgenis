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
	public void deleteEntityMeta(String entityFullName)
	{
		this.decorated.deleteEntityMeta(entityFullName);
		this.reindexActionRegisterService.register(entityFullName, CudType.DELETE, DataType.METADATA, null);
	}

	@Override
	public Repository<Entity> addEntityMeta(EntityMetaData entityMeta)
	{
		this.reindexActionRegisterService.register(entityMeta.getName(), CudType.CREATE, DataType.METADATA, null);
		return this.decorated.addEntityMeta(entityMeta);

	}

	@Override
	public void addAttribute(String entityFullName, AttributeMetaData attribute)
	{
		this.decorated.addAttribute(entityFullName, attribute);
		this.reindexActionRegisterService.register(entityFullName, CudType.UPDATE, DataType.METADATA, null);
	}

	@Override
	public void deleteAttribute(String entityFullName, String attributeName)
	{
		this.decorated.deleteAttribute(entityFullName, attributeName);
		this.reindexActionRegisterService.register(entityFullName, CudType.UPDATE, DataType.METADATA, null);
	}

	@Override
	public void addAttributeSync(String entityFullName, AttributeMetaData attribute)
	{
		this.decorated.addAttributeSync(entityFullName, attribute);
		this.reindexActionRegisterService.register(entityFullName, CudType.UPDATE, DataType.METADATA, null);
	}

	@Override
	public Iterator<Repository<Entity>> iterator()
	{
		return this.decorated.iterator();
	}

	@Override
	public Iterable<String> getEntityNames()
	{
		return this.decorated.getEntityNames();
	}

	@Override
	public Repository<Entity> getRepository(String name)
	{
		return this.decorated.getRepository(name);
	}
}
