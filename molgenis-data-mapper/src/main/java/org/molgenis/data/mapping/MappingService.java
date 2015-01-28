package org.molgenis.data.mapping;

import java.util.List;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.mapping.model.EntityMapping;
import org.molgenis.data.mapping.model.MappingProject;

public interface MappingService
{
	abstract void addEntityMapping(EntityMapping entityMapping);

	/**
	 * Creates a new {@link MappingProject}
	 * 
	 * @param target
	 *            name of the first target entity
	 */
	abstract void addMappingProject(String name, MolgenisUser owner, String target);

	abstract List<MappingProject> getAllMappingProjects();

	abstract void updateMappingProject(MappingProject mappingProject);

	abstract MappingProject getMappingProject(String identifier);

}
