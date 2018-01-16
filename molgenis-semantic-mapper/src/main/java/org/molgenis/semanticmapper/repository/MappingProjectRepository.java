package org.molgenis.semanticmapper.repository;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.semanticmapper.mapping.model.MappingProject;

import java.util.List;

public interface MappingProjectRepository
{
	/**
	 * Adds a {@link MappingProject} to the {@link MappingProjectRepository}
	 */
	void add(MappingProject mappingProject);

	/**
	 * Update an existing {@link MappingProject}
	 */
	void update(MappingProject mappingProject);

	/**
	 * Get a {@link MappingProject} object based on the project identifier
	 */
	MappingProject getMappingProject(String identifier);

	/**
	 * Get a list of all {@link MappingProject}
	 */
	List<MappingProject> getAllMappingProjects();

	/**
	 * Get a list of {@link MappingProject} based on a query
	 */
	List<MappingProject> getMappingProjects(Query<Entity> q);

	/**
	 * Delete a mapping project.
	 *
	 * @param mappingProjectId id of the {@link MappingProject} to delete
	 */
	void delete(String mappingProjectId);

}