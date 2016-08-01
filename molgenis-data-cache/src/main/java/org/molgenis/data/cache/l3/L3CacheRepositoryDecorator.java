package org.molgenis.data.cache.l3;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;

import java.util.List;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.RepositoryCapability.CACHEABLE;

public class L3CacheRepositoryDecorator extends AbstractRepositoryDecorator
{
	private final L3Cache l3Cache;
	private final boolean cacheable;
	private final Repository<Entity> decoratedRepository;

	public L3CacheRepositoryDecorator(Repository<Entity> decoratedRepository, L3Cache l3Cache)
	{
		this.decoratedRepository = requireNonNull(decoratedRepository);
		this.l3Cache = requireNonNull(l3Cache);
		this.cacheable = decoratedRepository.getCapabilities().containsAll(newArrayList(CACHEABLE));
	}

	@Override
	protected Repository<Entity> delegate()
	{
		return decoratedRepository;
	}

	/**
	 * Retrieves multiple entities
	 *
	 * @param q
	 * @return
	 */
	@Override
	public Stream<Entity> findAll(Query<Entity> q)
	{
		if (cacheable)
		{
			List<Object> ids = l3Cache.get(delegate(), q);
			if (!ids.isEmpty()) return delegate().findAll(ids.stream());
		}
		return delegate().findAll(q);
	}

	@Override
	public Entity findOne(Query<Entity> q)
	{
		if (cacheable)
		{
			List<Object> ids = l3Cache.get(delegate(), q);
			if (!ids.isEmpty()) return delegate().findOneById(ids.get(0));
		}
		return delegate().findOne(q);
	}

	// TODO check which ones are needed

	@Override
	public void delete(Entity entity)
	{
		delegate().delete(entity);
	}

	@Override
	public void deleteById(Object id)
	{
		delegate().deleteById(id);
	}

	@Override
	public void deleteAll()
	{
		delegate().deleteAll();
	}

	@Override
	public void add(Entity entity)
	{
		delegate().add(entity);
	}

	@Override
	public Integer add(Stream<Entity> entities)
	{
		return delegate().add(entities);
	}

	@Override
	public void update(Stream<Entity> entities)
	{
		delegate().update(entities);
	}

	@Override
	public void delete(Stream<Entity> entities)
	{
		delegate().delete(entities);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		delegate().deleteAll(ids);
	}

}
