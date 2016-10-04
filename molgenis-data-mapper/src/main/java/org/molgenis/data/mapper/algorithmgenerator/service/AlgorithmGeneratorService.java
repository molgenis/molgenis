package org.molgenis.data.mapper.algorithmgenerator.service;

import org.molgenis.data.mapper.algorithmgenerator.bean.GeneratedAlgorithm;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedAttribute;

import java.util.List;
import java.util.Map;

public interface AlgorithmGeneratorService
{
	abstract String generate(Attribute targetAttribute, List<Attribute> sourceAttributes,
			EntityMetaData targetEntityMetaData, EntityMetaData sourceEntityMetaData);

	abstract GeneratedAlgorithm generate(Attribute targetAttribute,
			Map<Attribute, ExplainedAttribute> sourceAttributes, EntityMetaData targetEntityMetaData,
			EntityMetaData sourceEntityMetaData);
}
