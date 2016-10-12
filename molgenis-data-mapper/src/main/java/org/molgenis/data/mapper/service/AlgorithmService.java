package org.molgenis.data.mapper.service;

import org.molgenis.data.Entity;
import org.molgenis.data.mapper.mapping.model.AttributeMapping;
import org.molgenis.data.mapper.mapping.model.EntityMapping;
import org.molgenis.data.mapper.service.impl.AlgorithmEvaluation;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityMetaData;

import java.util.Collection;
import java.util.List;

public interface AlgorithmService
{
	/**
	 * Applies an algorithm to the given attribute of given source entities.
	 *
	 * @param targetAttribute
	 * @param algorithm
	 * @param sourceEntities
	 * @return algorithm evaluation for each source entity
	 */
	Iterable<AlgorithmEvaluation> applyAlgorithm(Attribute targetAttribute, String algorithm,
			Iterable<Entity> sourceEntities);

	/**
	 * Applies an {@link AttributeMapping} to a source {@link Entity}
	 *
	 * @param attributeMapping {@link AttributeMapping} to apply
	 * @param sourceEntity     {@link Entity} to apply the mapping to
	 * @return Object containing the mapped value
	 */
	Object apply(AttributeMapping attributeMapping, Entity sourceEntity, EntityMetaData sourceEntityMetaData);

	/**
	 * Retrieves the names of the source attributes in an algorithm
	 *
	 * @param algorithmScript String with the algorithm script
	 * @return Collection of source attribute name Strings
	 */
	Collection<String> getSourceAttributeNames(String algorithmScript);

	/**
	 * Creates an attribute mapping after the semantic search service finds one
	 *
	 * @param sourceEntityMetaData
	 * @param targetEntityMetaData
	 * @param mapping
	 * @param targetAttribute
	 */
	void autoGenerateAlgorithm(EntityMetaData sourceEntityMetaData, EntityMetaData targetEntityMetaData,
			EntityMapping mapping, Attribute targetAttribute);

	/**
	 * Generates the algorithm based on the given targetAttribute and sourceAttribute
	 *
	 * @param targetAttribute
	 * @param targetEntityMetaData
	 * @param sourceAttributes
	 * @param sourceEntityMetaData
	 * @return the generate algorithm
	 */
	String generateAlgorithm(Attribute targetAttribute, EntityMetaData targetEntityMetaData,
			List<Attribute> sourceAttributes, EntityMetaData sourceEntityMetaData);
}
