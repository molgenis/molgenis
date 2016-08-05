package org.molgenis.data.mapper.service.impl;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.RowLevelSecurityRepositoryDecorator.UPDATE_ATTRIBUTE;
import static org.molgenis.data.RowLevelSecurityUtils.removeUpdateAttributeIfRowLevelSecured;
import static org.molgenis.data.mapper.meta.MappingProjectMetaData.NAME;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.elasticsearch.common.collect.Lists;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.auth.MolgenisUserMetaData;
import org.molgenis.data.*;
import org.molgenis.data.mapper.mapping.model.AttributeMapping;
import org.molgenis.data.mapper.mapping.model.EntityMapping;
import org.molgenis.data.mapper.mapping.model.MappingProject;
import org.molgenis.data.mapper.mapping.model.MappingTarget;
import org.molgenis.data.mapper.repository.MappingProjectRepository;
import org.molgenis.data.mapper.service.AlgorithmService;
import org.molgenis.data.mapper.service.MappingService;
import org.molgenis.data.meta.PackageImpl;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.molgenis.security.permission.PermissionSystemService;
import org.molgenis.util.HugeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Maps;

public class MappingServiceImpl implements MappingService
{
	private static final Logger LOG = LoggerFactory.getLogger(MappingServiceImpl.class);

	private static final int BATCH_SIZE = 1000;

	private final DataService dataService;

	private final AlgorithmService algorithmService;

	private final IdGenerator idGenerator;

	private final MappingProjectRepository mappingProjectRepository;

	private final PermissionSystemService permissionSystemService;

	@Autowired
	public MappingServiceImpl(DataService dataService, AlgorithmService algorithmService, IdGenerator idGenerator,
			MappingProjectRepository mappingProjectRepository, PermissionSystemService permissionSystemService)
	{
		this.dataService = requireNonNull(dataService);
		this.algorithmService = requireNonNull(algorithmService);
		this.idGenerator = requireNonNull(idGenerator);
		this.mappingProjectRepository = requireNonNull(mappingProjectRepository);
		this.permissionSystemService = requireNonNull(permissionSystemService);
	}

	@Override
	@RunAsSystem
	public MappingProject addMappingProject(String projectName, MolgenisUser owner, String target)
	{
		MappingProject mappingProject = new MappingProject(projectName, owner);

		EntityMetaData entityMetaData = dataService.getEntityMetaData(target);
		entityMetaData = removeUpdateAttributeIfRowLevelSecured(entityMetaData);
		mappingProject.addTarget(entityMetaData);
		mappingProjectRepository.add(mappingProject);
		return mappingProject;
	}

	@Override
	@RunAsSystem
	public void deleteMappingProject(String mappingProjectId)
	{
		mappingProjectRepository.delete(mappingProjectId);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SYSTEM, ROLE_SU, ROLE_PLUGIN_WRITE_MENUMANAGER')")
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

			if (mappingProjectRepository.getMappingProjects(new QueryImpl().eq(NAME, clonedMappingProjectName))
					.isEmpty())
			{
				break;
			}
		}

		return cloneMappingProject(mappingProject, clonedMappingProjectName);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SYSTEM, ROLE_SU, ROLE_PLUGIN_WRITE_MENUMANAGER')")
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
	public String applyMappings(MappingTarget mappingTarget, String entityName)
	{
		DefaultEntityMetaData targetMetaData = new DefaultEntityMetaData(entityName, mappingTarget.getTarget());
		targetMetaData.setPackage(PackageImpl.defaultPackage);
		targetMetaData.setLabel(entityName);
		targetMetaData.addAttribute("source");

		// add a new repository if the target repo doesn't exist
		Repository targetRepo;
		if (!dataService.hasRepository(entityName))
		{
			if (targetMetaData.isRowLevelSecured())
			{
				DefaultEntityMetaData defaultEntityMetaData = new DefaultEntityMetaData(targetMetaData);
				defaultEntityMetaData.addAttributeMetaData(
						new DefaultAttributeMetaData(UPDATE_ATTRIBUTE).setDataType(MolgenisFieldTypes.MREF)
								.setRefEntity(new MolgenisUserMetaData()));
				targetMetaData = defaultEntityMetaData;
			}
			targetRepo = dataService.getMeta().addEntityMeta(targetMetaData);
			permissionSystemService.giveUserEntityPermissions(SecurityContextHolder.getContext(),
					Collections.singletonList(targetRepo.getName()));
		}
		else
		{
			targetRepo = dataService.getRepository(entityName);
		}

		try
		{
			LOG.info("Applying mappings to repository [" + targetMetaData.getName() + "]");
			applyMappingsToRepositories(mappingTarget, targetRepo);
			LOG.info("Done applying mappings to repository [" + targetMetaData.getName() + "]");
			return targetMetaData.getName();
		}
		catch (RuntimeException ex)
		{
			LOG.error("Error applying mappings, dropping created repository.", ex);
			dataService.getMeta().deleteEntityMeta(targetMetaData.getName());
			throw ex;
		}
	}

	private void applyMappingsToRepositories(MappingTarget mappingTarget, Repository targetRepo)
	{
		for (EntityMapping sourceMapping : mappingTarget.getEntityMappings())
		{
			applyMappingToRepo(sourceMapping, targetRepo);
		}
	}

	private void applyMappingToRepo(EntityMapping sourceMapping, Repository targetRepo)
	{
		EntityMetaData targetMetaData = targetRepo.getEntityMetaData();
		Repository sourceRepo = dataService.getRepository(sourceMapping.getName());

		sourceRepo.iterator().forEachRemaining(sourceEntity -> {
			MapEntity mappedEntity = applyMappingToEntity(sourceMapping, sourceEntity, targetMetaData,
					sourceMapping.getSourceEntityMetaData(), targetRepo);

			if (targetRepo.findOne(mappedEntity.getIdValue()) == null)
			{
				targetRepo.add(mappedEntity);
			}
			else
			{
				targetRepo.update(mappedEntity);
			}
		});
	}

	private MapEntity applyMappingToEntity(EntityMapping sourceMapping, Entity sourceEntity,
			EntityMetaData targetMetaData, EntityMetaData sourceEntityMetaData, Repository targetRepository)
	{
		MapEntity target = new MapEntity(targetMetaData);
		target.set("source", sourceMapping.getName());

		sourceMapping.getAttributeMappings().forEach(
				attributeMapping -> applyMappingToAttribute(attributeMapping, sourceEntity, target,
						sourceEntityMetaData));
		return target;
	}

	@Override
	public String generateId(FieldType dataType, Long count)
	{
		Object id;
		if (dataType.equals(MolgenisFieldTypes.INT) || dataType.equals(MolgenisFieldTypes.LONG) || dataType
				.equals(MolgenisFieldTypes.DECIMAL))
		{
			id = count + 1;
		}
		else
		{
			id = idGenerator.generateId();
		}
		return id.toString();
	}

	private void applyMappingToAttribute(AttributeMapping attributeMapping, Entity sourceEntity, MapEntity target,
			EntityMetaData entityMetaData)
	{
		target.set(attributeMapping.getTargetAttributeMetaData().getName(),
				algorithmService.apply(attributeMapping, sourceEntity, entityMetaData));
	}
}
