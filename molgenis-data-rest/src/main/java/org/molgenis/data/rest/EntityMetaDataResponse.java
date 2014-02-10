package org.molgenis.data.rest;

import java.util.LinkedHashMap;
import java.util.Map;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
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

	public EntityMetaDataResponse(EntityMetaData meta)
	{
		name = meta.getName();
		description = meta.getDescription();
		label = meta.getLabel();

		for (AttributeMetaData attr : meta.getAttributes())
		{
			if (attr.isVisible() && !attr.getName().equals("__Type"))
			{
				attributes.put(attr.getName(), new AttributeMetaDataResponse(attr));
			}
		}
	}

	public static class AttributeMetaDataResponse
	{
		private final FieldTypeEnum fieldType;
		private String description = null;
		private boolean nillable = true;
		private boolean readOnly = false;
		private Object defaultValue = null;
		private boolean idAttribute = false;
		private boolean labelAttribute = false;
		private Href refEntity = null;
		private String label = null;
		private boolean unique = false;

		public AttributeMetaDataResponse(AttributeMetaData attr)
		{
			fieldType = attr.getDataType().getEnumType();
			description = attr.getDescription();
			nillable = attr.isNillable();
			readOnly = attr.isReadonly();
			defaultValue = attr.getDefaultValue();
			idAttribute = attr.isIdAtrribute();
			labelAttribute = attr.isLabelAttribute();

			if (attr.getRefEntity() != null)
			{
				String href = String.format("%s/%s/meta", RestController.BASE_URI, attr.getRefEntity().getName());
				refEntity = new Href(href);
			}

			label = attr.getLabel();
			unique = attr.isUnique();
		}

		public FieldTypeEnum getFieldType()
		{
			return fieldType;
		}

		public String getDescription()
		{
			return description;
		}

		public boolean isNillable()
		{
			return nillable;
		}

		public boolean isReadOnly()
		{
			return readOnly;
		}

		public Object getDefaultValue()
		{
			return defaultValue;
		}

		public boolean isIdAttribute()
		{
			return idAttribute;
		}

		public boolean isLabelAttribute()
		{
			return labelAttribute;
		}

		public Href getRefEntity()
		{
			return refEntity;
		}

		public String getLabel()
		{
			return label;
		}

		public boolean isUnique()
		{
			return unique;
		}

	}

	public static class Href
	{
		private final String href;

		public Href(String href)
		{
			super();
			this.href = href;
		}

		public String getHref()
		{
			return href;
		}

	}
}
