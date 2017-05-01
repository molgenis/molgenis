package org.molgenis.data.mapper.service.impl;

import org.molgenis.auth.User;
import org.molgenis.data.*;
import org.molgenis.data.jobs.Progress;
import org.molgenis.data.mapper.mapping.model.AttributeMapping;
import org.molgenis.data.mapper.mapping.model.EntityMapping;
import org.molgenis.data.mapper.mapping.model.MappingProject;
import org.molgenis.data.mapper.mapping.model.MappingTarget;
import org.molgenis.data.mapper.repository.MappingProjectRepository;
import org.molgenis.data.mapper.service.AlgorithmService;
import org.molgenis.data.mapper.service.MappingService;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.DefaultPackage;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.system.SystemPackageRegistry;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.security.permission.PermissionSystemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.google.api.client.util.Maps.newHashMap;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.mapper.meta.MappingProjectMetaData.NAME;
import static org.molgenis.data.meta.model.EntityType.AttributeCopyMode.DEEP_COPY_ATTRS;
import static org.molgenis.data.support.EntityTypeUtils.hasSelfReferences;
import static org.molgenis.data.support.EntityTypeUtils.isReferenceType;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

public class MappingServiceImpl implements MappingService
{
	public static final int MAPPING_BATCH_SIZE = 1000;

	private static final Logger LOG = LoggerFactory.getLogger(MappingServiceImpl.class);
	private static final String SOURCE = "source";

	private final DataService dataService;
	private final AlgorithmService algorithmService;
	private final MappingProjectRepository mappingProjectRepository;
	private final PermissionSystemService permissionSystemService;
	private final AttributeFactory attrMetaFactory;
	private final SystemPackageRegistry systemPackageRegistry;
	private final DefaultPackage defaultPackage;

	@Autowired
	public MappingServiceImpl(DataService dataService, AlgorithmService algorithmService,
			MappingProjectRepository mappingProjectRepository, PermissionSystemService permissionSystemService,
			AttributeFactory attrMetaFactory, SystemPackageRegistry systemPackageRegistry,
			DefaultPackage defaultPackage)
	{
		this.dataService = requireNonNull(dataService);
		this.algorithmService = requireNonNull(algorithmService);
		this.mappingProjectRepository = requireNonNull(mappingProjectRepository);
		this.permissionSystemService = requireNonNull(permissionSystemService);
		this.attrMetaFactory = requireNonNull(attrMetaFactory);
		this.systemPackageRegistry = systemPackageRegistry;
		this.defaultPackage = defaultPackage;
	}

	@Override
	@RunAsSystem
	@Transactional
	public MappingProject addMappingProject(String projectName, User owner, String target)
	{
		MappingProject mappingProject = new MappingProject(projectName, owner);
		mappingProject.addTarget(dataService.getEntityType(target));
		mappingProjectRepository.add(mappingProject);
		return mappingProject;
	}

	@Override
	@RunAsSystem
	@Transactional
	public void deleteMappingProject(String mappingProjectId)
	{
		mappingProjectRepository.delete(mappingProjectId);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SYSTEM, ROLE_SU, ROLE_PLUGIN_WRITE_menumanager')")
	@Transactional
	public MappingProject cloneMappingProject(String mappingProjectId)
	{
		MappingProject mappingProject = mappingProjectRepository.getMappingProject(mappingProjectId);
		if (mappingProject == null)
		{
			throw new UnknownEntityException("Mapping project [" + mappingProjectId + "] does not exist");
		}
		String mappingProjectName = mappingProject.getName();

		// determine cloned mapping project name (use Windows 7 naming strategy):
		String clonedMappingProjectName;
		for (int i = 1; ; ++i)
		{
			if (i == 1)
			{
				clonedMappingProjectName = mappingProjectName + " - Copy";
			}
			else
			{
				clonedMappingProjectName = mappingProjectName + " - Copy (" + i + ")";
			}

			if (mappingProjectRepository.getMappingProjects(new QueryImpl<>().eq(NAME, clonedMappingProjectName))
					.isEmpty())
			{
				break;
			}
		}

		return cloneMappingProject(mappingProject, clonedMappingProjectName);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SYSTEM, ROLE_SU, ROLE_PLUGIN_WRITE_menumanager')")
	@Transactional
	public MappingProject cloneMappingProject(String mappingProjectId, String clonedMappingProjectName)
	{
		MappingProject mappingProject = mappingProjectRepository.getMappingProject(mappingProjectId);
		if (mappingProject == null)
		{
			throw new UnknownEntityException("Mapping project [" + mappingProjectId + "] does not exist");
		}

		return cloneMappingProject(mappingProject, clonedMappingProjectName);
	}

	private MappingProject cloneMappingProject(MappingProject mappingProject, String clonedMappingProjectName)
	{
		mappingProject.removeIdentifiers();
		mappingProject.setName(clonedMappingProjectName);
		mappingProjectRepository.add(mappingProject);
		return mappingProject;
	}

	@Override
	@RunAsSystem
	public List<MappingProject> getAllMappingProjects()
	{
		return mappingProjectRepository.getAllMappingProjects();
	}

	@Override
	@RunAsSystem
	@Transactional
	public void updateMappingProject(MappingProject mappingProject)
	{
		mappingProjectRepository.update(mappingProject);
	}

	@Override
	@RunAsSystem
	public MappingProject getMappingProject(String identifier)
	{
		return mappingProjectRepository.getMappingProject(identifier);
	}

	@Override
	public void applyMappings(MappingTarget mappingTarget, String entityTypeId, Progress progress)
	{
		applyMappings(mappingTarget, entityTypeId, true, progress);
	}

	@Override
	@Transactional
	public void applyMappings(MappingTarget mappingTarget, String entityTypeId, boolean addSourceAttribute,
			Progress progress)
	{
		EntityType targetMetadata = createTargetMetadata(entityTypeId, mappingTarget, addSourceAttribute);
		Repository<Entity> targetRepo = getTargetRepository(targetMetadata, addSourceAttribute);

		try
		{
			LOG.info("Applying mappings to repository [" + targetMetadata.getId() + "]");
			applyMappingsToRepositories(mappingTarget, targetRepo, addSourceAttribute, progress);
			if (hasSelfReferences(targetRepo.getEntityType()))
			{
				LOG.info("Self reference found, applying the mapping for a second time to set references");
				applyMappingsToRepositories(mappingTarget, targetRepo, addSourceAttribute, progress);
			}
			LOG.info("Done applying mappings to repository [" + targetMetadata.getId() + "]");
		}
		catch (RuntimeException ex)
		{
			// Mapping to the target model, if something goes wrong we do not want to delete it
			LOG.error("Error applying mappings to the target", ex);
			throw ex;
		}
	}

	private EntityType createTargetMetadata(String entityTypeId, MappingTarget mappingTarget,
			boolean addSourceAttribute)
	{
		EntityType targetMetaData = EntityType.newInstance(mappingTarget.getTarget(), DEEP_COPY_ATTRS, attrMetaFactory);
		targetMetaData.setId(entityTypeId);
		targetMetaData.setLabel(entityTypeId);

		if (addSourceAttribute)
		{
			targetMetaData.addAttribute(attrMetaFactory.create().setName(SOURCE));
		}

		if (targetMetaData.getPackage() == null || systemPackageRegistry.containsPackage(targetMetaData.getPackage()))
		{
			targetMetaData.setPackage(defaultPackage);
		}

		return targetMetaData;
	}

	private Repository<Entity> getTargetRepository(EntityType targetMetadata, boolean addSourceAttribute)
	{
		Repository<Entity> targetRepo;
		String entityTypeId = targetMetadata.getId();
		if (!dataService.hasRepository(entityTypeId))
		{
			// Create a new repository
			targetRepo = runAsSystem(() -> dataService.getMeta().createRepository(targetMetadata));
			permissionSystemService.giveUserWriteMetaPermissions(targetMetadata);
		}
		else
		{
			// Get an existing repository
			targetRepo = dataService.getRepository(entityTypeId);

			// Compare the metadata between the target repository and the mapping target
			// Returns detailed information in case something is not compatible
			compareTargetMetaDatas(targetRepo.getEntityType(), targetMetadata);

			// If the addSourceAttribute is true, but the existing repository does not have the SOURCE attribute yet
			// Get the existing metadata and add the SOURCE attribute
			EntityType existingTargetMetaData = targetRepo.getEntityType();
			if (existingTargetMetaData.getAttribute(SOURCE) == null && addSourceAttribute)
			{
				existingTargetMetaData.addAttribute(attrMetaFactory.create().setName(SOURCE));
				dataService.getMeta().updateEntityType(existingTargetMetaData);
			}
		}
		return targetRepo;
	}

	public Stream<EntityType> getCompatibleEntityTypes(EntityType target)
	{
		return dataService.getMeta().getEntityTypes().filter(candidate -> !candidate.isAbstract())
				.filter(isCompatible(target));
	}

	private Predicate<EntityType> isCompatible(EntityType target)
	{
		return candidate ->
		{
			try
			{
				compareTargetMetaDatas(candidate, target);
				return true;
			}
			catch (MolgenisDataException incompatible)
			{
				return false;
			}
		};
	}

	/**
	 * Compares the attributes between the target repository and the mapping target.
	 * Applied Rules:
	 * - The mapping target can not contain attributes which are not in the target repository
	 * - The attributes of the mapping target with the same name as attributes in the target repository should have the same type
	 * - If there are reference attributes, the name of the reference entity should be the same in both the target repository as in the mapping target
	 *
	 * @param targetRepositoryEntityType the target repository EntityType to check
	 * @param mappingTargetEntityType the mapping target EntityType to check
	 * @throws MolgenisDataException if the types are not compatible
	 */
	private void compareTargetMetaDatas(EntityType targetRepositoryEntityType, EntityType mappingTargetEntityType)
	{
		Map<String, Attribute> targetRepositoryAttributeMap = newHashMap();
		targetRepositoryEntityType.getAtomicAttributes()
				.forEach(attribute -> targetRepositoryAttributeMap.put(attribute.getName(), attribute));

		for (Attribute mappingTargetAttribute : mappingTargetEntityType.getAtomicAttributes())
		{
			String mappingTargetAttributeName = mappingTargetAttribute.getName();
			Attribute targetRepositoryAttribute = targetRepositoryAttributeMap.get(mappingTargetAttributeName);
			if (targetRepositoryAttribute == null)
			{
				throw new MolgenisDataException(format("Target repository does not contain the following attribute: %s",
						mappingTargetAttributeName));
			}

			AttributeType targetRepositoryAttributeType = targetRepositoryAttribute.getDataType();
			AttributeType mappingTargetAttributeType = mappingTargetAttribute.getDataType();
			if (!mappingTargetAttributeType.equals(targetRepositoryAttributeType))
			{
				throw new MolgenisDataException(
						format("attribute %s in the mapping target is type %s while attribute %s in the target repository is type %s. Please make sure the types are the same",
								mappingTargetAttributeName, mappingTargetAttributeType,
								targetRepositoryAttribute.getName(), targetRepositoryAttributeType));
			}

			if (isReferenceType(mappingTargetAttribute))
			{
				String mappingTargetRefEntityName = mappingTargetAttribute.getRefEntity().getId();
				String targetRepositoryRefEntityName = targetRepositoryAttribute.getRefEntity().getId();
				if (!mappingTargetRefEntityName.equals(targetRepositoryRefEntityName))
				{
					throw new MolgenisDataException(
							format("In the mapping target, attribute %s of type %s has reference entity %s while in the target repository attribute %s of type %s has reference entity %s. "
											+ "Please make sure the reference entities of your mapping target are pointing towards the same reference entities as your target repository",
									mappingTargetAttributeName, mappingTargetAttributeType, mappingTargetRefEntityName,
									targetRepositoryAttribute.getName(), targetRepositoryAttributeType,
									targetRepositoryRefEntityName));
				}
			}
		}
	}

	private void applyMappingsToRepositories(MappingTarget mappingTarget, Repository<Entity> targetRepo,
			boolean addSourceAttribute, Progress progress)
	{
		for (EntityMapping sourceMapping : mappingTarget.getEntityMappings())
		{
			applyMappingToRepo(sourceMapping, targetRepo, addSourceAttribute, progress);
		}
	}

	private void applyMappingToRepo(EntityMapping sourceMapping, Repository<Entity> targetRepo,
			boolean addSourceAttribute, Progress progress)
	{
		EntityType targetMetaData = targetRepo.getEntityType();
		Repository<Entity> sourceRepo = dataService.getRepository(sourceMapping.getName());

		progress.status(format("Mapping source %s", sourceRepo.getEntityType().getLabel()));

		if (targetRepo.count() == 0)
		{
			sourceRepo.forEachBatched(entities ->
			{
				mapAndAddEntities(sourceMapping, targetRepo, addSourceAttribute, targetMetaData, entities);
				progress.increment(1);
			}, MAPPING_BATCH_SIZE);
		}
		else
		{
			sourceRepo.forEachBatched(entities ->
			{
				mapAndUpsertEntities(sourceMapping, targetRepo, addSourceAttribute, targetMetaData, entities);
				progress.increment(1);
			}, MAPPING_BATCH_SIZE);
		}
	}

	private void mapAndUpsertEntities(EntityMapping sourceMapping, Repository<Entity> targetRepo,
			boolean addSourceAttribute, EntityType targetMetaData, List<Entity> entities)
	{
		entities.forEach(sourceEntity ->
		{
			// FIXME adding/updating row-by-row is a performance bottleneck, this code could do streaming upsert
			Entity mappedEntity = applyMappingToEntity(sourceMapping, sourceEntity, targetMetaData, addSourceAttribute);
			if (targetRepo.findOneById(mappedEntity.getIdValue()) == null)
			{
				targetRepo.add(mappedEntity);
			}
			else
			{
				targetRepo.update(mappedEntity);
			}
		});
	}

	private void mapAndAddEntities(EntityMapping sourceMapping, Repository<Entity> targetRepo,
			boolean addSourceAttribute, EntityType targetMetaData, List<Entity> entities)
	{
		targetRepo.add(entities.stream()
				.map(sourceEntity -> applyMappingToEntity(sourceMapping, sourceEntity, targetMetaData,
						addSourceAttribute)));
	}

	private Entity applyMappingToEntity(EntityMapping sourceMapping, Entity sourceEntity, EntityType targetMetaData,
			boolean addSourceAttribute)
	{
		Entity target = new DynamicEntity(targetMetaData);
		if (addSourceAttribute)
		{
			target.set(SOURCE, sourceMapping.getName());
		}

		sourceMapping.getAttributeMappings().forEach(
				attributeMapping -> applyMappingToAttribute(attributeMapping, sourceEntity, target,
						sourceMapping.getSourceEntityType()));
		return target;
	}

	private void applyMappingToAttribute(AttributeMapping attributeMapping, Entity sourceEntity, Entity target,
			EntityType entityType)
	{
		String targetAttributeName = attributeMapping.getTargetAttribute().getName();
		Object typedValue = algorithmService.apply(attributeMapping, sourceEntity, entityType);
		target.set(targetAttributeName, typedValue);
	}
}
