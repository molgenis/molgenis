package org.molgenis.semanticmapper.repository;

import org.molgenis.data.Entity;
import org.molgenis.semanticmapper.mapping.model.EntityMapping;
import org.molgenis.semanticmapper.meta.EntityMappingMetaData;

import java.util.Collection;
import java.util.List;

/**
 * O/R Mapping between {@link EntityMappingMetaData} Entities and {@link EntityMapping} s.
 */
public interface EntityMappingRepository
{
	/**
	 * Creates a list of fully reconstructed {@link EntityMapping}s.
	 *
	 * @param entityMappingEntities List of {@link Entity}s with {@link EntityMappingMetaData} metadata
	 * @return a list of {@link EntityMapping}s.
	 */
	List<EntityMapping> toEntityMappings(List<Entity> entityMappingEntities);

	/**
	 * Inserts or updates a list of EntityMappings and their AttributeMappings. Will generate IDs if they are not yet
	 * specified.
	 *
	 * @return a list of Entities that have been added or updated
	 */
	List<Entity> upsert(Collection<EntityMapping> collection);

}