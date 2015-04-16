package org.molgenis.data.mapper.algorithm;

import java.util.Collection;
import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.mapper.mapping.model.AttributeMapping;

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
}
