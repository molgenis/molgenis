package org.molgenis.data.mapper.algorithmgenerator.service;

import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;

public interface AlgorithmGeneratorService
{
	public abstract String generate(AttributeMetaData targetAttribute, List<AttributeMetaData> sourceAttributes,
			EntityMetaData targetEntityMetaData, EntityMetaData sourceEntityMetaData);
}
