package org.molgenis.data.mapper.algorithmgenerator.service;

import java.util.List;

import org.molgenis.data.AttributeMetaData;

public interface MapCategoryService
{
	public abstract String generate(AttributeMetaData targetAttribute, List<AttributeMetaData> sourceAttributes);

	public abstract boolean isValid(AttributeMetaData targetAttribute, List<AttributeMetaData> sourceAttributes);
}
