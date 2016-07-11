package org.molgenis.data.mapper.algorithmgenerator.service;

import java.util.List;
import java.util.Map;

import org.molgenis.data.mapper.algorithmgenerator.bean.GeneratedAlgorithm;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedAttributeMetaData;

public interface AlgorithmGeneratorService
{
	abstract String generate(AttributeMetaData targetAttribute, List<AttributeMetaData> sourceAttributes,
			EntityMetaData targetEntityMetaData, EntityMetaData sourceEntityMetaData);

	abstract GeneratedAlgorithm generate(AttributeMetaData targetAttribute,
			Map<AttributeMetaData, ExplainedAttributeMetaData> sourceAttributes, EntityMetaData targetEntityMetaData,
			EntityMetaData sourceEntityMetaData);
}
