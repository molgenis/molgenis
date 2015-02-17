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
	private final String idAttribute;
	private final Boolean isAbstract;

	/**
	 * 
	 * @param meta
	 * @param attributesSet
	 *            set of lowercase attribute names to include in response
	 * @param attributeExpandsSet
	 *            set of lowercase attribute names to expand in response
	 */
	public EntityMetaDataResponse(EntityMetaData meta, Set<String> attributesSet,
			Map<String, Set<String>> attributeExpandsSet)
	{
		String name = meta.getName();
		this.href = String.format("%s/%s/meta", RestController.BASE_URI, name);

		if (attributesSet == null || attributesSet.contains("name".toLowerCase()))
		{
			this.name = name;
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
					if (attributeExpandsSet != null && attributeExpandsSet.containsKey("attributes".toLowerCase()))
					{
						Set<String> subAttributesSet = attributeExpandsSet.get("attributes".toLowerCase());
						this.attributes.put(attr.getName(), new AttributeMetaDataResponse(name, attr, subAttributesSet,
								null));
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

		if (attributesSet == null || attributesSet.contains("idAttribute".toLowerCase()))
		{
			AttributeMetaData idAttribute = meta.getIdAttribute();
			this.idAttribute = idAttribute != null ? idAttribute.getName() : null;
		}
		else this.idAttribute = null;

		if (attributesSet == null || attributesSet.contains("abstract".toLowerCase()))
		{
			isAbstract = meta.isAbstract();
		}
		else this.isAbstract = null;
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

	public String getIdAttribute()
	{
		return idAttribute;
	}

	public Map<String, Object> getAttributes()
	{
		return ImmutableMap.copyOf(attributes);
	}

	public String getLabelAttribute()
	{
		return labelAttribute;
	}

	public boolean isAbstract()
	{
		return isAbstract;
	}
}
