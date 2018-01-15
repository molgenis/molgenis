package org.molgenis.semanticmapper.algorithmgenerator.service;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedAttribute;
import org.molgenis.semanticmapper.algorithmgenerator.bean.GeneratedAlgorithm;

import java.util.List;
import java.util.Map;

public interface AlgorithmGeneratorService
{
	String generate(Attribute targetAttribute, List<Attribute> sourceAttributes, EntityType targetEntityType,
			EntityType sourceEntityType);

	GeneratedAlgorithm generate(Attribute targetAttribute, Map<Attribute, ExplainedAttribute> sourceAttributes,
			EntityType targetEntityType, EntityType sourceEntityType);
}
