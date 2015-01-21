package org.molgenis.data.mapping;

import java.util.List;

import org.molgenis.auth.MolgenisUser;

public interface MappingService
{
	abstract void addEntityMapping(EntityMapping entityMapping);

	abstract void addMappingProject(String string, MolgenisUser currentUser, List<String> targetEntityIdentifiers);

	abstract List<MappingProject> getAllMappingProjects();

	abstract void updateMappingProject(MappingProject mappingProject);

	abstract MappingProject getMappingProject(String identifier);

	abstract List<AttributeMapping> getAttributeMappings(String identifier);
	
}
