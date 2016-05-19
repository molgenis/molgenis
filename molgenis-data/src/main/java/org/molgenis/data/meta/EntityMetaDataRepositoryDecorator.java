package org.molgenis.data.meta;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static org.molgenis.util.SecurityDecoratorUtils.validatePermission;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityListener;
import org.molgenis.data.Fetch;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.system.SystemEntityMetaDataRegistry;
import org.molgenis.data.semantic.Tag;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;
import com.google.common.collect.TreeTraverser;

public class EntityMetaDataRepositoryDecorator implements Repository<EntityMetaData>
{
	private final Repository<EntityMetaData> decoratedRepo;
	private final SystemEntityMetaDataRegistry systemEntityMetaDataRegistry;
	private final DataService dataService;

	public EntityMetaDataRepositoryDecorator(Repository<EntityMetaData> decoratedRepo, DataService dataService,
			SystemEntityMetaDataRegistry systemEntityMetaDataRegistry)
	{
		this.decoratedRepo = requireNonNull(decoratedRepo);
		this.dataService = requireNonNull(dataService);
		this.systemEntityMetaDataRegistry = requireNonNull(systemEntityMetaDataRegistry);
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
	public EntityMetaData getEntityMetaData()
	{
		return decoratedRepo.getEntityMetaData();
	}

	@Override
	public long count()
	{
		return decoratedRepo.count();
	}

	@Override
	public Query<EntityMetaData> query()
	{
		return decoratedRepo.query();
	}

	@Override
	public long count(Query<EntityMetaData> q)
	{
		return decoratedRepo.count(q);
	}

	@Override
	public Stream<EntityMetaData> findAll(Query<EntityMetaData> q)
	{
		return decoratedRepo.findAll(q);
	}

	@Override
	public Iterator<EntityMetaData> iterator()
	{
		return decoratedRepo.iterator();
	}

	@Override
	public Stream<EntityMetaData> stream(Fetch fetch)
	{
		return decoratedRepo.stream(fetch);
	}

	@Override
	public EntityMetaData findOne(Query<EntityMetaData> q)
	{
		return decoratedRepo.findOne(q);
	}

	@Override
	public EntityMetaData findOneById(Object id)
	{
		return decoratedRepo.findOneById(id);
	}

	@Override
	public EntityMetaData findOneById(Object id, Fetch fetch)
	{
		return decoratedRepo.findOneById(id, fetch);
	}

	@Override
	public Stream<EntityMetaData> findAll(Stream<Object> ids)
	{
		return decoratedRepo.findAll(ids);
	}

	@Override
	public Stream<EntityMetaData> findAll(Stream<Object> ids, Fetch fetch)
	{
		return decoratedRepo.findAll(ids, fetch);
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		return decoratedRepo.aggregate(aggregateQuery);
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
					format("Unknown entityMetaData [%s] with id [%s]", getName(), id.toString()));
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

	@Override
	public void flush()
	{
		decoratedRepo.flush();
	}

	@Override
	public void clearCache()
	{
		decoratedRepo.clearCache();
	}

	@Override
	public void create()
	{
		decoratedRepo.create();
	}

	@Override
	public void drop()
	{
		decoratedRepo.drop();
	}

	@Override
	public void rebuildIndex()
	{
		decoratedRepo.rebuildIndex();
	}

	@Override
	public void addEntityListener(EntityListener entityListener)
	{
		decoratedRepo.addEntityListener(entityListener);
	}

	@Override
	public void removeEntityListener(EntityListener entityListener)
	{
		decoratedRepo.removeEntityListener(entityListener);
	}

	private void addEntityMetaData(EntityMetaData entityMetaData)
	{
		validateAddAllowed(entityMetaData);

		// add row to entities table
		decoratedRepo.add(entityMetaData);

		// create entityMetaData table
		// FIXME remove commented code
		//		if (!dataService.getMeta().isMetaRepository(entityMetaData.getString(EntityMetaDataMetaData.FULL_NAME)))
		//		{
		if (!entityMetaData.isAbstract())
		{
			dataService.getMeta().getBackend(entityMetaData.getBackend()).createRepository(entityMetaData);
			//			((DataServiceImpl) dataService).addRepository(entityRepo); // FIXME remove cast
		}
		//		}
	}

	private void validateAddAllowed(EntityMetaData entityMetaData)
	{
		String entityName = entityMetaData.getName();
		validatePermission(entityName, Permission.WRITEMETA);

		// TODO replace with exists() once Repository.exists has been implemented
		EntityMetaData existingEntityMetaData = findOneById(entityMetaData.getIdValue(),
				new Fetch().field(EntityMetaDataMetaData.FULL_NAME));
		if (existingEntityMetaData != null)
		{
			throw new MolgenisDataException(format("Adding existing entityMetaData [%s] is not allowed", entityName));
		}

		MetaValidationUtils.validateEntityMetaData(entityMetaData);
	}

	private void updateEntity(EntityMetaData entityMetaData)
	{
		validateUpdateAllowed(entityMetaData);

		// TODO replace with exists() once Repository.exists has been implemented
		EntityMetaData existingEntityMetaData = findOneById(entityMetaData.getIdValue());
		if (existingEntityMetaData == null)
		{
			throw new UnknownEntityException(format("Unknown entityMetaData [%s] with id [%s]", getName(),
					entityMetaData.getIdValue().toString()));
		}
		updateEntityAttributes(entityMetaData, existingEntityMetaData);

		decoratedRepo.update(entityMetaData);
	}

	/**
	 * Updating entityMetaData meta data is allowed for non-system entities. For system entities updating entityMetaData meta data is
	 * only allowed if the meta data defined in Java differs from the meta data stored in the database (in other words
	 * the Java code was updated).
	 *
	 * @param entityMetaData
	 */
	private void validateUpdateAllowed(EntityMetaData entityMetaData)
	{
		String entityName = entityMetaData.getName();
		validatePermission(entityName, Permission.WRITEMETA);

		EntityMetaData existingEntityMetaData = systemEntityMetaDataRegistry.getSystemEntityMetaData(entityName);
		if (entityMetaData
				!= null /*&& !MetaUtils.equals(entityMetaData, existingEntityMetaData, dataService.getMeta())*/) // FIXME reenable equals check
		{
			throw new MolgenisDataException(format("Updating system entityMetaData [%s] is not allowed", entityName));
		}

		MetaValidationUtils.validateEntityMetaData(entityMetaData);
	}

	private void updateEntityAttributes(EntityMetaData entityMetaData, EntityMetaData existingEntity)
	{
		EntityMetaData currentEntityMetaData = findOneById(entityMetaData.getIdValue(),
				new Fetch().field(EntityMetaDataMetaData.FULL_NAME)
						.field(EntityMetaDataMetaData.ATTRIBUTES, new Fetch().field(AttributeMetaDataMetaData.NAME)));
		Map<String, AttributeMetaData> currentAttrMap = StreamSupport
				.stream(currentEntityMetaData.getOwnAttributes().spliterator(), false)
				.collect(toMap(attrEntity -> attrEntity.getName(), Function.identity()));
		Map<String, AttributeMetaData> updateAttrMap = StreamSupport
				.stream(entityMetaData.getOwnAttributes().spliterator(), false)
				.collect(toMap(attrEntity -> attrEntity.getName(), Function.identity()));

		Set<String> deletedAttrNames = Sets.difference(currentAttrMap.keySet(), updateAttrMap.keySet());
		Set<String> addedAttrNames = Sets.difference(updateAttrMap.keySet(), currentAttrMap.keySet());
		Set<String> existingAttrNames = Sets.intersection(currentAttrMap.keySet(), updateAttrMap.keySet());

		if (!deletedAttrNames.isEmpty() || !addedAttrNames.isEmpty() || !existingAttrNames.isEmpty())
		{
			String entityName = entityMetaData.getName();
			String backend = entityMetaData.getBackend();
			RepositoryCollection repoCollection = dataService.getMeta().getBackend(backend);
			if (!(repoCollection instanceof RepositoryCollection))
			{
				throw new MolgenisDataException(
						format("Modifying attributes not allowed for entityMetaData [%s]", entityName));
			}
			RepositoryCollection manageableRepoCollection = repoCollection;

			if (!deletedAttrNames.isEmpty())
			{
				deletedAttrNames.forEach(deletedAttrName -> {
					manageableRepoCollection.deleteAttribute(entityName, deletedAttrName);
				});
			}

			if (!addedAttrNames.isEmpty())
			{
				addedAttrNames.stream().map(updateAttrMap::get).forEach(addedAttrEntity -> {
					manageableRepoCollection.addAttribute(entityName, addedAttrEntity);
				});
			}

			if (!existingAttrNames.isEmpty())
			{
				existingAttrNames.stream().filter(existingAttrName -> {
					Entity currentAttr = currentAttrMap.get(existingAttrName);
					Entity updatedAttr = updateAttrMap.get(existingAttrName);
					return false; // FIXME
					// return !MetaUtils.equals(entityMetaEntity, entityMeta, this);
				}).map(existingAttrName -> {
					throw new UnsupportedOperationException(
							format("Cannot update attribute(s) [%s] of entityMetaData [%s]",
									existingAttrNames.stream().collect(joining(",")), entityName));
				});

			}
		}
	}

	private void deleteEntityMetaData(EntityMetaData entityEntity)
	{
		validateDeleteAllowed(entityEntity);

		// delete row from entities table
		decoratedRepo.delete(entityEntity);

		// delete rows from attributes table
		deleteEntityAttributes(entityEntity);

		// delete rows from tags table
		deleteEntityTags(entityEntity);

		// delete entityMetaData table
		deleteEntityInstances(entityEntity);

		// delete entityMetaData permissions
		deleteEntityPermissions(entityEntity);
	}

	private void validateDeleteAllowed(EntityMetaData entityMetaData)
	{
		String entityName = entityMetaData.getName();
		validatePermission(entityName, Permission.WRITEMETA);

		boolean isSystem = systemEntityMetaDataRegistry.hasSystemEntityMetaData(entityName);
		if (isSystem)
		{
			throw new MolgenisDataException(format("Deleting system entityMetaData [%s] is not allowed", entityName));
		}
	}

	private void deleteEntityAttributes(EntityMetaData entityMetaData)
	{
		Iterable<AttributeMetaData> rootAttrs = entityMetaData.getOwnAttributes();
		Stream<AttributeMetaData> allAttrs = StreamSupport.stream(rootAttrs.spliterator(), false)
				.flatMap(attrEntity -> StreamSupport.stream(new TreeTraverser<AttributeMetaData>()
				{
					@Override
					public Iterable<AttributeMetaData> children(AttributeMetaData attr)
					{
						return attr.getAttributeParts();
					}
				}.postOrderTraversal(attrEntity).spliterator(), false));
		dataService.delete(AttributeMetaDataMetaData.ENTITY_NAME, allAttrs);
	}

	private void deleteEntityTags(EntityMetaData entityMetaData)
	{
		Iterable<Tag> tags = entityMetaData.getTags();
		dataService.delete(TagMetaData.ENTITY_NAME, StreamSupport.stream(tags.spliterator(), false));
	}

	private void deleteEntityInstances(EntityMetaData entityMetaData)
	{
		String entityName = entityMetaData.getName();
		String backend = entityMetaData.getBackend();
		dataService.getMeta().getBackend(backend)
				.deleteRepository(entityName); // FIXME call deleteRepo directly on metadataservice
	}

	private void deleteEntityPermissions(EntityMetaData entityMetaData)
	{
		String entityName = entityMetaData.getName();
		List<String> authorities = SecurityUtils.getEntityAuthorities(entityName);

		// User permissions
		if (dataService.hasRepository("UserAuthority"))
		{
			Stream<Entity> userPermissions = dataService.query("UserAuthority").in("role", authorities).findAll();
			dataService.delete("UserAuthority", userPermissions);
		}

		// Group permissions
		if (dataService.hasRepository("GroupAuthority"))
		{
			Stream<Entity> groupPermissions = dataService.query("GroupAuthority").in("role", authorities).findAll();
			dataService.delete("GroupAuthority", groupPermissions);
		}
	}
}