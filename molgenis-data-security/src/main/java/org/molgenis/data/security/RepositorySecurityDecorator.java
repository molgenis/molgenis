package org.molgenis.data.security;

import org.molgenis.data.*;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.security.core.Permission;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.molgenis.data.security.util.SecurityDecoratorUtils.validatePermission;

/**
 * Repository decorated that validates that current user has permission to perform an operation for an entity type.
 */
public class RepositorySecurityDecorator extends AbstractRepositoryDecorator<Entity>
{
	public RepositorySecurityDecorator(Repository<Entity> delegateRepository)
	{
		super(delegateRepository);
	}

	@Override
	public Iterator<Entity> iterator()
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, Permission.READ);
		return delegate().iterator();
	}

	@Override
	public void forEachBatched(Fetch fetch, Consumer<List<Entity>> consumer, int batchSize)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, Permission.READ);
		delegate().forEachBatched(fetch, consumer, batchSize);
	}

	@Override
	public void close() throws IOException
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, Permission.WRITE);
		delegate().close();
	}

	@Override
	public long count(Query<Entity> q)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, Permission.COUNT);
		return delegate().count(q);
	}

	@Override
	public Stream<Entity> findAll(Query<Entity> q)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, Permission.READ);
		return delegate().findAll(q);
	}

	@Override
	public Entity findOne(Query<Entity> q)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, Permission.READ);
		return delegate().findOne(q);
	}

	@Override
	public Entity findOneById(Object id)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, Permission.READ);
		return delegate().findOneById(id);
	}

	@Override
	public Entity findOneById(Object id, Fetch fetch)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, Permission.READ);
		return delegate().findOneById(id, fetch);
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, Permission.READ);
		return delegate().findAll(ids);
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids, Fetch fetch)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, Permission.READ);
		return delegate().findAll(ids, fetch);
	}

	@Override
	public long count()
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, Permission.COUNT);
		return delegate().count();
	}

	@Override
	public void update(Entity entity)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, Permission.WRITE);
		delegate().update(entity);
	}

	@Override
	public void update(Stream<Entity> entities)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, Permission.WRITE);
		delegate().update(entities);
	}

	@Override
	public void delete(Entity entity)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, Permission.WRITE);
		delegate().delete(entity);
	}

	@Override
	public void delete(Stream<Entity> entities)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, Permission.WRITE);
		delegate().delete(entities);
	}

	@Override
	public void deleteById(Object id)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, Permission.WRITE);
		delegate().deleteById(id);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, Permission.WRITE);
		delegate().deleteAll(ids);
	}

	@Override
	public void deleteAll()
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, Permission.WRITE);
		delegate().deleteAll();
	}

	@Override
	public void add(Entity entity)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, Permission.WRITE);
		delegate().add(entity);
	}

	@Override
	public Integer add(Stream<Entity> entities)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, Permission.WRITE);
		return delegate().add(entities);
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, Permission.COUNT);
		return delegate().aggregate(aggregateQuery);
	}
}
