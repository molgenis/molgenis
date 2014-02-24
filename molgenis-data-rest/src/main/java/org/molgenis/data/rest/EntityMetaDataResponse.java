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
	private final String href;
	private final String name;
	private final String label;
	private final String description;
	private final Map<String, Object> attributes;
	private final String labelAttribute;

	public EntityMetaDataResponse(EntityMetaData meta, Set<String> attributesSet, Set<String> attributeExpandsSet)
	{
		this.href = String.format("%s/%s/meta", RestController.BASE_URI, meta.getName());

		if (attributesSet == null || attributesSet.contains("name".toLowerCase()))
		{
			this.name = meta.getName();
		}
		else this.name = null;

		if (attributesSet == null || attributesSet.contains("description".toLowerCase()))
		{
			this.description = meta.getDescription();
		}
		else this.description = null;

		if (attributesSet == null || attributesSet.contains("label".toLowerCase()))
		{
			label = meta.getLabel();
		}
		else this.label = null;

		if (attributesSet == null || attributesSet.contains("attributes".toLowerCase()))
		{
			this.attributes = new LinkedHashMap<String, Object>();

			for (AttributeMetaData attr : meta.getAttributes())
			{
				if (attr.isVisible() && !attr.getName().equals("__Type"))
				{
					if (attributeExpandsSet != null && attributeExpandsSet.contains("attributes"))
					{
						this.attributes.put(attr.getName(), new AttributeMetaDataResponse(name, attr));
					}
					else
					{
						String attrHref = String.format("%s/%s/meta/%s", RestController.BASE_URI, name, attr.getName());
						this.attributes.put(attr.getName(), Collections.singletonMap("href", attrHref));
					}
				}
			}
		}
		else this.attributes = null;

		if (attributesSet == null || attributesSet.contains("labelAttribute".toLowerCase()))
		{
			AttributeMetaData labelAttribute = meta.getLabelAttribute();
			this.labelAttribute = labelAttribute != null ? labelAttribute.getName() : null;
		}
		else this.labelAttribute = null;
	}

	public String getHref()
	{
		return href;
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

	public String getLabelAttribute()
	{
		return labelAttribute;
	}
}
