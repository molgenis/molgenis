package org.molgenis.data.rest;

import java.util.LinkedHashMap;
import java.util.Map;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;

import com.google.common.collect.ImmutableMap;

public class EntityMetaDataResponse
{
	private final String name;
	private final String label;
	private final String description;
	private final Map<String, AttributeMetaDataResponse> attributes = new LinkedHashMap<String, AttributeMetaDataResponse>();

	public String getName()
	{
		return name;
	}

	public String getLabel()
	{
		return label;
	}

	public String getDescription()
	{
		return description;
	}

	public Map<String, AttributeMetaDataResponse> getAttributes()
	{
		return ImmutableMap.copyOf(attributes);
	}

	public EntityMetaDataResponse(EntityMetaData meta, Iterable<AttributeMetaData> metaDataAttributes)
	{
		name = meta.getName();
		description = meta.getDescription();
		label = meta.getLabel();

		for (AttributeMetaData attr : metaDataAttributes)
		{
			if (attr.isVisible() && !attr.getName().equals("__Type"))
			{
				attributes.put(attr.getName(), new AttributeMetaDataResponse(attr));
			}
		}
	}

}
