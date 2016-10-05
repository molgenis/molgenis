package org.molgenis.data.mapper.algorithmgenerator.service;

import org.molgenis.data.mapper.algorithmgenerator.bean.GeneratedAlgorithm;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedAttributeMetaData;

import java.util.List;
import java.util.Map;

public interface AlgorithmGeneratorService
{
	String generate(AttributeMetaData targetAttribute, List<AttributeMetaData> sourceAttributes,
			EntityType targetEntityType, EntityType sourceEntityType);

	GeneratedAlgorithm generate(AttributeMetaData targetAttribute,
			Map<AttributeMetaData, ExplainedAttributeMetaData> sourceAttributes, EntityType targetEntityType,
			EntityType sourceEntityType);
}
