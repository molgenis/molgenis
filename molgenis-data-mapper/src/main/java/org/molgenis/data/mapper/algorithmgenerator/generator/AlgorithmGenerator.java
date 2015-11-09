package org.molgenis.data.mapper.algorithmgenerator.generator;

import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;

public interface AlgorithmGenerator
{
	public abstract String generate(AttributeMetaData targetAttribute, List<AttributeMetaData> sourceAttributes,
			EntityMetaData targetEntityMetaData, EntityMetaData sourceEntityMetaData);

	public abstract boolean isSuitable(AttributeMetaData targetAttribute, List<AttributeMetaData> sourceAttributes);
}
