package org.molgenis.data.mapper.service.impl;

import java.util.stream.Stream;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;

/**
 * Find suitable algorithm templates for provided meta data used to map source data to target data.
 */
public interface AlgorithmTemplateService
{
	/**
	 * 
	 * @param targetAttr
	 *            target attribute
	 * @param targetEntityMeta
	 *            target entity meta data
	 * @param sourceEntityMeta
	 *            source entity meta data
	 * @return algorithm templates that can be rendered using the given source and target
	 */
	Stream<AlgorithmTemplate> find(AttributeMetaData targetAttr, EntityMetaData targetEntityMeta,
			EntityMetaData sourceEntityMeta);
}
