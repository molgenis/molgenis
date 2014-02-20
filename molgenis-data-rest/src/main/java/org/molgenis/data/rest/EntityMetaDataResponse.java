package org.molgenis.data.rest;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;

import com.google.common.collect.ImmutableMap;

public class EntityMetaDataResponse
{
	private final String name;
	private final String label;
	private final String description;
	private final Map<String, Object> attributes;

	public EntityMetaDataResponse(EntityMetaData meta, Set<String> expandFieldSet)
	{
		name = meta.getName();
		description = meta.getDescription();
		label = meta.getLabel();
		attributes = new LinkedHashMap<String, Object>();

		for (AttributeMetaData attr : meta.getAttributes())
		{
			if (attr.isVisible() && !attr.getName().equals("__Type"))
			{
				if (expandFieldSet != null && expandFieldSet.contains("attributes"))
				{
					attributes.put(attr.getName(), new AttributeMetaDataResponse(name, attr));
				}
				else
				{
					String attrHref = String.format("%s/%s/meta/%s", RestController.BASE_URI, name, attr.getName());
					attributes.put(attr.getName(), Collections.singletonMap("href", attrHref));
				}
			}
		}
	}

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

	public Map<String, Object> getAttributes()
	{
		return ImmutableMap.copyOf(attributes);
	}
}
