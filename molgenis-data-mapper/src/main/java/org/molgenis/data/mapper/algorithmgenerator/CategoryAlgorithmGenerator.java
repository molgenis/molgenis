package org.molgenis.data.mapper.algorithmgenerator;

import java.util.List;

import org.molgenis.data.AttributeMetaData;

public interface CategoryAlgorithmGenerator
{
	abstract boolean isSuitable(AttributeMetaData targetAttribute, AttributeMetaData sourceAttribute);

	abstract String generate(AttributeMetaData targetAttribute, AttributeMetaData sourceAttribute);

	abstract void combineCategories(List<AttributeMetaData> sourceAttributes);
}
