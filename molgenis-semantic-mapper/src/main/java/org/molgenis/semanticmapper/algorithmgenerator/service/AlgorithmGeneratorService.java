package org.molgenis.semanticmapper.algorithmgenerator.service;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.semanticmapper.algorithmgenerator.bean.GeneratedAlgorithm;
import org.molgenis.semanticsearch.explain.bean.ExplainedAttribute;
import org.molgenis.semanticsearch.semantic.Hits;

import java.util.List;

public interface AlgorithmGeneratorService
{
	String generate(Attribute targetAttribute, List<Attribute> sourceAttributes, EntityType targetEntityType,
			EntityType sourceEntityType);

	GeneratedAlgorithm generate(Attribute targetAttribute, Hits<ExplainedAttribute> sourceAttributes,
			EntityType targetEntityType, EntityType sourceEntityType);
}
