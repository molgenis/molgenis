package org.molgenis.data.mapper.algorithmgenerator.generator;

import java.util.List;

import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.EntityMetaData;

public interface AlgorithmGenerator
{
	String generate(AttributeMetaData targetAttribute, List<AttributeMetaData> sourceAttributes,
			EntityMetaData targetEntityMetaData, EntityMetaData sourceEntityMetaData);

	boolean isSuitable(AttributeMetaData targetAttribute, List<AttributeMetaData> sourceAttributes);
}
