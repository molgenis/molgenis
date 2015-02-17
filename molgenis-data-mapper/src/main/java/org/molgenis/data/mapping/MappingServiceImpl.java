package org.molgenis.data.mapping;

import java.util.Collections;
import java.util.List;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.ManageableCrudRepositoryCollection;
import org.molgenis.data.algorithm.AlgorithmService;
import org.molgenis.data.mapping.model.AttributeMapping;
import org.molgenis.data.mapping.model.EntityMapping;
import org.molgenis.data.mapping.model.MappingProject;
import org.molgenis.data.mapping.model.MappingTarget;
import org.molgenis.data.meta.PackageImpl;
import org.molgenis.data.repository.MappingProjectRepository;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.security.permission.PermissionSystemService;
import org.molgenis.security.runas.RunAsSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.IdGenerator;

public class MappingServiceImpl implements MappingService
{
	private static final Logger LOG = LoggerFactory.getLogger(MappingServiceImpl.class);

	@Autowired
	private DataService dataService;

	@Autowired
	private AlgorithmService algorithmService;

	@Autowired
	private ManageableCrudRepositoryCollection manageableCrudRepositoryCollection;

	@Autowired
	private IdGenerator idGenerator;

	@Autowired
	private MappingProjectRepository mappingProjectRepository;

	@Autowired
	private PermissionSystemService permissionSystemService;

	@Override
	@RunAsSystem
	public MappingProject addMappingProject(String projectName, MolgenisUser owner, String target)
	{
		MappingProject mappingProject = new MappingProject(projectName, owner);
		mappingProject.addTarget(dataService.getEntityMetaData(target));
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
	public String applyMappings(MappingTarget mappingTarget, String newEntityName)
	{
		DefaultEntityMetaData targetMetaData = new DefaultEntityMetaData(newEntityName, mappingTarget.getTarget());
		targetMetaData.setPackage(PackageImpl.defaultPackage);
		targetMetaData.setLabel(newEntityName);
		targetMetaData.addAttribute("source");
		CrudRepository targetRepo = manageableCrudRepositoryCollection.add(targetMetaData);
		permissionSystemService.giveUserEntityAndMenuPermissions(SecurityContextHolder.getContext(),
				Collections.singletonList(targetRepo.getName()));
		try
		{
			applyMappingsToRepositories(mappingTarget, targetRepo);
			return targetMetaData.getName();
		}
		catch (RuntimeException ex)
		{
			LOG.error("Error applying mappings, dropping created repository.", ex);
			manageableCrudRepositoryCollection.dropEntityMetaData(targetMetaData.getName());
			throw ex;
		}
	}

	private void applyMappingsToRepositories(MappingTarget mappingTarget, CrudRepository targetRepo)
	{
		for (EntityMapping sourceMapping : mappingTarget.getEntityMappings())
		{
			applyMappingToRepo(sourceMapping, targetRepo);
		}
	}

	private void applyMappingToRepo(EntityMapping sourceMapping, CrudRepository targetRepo)
	{
		EntityMetaData targetMetaData = targetRepo.getEntityMetaData();
		CrudRepository sourceRepo = dataService.getCrudRepository(sourceMapping.getName());
		for (Entity sourceEntity : sourceRepo)
		{
			MapEntity mappedEntity = applyMappingToEntity(sourceMapping, sourceEntity, targetMetaData);
			targetRepo.add(mappedEntity);
		}
	}

	private MapEntity applyMappingToEntity(EntityMapping sourceMapping, Entity sourceEntity,
			EntityMetaData targetMetaData)
	{
		MapEntity target = new MapEntity(targetMetaData);
		target.set(targetMetaData.getIdAttribute().getName(), idGenerator.generateId().toString());
		target.set("source", sourceMapping.getName());
		sourceMapping.getAttributeMappings().forEach(
				attributeMapping -> applyMappingToAttribute(attributeMapping, sourceEntity, target));
		return target;
	}

	private void applyMappingToAttribute(AttributeMapping attributeMapping, Entity sourceEntity, MapEntity target)
	{
		if (!attributeMapping.getTargetAttributeMetaData().isIdAtrribute())
		{
			target.set(attributeMapping.getTargetAttributeMetaData().getName(),
					algorithmService.apply(attributeMapping, sourceEntity));
		}
	}
}
