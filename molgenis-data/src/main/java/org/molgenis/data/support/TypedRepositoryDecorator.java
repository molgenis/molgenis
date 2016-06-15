package org.molgenis.data.support;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityListener;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.SystemEntityFactory;
import org.molgenis.data.meta.EntityMetaData;

/**
 * Adapts a {@link Repository} with untyped entities to a Repository with typed entities
 *
 * @param <E> the type of entity
 */
public class TypedRepositoryDecorator<E extends Entity> implements Repository<E>
{
	private final Repository<Entity> untypedRepo;
	private final SystemEntityFactory<E, Object> systemEntityFactory;

	public TypedRepositoryDecorator(Repository<Entity> untypedRepo, SystemEntityFactory<E, Object> systemEntityFactory)
	{
		this.untypedRepo = requireNonNull(untypedRepo);
		this.systemEntityFactory = requireNonNull(systemEntityFactory);
	}

	@Override
	public Iterator<E> iterator()
	{
		return asTypedStream(StreamSupport
				.stream(Spliterators.spliteratorUnknownSize(untypedRepo.iterator(), Spliterator.ORDERED), false))
				.iterator();
	}

	@Override
	public Stream<E> stream()
	{
		return asTypedStream(untypedRepo.stream());
	}

	@Override
	public Stream<E> stream(Fetch fetch)
	{
		return asTypedStream(untypedRepo.stream(fetch));
	}

	@Override
	public void close() throws IOException
	{
		untypedRepo.close();
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return untypedRepo.getCapabilities();
	}

	@Override
	public Set<Operator> getQueryOperators()
	{
		return untypedRepo.getQueryOperators();
	}

	@Override
	public String getName()
	{
		return untypedRepo.getName();
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return untypedRepo.getEntityMetaData();
	}

	@Override
	public long count()
	{
		return untypedRepo.count();
	}

	@Override
	public Query<E> query()
	{
		return asTypedQuery(untypedRepo.query());
	}

	@Override
	public long count(Query<E> q)
	{
		return untypedRepo.count(asUntypedQuery(q));
	}

	@Override
	public Stream<E> findAll(Query<E> q)
	{
		return asTypedStream(untypedRepo.findAll(asUntypedQuery(q)));
	}

	@Override
	public E findOne(Query<E> q)
	{
		return asTypedEntity(untypedRepo.findOne(asUntypedQuery(q)));
	}

	@Override
	public E findOneById(Object id)
	{
		return asTypedEntity(untypedRepo.findOneById(id));
	}

	@Override
	public E findOneById(Object id, Fetch fetch)
	{
		return asTypedEntity(untypedRepo.findOneById(id, fetch));
	}

	@Override
	public Stream<E> findAll(Stream<Object> ids)
	{
		return asTypedStream(untypedRepo.findAll(ids));
	}

	@Override
	public Stream<E> findAll(Stream<Object> ids, Fetch fetch)
	{
		return asTypedStream(untypedRepo.findAll(ids, fetch));
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		return untypedRepo.aggregate(aggregateQuery);
	}

	@Override
	public void update(E entity)
	{
		untypedRepo.update(entity);
	}

	@Override
	public void update(Stream<E> entities)
	{
		untypedRepo.update(asUntypedStream(entities));
	}

	@Override
	public void delete(E entity)
	{
		untypedRepo.delete(entity);
	}

	@Override
	public void delete(Stream<E> entities)
	{
		untypedRepo.delete(asUntypedStream(entities));
	}

	@Override
	public void deleteById(Object id)
	{
		untypedRepo.deleteById(id);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		untypedRepo.deleteAll(ids);
	}

	@Override
	public void deleteAll()
	{
		untypedRepo.deleteAll();
	}

	@Override
	public void add(E entity)
	{
		untypedRepo.add(entity);
	}

	@Override
	public Integer add(Stream<E> entities)
	{
		return untypedRepo.add(asUntypedStream(entities));
	}

	@Override
	public void flush()
	{
		untypedRepo.flush();
	}

	@Override
	public void clearCache()
	{
		untypedRepo.clearCache();
	}

	@Override
	public void rebuildIndex()
	{
		untypedRepo.rebuildIndex();
	}

	@Override
	public void addEntityListener(EntityListener entityListener)
	{
		untypedRepo.addEntityListener(entityListener);
	}

	@Override
	public void removeEntityListener(EntityListener entityListener)
	{
		untypedRepo.removeEntityListener(entityListener);
	}

	@SuppressWarnings("unchecked")
	private E asTypedEntity(Entity untypedEntity)
	{
		return systemEntityFactory.create(untypedEntity);
	}

	private Stream<E> asTypedStream(Stream<Entity> untypedEntities)
	{
		return untypedEntities.map(this::asTypedEntity);
	}

	private Stream<Entity> asUntypedStream(Stream<E> typedEntities)
	{
		return (Stream<Entity>) (Stream<? extends Entity>) typedEntities;
	}

	private Query<E> asTypedQuery(Query<Entity> untypedQuery)
	{
		QueryImpl<E> typedQuery = new QueryImpl<>(untypedQuery.getRules());
		typedQuery.setPageSize(untypedQuery.getPageSize());
		typedQuery.setOffset(untypedQuery.getOffset());
		typedQuery.setSort(untypedQuery.getSort());
		typedQuery.setFetch(untypedQuery.getFetch());
		typedQuery.setRepository(this);
		return typedQuery;
	}

	@SuppressWarnings("unchecked")
	private Query<Entity> asUntypedQuery(Query<E> typedQuery)
	{
		return (Query<Entity>) (Query<? extends Entity>) typedQuery;
	}
}