package org.molgenis.semanticmapper.algorithmgenerator.generator;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

import java.util.List;

public interface AlgorithmGenerator
{
	String generate(Attribute targetAttribute, List<Attribute> sourceAttributes, EntityType targetEntityType,
			EntityType sourceEntityType);

	boolean isSuitable(Attribute targetAttribute, List<Attribute> sourceAttributes);
}
