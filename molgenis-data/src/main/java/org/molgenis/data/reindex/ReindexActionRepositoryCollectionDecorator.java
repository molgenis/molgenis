package org.molgenis.data.reindex;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.CudType.CREATE;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.CudType.DELETE;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.CudType.UPDATE;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.DataType.METADATA;

import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.RepositoryCollectionCapability;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;

/**
 * Decorator around a {@link Repository} that registers changes made to its data with the
 * {@link ReindexActionRegisterService}.
 */
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
		this.reindexActionRegisterService.register(entityMeta.getName(), DELETE, METADATA, null);
		this.decorated.deleteRepository(entityMeta);
	}

	@Override
	public void addAttribute(String entityFullName, AttributeMetaData attribute)
	{
		this.reindexActionRegisterService.register(entityFullName, UPDATE, METADATA, null);
		this.decorated.addAttribute(entityFullName, attribute);
	}

	@Override
	public void updateAttribute(EntityMetaData entityMetaData, AttributeMetaData attr, AttributeMetaData updatedAttr)
	{
		this.reindexActionRegisterService.register(entityMetaData.getName(), UPDATE, METADATA, null);
		this.decorated.updateAttribute(entityMetaData, attr, updatedAttr);
	}

	@Override
	public void deleteAttribute(String entityFullName, String attributeName)
	{
		this.reindexActionRegisterService.register(entityFullName, UPDATE, METADATA, null);
		this.decorated.deleteAttribute(entityFullName, attributeName);
	}

	@Override
	public Stream<String> getLanguageCodes()
	{
		return this.decorated.getLanguageCodes();
	}

	@Override
	public Iterator<Repository<Entity>> iterator()
	{
		return this.decorated.iterator();
	}

	@Override
	public Repository<Entity> createRepository(EntityMetaData entityMeta)
	{
		this.reindexActionRegisterService.register(entityMeta.getName(), CREATE, METADATA, null);
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
