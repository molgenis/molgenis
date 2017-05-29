package org.molgenis.data.mapper.repository;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.mapper.mapping.model.MappingProject;

import java.util.List;

public interface MappingProjectRepository
{
	/**
	 * Adds a {@link MappingProject} to the {@link MappingProjectRepository}
	 *
	 * @param mappingProject
	 */
	void add(MappingProject mappingProject);

	/**
	 * Update an existing {@link MappingProject}
	 *
	 * @param mappingProject
	 */
	void update(MappingProject mappingProject);

	/**
	 * Get a {@link MappingProject} object based on the project identifier
	 *
	 * @param identifier
	 * @return
	 */
	MappingProject getMappingProject(String identifier);

	/**
	 * Get a list of all {@link MappingProject}
	 *
	 * @return
	 */
	List<MappingProject> getAllMappingProjects();

	/**
	 * Get a list of {@link MappingProject} based on a query
	 *
	 * @param q
	 * @return
	 */
	List<MappingProject> getMappingProjects(Query<Entity> q);

	/**
	 * Delete a mapping project.
	 *
	 * @param mappingProjectId id of the {@link MappingProject} to delete
	 */
	void delete(String mappingProjectId);

}