package org.molgenis.data.mapper.repository;

import org.molgenis.data.Entity;
import org.molgenis.data.mapper.mapping.model.AttributeMapping;
import org.molgenis.data.mapper.mapping.model.EntityMapping;
import org.molgenis.data.mapper.meta.AttributeMappingMetaData;
import org.molgenis.data.mapper.meta.EntityMappingMetaData;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;

import java.util.Collection;
import java.util.List;

/**
 * O/R Mapping between {@link EntityMappingMetaData} Entities and {@link EntityMapping} s.
 */
public interface AttributeMappingRepository
{
	/**
	 * Creates a list of fully reconstructed {@link AttributeMapping}s.
	 *
	 * @param attributeMappingEntities List of {@link Entity}s with {@link AttributeMappingMetaData} metadata
	 * @param sourceEntityMetaData     {@link EntityMetaData} of the source entity of the attribute mapping, used to look up the
	 *                                 {@link AttributeMetaData}
	 * @param targetEntityMetaData     {@link EntityMetaData} of the target entity of the attribute mapping, used to look up the
	 *                                 {@link AttributeMetaData}
	 * @return a list of {@link AttributeMapping}s.
	 */
	List<AttributeMapping> getAttributeMappings(List<Entity> attributeMappingEntities,
			EntityMetaData sourceEntityMetaData, EntityMetaData targetEntityMetaData);

	/**
	 * Inserts or updates a {@link Collection} of {@link AttributeMapping}. Will generate IDs if they are not yet
	 * specified.
	 *
	 * @return a list of Entities that have been added or updated
	 */
	List<Entity> upsert(Collection<AttributeMapping> collection);

	/**
	 * Translates an algorithm to a list of {@link AttributeMetaData} based on the algorithm, and the
	 * {@link EntityMetaData} of the source entity
	 *
	 * @param algorithm
	 * @param sourceEntityMetaData
	 * @return a list of {@link AttributeMetaData}
	 */
	List<AttributeMetaData> retrieveAttributeMetaDatasFromAlgorithm(String algorithm,
			EntityMetaData sourceEntityMetaData);

}