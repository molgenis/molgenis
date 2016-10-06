package org.molgenis.data.mapper.algorithmgenerator.service;

import org.molgenis.data.mapper.algorithmgenerator.bean.GeneratedAlgorithm;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedMatchCandidate;

import java.util.List;
import java.util.Map;

public interface AlgorithmGeneratorService
{
	String generate(AttributeMetaData targetAttribute, List<AttributeMetaData> sourceAttributes,
			EntityMetaData targetEntityMetaData, EntityMetaData sourceEntityMetaData);

	GeneratedAlgorithm generate(AttributeMetaData targetAttribute,
			Map<AttributeMetaData, ExplainedMatchCandidate<AttributeMetaData>> sourceAttributes,
			EntityMetaData targetEntityMetaData, EntityMetaData sourceEntityMetaData);
}
