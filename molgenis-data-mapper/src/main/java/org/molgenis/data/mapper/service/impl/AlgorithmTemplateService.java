package org.molgenis.data.mapper.service.impl;

import java.util.stream.Stream;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;

public interface AlgorithmTemplateService
{
	Stream<AlgorithmTemplate> find(AttributeMetaData targetAttr, EntityMetaData targetEntityMeta,
			EntityMetaData sourceEntityMeta);
}
