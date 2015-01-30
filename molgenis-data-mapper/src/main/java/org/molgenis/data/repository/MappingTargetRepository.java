package org.molgenis.data.repository;

import java.util.Collection;
import java.util.List;

import org.molgenis.data.Entity;
import org.molgenis.data.mapping.model.EntityMapping;
import org.molgenis.data.mapping.model.MappingTarget;
import org.molgenis.data.meta.MappingTargetMetaData;

public interface MappingTargetRepository
{
	/**
	 * Inserts or updates a list of MappingTargets and their EntityMappings and AttributeMappings. Will generate IDs if
	 * they are not yet specified.
	 * 
	 * @return a list of Entities that have been added or updated
	 */
	List<Entity> upsert(Collection<MappingTarget> collection);

	/**
	 * Creates a list of fully reconstructed {@link EntityMapping}s.
	 * 
	 * @param mappingTargetEntities
	 *            List of {@link Entity}s with {@link MappingTargetMetaData} metadata
	 * @return a list of {@link MappingTarget}s.
	 */
	List<MappingTarget> toMappingTargets(List<Entity> mappingTargetEntities);
}
