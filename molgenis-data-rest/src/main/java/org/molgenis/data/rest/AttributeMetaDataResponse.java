package org.molgenis.data.rest;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class AttributeMetaDataResponse
{
	private final String href;
	private final FieldTypeEnum fieldType;
	private final String name;
	private final String label;
	private final String description;
	private final List<?> attributes;
	private final Href refEntity;
	private final boolean nillable;
	private final boolean readOnly;
	private final Object defaultValue;
	private final boolean idAttribute;
	private final boolean labelAttribute;
	private final boolean unique;

	public AttributeMetaDataResponse(String entityParentName, AttributeMetaData attr)
	{
		this(entityParentName, attr, null);
	}

	public AttributeMetaDataResponse(final String entityParentName, AttributeMetaData attr,
			final Set<String> expandFieldSet)
	{
		String attrName = attr.getName();

		this.href = String.format("%s/%s/meta/%s", RestController.BASE_URI, entityParentName, attrName);
		this.fieldType = attr.getDataType().getEnumType();
		this.name = attrName;
		this.label = attr.getLabel();
		this.description = attr.getDescription();

		EntityMetaData refEntity = attr.getRefEntity();
		this.refEntity = refEntity != null ? new Href(String.format("%s/%s/meta", RestController.BASE_URI,
				entityParentName)) : null;

		Iterable<AttributeMetaData> attributeParts = attr.getAttributeParts();
		this.attributes = attributeParts != null ? Lists.newArrayList(Iterables.transform(attributeParts,
				new Function<AttributeMetaData, Object>()
				{

					@Override
					public Object apply(AttributeMetaData attributeMetaData)
					{
						if (expandFieldSet != null && expandFieldSet.contains("attributes"))
						{
							return new AttributeMetaDataResponse(entityParentName, attributeMetaData, null);
						}
						else
						{
							return Collections.<String, Object> singletonMap("href", String.format("%s/%s/meta/%s",
									RestController.BASE_URI, entityParentName, attributeMetaData.getName()));
						}
					}
				})) : null;

		this.nillable = attr.isNillable();
		this.readOnly = attr.isReadonly();
		this.defaultValue = attr.getDefaultValue();
		this.idAttribute = attr.isIdAtrribute();
		this.labelAttribute = attr.isLabelAttribute();
		this.unique = attr.isUnique();
	}

	public String getHref()
	{
		return href;
	}

	public FieldTypeEnum getFieldType()
	{
		return fieldType;
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

	public List<?> getAttributes()
	{
		return attributes;
	}

	public Href getRefEntity()
	{
		return refEntity;
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

	public boolean isUnique()
	{
		return unique;
	}
}
