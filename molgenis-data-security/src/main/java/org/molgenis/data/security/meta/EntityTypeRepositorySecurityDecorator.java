package org.molgenis.data.security.meta;

import org.molgenis.auth.GroupAuthority;
import org.molgenis.auth.UserAuthority;
import org.molgenis.data.*;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.system.SystemEntityTypeRegistry;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.utils.SecurityUtils;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.auth.AuthorityMetaData.ROLE;
import static org.molgenis.auth.GroupAuthorityMetaData.GROUP_AUTHORITY;
import static org.molgenis.auth.UserAuthorityMetaData.USER_AUTHORITY;
import static org.molgenis.security.core.Permission.COUNT;
import static org.molgenis.security.core.Permission.READ;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserIsSuOrSystem;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserIsSystem;
import static org.molgenis.util.SecurityDecoratorUtils.validatePermission;

/**
 * Decorator for the entity type repository:
 * - filters requested entities based on the permissions of the current user
 * - validates permissions when adding, updating or deleting entity types
 * <p>
 * TODO replace permission based entity filtering with generic row-level security once available
 */
public class EntityTypeRepositorySecurityDecorator extends AbstractRepositoryDecorator<EntityType>
{
	private final Repository<EntityType> decoratedRepo;
	private final SystemEntityTypeRegistry systemEntityTypeRegistry;
	private final MolgenisPermissionService permissionService;
	private final DataService dataService;

	public EntityTypeRepositorySecurityDecorator(Repository<EntityType> decoratedRepo,
			SystemEntityTypeRegistry systemEntityTypeRegistry, MolgenisPermissionService permissionService,
			DataService dataService)
	{
		this.decoratedRepo = requireNonNull(decoratedRepo);
		this.systemEntityTypeRegistry = requireNonNull(systemEntityTypeRegistry);
		this.permissionService = requireNonNull(permissionService);
		this.dataService = requireNonNull(dataService);
	}

	@Override
	protected Repository<EntityType> delegate()
	{
		return decoratedRepo;
	}

	@Override
	public long count()
	{
		if (currentUserIsSuOrSystem())
		{
			return decoratedRepo.count();
		}
		else
		{
			Stream<EntityType> EntityTypes = StreamSupport.stream(decoratedRepo.spliterator(), false);
			return filterCountPermission(EntityTypes).count();
		}
	}

	@Override
	public long count(Query<EntityType> q)
	{
		if (currentUserIsSuOrSystem())
		{
			return decoratedRepo.count(q);
		}
		else
		{
			// ignore query offset and page size
			Query<EntityType> qWithoutLimitOffset = new QueryImpl<>(q);
			qWithoutLimitOffset.offset(0).pageSize(Integer.MAX_VALUE);
			Stream<EntityType> EntityTypes = decoratedRepo.findAll(qWithoutLimitOffset);
			return filterCountPermission(EntityTypes).count();
		}
	}

	@Override
	public Stream<EntityType> findAll(Query<EntityType> q)
	{
		if (currentUserIsSuOrSystem())
		{
			return decoratedRepo.findAll(q);
		}
		else
		{
			Query<EntityType> qWithoutLimitOffset = new QueryImpl<>(q);
			qWithoutLimitOffset.offset(0).pageSize(Integer.MAX_VALUE);
			Stream<EntityType> EntityTypes = decoratedRepo.findAll(qWithoutLimitOffset);
			Stream<EntityType> filteredEntityTypes = filterReadPermission(EntityTypes);
			if (q.getOffset() > 0)
			{
				filteredEntityTypes = filteredEntityTypes.skip(q.getOffset());
			}
			if (q.getPageSize() > 0)
			{
				filteredEntityTypes = filteredEntityTypes.limit(q.getPageSize());
			}
			return filteredEntityTypes;
		}
	}

	@Override
	public Iterator<EntityType> iterator()
	{
		if (currentUserIsSuOrSystem())
		{
			return decoratedRepo.iterator();
		}
		else
		{
			Stream<EntityType> EntityTypeStream = StreamSupport.stream(decoratedRepo.spliterator(), false);
			return filterReadPermission(EntityTypeStream).iterator();
		}
	}

	@Override
	public void forEachBatched(Fetch fetch, Consumer<List<EntityType>> consumer, int batchSize)
	{
		if (currentUserIsSuOrSystem())
		{
			decoratedRepo.forEachBatched(fetch, consumer, batchSize);
		}
		else
		{
			FilteredConsumer filteredConsumer = new FilteredConsumer(consumer, permissionService);
			decoratedRepo.forEachBatched(fetch, filteredConsumer::filter, batchSize);
		}
	}

	@Override
	public EntityType findOne(Query<EntityType> q)
	{
		if (currentUserIsSuOrSystem())
		{
			return decoratedRepo.findOne(q);
		}
		else
		{
			// ignore query offset and page size
			return filterReadPermission(decoratedRepo.findOne(q));
		}
	}

	@Override
	public EntityType findOneById(Object id)
	{
		if (currentUserIsSuOrSystem())
		{
			return decoratedRepo.findOneById(id);
		}
		else
		{
			return filterReadPermission(decoratedRepo.findOneById(id));
		}
	}

	@Override
	public EntityType findOneById(Object id, Fetch fetch)
	{
		if (currentUserIsSuOrSystem())
		{
			return decoratedRepo.findOneById(id, fetch);
		}
		else
		{
			return filterReadPermission(decoratedRepo.findOneById(id, fetch));
		}
	}

	@Override
	public Stream<EntityType> findAll(Stream<Object> ids)
	{
		if (currentUserIsSuOrSystem())
		{
			return decoratedRepo.findAll(ids);
		}
		else
		{
			return filterReadPermission(decoratedRepo.findAll(ids));
		}
	}

	@Override
	public Stream<EntityType> findAll(Stream<Object> ids, Fetch fetch)
	{
		if (currentUserIsSuOrSystem())
		{
			return decoratedRepo.findAll(ids, fetch);
		}
		else
		{
			return filterReadPermission(decoratedRepo.findAll(ids, fetch));
		}
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		if (currentUserIsSuOrSystem())
		{
			return decoratedRepo.aggregate(aggregateQuery);
		}
		else
		{
			throw new MolgenisDataAccessException(format("Aggregation on entity [%s] not allowed", getName()));
		}
	}

	@Override
	public void update(EntityType entity)
	{
		validateUpdateAllowed(entity);
		super.update(entity);
	}

	@Override
	public void update(Stream<EntityType> entities)
	{
		super.update(entities.filter(entityType ->
		{
			validateUpdateAllowed(entityType);
			return true;
		}));
	}

	@Override
	public void delete(EntityType entity)
	{
		validateDeleteAllowed(entity);
		deleteEntityPermissions(entity);
		super.delete(entity);
	}

	@Override
	public void delete(Stream<EntityType> entities)
	{
		super.delete(entities.filter(entityType ->
		{
			validateDeleteAllowed(entityType);
			deleteEntityPermissions(entityType);
			return true;
		}));
	}

	@Override
	public void deleteById(Object id)
	{
		validateDeleteAllowed(id);
		deleteEntityPermissions(id.toString());
		super.deleteById(id);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		super.deleteAll(ids.filter(id ->
		{
			validateDeleteAllowed(id);
			deleteEntityPermissions(id.toString());
			return true;
		}));
	}

	@Override
	public void deleteAll()
	{
		iterator().forEachRemaining(entityType ->
		{
			this.validateDeleteAllowed(entityType);
			deleteEntityPermissions(entityType);
		});
		super.deleteAll();
	}

	private void deleteEntityPermissions(EntityType entityType)
	{
		deleteEntityPermissions(entityType.getId());
	}

	private void deleteEntityPermissions(String entityTypeId)
	{
		List<String> authorities = SecurityUtils.getEntityAuthorities(entityTypeId);

		// User permissions
		List<UserAuthority> userPermissions = dataService.query(USER_AUTHORITY, UserAuthority.class)
														 .in(ROLE, authorities)
														 .findAll()
														 .collect(toList());
		if (!userPermissions.isEmpty())
		{
			dataService.delete(USER_AUTHORITY, userPermissions.stream());
		}
		// Group permissions
		List<GroupAuthority> groupPermissions = dataService.query(GROUP_AUTHORITY, GroupAuthority.class)
														   .in(ROLE, authorities)
														   .findAll()
														   .collect(toList());
		if (!groupPermissions.isEmpty())
		{
			dataService.delete(GROUP_AUTHORITY, groupPermissions.stream());
		}
	}

	@Override
	public void add(EntityType entity)
	{
		validateAddAllowed(entity);
		super.add(entity);
	}

	@Override
	public Integer add(Stream<EntityType> entities)
	{
		return super.add(entities.filter(entityType ->
		{
			validateAddAllowed(entityType);
			return true;
		}));
	}

	private void validateAddAllowed(EntityType entityType)
	{
		validatePermission(entityType, Permission.WRITEMETA);
	}

	/**
	 * Updating entityType meta data is allowed for non-system entities. For system entities updating entityType meta data is
	 * only allowed if the meta data defined in Java differs from the meta data stored in the database (in other words
	 * the Java code was updated).
	 *
	 * @param entityType entity meta data
	 */
	private void validateUpdateAllowed(EntityType entityType)
	{
		validatePermission(entityType, Permission.WRITEMETA);

		boolean isSystem = systemEntityTypeRegistry.hasSystemEntityType(entityType.getId());
		//FIXME: should only be possible to update system entities during bootstrap!
		if (isSystem && !currentUserIsSystem())
		{
			throw new MolgenisDataException(
					format("Updating system entity meta data [%s] is not allowed", entityType.getLabel()));
		}
	}

	private void validateDeleteAllowed(EntityType entityType)
	{
		validatePermission(entityType, Permission.WRITEMETA);

		String entityTypeId = entityType.getId();
		boolean isSystem = systemEntityTypeRegistry.hasSystemEntityType(entityTypeId);
		if (isSystem)
		{
			throw new MolgenisDataException(
					format("Deleting system entity meta data [%s] is not allowed", entityTypeId));
		}
	}

	private void validateDeleteAllowed(Object entityTypeId)
	{
		EntityType entityType = findOneById(entityTypeId);
		if (entityType == null)
		{
			throw new UnknownEntityException(
					format("Unknown entity meta data [%s] with id [%s]", getName(), entityTypeId.toString()));
		}
		validateDeleteAllowed(entityType);
	}

	private EntityType filterReadPermission(EntityType entityType)
	{
		return entityType != null ? filterReadPermission(Stream.of(entityType)).findFirst().orElse(null) : null;
	}

	private Stream<EntityType> filterReadPermission(Stream<EntityType> EntityTypeStream)
	{
		return filterPermission(EntityTypeStream, READ);
	}

	private Stream<EntityType> filterCountPermission(Stream<EntityType> EntityTypeStream)
	{
		return filterPermission(EntityTypeStream, COUNT);
	}

	private Stream<EntityType> filterPermission(Stream<EntityType> EntityTypeStream, Permission permission)
	{
		return EntityTypeStream.filter(
				entityType -> permissionService.hasPermissionOnEntity(entityType.getId(), permission));
	}

	private static class FilteredConsumer
	{
		private final Consumer<List<EntityType>> consumer;
		private final MolgenisPermissionService permissionService;

		FilteredConsumer(Consumer<List<EntityType>> consumer, MolgenisPermissionService permissionService)
		{
			this.consumer = requireNonNull(consumer);
			this.permissionService = requireNonNull(permissionService);
		}

		void filter(List<EntityType> entityTypes)
		{
			List<EntityType> filteredEntityTypes = entityTypes.stream()
															  .filter(entityType -> permissionService.hasPermissionOnEntity(
																	  entityType.getId(), READ))
															  .collect(toList());
			consumer.accept(filteredEntityTypes);
		}
	}
}