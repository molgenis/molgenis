package org.molgenis.data.security.meta;

import org.molgenis.data.*;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.system.SystemEntityTypeRegistry;
import org.molgenis.data.security.auth.GroupAuthority;
import org.molgenis.data.security.auth.UserAuthority;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.PermissionService;
import org.molgenis.security.core.utils.SecurityUtils;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.security.auth.AuthorityMetaData.ROLE;
import static org.molgenis.data.security.auth.GroupAuthorityMetaData.GROUP_AUTHORITY;
import static org.molgenis.data.security.auth.UserAuthorityMetaData.USER_AUTHORITY;
import static org.molgenis.data.security.util.SecurityDecoratorUtils.validatePermission;
import static org.molgenis.security.core.Permission.COUNT;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserIsSuOrSystem;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserIsSystem;

/**
 * Decorator for the entity type repository:
 * - filters requested entities based on the permissions of the current user
 * - validates permissions when adding, updating or deleting entity types
 * <p>
 * TODO replace permission based entity filtering with generic row-level security once available
 */
public class EntityTypeRepositorySecurityDecorator extends AbstractRepositoryDecorator<EntityType>
{
	private final SystemEntityTypeRegistry systemEntityTypeRegistry;
	private final PermissionService permissionService;
	private final DataService dataService;

	public EntityTypeRepositorySecurityDecorator(Repository<EntityType> delegateRepository,
			SystemEntityTypeRegistry systemEntityTypeRegistry, PermissionService permissionService,
			DataService dataService)
	{
		super(delegateRepository);
		this.systemEntityTypeRegistry = requireNonNull(systemEntityTypeRegistry);
		this.permissionService = requireNonNull(permissionService);
		this.dataService = requireNonNull(dataService);
	}

	@Override
	public long count()
	{
		if (currentUserIsSuOrSystem())
		{
			return delegate().count();
		}
		else
		{
			Stream<EntityType> EntityTypes = StreamSupport.stream(delegate().spliterator(), false);
			return filterCountPermission(EntityTypes).count();
		}
	}

	@Override
	public long count(Query<EntityType> q)
	{
		if (currentUserIsSuOrSystem())
		{
			return delegate().count(q);
		}
		else
		{
			// ignore query offset and page size
			Query<EntityType> qWithoutLimitOffset = new QueryImpl<>(q);
			qWithoutLimitOffset.offset(0).pageSize(Integer.MAX_VALUE);
			Stream<EntityType> EntityTypes = delegate().findAll(qWithoutLimitOffset);
			return filterCountPermission(EntityTypes).count();
		}
	}

	//Users with COUNT permission on an entity need to be able to READ the METAdata of this entity
	//see: https://github.com/molgenis/molgenis/issues/6383
	@Override
	public Stream<EntityType> findAll(Query<EntityType> q)
	{
		if (currentUserIsSuOrSystem())
		{
			return delegate().findAll(q);
		}
		else
		{
			Query<EntityType> qWithoutLimitOffset = new QueryImpl<>(q);
			qWithoutLimitOffset.offset(0).pageSize(Integer.MAX_VALUE);
			Stream<EntityType> EntityTypes = delegate().findAll(qWithoutLimitOffset);
			Stream<EntityType> filteredEntityTypes = filterCountPermission(EntityTypes);
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

	//Users with COUNT permission on an entity need to be able to READ the METAdata of this entity
	//see: https://github.com/molgenis/molgenis/issues/6383
	@Override
	public Iterator<EntityType> iterator()
	{
		if (currentUserIsSuOrSystem())
		{
			return delegate().iterator();
		}
		else
		{
			Stream<EntityType> EntityTypeStream = StreamSupport.stream(delegate().spliterator(), false);
			return filterCountPermission(EntityTypeStream).iterator();
		}
	}

	//Users with COUNT permission on an entity need to be able to READ the METAdata of this entity
	//see: https://github.com/molgenis/molgenis/issues/6383
	@Override
	public void forEachBatched(Fetch fetch, Consumer<List<EntityType>> consumer, int batchSize)
	{
		if (currentUserIsSuOrSystem())
		{
			delegate().forEachBatched(fetch, consumer, batchSize);
		}
		else
		{
			FilteredConsumer filteredConsumer = new FilteredConsumer(consumer, permissionService);
			delegate().forEachBatched(fetch, filteredConsumer::filter, batchSize);
		}
	}

	//Users with COUNT permission on an entity need to be able to READ the METAdata of this entity
	//see: https://github.com/molgenis/molgenis/issues/6383
	@Override
	public EntityType findOne(Query<EntityType> q)
	{
		if (currentUserIsSuOrSystem())
		{
			return delegate().findOne(q);
		}
		else
		{
			// ignore query offset and page size
			return filterCountPermission(delegate().findOne(q));
		}
	}

	//Users with COUNT permission on an entity need to be able to READ the METAdata of this entity
	//see: https://github.com/molgenis/molgenis/issues/6383
	@Override
	public EntityType findOneById(Object id)
	{
		if (currentUserIsSuOrSystem())
		{
			return delegate().findOneById(id);
		}
		else
		{
			return filterCountPermission(delegate().findOneById(id));
		}
	}

	//Users with COUNT permission on an entity need to be able to READ the METAdata of this entity
	//see: https://github.com/molgenis/molgenis/issues/6383
	@Override
	public EntityType findOneById(Object id, Fetch fetch)
	{
		if (currentUserIsSuOrSystem())
		{
			return delegate().findOneById(id, fetch);
		}
		else
		{
			return filterCountPermission(delegate().findOneById(id, fetch));
		}
	}

	//Users with COUNT permission on an entity need to be able to READ the METAdata of this entity
	//see: https://github.com/molgenis/molgenis/issues/6383
	@Override
	public Stream<EntityType> findAll(Stream<Object> ids)
	{
		if (currentUserIsSuOrSystem())
		{
			return delegate().findAll(ids);
		}
		else
		{
			return filterCountPermission(delegate().findAll(ids));
		}
	}

	//Users with COUNT permission on an entity need to be able to READ the METAdata of this entity
	//see: https://github.com/molgenis/molgenis/issues/6383
	@Override
	public Stream<EntityType> findAll(Stream<Object> ids, Fetch fetch)
	{
		if (currentUserIsSuOrSystem())
		{
			return delegate().findAll(ids, fetch);
		}
		else
		{
			return filterCountPermission(delegate().findAll(ids, fetch));
		}
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		if (currentUserIsSuOrSystem())
		{
			return delegate().aggregate(aggregateQuery);
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

	private EntityType filterCountPermission(EntityType entityType)
	{
		return entityType != null ? filterCountPermission(Stream.of(entityType)).findFirst().orElse(null) : null;
	}

	private Stream<EntityType> filterCountPermission(Stream<EntityType> EntityTypeStream)
	{
		return filterPermission(EntityTypeStream, COUNT);
	}

	private Stream<EntityType> filterPermission(Stream<EntityType> EntityTypeStream, Permission permission)
	{
		return EntityTypeStream.filter(
				entityType -> permissionService.hasPermissionOnEntityType(entityType.getId(), permission));
	}

	private static class FilteredConsumer
	{
		private final Consumer<List<EntityType>> consumer;
		private final PermissionService permissionService;

		FilteredConsumer(Consumer<List<EntityType>> consumer, PermissionService permissionService)
		{
			this.consumer = requireNonNull(consumer);
			this.permissionService = requireNonNull(permissionService);
		}

		void filter(List<EntityType> entityTypes)
		{
			List<EntityType> filteredEntityTypes = entityTypes.stream()
															  .filter(entityType -> permissionService.hasPermissionOnEntityType(
																	  entityType.getId(), COUNT))
															  .collect(toList());
			consumer.accept(filteredEntityTypes);
		}
	}
}