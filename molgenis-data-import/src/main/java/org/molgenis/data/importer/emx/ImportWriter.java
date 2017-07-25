package org.molgenis.data.importer.emx;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.*;
import org.molgenis.data.i18n.model.L10nString;
import org.molgenis.data.i18n.model.Language;
import org.molgenis.data.importer.EntityImportReport;
import org.molgenis.data.importer.MetaDataChanges;
import org.molgenis.data.importer.ParsedMetaData;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.EntityTypeDependencyResolver;
import org.molgenis.data.meta.model.*;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.validation.ConstraintViolation;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.permission.PermissionSystemService;
import org.molgenis.util.HugeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.EntityManager.CreationMode.POPULATE;
import static org.molgenis.data.i18n.model.L10nStringMetaData.L10N_STRING;
import static org.molgenis.data.i18n.model.LanguageMetadata.LANGUAGE;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

/**
 * Writes the imported metadata and data to target {@link RepositoryCollection}.
 */
public class ImportWriter
{
	private static final Logger LOG = LoggerFactory.getLogger(ImportWriter.class);

	private final DataService dataService;
	private final PermissionSystemService permissionSystemService;
	private final MolgenisPermissionService molgenisPermissionService;
	private final EntityManager entityManager;
	private final EntityTypeDependencyResolver entityTypeDependencyResolver;

	/**
	 * Creates the ImportWriter
	 *
	 * @param dataService                  {@link DataService} to query existing repositories and transform entities
	 * @param permissionSystemService      {@link PermissionSystemService} to give permissions on uploaded entities
	 * @param entityManager                entity manager to create new entities
	 * @param entityTypeDependencyResolver entity type dependency resolver
	 */
	public ImportWriter(DataService dataService, PermissionSystemService permissionSystemService,
			MolgenisPermissionService molgenisPermissionService, EntityManager entityManager,
			EntityTypeDependencyResolver entityTypeDependencyResolver)
	{
		this.dataService = requireNonNull(dataService);
		this.permissionSystemService = requireNonNull(permissionSystemService);
		this.molgenisPermissionService = requireNonNull(molgenisPermissionService);
		this.entityManager = requireNonNull(entityManager);
		this.entityTypeDependencyResolver = requireNonNull(entityTypeDependencyResolver);
	}

	@Transactional
	public EntityImportReport doImport(EmxImportJob job)
	{
		// languages first
		importLanguages(job.report, job.parsedMetaData.getLanguages(), job.dbAction, job.metaDataChanges);
		runAsSystem(() ->
		{
			importTags(job.parsedMetaData);
			importPackages(job.parsedMetaData);
		});
		importEntityTypes(job.parsedMetaData.getEntities(), job.report);

		List<EntityType> resolvedEntityTypes = entityTypeDependencyResolver.resolve(job.parsedMetaData.getEntities());
		importData(job.report, resolvedEntityTypes, job.source, job.dbAction, job.packageId);
		importI18nStrings(job.report, job.parsedMetaData.getL10nStrings(), job.dbAction);

		return job.report;
	}

	private void importEntityTypes(ImmutableCollection<EntityType> entityTypes, EntityImportReport importReport)
	{
		GroupedEntityTypes groupedEntityTypes = groupEntityTypes(entityTypes);
		if (!SecurityUtils.currentUserIsSu())
		{
			validateEntityTypePermissions(groupedEntityTypes.getUpdatedEntityTypes());
			createEntityTypePermissions(groupedEntityTypes.getNewEntityTypes());
		}
		upsertEntityTypes(groupedEntityTypes);

		groupedEntityTypes.getNewEntityTypes().stream().map(EntityType::getId).forEach(importReport::addNewEntity);
	}

	private void validateEntityTypePermissions(ImmutableCollection<EntityType> entityTypes)
	{
		entityTypes.forEach(this::validateEntityTypePermission);
	}

	private void validateEntityTypePermission(EntityType entityType)
	{
		String entityTypeName = entityType.getId();
		if (!molgenisPermissionService.hasPermissionOnEntity(entityTypeName, Permission.READ))
		{
			throw new MolgenisValidationException(
					new ConstraintViolation(format("Permission denied on existing entity type [%s]", entityTypeName)));
		}
	}

	private void createEntityTypePermissions(ImmutableCollection<EntityType> entityTypes)
	{
		permissionSystemService.giveUserWriteMetaPermissions(entityTypes);
	}

	private GroupedEntityTypes groupEntityTypes(ImmutableCollection<EntityType> entities)
	{
		return runAsSystem(() ->
		{
			Map<String, EntityType> existingEntityTypeMap = new HashMap<>();
			for (EntityType entityType : entities)
			{
				EntityType existing = dataService.findOneById(ENTITY_TYPE_META_DATA, entityType.getId(),
						EntityType.class);
				if (existing != null)
				{
					existingEntityTypeMap.put(entityType.getId(), entityType);
				}
			}

			ImmutableCollection<EntityType> newEntityTypes = entities.stream()
																	 .filter(entityType -> !existingEntityTypeMap.containsKey(
																			 entityType.getId()))
																	 .collect(collectingAndThen(toList(),
																			 ImmutableList::copyOf));

			ImmutableCollection<EntityType> existingEntityTypes = entities.stream()
																		  .filter(entityType -> existingEntityTypeMap.containsKey(
																				  entityType.getId()))
																		  .collect(collectingAndThen(toList(),
																				  ImmutableList::copyOf));

			return new GroupedEntityTypes(newEntityTypes, existingEntityTypes);
		});
	}

	private static class GroupedEntityTypes
	{
		private final ImmutableCollection<EntityType> newEntityTypes;
		private final ImmutableCollection<EntityType> updatedEntityTypes;

		public GroupedEntityTypes(ImmutableCollection<EntityType> newEntityTypes,
				ImmutableCollection<EntityType> updatedEntityTypes)
		{

			this.newEntityTypes = requireNonNull(newEntityTypes);
			this.updatedEntityTypes = requireNonNull(updatedEntityTypes);
		}

		public ImmutableCollection<EntityType> getNewEntityTypes()
		{
			return newEntityTypes;
		}

		public ImmutableCollection<EntityType> getUpdatedEntityTypes()
		{
			return updatedEntityTypes;
		}
	}

	private void importLanguages(EntityImportReport report, Map<String, Language> languages, DatabaseAction dbAction,
			MetaDataChanges metaDataChanges)
	{
		if (!languages.isEmpty())
		{
			Repository<Language> repo = dataService.getRepository(LANGUAGE, Language.class);

			// Find new ones
			languages.values().stream().map(Entity::getIdValue).forEach(id ->
			{
				if (repo.findOneById(id) == null)
				{
					metaDataChanges.addLanguage(languages.get(id));
				}
			});

			int count = update(repo, languages.values(), dbAction);
			report.addEntityCount(LANGUAGE, count);
		}
	}

	private void importI18nStrings(EntityImportReport report, Map<String, L10nString> i18nStrings,
			DatabaseAction dbAction)
	{
		if (!i18nStrings.isEmpty())
		{
			Repository<L10nString> repo = dataService.getRepository(L10N_STRING, L10nString.class);
			int count = update(repo, i18nStrings.values(), dbAction);
			report.addEntityCount(L10N_STRING, count);
		}
	}

	/**
	 * Imports entity data for all entities in resolved from source
	 */
	private void importData(EntityImportReport report, Iterable<EntityType> resolved, RepositoryCollection source,
			DatabaseAction dbAction, String defaultPackage)
	{
		for (final EntityType entityType : resolved)
		{
			String name = entityType.getId();

			// Languages and i18nstrings are already done
			if (!name.equalsIgnoreCase(LANGUAGE) && !name.equalsIgnoreCase(L10N_STRING) && dataService.hasRepository(
					name))
			{
				Repository<Entity> repository = dataService.getRepository(name);
				Repository<Entity> emxEntityRepo = source.getRepository(entityType.getId());

				// Try without default package
				if ((emxEntityRepo == null) && (defaultPackage != null) && entityType.getId()
																					 .toLowerCase()
																					 .startsWith(
																							 defaultPackage.toLowerCase()
																									 + PACKAGE_SEPARATOR))
				{
					emxEntityRepo = source.getRepository(entityType.getId().substring(defaultPackage.length() + 1));
				}

				// check to prevent nullpointer when importing metadata only
				if (emxEntityRepo != null)
				{
					// transforms entities so that they match the entity meta data of the output repository
					Iterable<Entity> entities = Iterables.transform(emxEntityRepo,
							emxEntity -> toEntity(entityType, emxEntity));
					int count = update(repository, entities, dbAction);
					report.addEntityCount(name, count);
				}
			}
		}
	}

	/**
	 * Create an entity from the EMX entity
	 *
	 * @param entityType entity meta data
	 * @param emxEntity  EMX entity
	 * @return MOLGENIS entity
	 */
	private Entity toEntity(EntityType entityType, Entity emxEntity)
	{
		Entity entity = entityManager.create(entityType, POPULATE);
		for (Attribute attr : entityType.getAtomicAttributes())
		{
			if (attr.getExpression() == null && !attr.isMappedBy())
			{
				String attrName = attr.getName();
				Object emxValue = emxEntity.get(attrName);

				AttributeType attrType = attr.getDataType();
				switch (attrType)
				{
					case BOOL:
					case DATE:
					case DATE_TIME:
					case DECIMAL:
					case EMAIL:
					case ENUM:
					case HTML:
					case HYPERLINK:
					case INT:
					case LONG:
					case SCRIPT:
					case STRING:
					case TEXT:
						Object value = emxValue != null ? DataConverter.convert(emxValue, attr) : null;
						if ((!attr.isAuto() || value != null) && (!attr.hasDefaultValue() || value != null))
						{
							entity.set(attrName, value);
						}
						break;
					case CATEGORICAL:
					case FILE:
					case XREF:
						// DataConverter.convert performs no conversion for reference types
						Entity refEntity;
						if (emxValue != null)
						{
							if (emxValue instanceof Entity)
							{
								refEntity = toEntity(attr.getRefEntity(), (Entity) emxValue);
							}
							else
							{
								EntityType xrefEntity = attr.getRefEntity();
								Object entityId = DataConverter.convert(emxValue, xrefEntity.getIdAttribute());
								refEntity = entityManager.getReference(xrefEntity, entityId);
							}
						}
						else
						{
							refEntity = null;
						}

						// do not set generated auto refEntities to null
						if ((!attr.isAuto() || refEntity != null) && (!attr.hasDefaultValue() || refEntity != null))
						{
							entity.set(attrName, refEntity);
						}
						break;
					case CATEGORICAL_MREF:
					case MREF:
						List<Entity> refEntities;
						// DataConverter.convert performs no conversion for reference types
						if (emxValue != null)
						{
							if (emxValue instanceof Iterable<?>)
							{
								List<Entity> mrefEntities = new ArrayList<>();
								for (Object emxValueItem : (Iterable<?>) emxValue)
								{
									Entity entityValue;
									if (emxValueItem instanceof Entity)
									{
										entityValue = toEntity(attr.getRefEntity(), (Entity) emxValueItem);
									}
									else
									{
										EntityType xrefEntity = attr.getRefEntity();
										Object entityId = DataConverter.convert(emxValueItem,
												xrefEntity.getIdAttribute());
										entityValue = entityManager.getReference(xrefEntity, entityId);
									}
									mrefEntities.add(entityValue);
								}
								refEntities = mrefEntities;
							}
							else
							{
								EntityType mrefEntity = attr.getRefEntity();
								Attribute refIdAttr = mrefEntity.getIdAttribute();

								String[] tokens = StringUtils.split(emxValue.toString(), ',');
								List<Entity> mrefEntities = new ArrayList<>();
								for (String token : tokens)
								{
									Object entityId = DataConverter.convert(token.trim(), refIdAttr);
									mrefEntities.add(entityManager.getReference(mrefEntity, entityId));
								}
								refEntities = mrefEntities;
							}
						}
						else
						{
							refEntities = emptyList();
						}

						// do not set generated auto refEntities to null
						if (!refEntities.isEmpty())
						{
							entity.set(attrName, refEntities);
						}
						break;
					case COMPOUND:
						throw new RuntimeException(format("Illegal attribute type [%s]", attrType.toString()));
					default:
						throw new RuntimeException(format("Unknown attribute type [%s]", attrType.toString()));
				}
			}
		}
		return entity;
	}

	private void upsertEntityTypes(GroupedEntityTypes groupedEntityTypes)
	{
		// retrieve existing entity types
		Fetch entityTypeFetch = createEntityTypeWithAttributesFetch();

		ImmutableCollection<EntityType> updatedEntityTypes = groupedEntityTypes.getUpdatedEntityTypes();

		Map<String, EntityType> existingEntityTypeMap = new HashMap<>();
		for (EntityType entityType : updatedEntityTypes)
		{
			EntityType existing = dataService.findOneById(ENTITY_TYPE_META_DATA, entityType.getId(), EntityType.class);
			if (existing != null)
			{
				existingEntityTypeMap.put(entityType.getId(), existing);
			}
		}

		// inject attribute and entityType identifiers in entity types to import
		updatedEntityTypes.forEach(entityType ->
		{
			EntityType existingEntityType = existingEntityTypeMap.get(entityType.getId());
			entityType.getOwnAllAttributes().forEach(ownAttr ->
			{
				Attribute existingAttr = existingEntityType.getAttribute(ownAttr.getName());
				if (existingAttr != null)
				{
					ownAttr.setIdentifier(existingAttr.getIdentifier());
					ownAttr.setEntity(existingEntityType);
				}
			});
			entityType.setId(existingEntityType.getId());
		});

		// add or update entity types
		List<EntityType> entityTypes = newArrayList(concat(updatedEntityTypes, groupedEntityTypes.getNewEntityTypes()));
		runAsSystem(() -> dataService.getMeta().upsertEntityTypes(entityTypes));
	}

	private static Fetch createEntityTypeWithAttributesFetch()
	{
		return new Fetch().field(EntityTypeMetadata.PACKAGE)
						  .field(EntityTypeMetadata.ATTRIBUTES,
								  new Fetch().field(AttributeMetadata.ID).field(AttributeMetadata.NAME));
	}

	/**
	 * Adds the packages from the packages sheet to the {@link org.molgenis.data.meta.MetaDataService}.
	 */
	private void importPackages(ParsedMetaData parsedMetaData)
	{
		ImmutableCollection<Package> packages = parsedMetaData.getPackages().values();
		dataService.getMeta().upsertPackages(packages.stream().filter(Objects::nonNull));
	}

	/**
	 * Imports the tags from the tag sheet.
	 */
	// FIXME: can everybody always update a tag?
	private void importTags(ParsedMetaData parsedMetaData)
	{
		ImmutableCollection<Tag> tags = parsedMetaData.getTags().values();
		dataService.getMeta().upsertTags(tags);
	}

	/**
	 * Updates a repository with entities.
	 *
	 * @param repo     the {@link Repository} to update
	 * @param entities the entities to
	 * @param dbAction {@link DatabaseAction} describing how to merge the existing entities
	 * @return number of updated entities
	 */
	private <E extends Entity> int update(Repository<E> repo, Iterable<E> entities, DatabaseAction dbAction)
	{
		if (entities == null) return 0;

		if (!molgenisPermissionService.hasPermissionOnEntity(repo.getName(), Permission.WRITE))
		{
			throw new MolgenisDataAccessException("No WRITE permission on entity '" + repo.getName()
					+ "'. Is this entity already imported by another user who did not grant you WRITE permission?");
		}

		int count = 0;
		switch (dbAction)
		{
			case ADD:
				count = repo.add(stream(entities.spliterator(), false));
				break;
			case ADD_IGNORE_EXISTING:
			{
				HugeSet<Object> existingIds = getExistingEntityIds(repo, entities);
				try
				{
					String idAttributeName = repo.getEntityType().getIdAttribute().getName();
					int batchSize = 1000;
					List<E> newEntities = newArrayList();

					for (E entity : entities)
					{
						count++;
						Object id = entity.get(idAttributeName);
						if (!existingIds.contains(id))
						{
							newEntities.add(entity);
							if (newEntities.size() == batchSize)
							{
								repo.add(newEntities.stream());
								newEntities.clear();
							}
						}
					}

					if (!newEntities.isEmpty())
					{
						repo.add(newEntities.stream());
					}
				}
				finally
				{
					IOUtils.closeQuietly(existingIds);
				}
				break;
			}
			case ADD_UPDATE_EXISTING:
			{
				HugeSet<Object> existingIds = getExistingEntityIds(repo, entities);
				try
				{
					String idAttributeName = repo.getEntityType().getIdAttribute().getName();
					int batchSize = 1000;
					List<E> existingEntities = new ArrayList<>(batchSize);
					List<Integer> existingEntitiesRowIndex = new ArrayList<>(batchSize);
					List<E> newEntities = new ArrayList<>(batchSize);
					List<Integer> newEntitiesRowIndex = new ArrayList<>(batchSize);

					for (E entity : entities)
					{
						count++;
						Object id = entity.get(idAttributeName);
						if (existingIds.contains(id))
						{
							existingEntitiesRowIndex.add(count);
							existingEntities.add(entity);
							if (existingEntities.size() == batchSize)
							{
								updateInRepo(repo, existingEntities, existingEntitiesRowIndex);
							}
						}
						else
						{
							newEntitiesRowIndex.add(count);
							newEntities.add(entity);
							if (newEntities.size() == batchSize)
							{
								insertIntoRepo(repo, newEntities, newEntitiesRowIndex);
							}
						}
					}

					if (!existingEntities.isEmpty())
					{
						updateInRepo(repo, existingEntities, existingEntitiesRowIndex);
					}
					if (!newEntities.isEmpty())
					{
						insertIntoRepo(repo, newEntities, newEntitiesRowIndex);
					}
				}
				finally
				{
					IOUtils.closeQuietly(existingIds);
				}
				break;
			}
			case UPDATE:
				AtomicInteger atomicCount = new AtomicInteger(0);
				repo.update(stream(entities.spliterator(), false).filter(entity ->
				{
					atomicCount.incrementAndGet();
					return true;
				}));
				count = atomicCount.get();
				break;
			default:
				throw new RuntimeException(format("Unknown database action [%s]", dbAction.toString()));
		}

		return count;
	}

	private static <E extends Entity> HugeSet<Object> getExistingEntityIds(Repository<E> repo, Iterable<E> entities)
	{
		String idAttributeName = repo.getEntityType().getIdAttribute().getName();

		HugeSet<Object> ids = new HugeSet<>();
		HugeSet<Object> existingIds = new HugeSet<>();

		try
		{
			for (Entity entity : entities)
			{
				Object id = entity.get(idAttributeName);
				if (id != null)
				{
					ids.add(id);
				}
			}

			if (!ids.isEmpty())
			{
				// Check if the ids already exist
				if (repo.count() > 0)
				{
					int batchSize = 100;
					Query<E> q = new QueryImpl<>();
					Iterator<Object> it = ids.iterator();
					int batchCount = 0;
					while (it.hasNext())
					{
						Object id = it.next();
						q.eq(idAttributeName, id);
						batchCount++;
						if (batchCount == batchSize || !it.hasNext())
						{
							repo.findAll(q).forEach(existing -> existingIds.add(existing.getIdValue()));
							q = new QueryImpl<>();
							batchCount = 0;
						}
						else
						{
							q.or();
						}
					}
				}
			}
		}
		catch (RuntimeException e)
		{
			IOUtils.closeQuietly(existingIds);
			throw e;
		}
		finally
		{
			IOUtils.closeQuietly(ids);
		}
		return existingIds;
	}

	private <E extends Entity> void updateInRepo(Repository<E> repo, List<E> existingEntities,
			List<Integer> existingEntitiesRowIndex)
	{
		try
		{
			repo.update(existingEntities.stream());
		}
		catch (MolgenisValidationException mve)
		{
			mve.renumberViolationRowIndices(existingEntitiesRowIndex);
			throw mve;
		}
		existingEntities.clear();
		existingEntitiesRowIndex.clear();
	}

	private <E extends Entity> void insertIntoRepo(Repository<E> repo, List<E> newEntities,
			List<Integer> newEntitiesRowIndex)
	{
		try
		{
			repo.add(newEntities.stream());
		}
		catch (MolgenisValidationException mve)
		{
			mve.renumberViolationRowIndices(newEntitiesRowIndex);
			throw mve;
		}
		newEntities.clear();
		newEntitiesRowIndex.clear();
	}
}