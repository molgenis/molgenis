package org.molgenis.semanticmapper.service.impl;

import org.molgenis.data.*;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.DefaultPackage;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.permission.PermissionSystemService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.jobs.Progress;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.semanticmapper.mapping.model.AttributeMapping;
import org.molgenis.semanticmapper.mapping.model.EntityMapping;
import org.molgenis.semanticmapper.mapping.model.MappingProject;
import org.molgenis.semanticmapper.mapping.model.MappingTarget;
import org.molgenis.semanticmapper.repository.MappingProjectRepository;
import org.molgenis.semanticmapper.service.AlgorithmService;
import org.molgenis.semanticmapper.service.MappingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.google.api.client.util.Maps.newHashMap;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.EntityManager.CreationMode.POPULATE;
import static org.molgenis.data.meta.model.EntityType.AttributeCopyMode.DEEP_COPY_ATTRS;
import static org.molgenis.data.support.EntityTypeUtils.hasSelfReferences;
import static org.molgenis.data.support.EntityTypeUtils.isReferenceType;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;
import static org.molgenis.semanticmapper.meta.MappingProjectMetaData.NAME;

public class MappingServiceImpl implements MappingService
{
	public static final int MAPPING_BATCH_SIZE = 1000;

	static final String SOURCE = "source";

	private final DataService dataService;
	private final AlgorithmService algorithmService;
	private final MappingProjectRepository mappingProjectRepository;
	private final PermissionSystemService permissionSystemService;
	private final AttributeFactory attrMetaFactory;
	private final DefaultPackage defaultPackage;
	private final EntityManager entityManager;

	public MappingServiceImpl(DataService dataService, AlgorithmService algorithmService,
			MappingProjectRepository mappingProjectRepository, PermissionSystemService permissionSystemService,
			AttributeFactory attrMetaFactory, DefaultPackage defaultPackage, EntityManager entityManager)
	{
		this.dataService = requireNonNull(dataService);
		this.algorithmService = requireNonNull(algorithmService);
		this.mappingProjectRepository = requireNonNull(mappingProjectRepository);
		this.permissionSystemService = requireNonNull(permissionSystemService);
		this.attrMetaFactory = requireNonNull(attrMetaFactory);
		this.defaultPackage = requireNonNull(defaultPackage);
		this.entityManager = requireNonNull(entityManager);
	}

	@Override
	@RunAsSystem
	@Transactional
	public MappingProject addMappingProject(String projectName, String target)
	{
		MappingProject mappingProject = new MappingProject(projectName);
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
	@Transactional
	public long applyMappings(String mappingProjectId, String entityTypeId, Boolean addSourceAttribute,
			String packageId, String label, Progress progress)
	{
		MappingProject mappingProject = getMappingProject(mappingProjectId);
		MappingTarget mappingTarget = mappingProject.getMappingTargets().get(0);
		progress.setProgressMax(calculateMaxProgress(mappingTarget));

		progress.progress(0, format("Checking target repository [%s]...", entityTypeId));
		EntityType targetMetadata = createTargetMetadata(mappingTarget, entityTypeId, packageId, label,
				addSourceAttribute);
		Repository<Entity> targetRepo = getTargetRepository(entityTypeId, targetMetadata);
		return applyMappingsInternal(mappingTarget, targetRepo, progress);
	}

	EntityType createTargetMetadata(MappingTarget mappingTarget, String entityTypeId, String packageId, String label,
			Boolean addSourceAttribute)
	{
		EntityType targetMetadata = EntityType.newInstance(mappingTarget.getTarget(), DEEP_COPY_ATTRS, attrMetaFactory);
		targetMetadata.setId(entityTypeId);

		if (label != null)
		{
			targetMetadata.setLabel(label);
		}
		else
		{
			targetMetadata.setLabel(entityTypeId);
		}

		if (TRUE.equals(addSourceAttribute))
		{
			targetMetadata.addAttribute(attrMetaFactory.create().setName(SOURCE));
		}

		if (packageId == null)
		{
			targetMetadata.setPackage(defaultPackage);
		}
		else
		{
			targetMetadata.setPackage(dataService.getMeta().getPackage(packageId));
		}

		return targetMetadata;
	}

	private Repository<Entity> getTargetRepository(String entityTypeId, EntityType targetMetadata)
	{
		Repository<Entity> targetRepo;
		if (!dataService.hasRepository(entityTypeId))
		{
			targetRepo = addTargetEntityType(targetMetadata);
		}
		else
		{
			targetRepo = dataService.getRepository(entityTypeId);
			compareTargetMetadatas(targetRepo.getEntityType(), targetMetadata);
		}
		return targetRepo;
	}

	private Repository<Entity> addTargetEntityType(EntityType targetMetadata)
	{
		Repository<Entity> targetRepo = runAsSystem(() -> dataService.getMeta().createRepository(targetMetadata));
		permissionSystemService.giveUserWriteMetaPermissions(targetMetadata);
		return targetRepo;
	}

	private long applyMappingsInternal(MappingTarget mappingTarget, Repository<Entity> targetRepo, Progress progress)
	{
		progress.status("Applying mappings to repository [" + targetRepo.getEntityType().getId() + "]");
		long result = applyMappingsToRepositories(mappingTarget, targetRepo, progress);
		if (hasSelfReferences(targetRepo.getEntityType()))
		{
			progress.status("Self reference found, applying the mapping for a second time to set references");
			applyMappingsToRepositories(mappingTarget, targetRepo, progress);
		}
		progress.status("Done applying mappings to repository [" + targetRepo.getEntityType().getId() + "]");
		return result;
	}

	public Stream<EntityType> getCompatibleEntityTypes(EntityType target)
	{
		return dataService.getMeta()
						  .getEntityTypes()
						  .filter(candidate -> !candidate.isAbstract())
						  .filter(isCompatible(target));
	}

	private Predicate<EntityType> isCompatible(EntityType target)
	{
		return candidate ->
		{
			try
			{
				compareTargetMetadatas(candidate, target);
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
	 * @param mappingTargetEntityType    the mapping target EntityType to check
	 * @throws MolgenisDataException if the types are not compatible
	 */
	private void compareTargetMetadatas(EntityType targetRepositoryEntityType, EntityType mappingTargetEntityType)
	{
		Map<String, Attribute> targetRepositoryAttributeMap = newHashMap();
		targetRepositoryEntityType.getAtomicAttributes()
								  .forEach(attribute -> targetRepositoryAttributeMap.put(attribute.getName(),
										  attribute));

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

	private long applyMappingsToRepositories(MappingTarget mappingTarget, Repository<Entity> targetRepo,
			Progress progress)
	{
		return mappingTarget.getEntityMappings()
							.stream()
							.mapToLong(sourceMapping -> applyMappingToRepo(sourceMapping, targetRepo, progress))
							.sum();
	}

	long applyMappingToRepo(EntityMapping sourceMapping, Repository<Entity> targetRepo, Progress progress)
	{
		progress.status(format("Mapping source [%s]...", sourceMapping.getLabel()));
		AtomicLong counter = new AtomicLong();

		boolean canAdd = targetRepo.count() == 0;
		dataService.getRepository(sourceMapping.getName())
				   .forEachBatched(
						   entities -> processBatch(sourceMapping, targetRepo, progress, counter, canAdd, entities),
						   MAPPING_BATCH_SIZE);

		progress.status(format("Mapped %s [%s] entities.", counter, sourceMapping.getLabel()));
		return counter.get();
	}

	private void processBatch(EntityMapping sourceMapping, Repository<Entity> targetRepo, Progress progress,
			AtomicLong counter, boolean canAdd, List<Entity> entities)
	{
		List<Entity> mappedEntities = mapEntities(sourceMapping, targetRepo.getEntityType(), entities);
		if (canAdd)
		{
			targetRepo.add(mappedEntities.stream());
		}
		else
		{
			targetRepo.upsertBatch(mappedEntities);
		}
		progress.increment(1);
		counter.addAndGet(entities.size());
	}

	private List<Entity> mapEntities(EntityMapping sourceMapping, EntityType targetMetaData, List<Entity> entities)
	{
		return entities.stream()
					   .map(sourceEntity -> applyMappingToEntity(sourceMapping, sourceEntity, targetMetaData))
					   .collect(toList());
	}

	/**
	 * Package-private for testablility
	 */
	Entity applyMappingToEntity(EntityMapping sourceMapping, Entity sourceEntity, EntityType targetMetaData)
	{
		Entity target = entityManager.create(targetMetaData, POPULATE);

		if (targetMetaData.getAttribute(SOURCE) != null)
		{
			target.set(SOURCE, sourceMapping.getName());
		}

		sourceMapping.getAttributeMappings()
					 .forEach(attributeMapping -> applyMappingToAttribute(attributeMapping, sourceEntity, target,
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

	int calculateMaxProgress(MappingTarget mappingTarget)
	{
		int batches = mappingTarget.getEntityMappings().stream().mapToInt(this::countBatches).sum();
		if (mappingTarget.hasSelfReferences())
		{
			batches *= 2;
		}
		return batches;
	}

	private int countBatches(EntityMapping entityMapping)
	{
		long sourceRows = dataService.count(entityMapping.getSourceEntityType().getId());

		long batches = sourceRows / MAPPING_BATCH_SIZE;
		long remainder = sourceRows % MAPPING_BATCH_SIZE;

		if (remainder > 0)
		{
			batches++;
		}

		return (int) batches;
	}
}
