package org.molgenis.semanticmapper.repository;

import org.molgenis.data.Entity;
import org.molgenis.semanticmapper.mapping.model.AttributeMapping;
import org.molgenis.semanticmapper.mapping.model.EntityMapping;
import org.molgenis.semanticmapper.mapping.model.MappingTarget;
import org.molgenis.semanticmapper.meta.MappingTargetMetaData;

import java.util.Collection;
import java.util.List;

public interface MappingTargetRepository
{
	/**
	 * Inserts or updates a list of {@link MappingTarget}s and their {@link EntityMapping}s and {@link AttributeMapping}
	 * s. Will generate IDs if they are not yet specified.
	 *
	 * @return a {@link List} of {@link Entity}s that have been added or updated
	 */
	List<Entity> upsert(Collection<MappingTarget> collection);

	/**
	 * Creates a list of fully reconstructed {@link MappingTarget}s.
	 *
	 * @param mappingTargetEntities List of {@link Entity}s with {@link MappingTargetMetaData} metadata
	 * @return a {@link List} of {@link MappingTarget}s.
	 */
	List<MappingTarget> toMappingTargets(List<Entity> mappingTargetEntities);
}
