package org.molgenis.data.meta;

import com.google.common.collect.Sets;
import com.google.common.collect.TreeTraverser;
import org.molgenis.data.*;
import org.molgenis.data.meta.model.*;
import org.molgenis.data.meta.system.SystemEntityMetaDataRegistry;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.molgenis.auth.AuthorityMetaData.ROLE;
import static org.molgenis.auth.GroupAuthorityMetaData.GROUP_AUTHORITY;
import static org.molgenis.auth.UserAuthorityMetaData.USER_AUTHORITY;
import static org.molgenis.data.meta.model.AttributeMetaDataMetaData.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.ENTITY_META_DATA;
import static org.molgenis.data.meta.model.TagMetaData.TAG;
import static org.molgenis.security.core.Permission.COUNT;
import static org.molgenis.security.core.Permission.READ;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserIsSu;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserisSystem;
import static org.molgenis.util.SecurityDecoratorUtils.validatePermission;

public class EntityMetaDataRepositoryDecorator implements Repository<EntityMetaData>
{
	private final Repository<EntityMetaData> decoratedRepo;
	private final DataService dataService;
	private final SystemEntityMetaDataRegistry systemEntityMetaDataRegistry;
	private final MolgenisPermissionService permissionService;

	public EntityMetaDataRepositoryDecorator(Repository<EntityMetaData> decoratedRepo, DataService dataService,
			SystemEntityMetaDataRegistry systemEntityMetaDataRegistry, MolgenisPermissionService permissionService)
	{
		this.decoratedRepo = requireNonNull(decoratedRepo);
		this.dataService = requireNonNull(dataService);
		this.systemEntityMetaDataRegistry = requireNonNull(systemEntityMetaDataRegistry);
		this.permissionService = requireNonNull(permissionService);
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return decoratedRepo.getCapabilities();
	}

	@Override
	public void close() throws IOException
	{
		decoratedRepo.close();
	}

	@Override
	public String getName()
	{
		return decoratedRepo.getName();
	}

	@Override
	public Set<QueryRule.Operator> getQueryOperators()
	{
		return decoratedRepo.getQueryOperators();
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return decoratedRepo.getEntityMetaData();
	}

	@Override
	public long count()
	{
		if (currentUserIsSu() || currentUserisSystem())
		{
			return decoratedRepo.count();
		}
		else
		{
			Stream<EntityMetaData> entityMetaDatas = StreamSupport.stream(decoratedRepo.spliterator(), false);
			return filterCountPermission(entityMetaDatas).count();
		}
	}

	@Override
	public Query<EntityMetaData> query()
	{
		return decoratedRepo.query();
	}

	@Override
	public long count(Query<EntityMetaData> q)
	{
		if (currentUserIsSu() || currentUserisSystem())
		{
			return decoratedRepo.count(q);
		}
		else
		{
			// ignore query offset and page size
			Stream<EntityMetaData> entityMetaDatas = decoratedRepo.findAll(q);
			return filterCountPermission(entityMetaDatas).count();
		}
	}

	@Override
	public Stream<EntityMetaData> findAll(Query<EntityMetaData> q)
	{
		if (currentUserIsSu() || currentUserisSystem())
		{
			return decoratedRepo.findAll(q);
		}
		else
		{
			Query<EntityMetaData> qWithoutLimitOffset = new QueryImpl<>(q);
			qWithoutLimitOffset.offset(0).pageSize(Integer.MAX_VALUE);
			Stream<EntityMetaData> entityMetaDatas = decoratedRepo.findAll(qWithoutLimitOffset);
			Stream<EntityMetaData> filteredEntityMetaDatas = filterReadPermission(entityMetaDatas);
			if (q.getOffset() > 0)
			{
				filteredEntityMetaDatas = filteredEntityMetaDatas.skip(q.getOffset());
			}
			if (q.getPageSize() > 0)
			{
				filteredEntityMetaDatas = filteredEntityMetaDatas.limit(q.getPageSize());
			}
			return filteredEntityMetaDatas;
		}
	}

	@Override
	public Iterator<EntityMetaData> iterator()
	{
		if (currentUserIsSu() || currentUserisSystem())
		{
			return decoratedRepo.iterator();
		}
		else
		{
			Stream<EntityMetaData> entityMetaDataStream = StreamSupport.stream(decoratedRepo.spliterator(), false);
			return filterReadPermission(entityMetaDataStream).iterator();
		}
	}

	@Override
	public void forEachBatched(Fetch fetch, Consumer<List<EntityMetaData>> consumer, int batchSize)
	{
		if (currentUserIsSu() || currentUserisSystem())
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
	public EntityMetaData findOne(Query<EntityMetaData> q)
	{
		if (currentUserIsSu() || currentUserisSystem())
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
	public EntityMetaData findOneById(Object id)
	{
		if (currentUserIsSu() || currentUserisSystem())
		{
			return decoratedRepo.findOneById(id);
		}
		else
		{
			return filterReadPermission(decoratedRepo.findOneById(id));
		}
	}

	@Override
	public EntityMetaData findOneById(Object id, Fetch fetch)
	{
		if (currentUserIsSu() || currentUserisSystem())
		{
			return decoratedRepo.findOneById(id, fetch);
		}
		else
		{
			return filterReadPermission(decoratedRepo.findOneById(id, fetch));
		}
	}

	@Override
	public Stream<EntityMetaData> findAll(Stream<Object> ids)
	{
		if (currentUserIsSu() || currentUserisSystem())
		{
			return decoratedRepo.findAll(ids);
		}
		else
		{
			return filterReadPermission(decoratedRepo.findAll(ids));
		}
	}

	@Override
	public Stream<EntityMetaData> findAll(Stream<Object> ids, Fetch fetch)
	{
		if (currentUserIsSu() || currentUserisSystem())
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
		if (currentUserIsSu() || currentUserisSystem())
		{
			return decoratedRepo.aggregate(aggregateQuery);
		}
		else
		{
			throw new MolgenisDataAccessException(format("Aggregation on entity [%s] not allowed", getName()));
		}
	}

	@Transactional
	@Override
	public void update(EntityMetaData entity)
	{
		updateEntity(entity);
	}

	@Transactional
	@Override
	public void update(Stream<EntityMetaData> entities)
	{
		entities.forEach(this::updateEntity);
	}

	@Transactional
	@Override
	public void delete(EntityMetaData entity)
	{
		deleteEntityMetaData(entity);
	}

	@Transactional
	@Override
	public void delete(Stream<EntityMetaData> entities)
	{
		entities.forEach(this::deleteEntityMetaData);
	}

	@Transactional
	@Override
	public void deleteById(Object id)
	{
		EntityMetaData entityMetaData = findOneById(id);
		if (entityMetaData == null)
		{
			throw new UnknownEntityException(
					format("Unknown entity meta data [%s] with id [%s]", getName(), id.toString()));
		}
		deleteEntityMetaData(entityMetaData);
	}

	@Transactional
	@Override
	public void deleteAll(Stream<Object> ids)
	{
		findAll(ids).forEach(this::deleteEntityMetaData);
	}

	@Transactional
	@Override
	public void deleteAll()
	{
		iterator().forEachRemaining(this::deleteEntityMetaData);
	}

	@Transactional
	@Override
	public void add(EntityMetaData entity)
	{
		addEntityMetaData(entity);
	}

	@Transactional
	@Override
	public Integer add(Stream<EntityMetaData> entities)
	{
		AtomicInteger count = new AtomicInteger();
		entities.filter(entity -> {
			count.incrementAndGet();
			return true;
		}).forEach(this::addEntityMetaData);
		return count.get();
	}

	private void addEntityMetaData(EntityMetaData entityMetaData)
	{
		validateAddAllowed(entityMetaData);

		if (entityMetaData.getBackend() == null)
		{
			entityMetaData.setBackend(dataService.getMeta().getDefaultBackend().getName());
		}

		// add row to entities table
		decoratedRepo.add(entityMetaData);
		if (!entityMetaData.isAbstract() && !dataService.getMeta().isMetaEntityMetaData(entityMetaData))
		{
			dataService.getMeta().getBackend(entityMetaData.getBackend()).createRepository(entityMetaData);
		}
	}

	private void validateAddAllowed(EntityMetaData entityMetaData)
	{
		String entityName = entityMetaData.getName();
		validatePermission(entityName, Permission.WRITEMETA);

		// TODO replace with exists() once Repository.exists has been implemented
		EntityMetaData existingEntityMetaData = findOneById(entityName,
				new Fetch().field(EntityMetaDataMetaData.FULL_NAME));
		if (existingEntityMetaData != null)
		{
			throw new MolgenisDataException(format("Adding existing entity meta data [%s] is not allowed", entityName));
		}

		// FIXME: Importer validates emd twice!!
		MetaValidationUtils.validateEntityMetaData(entityMetaData);
	}

	private void updateEntity(EntityMetaData entityMetaData)
	{
		validateUpdateAllowed(entityMetaData);

		// TODO replace with exists() once Repository.exists has been implemented
		EntityMetaData existingEntityMetaData = findOneById(entityMetaData.getIdValue());
		if (existingEntityMetaData == null)
		{
			throw new UnknownEntityException(format("Unknown entity meta data [%s] with id [%s]", getName(),
					entityMetaData.getIdValue().toString()));
		}
		updateEntityAttributes(entityMetaData);

		decoratedRepo.update(entityMetaData);
	}

	/**
	 * Updating entityMetaData meta data is allowed for non-system entities. For system entities updating entityMetaData meta data is
	 * only allowed if the meta data defined in Java differs from the meta data stored in the database (in other words
	 * the Java code was updated).
	 *
	 * @param entityMetaData entity meta data
	 */
	private void validateUpdateAllowed(EntityMetaData entityMetaData)
	{
		String entityName = entityMetaData.getName();
		validatePermission(entityName, Permission.WRITEMETA);

		SystemEntityMetaData systemEntityMeta = systemEntityMetaDataRegistry.getSystemEntityMetaData(entityName);
		if (systemEntityMeta != null && !currentUserisSystem())
		{
			throw new MolgenisDataException(format("Updating system entity meta data [%s] is not allowed", entityName));
		}

		MetaValidationUtils.validateEntityMetaData(entityMetaData);
	}

	private void updateEntityAttributes(EntityMetaData entityMetaData)
	{
		EntityMetaData currentEntityMetaData = findOneById(entityMetaData.getIdValue(),
				new Fetch().field(EntityMetaDataMetaData.FULL_NAME)
						.field(EntityMetaDataMetaData.ATTRIBUTES, new Fetch().field(AttributeMetaDataMetaData.NAME)));
		Map<String, AttributeMetaData> currentAttrMap = StreamSupport
				.stream(currentEntityMetaData.getOwnAttributes().spliterator(), false)
				.collect(toMap(AttributeMetaData::getName, Function.identity()));
		Map<String, AttributeMetaData> updateAttrMap = StreamSupport
				.stream(entityMetaData.getOwnAttributes().spliterator(), false)
				.collect(toMap(AttributeMetaData::getName, Function.identity()));

		Set<String> deletedAttrNames = Sets.difference(currentAttrMap.keySet(), updateAttrMap.keySet());
		Set<String> addedAttrNames = Sets.difference(updateAttrMap.keySet(), currentAttrMap.keySet());

		if (!deletedAttrNames.isEmpty() || !addedAttrNames.isEmpty())
		{
			String entityName = entityMetaData.getName();
			String backend = entityMetaData.getBackend();
			RepositoryCollection repoCollection = dataService.getMeta().getBackend(backend);

			if (!deletedAttrNames.isEmpty())
			{
				deletedAttrNames.forEach(deletedAttrName -> {
					repoCollection.deleteAttribute(entityName, deletedAttrName);

					if (entityMetaData.getName().equals(ENTITY_META_DATA))
					{
						// update system entity meta data
						systemEntityMetaDataRegistry.getSystemEntityMetaData(ENTITY_META_DATA)
								.removeAttribute(currentAttrMap.get(deletedAttrName));
					}
				});
			}

			if (!addedAttrNames.isEmpty())
			{
				addedAttrNames.stream().map(updateAttrMap::get).forEach(addedAttrEntity -> {
					repoCollection.addAttribute(entityName, addedAttrEntity);

					if (entityMetaData.getName().equals(ENTITY_META_DATA))
					{
						// update system entity meta data
						systemEntityMetaDataRegistry.getSystemEntityMetaData(ENTITY_META_DATA)
								.addAttribute(addedAttrEntity);
					}
				});
			}
		}
	}

	private void deleteEntityMetaData(EntityMetaData entityMeta)
	{
		validateDeleteAllowed(entityMeta);

		// delete entityMetaData table
		if (!entityMeta.isAbstract())
		{
			deleteEntityRepository(entityMeta);
		}

		// delete entityMetaData permissions
		deleteEntityPermissions(entityMeta);

		// delete row from entities table
		decoratedRepo.delete(entityMeta);

		// delete rows from attributes table
		deleteEntityAttributes(entityMeta);

		// delete rows from tags table
		deleteEntityTags(entityMeta);
	}

	private void validateDeleteAllowed(EntityMetaData entityMetaData)
	{
		String entityName = entityMetaData.getName();
		validatePermission(entityName, Permission.WRITEMETA);

		boolean isSystem = systemEntityMetaDataRegistry.hasSystemEntityMetaData(entityName);
		if (isSystem)
		{
			throw new MolgenisDataException(format("Deleting system entity meta data [%s] is not allowed", entityName));
		}
	}

	private void deleteEntityAttributes(EntityMetaData entityMetaData)
	{
		Iterable<AttributeMetaData> rootAttrs = entityMetaData.getOwnAttributes();
		Stream<AttributeMetaData> allAttrs = StreamSupport.stream(rootAttrs.spliterator(), false).flatMap(
				attrEntity -> StreamSupport
						.stream(new AttributeMetaDataTreeTraverser().postOrderTraversal(attrEntity).spliterator(),
								false));
		dataService.delete(ATTRIBUTE_META_DATA, allAttrs);
	}

	private void deleteEntityTags(EntityMetaData entityMetaData)
	{
		Iterable<Tag> tags = entityMetaData.getTags();
		dataService.delete(TAG, StreamSupport.stream(tags.spliterator(), false));
	}

	private void deleteEntityRepository(EntityMetaData entityMetaData)
	{
		String backend = entityMetaData.getBackend();
		dataService.getMeta().getBackend(backend)
				.deleteRepository(entityMetaData); // FIXME call deleteRepo directly on metadataservice
	}

	private void deleteEntityPermissions(EntityMetaData entityMetaData)
	{
		String entityName = entityMetaData.getName();
		List<String> authorities = SecurityUtils.getEntityAuthorities(entityName);

		// User permissions
		if (dataService.hasRepository(USER_AUTHORITY))
		{
			Stream<Entity> userPermissions = dataService.query(USER_AUTHORITY).in(ROLE, authorities).findAll();
			dataService.delete(USER_AUTHORITY, userPermissions);
		}

		// Group permissions
		if (dataService.hasRepository(GROUP_AUTHORITY))
		{
			Stream<Entity> groupPermissions = dataService.query(GROUP_AUTHORITY).in(ROLE, authorities).findAll();
			dataService.delete(GROUP_AUTHORITY, groupPermissions);
		}
	}

	private static class AttributeMetaDataTreeTraverser extends TreeTraverser<AttributeMetaData>
	{

		@Override
		public Iterable<AttributeMetaData> children(@Nonnull AttributeMetaData attr)
		{
			return attr.getAttributeParts();
		}

	}

	private EntityMetaData filterReadPermission(EntityMetaData entityMeta)
	{
		return entityMeta != null ? filterReadPermission(Stream.of(entityMeta)).findFirst().orElse(null) : null;
	}

	private Stream<EntityMetaData> filterReadPermission(Stream<EntityMetaData> entityMetaDataStream)
	{
		return filterPermission(entityMetaDataStream, READ);
	}

	private Stream<EntityMetaData> filterCountPermission(Stream<EntityMetaData> entityMetaDataStream)
	{
		return filterPermission(entityMetaDataStream, COUNT);
	}

	private Stream<EntityMetaData> filterPermission(Stream<EntityMetaData> entityMetaDataStream, Permission permission)
	{
		return entityMetaDataStream
				.filter(entityMeta -> permissionService.hasPermissionOnEntity(entityMeta.getName(), permission));
	}

	private static class FilteredConsumer
	{
		private final Consumer<List<EntityMetaData>> consumer;
		private final MolgenisPermissionService permissionService;

		FilteredConsumer(Consumer<List<EntityMetaData>> consumer, MolgenisPermissionService permissionService)
		{
			this.consumer = requireNonNull(consumer);
			this.permissionService = requireNonNull(permissionService);
		}

		public void filter(List<EntityMetaData> entityMetas)
		{
			List<EntityMetaData> filteredEntityMetas = entityMetas.stream()
					.filter(entityMeta -> permissionService.hasPermissionOnEntity(entityMeta.getName(), READ))
					.collect(toList());
			consumer.accept(filteredEntityMetas);
		}

	}
}