package org.molgenis.data.mapper.algorithmgenerator;

import org.molgenis.data.AttributeMetaData;

public interface AlgorithmGenerator
{
	abstract boolean isSuitable(AttributeMetaData targetAttributeMetaData, AttributeMetaData sourceAttributeMetaData);

	abstract String generate(AttributeMetaData targetAttributeMetaData, AttributeMetaData sourceAttributeMetaData);
}
