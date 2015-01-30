package org.molgenis.data.algorithm;

import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.mapping.model.AttributeMapping;
import org.molgenis.data.mapping.model.EntityMapping;

public interface AlgorithmService
{
	public List<Object> applyAlgorithm(AttributeMetaData targetAttribute, Iterable<AttributeMetaData> sourceAttributes,
			String algorithm, Repository sourceRepo);

	public List<Object> applyAlgorithm(AttributeMapping attributeMapping, Repository sourceRepo);

	public List<Entity> applyAlgorithms(EntityMapping entityMappings);
}
