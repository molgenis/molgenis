package org.molgenis.data.mapper.algorithmgenerator.generator;

import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityType;

import java.util.List;

public interface AlgorithmGenerator
{
	String generate(AttributeMetaData targetAttribute, List<AttributeMetaData> sourceAttributes,
			EntityType targetEntityType, EntityType sourceEntityType);

	boolean isSuitable(AttributeMetaData targetAttribute, List<AttributeMetaData> sourceAttributes);
}
