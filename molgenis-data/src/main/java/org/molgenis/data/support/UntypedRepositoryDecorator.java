package org.molgenis.data.support;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityListener;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.util.EntityUtils;

/**
 * Created by Dennis on 5/25/2016.
 */
public class UntypedRepositoryDecorator<E extends Entity> implements Repository<Entity>
{
	private final Repository<E> typedRepo;
	private final Class<E> entityClass;

	public UntypedRepositoryDecorator(Repository<E> typedRepo, Class<E> entityClass)
	{
		this.typedRepo = requireNonNull(typedRepo);
		this.entityClass = requireNonNull(entityClass);
	}

	@Override
	public Stream<Entity> stream(Fetch fetch)
	{
		return asUntypedStream(typedRepo.stream(fetch));
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return typedRepo.getCapabilities();
	}

	@Override
	public String getName()
	{
		return typedRepo.getName();
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return typedRepo.getEntityMetaData();
	}

	@Override
	public long count()
	{
		return typedRepo.count();
	}

	@Override
	public Query<Entity> query()
	{
		return asUntypedQuery(typedRepo.query());
	}

	@Override
	public long count(Query<Entity> q)
	{
		return typedRepo.count(asTypedQuery(q));
	}

	@Override
	public Stream<Entity> findAll(Query<Entity> q)
	{
		return asUntypedStream(typedRepo.findAll(asTypedQuery(q)));
	}

	@Override
	public Entity findOne(Query<Entity> q)
	{
		return typedRepo.findOne(asTypedQuery(q));
	}

	@Override
	public Entity findOneById(Object id)
	{
		return typedRepo.findOneById(id);
	}

	@Override
	public Entity findOneById(Object id, Fetch fetch)
	{
		return typedRepo.findOneById(id, fetch);
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids)
	{
		return asUntypedStream(typedRepo.findAll(ids));
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids, Fetch fetch)
	{
		return asUntypedStream(typedRepo.findAll(ids, fetch));
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		return typedRepo.aggregate(aggregateQuery);
	}

	@Override
	public void update(Entity entity)
	{
		typedRepo.update(asTypedEntity(entity));
	}

	@Override
	public void update(Stream<Entity> entities)
	{
		typedRepo.update(asTypedStream(entities));
	}

	@Override
	public void delete(Entity entity)
	{
		typedRepo.delete(asTypedEntity(entity));
	}

	@Override
	public void delete(Stream<Entity> entities)
	{
		typedRepo.delete(asTypedStream(entities));
	}

	@Override
	public void deleteById(Object id)
	{
		typedRepo.deleteById(id);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		typedRepo.deleteAll(ids);
	}

	@Override
	public void deleteAll()
	{
		typedRepo.deleteAll();
	}

	@Override
	public void add(Entity entity)
	{
		typedRepo.add(asTypedEntity(entity));
	}

	@Override
	public Integer add(Stream<Entity> entities)
	{
		return typedRepo.add(asTypedStream(entities));
	}

	@Override
	public void flush()
	{
		typedRepo.flush();
	}

	@Override
	public void clearCache()
	{
		typedRepo.clearCache();
	}

	@Override
	public void rebuildIndex()
	{
		typedRepo.rebuildIndex();
	}

	@Override
	public void addEntityListener(EntityListener entityListener)
	{
		typedRepo.addEntityListener(entityListener);
	}

	@Override
	public void removeEntityListener(EntityListener entityListener)
	{
		typedRepo.removeEntityListener(entityListener);
	}

	@Override
	public void close() throws IOException
	{
		typedRepo.close();
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return asUntypedIterator(typedRepo.iterator());
	}

	// FIXME code duplication with TypedRepoDecorator
	@SuppressWarnings("unchecked")
	private E asTypedEntity(Entity untypedEntity)
	{
		E typedEntity;
		if (untypedEntity == null)
		{
			typedEntity = null;
		}
		else if (untypedEntity.getClass().equals(entityClass))
		{
			typedEntity = (E) untypedEntity;
		}
		else
		{
			// FIXME adapt EntityUtils such that it can wrap an untypedEntity in a typed entity
			typedEntity = EntityUtils.convert(untypedEntity, entityClass, null);
		}
		return typedEntity;
	}

	private Stream<E> asTypedStream(Stream<Entity> untypedEntities)
	{
		return untypedEntities.map(this::asTypedEntity);
	}

	@SuppressWarnings("unchecked")
	private Stream<Entity> asUntypedStream(Stream<E> typedEntities)
	{
		return (Stream<Entity>) typedEntities;
	}

	private Query<E> asTypedQuery(Query<Entity> untypedQuery)
	{
		QueryImpl<E> typedQuery = new QueryImpl<>(untypedQuery.getRules());
		typedQuery.setPageSize(untypedQuery.getPageSize());
		typedQuery.setOffset(untypedQuery.getOffset());
		typedQuery.setSort(untypedQuery.getSort());
		typedQuery.setFetch(untypedQuery.getFetch());
		typedQuery.setRepository(typedRepo);
		return typedQuery;
	}

	@SuppressWarnings("unchecked")
	private Query<Entity> asUntypedQuery(Query<E> typedQuery)
	{
		return (Query<Entity>) typedQuery;
	}

	@SuppressWarnings("unchecked")
	private Iterator<Entity> asUntypedIterator(Iterator<E> typedEntities)
	{
		return (Iterator<Entity>) typedEntities;
	}
}
