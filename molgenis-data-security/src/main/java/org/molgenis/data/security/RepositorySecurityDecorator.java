package org.molgenis.data.security;

import org.molgenis.data.*;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.exception.EntityTypePermissionDeniedException;
import org.molgenis.security.core.UserPermissionEvaluator;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.security.EntityTypePermission.*;

/**
 * Repository decorated that validates that current user has permission to perform an operation on an entity type's data.
 */
public class RepositorySecurityDecorator extends AbstractRepositoryDecorator<Entity>
{
	private final UserPermissionEvaluator permissionService;

	public RepositorySecurityDecorator(Repository<Entity> delegateRepository, UserPermissionEvaluator permissionService)
	{
		super(delegateRepository);
		this.permissionService = requireNonNull(permissionService);
	}

	@Override
	public Iterator<Entity> iterator()
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, READ_DATA);
		return delegate().iterator();
	}

	@Override
	public void forEachBatched(Fetch fetch, Consumer<List<Entity>> consumer, int batchSize)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, READ_DATA);
		delegate().forEachBatched(fetch, consumer, batchSize);
	}

	@Override
	public void close() throws IOException
	{
		delegate().close();
	}

	@Override
	public long count(Query<Entity> q)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, COUNT_DATA);
		return delegate().count(q);
	}

	@Override
	public Stream<Entity> findAll(Query<Entity> q)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, READ_DATA);
		return delegate().findAll(q);
	}

	@Override
	public Entity findOne(Query<Entity> q)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, READ_DATA);
		return delegate().findOne(q);
	}

	@Override
	public Entity findOneById(Object id)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, READ_DATA);
		return delegate().findOneById(id);
	}

	@Override
	public Entity findOneById(Object id, Fetch fetch)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, READ_DATA);
		return delegate().findOneById(id, fetch);
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, READ_DATA);
		return delegate().findAll(ids);
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids, Fetch fetch)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, READ_DATA);
		return delegate().findAll(ids, fetch);
	}

	@Override
	public long count()
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, COUNT_DATA);
		return delegate().count();
	}

	@Override
	public void update(Entity entity)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, UPDATE_DATA);
		delegate().update(entity);
	}

	@Override
	public void update(Stream<Entity> entities)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, UPDATE_DATA);
		delegate().update(entities);
	}

	@Override
	public void delete(Entity entity)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, DELETE_DATA);
		delegate().delete(entity);
	}

	@Override
	public void delete(Stream<Entity> entities)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, DELETE_DATA);
		delegate().delete(entities);
	}

	@Override
	public void deleteById(Object id)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, DELETE_DATA);
		delegate().deleteById(id);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, DELETE_DATA);
		delegate().deleteAll(ids);
	}

	@Override
	public void deleteAll()
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, DELETE_DATA);
		delegate().deleteAll();
	}

	@Override
	public void add(Entity entity)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, ADD_DATA);
		delegate().add(entity);
	}

	@Override
	public Integer add(Stream<Entity> entities)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, ADD_DATA);
		return delegate().add(entities);
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, AGGREGATE_DATA);
		return delegate().aggregate(aggregateQuery);
	}

	private void validatePermission(EntityType entityType, EntityTypePermission permission)
	{
		boolean hasPermission = permissionService.hasPermission(new EntityTypeIdentity(entityType.getId()), permission);
		if (!hasPermission)
		{
			throw new EntityTypePermissionDeniedException(permission, entityType);
		}
	}
}
