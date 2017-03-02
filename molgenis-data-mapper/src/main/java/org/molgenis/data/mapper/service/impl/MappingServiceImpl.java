package org.molgenis.data.mapper.service.impl;

import org.molgenis.auth.User;
import org.molgenis.data.*;
import org.molgenis.data.mapper.mapping.model.AttributeMapping;
import org.molgenis.data.mapper.mapping.model.EntityMapping;
import org.molgenis.data.mapper.mapping.model.MappingProject;
import org.molgenis.data.mapper.mapping.model.MappingTarget;
import org.molgenis.data.mapper.repository.MappingProjectRepository;
import org.molgenis.data.mapper.service.AlgorithmService;
import org.molgenis.data.mapper.service.MappingService;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.populate.IdGenerator;
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
	private static final Logger LOG = LoggerFactory.getLogger(MappingServiceImpl.class);

	public static final String SOURCE = "source";

	private final DataService dataService;
	private final AlgorithmService algorithmService;
	private final IdGenerator idGenerator;
	private final MappingProjectRepository mappingProjectRepository;
	private final PermissionSystemService permissionSystemService;
	private final AttributeFactory attrMetaFactory;

	@Autowired
	public MappingServiceImpl(DataService dataService, AlgorithmService algorithmService, IdGenerator idGenerator,
			MappingProjectRepository mappingProjectRepository, PermissionSystemService permissionSystemService,
			AttributeFactory attrMetaFactory)
	{
		this.dataService = requireNonNull(dataService);
		this.algorithmService = requireNonNull(algorithmService);
		this.idGenerator = requireNonNull(idGenerator);
		this.mappingProjectRepository = requireNonNull(mappingProjectRepository);
		this.permissionSystemService = requireNonNull(permissionSystemService);
		this.attrMetaFactory = requireNonNull(attrMetaFactory);
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

			if (mappingProjectRepository.getMappingProjects(new QueryImpl<Entity>().eq(NAME, clonedMappingProjectName))
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

	public String applyMappings(MappingTarget mappingTarget, String entityName)
	{
		return applyMappings(mappingTarget, entityName, true);
	}

	@Override
	@Transactional
	public String applyMappings(MappingTarget mappingTarget, String entityName, boolean addSourceAttribute)
	{
		EntityType targetMetaData = EntityType.newInstance(mappingTarget.getTarget(), DEEP_COPY_ATTRS, attrMetaFactory);
		targetMetaData.setPackage(null);
		targetMetaData.setId(idGenerator.generateId());
		targetMetaData.setName(entityName);
		targetMetaData.setLabel(entityName);
		if (addSourceAttribute)
		{
			targetMetaData.addAttribute(attrMetaFactory.create().setName(SOURCE));
		}

		Repository<Entity> targetRepo;
		if (!dataService.hasRepository(entityName))
		{
			// Create a new repository
			targetRepo = runAsSystem(() -> dataService.getMeta().createRepository(targetMetaData));
			permissionSystemService.giveUserEntityPermissions(targetMetaData);
		}
		else
		{
			// Get an existing repository
			targetRepo = dataService.getRepository(entityName);

			// Compare the metadata between the target repository and the mapping target
			// Returns detailed information in case something is not compatible
			compareTargetMetaDatas(targetRepo.getEntityType(), targetMetaData);

			// If the addSourceAttribute is true, but the existing repository does not have the SOURCE attribute yet
			// Get the existing metadata and add the SOURCE attribute
			EntityType existingTargetMetaData = targetRepo.getEntityType();
			if (existingTargetMetaData.getAttribute(SOURCE) == null && addSourceAttribute)
			{
				existingTargetMetaData.addAttribute(attrMetaFactory.create().setName(SOURCE));
				dataService.getMeta().updateEntityType(existingTargetMetaData);
			}
		}

		try
		{
			LOG.info("Applying mappings to repository [" + targetMetaData.getFullyQualifiedName() + "]");
			applyMappingsToRepositories(mappingTarget, targetRepo, addSourceAttribute);
			if (hasSelfReferences(targetRepo.getEntityType()))
			{
				LOG.info("Self reference found, applying the mapping for a second time to set references");
				applyMappingsToRepositories(mappingTarget, targetRepo, addSourceAttribute);
			}
			LOG.info("Done applying mappings to repository [" + targetMetaData.getFullyQualifiedName() + "]");
			return targetMetaData.getFullyQualifiedName();
		}
		catch (RuntimeException ex)
		{
			// Mapping to the target model, if something goes wrong we do not want to delete it
			LOG.error("Error applying mappings to the target", ex);
			throw ex;
		}
	}

	/**
	 * Compares the attributes between the target repository and the mapping target.
	 * Applied Rules:
	 * - The mapping target can not contain attributes which are not in the target repository
	 * - The attributes of the mapping target with the same name as attributes in the target repository should have the same type
	 * - If there are reference attributes, the name of the reference entity should be the same in both the target repository as in the mapping target
	 *
	 * @param targetRepositoryMetaData
	 * @param mappingTargetMetaData
	 * @return A {@link String} containing details on a potential mapping exception, or null if the attributes of both the target repository and mapping target are compatible
	 */
	private void compareTargetMetaDatas(EntityType targetRepositoryMetaData, EntityType mappingTargetMetaData)
	{
		Map<String, Attribute> targetRepositoryAttributeMap = newHashMap();
		targetRepositoryMetaData.getAtomicAttributes()
				.forEach(attribute -> targetRepositoryAttributeMap.put(attribute.getName(), attribute));

		for (Attribute mappingTargetAttribute : mappingTargetMetaData.getAtomicAttributes())
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
				String mappingTargetRefEntityName = mappingTargetAttribute.getRefEntity().getFullyQualifiedName();
				String targetRepositoryRefEntityName = targetRepositoryAttribute.getRefEntity().getFullyQualifiedName();
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
			boolean addSourceAttribute)
	{
		for (EntityMapping sourceMapping : mappingTarget.getEntityMappings())
		{
			applyMappingToRepo(sourceMapping, targetRepo, addSourceAttribute);
		}
	}

	private void applyMappingToRepo(EntityMapping sourceMapping, Repository<Entity> targetRepo,
			boolean addSourceAttribute)
	{
		EntityType targetMetaData = targetRepo.getEntityType();
		Repository<Entity> sourceRepo = dataService.getRepository(sourceMapping.getName());

		if (targetRepo.count() == 0)
		{
			sourceRepo.forEachBatched(entities -> targetRepo.add(entities.stream()
					.map(sourceEntity -> applyMappingToEntity(sourceMapping, sourceEntity, targetMetaData,
							sourceMapping.getSourceEntityType(), addSourceAttribute))), 1000);
		}
		else
		{
			// FIXME adding/updating row-by-row is a performance bottleneck, this code could do streaming upsert
			sourceRepo.iterator().forEachRemaining(sourceEntity ->
			{
				{
					Entity mappedEntity = applyMappingToEntity(sourceMapping, sourceEntity, targetMetaData,
							sourceMapping.getSourceEntityType(), addSourceAttribute);
					if (targetRepo.findOneById(mappedEntity.getIdValue()) == null)
					{
						targetRepo.add(mappedEntity);
					}
					else
					{
						targetRepo.update(mappedEntity);
					}
				}
			});
		}

	}

	private Entity applyMappingToEntity(EntityMapping sourceMapping, Entity sourceEntity, EntityType targetMetaData,
			EntityType sourceEntityType, boolean addSourceAttribute)
	{
		Entity target = new DynamicEntity(targetMetaData);
		if (addSourceAttribute)
		{
			target.set(SOURCE, sourceMapping.getName());
		}

		sourceMapping.getAttributeMappings().forEach(
				attributeMapping -> applyMappingToAttribute(attributeMapping, sourceEntity, target, sourceEntityType));
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
