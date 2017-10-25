package org.molgenis.data.importer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.EntityTypeDependencyResolver;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.MetaDataServiceImpl.EntityTypeWithoutMappedByAttributes;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.util.UnexpectedEnumException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ID;
import static org.molgenis.util.stream.MapCollectors.toLinkedMap;

@Component
public class DataPersisterImpl implements DataPersister
{
	private final MetaDataService metaDataService;
	private final DataService dataService;
	private final EntityTypeDependencyResolver entityTypeDependencyResolver;

	DataPersisterImpl(MetaDataService metaDataService, DataService dataService,
			EntityTypeDependencyResolver entityTypeDependencyResolver)
	{
		this.metaDataService = requireNonNull(metaDataService);
		this.dataService = requireNonNull(dataService);
		this.entityTypeDependencyResolver = requireNonNull(entityTypeDependencyResolver);
	}

	@Transactional
	@Override
	public PersistResult persist(DataProvider dataProvider, MetadataMode metadataMode, DataMode dataMode)
	{
		List<EntityType> sortedEntityTypes = getTopologicalSortedEntityTypes(dataProvider.getEntityTypes());

		preProcessEntityTypes(sortedEntityTypes, metadataMode);
		PersistResult persistResult = persistFirstPass(dataProvider, metadataMode, dataMode, sortedEntityTypes);
		persistSecondPass(dataProvider, sortedEntityTypes);

		return persistResult;
	}

	private void preProcessEntityTypes(List<EntityType> entityTypes, MetadataMode metadataMode)
	{
		switch (metadataMode)
		{
			case ADD:
				break;
			case UPDATE:
			case UPSERT:
				preProcessUpdatedEntityTypes(entityTypes);
				break;
			case NONE:
				break;
			default:
				throw new UnexpectedEnumException(metadataMode);
		}
	}

	private PersistResult persistFirstPass(DataProvider dataProvider, MetadataMode metadataMode, DataMode dataMode,
			List<EntityType> topologicalSortedEntityTypes)
	{
		ImmutableMap.Builder<String, Long> persistResultBuilder = ImmutableMap.builder();
		topologicalSortedEntityTypes.forEach(entityType ->
		{
			EntityType persistedEntityType = persistEntityTypeFirstPass(entityType, metadataMode);
			if (dataProvider.hasEntities(entityType))
			{
				Stream<Entity> entities = dataProvider.getEntities(entityType);
				long nrPersistedEntities = persistEntitiesFirstPass(persistedEntityType, entities, dataMode);
				persistResultBuilder.put(entityType.getId(), nrPersistedEntities);
			}
		});
		return PersistResult.create(persistResultBuilder.build());
	}

	private void persistSecondPass(DataProvider dataProvider, List<EntityType> topologicalSortedEntityTypes)
	{
		topologicalSortedEntityTypes.forEach(entityType ->
		{
			EntityType persistedEntityType = persistEntityTypeSecondPass(entityType);
			if (dataProvider.hasEntities(entityType))
			{
				Stream<Entity> entities = dataProvider.getEntities(entityType);
				persistEntitiesSecondPass(persistedEntityType, entities);
			}
		});
	}

	private void preProcessUpdatedEntityTypes(List<EntityType> entityTypes)
	{
		Map<String, EntityType> entityTypeMap = entityTypes.stream()
														   .collect(toLinkedMap(EntityType::getId, identity()));
		dataService.query(ENTITY_TYPE_META_DATA, EntityType.class)
				   .in(ID, entityTypeMap.keySet())
				   .findAll()
				   .forEach(existingEntityType ->
				   {
					   EntityType entityType = entityTypeMap.get(existingEntityType.getId());
					   injectAttributeIdentifiers(entityType, existingEntityType);
				   });
	}

	private void injectAttributeIdentifiers(EntityType entityType, EntityType existingEntityType)
	{
		entityType.getOwnAllAttributes().forEach(ownAttr ->
		{
			Attribute existingAttr = existingEntityType.getAttribute(ownAttr.getName());
			if (existingAttr != null)
			{
				ownAttr.setIdentifier(existingAttr.getIdentifier());
				ownAttr.setEntity(existingEntityType);
			}
		});
	}

	private long persistEntitiesFirstPass(EntityType entityType, Stream<Entity> entities, DataMode dataMode)
	{
		AtomicLong nrPersistedEntities = new AtomicLong(0L);
		switch (dataMode)
		{
			case ADD:
				addEntitiesFirstPass(entityType, entities.filter(countEntitiesFilter(nrPersistedEntities)));
				break;
			case UPDATE:
				updateEntitiesFirstPass(entityType, entities.filter(countEntitiesFilter(nrPersistedEntities)));
				break;
			case UPSERT:
				upsertEntitiesFirstPass(entityType, entities.filter(countEntitiesFilter(nrPersistedEntities)));
				break;
			default:
				throw new UnexpectedEnumException(dataMode);
		}
		return nrPersistedEntities.get();
	}

	private Predicate<Entity> countEntitiesFilter(AtomicLong nrPersistedEntities)
	{
		return entity ->
		{
			nrPersistedEntities.incrementAndGet();
			return true;
		};
	}

	private void addEntitiesFirstPass(EntityType entityType, Stream<Entity> entities)
	{
		String entityTypeId = entityType.getId();
		dataService.add(entityTypeId, entities);
	}

	private void updateEntitiesFirstPass(EntityType entityType, Stream<Entity> entities)
	{
		String entityTypeId = entityType.getId();
		dataService.update(entityTypeId, entities);
	}

	private void upsertEntitiesFirstPass(EntityType entityType, Stream<Entity> entities)
	{
		String entityTypeId = entityType.getId();
		Repository<Entity> repository = dataService.getRepository(entityTypeId);
		Iterators.partition(entities.iterator(), 1000).forEachRemaining(repository::upsertBatch);
	}

	private void persistEntitiesSecondPass(EntityType entityType, Stream<Entity> entities)
	{
		if (entityType.hasMappedByAttributes())
		{
			String entityTypeId = entityType.getId();
			dataService.update(entityTypeId, entities);
		}
	}

	private EntityType persistEntityTypeFirstPass(EntityType entityType, MetadataMode metadataMode)
	{
		EntityType persistedEntityType;
		switch (metadataMode)
		{
			case ADD:
				persistedEntityType = addEntityTypeFirstPass(entityType);
				break;
			case UPDATE:
				persistedEntityType = updateEntityTypeFirstPass(entityType);
				break;
			case UPSERT:
				persistedEntityType = upsertEntityTypeFirstPass(entityType);
				break;
			case NONE:
				persistedEntityType = entityType;
				break;
			default:
				throw new UnexpectedEnumException(metadataMode);
		}
		return persistedEntityType;
	}

	private EntityType addEntityTypeFirstPass(EntityType entityType)
	{
		EntityType persistableEntityType;
		if (entityType.hasMappedByAttributes())
		{
			persistableEntityType = new EntityTypeWithoutMappedByAttributes(entityType);
		}
		else
		{
			persistableEntityType = entityType;
		}
		metaDataService.addEntityType(persistableEntityType);
		return persistableEntityType;
	}

	private EntityType updateEntityTypeFirstPass(EntityType entityType)
	{
		String entityTypeId = entityType.getId();
		EntityType existingEntityType = dataService.findOneById(ENTITY_TYPE_META_DATA, entityTypeId, EntityType.class);
		if (existingEntityType == null)
		{
			throw new UnknownEntityException(format("Unknown entity type [%s]", entityType.getId()));
		}
		return updateEntityTypeFirstPass(entityType, existingEntityType);

	}

	private EntityType updateEntityTypeFirstPass(EntityType entityType, EntityType existingEntityType)
	{
		EntityType persistableEntityType;
		if (entityType.hasMappedByAttributes())
		{
			persistableEntityType = new EntityTypeWithoutMappedByAttributes(entityType, existingEntityType);
		}
		else
		{
			persistableEntityType = entityType;
		}
		metaDataService.updateEntityType(persistableEntityType);
		return persistableEntityType;
	}

	private EntityType upsertEntityTypeFirstPass(EntityType entityType)
	{
		EntityType persistableEntityType;
		String entityTypeId = entityType.getId();
		EntityType existingEntityType = dataService.findOneById(ENTITY_TYPE_META_DATA, entityTypeId, EntityType.class);
		if (existingEntityType == null)
		{
			persistableEntityType = addEntityTypeFirstPass(entityType);
		}
		else
		{
			persistableEntityType = updateEntityTypeFirstPass(entityType, existingEntityType);
		}
		return persistableEntityType;
	}

	private EntityType persistEntityTypeSecondPass(EntityType entityType)
	{
		if (entityType.hasMappedByAttributes())
		{
			metaDataService.updateEntityType(entityType);
		}
		return entityType;
	}

	private List<EntityType> getTopologicalSortedEntityTypes(Stream<EntityType> entityTypeStream)
	{
		return entityTypeDependencyResolver.resolve(entityTypeStream.collect(toList()));
	}
}
