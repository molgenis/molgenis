package org.molgenis.data.security;

import org.molgenis.data.*;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.security.core.UserPermissionEvaluator;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Repository decorated that validates that current user has permission to perform an operation for an entity type.
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
		validatePermission(entityType, EntityTypePermission.READ);
		return delegate().iterator();
	}

	@Override
	public void forEachBatched(Fetch fetch, Consumer<List<Entity>> consumer, int batchSize)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, EntityTypePermission.READ);
		delegate().forEachBatched(fetch, consumer, batchSize);
	}

	@Override
	public void close() throws IOException
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, EntityTypePermission.WRITE);
		delegate().close();
	}

	@Override
	public long count(Query<Entity> q)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, EntityTypePermission.COUNT);
		return delegate().count(q);
	}

	@Override
	public Stream<Entity> findAll(Query<Entity> q)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, EntityTypePermission.READ);
		return delegate().findAll(q);
	}

	@Override
	public Entity findOne(Query<Entity> q)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, EntityTypePermission.READ);
		return delegate().findOne(q);
	}

	@Override
	public Entity findOneById(Object id)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, EntityTypePermission.READ);
		return delegate().findOneById(id);
	}

	@Override
	public Entity findOneById(Object id, Fetch fetch)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, EntityTypePermission.READ);
		return delegate().findOneById(id, fetch);
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, EntityTypePermission.READ);
		return delegate().findAll(ids);
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids, Fetch fetch)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, EntityTypePermission.READ);
		return delegate().findAll(ids, fetch);
	}

	@Override
	public long count()
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, EntityTypePermission.COUNT);
		return delegate().count();
	}

	@Override
	public void update(Entity entity)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, EntityTypePermission.WRITE);
		delegate().update(entity);
	}

	@Override
	public void update(Stream<Entity> entities)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, EntityTypePermission.WRITE);
		delegate().update(entities);
	}

	@Override
	public void delete(Entity entity)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, EntityTypePermission.WRITE);
		delegate().delete(entity);
	}

	@Override
	public void delete(Stream<Entity> entities)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, EntityTypePermission.WRITE);
		delegate().delete(entities);
	}

	@Override
	public void deleteById(Object id)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, EntityTypePermission.WRITE);
		delegate().deleteById(id);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, EntityTypePermission.WRITE);
		delegate().deleteAll(ids);
	}

	@Override
	public void deleteAll()
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, EntityTypePermission.WRITE);
		delegate().deleteAll();
	}

	@Override
	public void add(Entity entity)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, EntityTypePermission.WRITE);
		delegate().add(entity);
	}

	@Override
	public Integer add(Stream<Entity> entities)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, EntityTypePermission.WRITE);
		return delegate().add(entities);
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		EntityType entityType = delegate().getEntityType();
		validatePermission(entityType, EntityTypePermission.COUNT);
		return delegate().aggregate(aggregateQuery);
	}

	private void validatePermission(EntityType entityType, EntityTypePermission permission)
	{
		boolean hasPermission = permissionService.hasPermission(new EntityTypeIdentity(entityType.getId()), permission);
		if (!hasPermission)
		{

			throw new MolgenisDataAccessException(
					format("No [%s] permission on entity type [%s] with id [%s]", toMessagePermission(permission),
							entityType.getLabel(), entityType.getId()));
		}
	}

	private static String toMessagePermission(EntityTypePermission permission)
	{
		String permissionStr;
		if (permission == EntityTypePermission.COUNT)
		{
			permissionStr = "COUNT";
		}
		else if (permission == EntityTypePermission.READ)
		{
			permissionStr = "READ";
		}
		else if (permission == EntityTypePermission.WRITE)
		{
			permissionStr = "WRITE";
		}
		else if (permission == EntityTypePermission.WRITEMETA)
		{
			permissionStr = "WRITEMETA";
		}
		else
		{
			throw new IllegalArgumentException("Illegal entity type permission");
		}
		return permissionStr;
	}
}
