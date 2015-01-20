package org.molgenis.data.mapping;

import java.util.HashMap;
import java.util.List;

import org.molgenis.auth.MolgenisUser;

public interface MappingService
{
	abstract void addEntityMapping(MappingProject mappingProject, EntityMapping entityMapping);

	abstract void addMappingProject(String string, MolgenisUser currentUser);

	abstract List<MappingProject> getAllMappingProjects();

	abstract void updateMappingProject(MappingProject mappingProject);

	abstract MappingProject getMappingProject(String identifier);

	abstract List<AttributeMapping> getAttributeMappings(String identifier);
	
}
