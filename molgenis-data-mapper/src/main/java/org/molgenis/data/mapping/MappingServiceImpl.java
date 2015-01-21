package org.molgenis.data.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.repository.AttributeMappingRepository;
import org.molgenis.data.repository.EntityMappingRepository;
import org.molgenis.data.repository.MappingProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class MappingServiceImpl implements MappingService
{
	@Autowired
	private MetaDataService metaDataService;
	private static final String SEPARATOR = "_";
	private AttributeMappingRepository attributeMappingRepository;
	private EntityMappingRepository entityMappingRepository;
	private MappingProjectRepository mappingProjectRepository;

	public MappingServiceImpl(AttributeMappingRepository attributeMappingRepository,
			EntityMappingRepository entityMappingRepository, MappingProjectRepository mappingProjectRepository)
	{
		this.attributeMappingRepository = attributeMappingRepository;
		this.entityMappingRepository = entityMappingRepository;
		this.mappingProjectRepository = mappingProjectRepository;
	}

	@Override
	public void addEntityMapping(EntityMapping entityMapping)
	{
		entityMappingRepository.add(entityMapping);
	}

	@Override
	public void addMappingProject(String projectName, MolgenisUser owner, List<String> targetEntityIdentifiers)
	{
		List<EntityMapping> entityMappings = new ArrayList<EntityMapping>();
		for (String targetEntityIdentifier : targetEntityIdentifiers)
		{
			EntityMetaData entityMetaData = metaDataService.getEntityMetaData(targetEntityIdentifier);
			entityMappings.add(new EntityMapping(createEntityMappingIdentifier(projectName, targetEntityIdentifier),
					entityMetaData, entityMetaData, null));
		}

		entityMappingRepository.add(entityMappings);
		mappingProjectRepository.add(new MappingProject(projectName, owner.getUsername(), entityMappings));
	}

	@Override
	public List<MappingProject> getAllMappingProjects()
	{
		return mappingProjectRepository.getAllMappingProjects();
	}

	@Override
	public void updateMappingProject(MappingProject mappingProject)
	{
		mappingProjectRepository.update(mappingProject);
	}

	@Override
	public MappingProject getMappingProject(String identifier)
	{
		return mappingProjectRepository.getMappingProject(identifier);
	}

	@Override
	public List<AttributeMapping> getAttributeMappings(String identifier)
	{
		return null; // TODO: Implement
	}

	private String createEntityMappingIdentifier(String projectMappingIdentifier, String targetEntityIdentifier)
	{
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(projectMappingIdentifier).append(SEPARATOR).append(targetEntityIdentifier);
		return stringBuilder.toString();
	}
}
