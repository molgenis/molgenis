package org.molgenis.data.mapper.service;

import java.util.Collection;
import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.mapper.mapping.model.AttributeMapping;
import org.molgenis.data.mapper.mapping.model.EntityMapping;

public interface AlgorithmService
{
	List<Object> applyAlgorithm(AttributeMetaData targetAttribute, String algorithm, Repository sourceRepo);

	/**
	 * Applies an {@link AttributeMapping} to a source {@link Entity}
	 * 
	 * @param attributeMapping
	 *            {@link AttributeMapping} to apply
	 * @param sourceEntity
	 *            {@link Entity} to apply the mapping to
	 * @return Object containing the mapped value
	 */
	Object apply(AttributeMapping attributeMapping, Entity sourceEntity, EntityMetaData sourceEntityMetaData);

	/**
	 * Retrieves the names of the source attributes in an algorithm
	 * 
	 * @param algorithmScript
	 *            String with the algorithm script
	 * @return Collection of source attribute name Strings
	 */
	Collection<String> getSourceAttributeNames(String algorithmScript);
	
	/**
	 * Creates an attribute mapping after the semantic search service finds one 
	 * @param sourceEntityMetaData
	 * @param targetEntityMetaData
	 * @param mapping
	 * @param targetAttribute
	 */
	void autoGenerateAlgorithm(EntityMetaData sourceEntityMetaData,
			EntityMetaData targetEntityMetaData, EntityMapping mapping, AttributeMetaData targetAttribute);
}
