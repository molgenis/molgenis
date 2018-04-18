package org.molgenis.semanticmapper.service;

import org.molgenis.data.meta.model.EntityType;
import org.molgenis.jobs.Progress;
import org.molgenis.semanticmapper.mapping.model.AttributeMapping;
import org.molgenis.semanticmapper.mapping.model.EntityMapping;
import org.molgenis.semanticmapper.mapping.model.MappingProject;
import org.molgenis.semanticmapper.mapping.model.MappingTarget;

import java.util.List;
import java.util.stream.Stream;

public interface MappingService
{
	/**
	 * Creates a new {@link MappingProject}
	 *
	 * @param target name of the first target entity
	 */
	MappingProject addMappingProject(String name, String target);

	/**
	 * Retrieves all {@link MappingProject}s.
	 *
	 * @return list of all {@link MappingProject}s.
	 */
	List<MappingProject> getAllMappingProjects();

	/**
	 * Updates a MappingProject in the repository. All {@link MappingTarget}s, {@link EntityMapping}s and
	 * {@link AttributeMapping}s are updated.
	 *
	 * @param mappingProject the {@link MappingProject} to update.
	 */
	void updateMappingProject(MappingProject mappingProject);

	/**
	 * Retrieves a {@link MappingProject} from the repository.
	 *
	 * @param identifier ID of the {@link MappingProject}
	 * @return the retrieved MappingProject
	 */
	MappingProject getMappingProject(String identifier);

	/**
	 * Applies all mappings in a {@link MappingTarget}
	 *
	 * @param mappingProjectId   the id of the MappingProject whose mappings are applied
	 * @param entityTypeId       the name of the entity to map to
	 * @param addSourceAttribute boolean indicating if the 'source' attribute should be added to the target repository
	 * @param packageId          the id of the destination Package, ignored when mapping to existing EntityType
	 * @param label              label of the target EntityType, ignored when mapping to existing EntityType
	 * @param progress           progress of the mapping
	 * @return the total amount of entities mapped
	 */
	long applyMappings(String mappingProjectId, String entityTypeId, Boolean addSourceAttribute, String packageId,
			String label, Progress progress);

	/**
	 * Deletes a {@link MappingProject}
	 *
	 * @param mappingProjectId id of the {@link MappingProject} to delete
	 */
	void deleteMappingProject(String mappingProjectId);

	/**
	 * Clones a {@link MappingProject}. Deep copies all related mappings. Automatically generates name for cloned
	 * {@link MappingProject}.
	 *
	 * @param mappingProjectId id of the {@link MappingProject} to clone
	 * @return cloned {@link MappingProject}
	 */
	MappingProject cloneMappingProject(String mappingProjectId);

	/**
	 * Clones a {@link MappingProject}. Deep copies all related mappings.
	 *
	 * @param mappingProjectId         id of the {@link MappingProject} to clone
	 * @param clonedMappingProjectName name of the cloned {@link MappingProject}
	 * @return cloned {@link MappingProject}
	 */
	MappingProject cloneMappingProject(String mappingProjectId, String clonedMappingProjectName);

	/**
	 * Retrieves a Stream of existing compatible {@link EntityType}s that are valid as a mapping target.
	 *
	 * @param target EntityType of the mapping target
	 * @return Stream of compatible {@link EntityType}s
	 */
	Stream<EntityType> getCompatibleEntityTypes(EntityType target);

}
