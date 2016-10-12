package org.molgenis.data.mapper.algorithmgenerator.generator;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityMetaData;

import java.util.List;

public interface AlgorithmGenerator
{
	String generate(Attribute targetAttribute, List<Attribute> sourceAttributes,
			EntityMetaData targetEntityMetaData, EntityMetaData sourceEntityMetaData);

	boolean isSuitable(Attribute targetAttribute, List<Attribute> sourceAttributes);
}
