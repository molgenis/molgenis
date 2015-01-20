package org.molgenis.data.mapping;

import java.util.List;

import org.molgenis.auth.MolgenisUser;

public interface MappingService
{
	abstract void addEntityMapping(MappingProject mappingProject, EntityMapping entityMapping);

	abstract void addMappingProject(String string, MolgenisUser currentUser);

	abstract List<MappingProject> getAllMappingProjects();
	
}
