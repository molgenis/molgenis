package org.molgenis.data;

import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.PermissionService;

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
	private final Repository<Entity> decoratedRepository;
	private final PermissionService permissionService;

	public RepositorySecurityDecorator(Repository<Entity> decoratedRepository, PermissionService permissionService)
	{
		this.decoratedRepository = requireNonNull(decoratedRepository);
		this.permissionService = requireNonNull(permissionService);
	}

	@Override
	protected Repository<Entity> delegate()
	{
		return decoratedRepository;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		EntityType entityType = decoratedRepository.getEntityType();
		validateCurrentUserPermission(entityType, Permission.READ);
		return decoratedRepository.iterator();
	}

	@Override
	public void forEachBatched(Fetch fetch, Consumer<List<Entity>> consumer, int batchSize)
	{
		EntityType entityType = decoratedRepository.getEntityType();
		validateCurrentUserPermission(entityType, Permission.READ);
		decoratedRepository.forEachBatched(fetch, consumer, batchSize);
	}

	@Override
	public long count(Query<Entity> q)
	{
		EntityType entityType = decoratedRepository.getEntityType();
		validateCurrentUserPermission(entityType, Permission.COUNT);
		return decoratedRepository.count(q);
	}

	@Override
	public Stream<Entity> findAll(Query<Entity> q)
	{
		EntityType entityType = decoratedRepository.getEntityType();
		validateCurrentUserPermission(entityType, Permission.READ);
		return decoratedRepository.findAll(q);
	}

	@Override
	public Entity findOne(Query<Entity> q)
	{
		EntityType entityType = decoratedRepository.getEntityType();
		validateCurrentUserPermission(entityType, Permission.READ);
		return decoratedRepository.findOne(q);
	}

	@Override
	public Entity findOneById(Object id)
	{
		EntityType entityType = decoratedRepository.getEntityType();
		validateCurrentUserPermission(entityType, Permission.READ);
		return decoratedRepository.findOneById(id);
	}

	@Override
	public Entity findOneById(Object id, Fetch fetch)
	{
		EntityType entityType = decoratedRepository.getEntityType();
		validateCurrentUserPermission(entityType, Permission.READ);
		return decoratedRepository.findOneById(id, fetch);
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids)
	{
		EntityType entityType = decoratedRepository.getEntityType();
		validateCurrentUserPermission(entityType, Permission.READ);
		return decoratedRepository.findAll(ids);
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids, Fetch fetch)
	{
		EntityType entityType = decoratedRepository.getEntityType();
		validateCurrentUserPermission(entityType, Permission.READ);
		return decoratedRepository.findAll(ids, fetch);
	}

	@Override
	public long count()
	{
		EntityType entityType = decoratedRepository.getEntityType();
		validateCurrentUserPermission(entityType, Permission.COUNT);
		return decoratedRepository.count();
	}

	@Override
	public void update(Entity entity)
	{
		EntityType entityType = decoratedRepository.getEntityType();
		validateCurrentUserPermission(entityType, Permission.WRITE);
		decoratedRepository.update(entity);
	}

	@Override
	public void update(Stream<Entity> entities)
	{
		EntityType entityType = decoratedRepository.getEntityType();
		validateCurrentUserPermission(entityType, Permission.WRITE);
		decoratedRepository.update(entities);
	}

	@Override
	public void delete(Entity entity)
	{
		EntityType entityType = decoratedRepository.getEntityType();
		validateCurrentUserPermission(entityType, Permission.WRITE);
		decoratedRepository.delete(entity);
	}

	@Override
	public void delete(Stream<Entity> entities)
	{
		EntityType entityType = decoratedRepository.getEntityType();
		validateCurrentUserPermission(entityType, Permission.WRITE);
		decoratedRepository.delete(entities);
	}

	@Override
	public void deleteById(Object id)
	{
		EntityType entityType = decoratedRepository.getEntityType();
		validateCurrentUserPermission(entityType, Permission.WRITE);
		decoratedRepository.deleteById(id);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		EntityType entityType = decoratedRepository.getEntityType();
		validateCurrentUserPermission(entityType, Permission.WRITE);
		decoratedRepository.deleteAll(ids);
	}

	@Override
	public void deleteAll()
	{
		EntityType entityType = decoratedRepository.getEntityType();
		validateCurrentUserPermission(entityType, Permission.WRITE);
		decoratedRepository.deleteAll();
	}

	@Override
	public void add(Entity entity)
	{
		EntityType entityType = decoratedRepository.getEntityType();
		validateCurrentUserPermission(entityType, Permission.WRITE);
		decoratedRepository.add(entity);
	}

	@Override
	public Integer add(Stream<Entity> entities)
	{
		EntityType entityType = decoratedRepository.getEntityType();
		validateCurrentUserPermission(entityType, Permission.WRITE);
		return decoratedRepository.add(entities);
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		EntityType entityType = decoratedRepository.getEntityType();
		validateCurrentUserPermission(entityType, Permission.COUNT);
		return decoratedRepository.aggregate(aggregateQuery);
	}

	private void validateCurrentUserPermission(EntityType entityType, Permission permission)
	{
		boolean granted = permissionService.hasPermissionOnEntityType(entityType.getId(), permission);
		if (!granted)
		{
			throw new MolgenisDataAccessException(
					format("No '%s' permission on entity type '%s' with id '%s'", permission.toString(),
							entityType.getLabel(), entityType.getId()));
		}
	}
}
