package org.molgenis.data.mapper;

import java.util.List;

import org.elasticsearch.common.collect.Lists;
import org.molgenis.data.AttributeMetaData;

public class MappingServiceResponse
{
	private final String targetEntityIdentifier;
	private final String sourceEntityIdentifier;
	private final AttributeMetaData targetAttribute;
	private final List<AttributeMetaData> sourceAttributes;

	public MappingServiceResponse(String targetEntityIdentifier, String sourceEntityIdentifier,
			AttributeMetaData targetAttribute, Iterable<AttributeMetaData> sourceAttributes)
	{
		this.targetEntityIdentifier = targetEntityIdentifier;
		this.sourceEntityIdentifier = sourceEntityIdentifier;
		this.targetAttribute = targetAttribute;
		this.sourceAttributes = Lists.<AttributeMetaData> newArrayList(sourceAttributes);
	}

	public String getTargetEntityIdentifier()
	{
		return targetEntityIdentifier;
	}

	public String getSourceEntityIdentifier()
	{
		return sourceEntityIdentifier;
	}

	public AttributeMetaData getTargetAttribute()
	{
		return targetAttribute;
	}

	public List<AttributeMetaData> getSourceAttributes()
	{
		return sourceAttributes;
	}
}
