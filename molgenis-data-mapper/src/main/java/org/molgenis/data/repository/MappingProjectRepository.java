package org.molgenis.data.repository;

import java.util.List;

import org.molgenis.data.Query;
import org.molgenis.data.mapping.model.MappingProject;

public interface MappingProjectRepository
{
	/**
	 * Adds a {@link MappingProject} to the {@link MappingProjectRepository}
	 * 
	 * @param mappingProject
	 */
	public abstract void add(MappingProject mappingProject);

	/**
	 * Update an existing {@link MappingProject}
	 * 
	 * @param mappingProject
	 */
	public abstract void update(MappingProject mappingProject);

	/**
	 * Get a {@link MappingProject} object based on the project identifier
	 * 
	 * @param identifier
	 * @return
	 */
	public abstract MappingProject getMappingProject(String identifier);

	/**
	 * Get a list of all {@link MappingProject}
	 * 
	 * @return
	 */
	public abstract List<MappingProject> getAllMappingProjects();

	/**
	 * Get a list of {@link MappingProject} based on a query
	 * 
	 * @param q
	 * @return
	 */
	public abstract List<MappingProject> getMappingProjects(Query q);

	/**
	 * Delete a mapping project.
	 * 
	 * @param mappingProjectId
	 *            id of the {@link MappingProject} to delete
	 */
	public abstract void delete(String mappingProjectId);

}