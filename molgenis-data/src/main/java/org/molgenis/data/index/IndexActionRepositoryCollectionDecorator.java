package org.molgenis.data.index;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.RepositoryCollectionCapability;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;

import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Decorator around a {@link Repository} that registers changes made to its data with the
 * {@link IndexActionRegisterServiceImpl}.
 */
public class IndexActionRepositoryCollectionDecorator implements RepositoryCollection
{
	private final RepositoryCollection decorated;
	private final IndexActionRegisterService indexActionRegisterService;

	public IndexActionRepositoryCollectionDecorator(RepositoryCollection decorated,
			IndexActionRegisterService indexActionRegisterService)
	{
		this.decorated = requireNonNull(decorated);
		this.indexActionRegisterService = requireNonNull(indexActionRegisterService);
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
		this.indexActionRegisterService.register(entityMeta.getName(), null);
		this.decorated.deleteRepository(entityMeta);
	}

	@Override
	public void addAttribute(EntityMetaData entityMeta, AttributeMetaData attribute)
	{
		this.indexActionRegisterService.register(entityMeta.getName(), null);
		this.decorated.addAttribute(entityMeta, attribute);
	}

	@Override
	public void updateAttribute(EntityMetaData entityMetaData, AttributeMetaData attr, AttributeMetaData updatedAttr)
	{
		this.indexActionRegisterService.register(entityMetaData.getName(), null);
		this.decorated.updateAttribute(entityMetaData, attr, updatedAttr);
	}

	@Override
	public void deleteAttribute(EntityMetaData entityMeta, AttributeMetaData attr)
	{
		this.indexActionRegisterService.register(entityMeta.getName(), null);
		this.decorated.deleteAttribute(entityMeta, attr);
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
		this.indexActionRegisterService.register(entityMeta.getName(), null);
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
