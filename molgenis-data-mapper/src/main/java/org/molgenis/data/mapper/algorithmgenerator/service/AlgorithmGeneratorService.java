package org.molgenis.data.mapper.algorithmgenerator.service;

import org.molgenis.data.mapper.algorithmgenerator.bean.GeneratedAlgorithm;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedAttributeMetaData;

import java.util.List;
import java.util.Map;

public interface AlgorithmGeneratorService
{
	String generate(Attribute targetAttribute, List<Attribute> sourceAttributes,
			EntityMetaData targetEntityMetaData, EntityMetaData sourceEntityMetaData);

	GeneratedAlgorithm generate(Attribute targetAttribute,
			Map<Attribute, ExplainedAttributeMetaData> sourceAttributes, EntityMetaData targetEntityMetaData,
			EntityMetaData sourceEntityMetaData);
}
