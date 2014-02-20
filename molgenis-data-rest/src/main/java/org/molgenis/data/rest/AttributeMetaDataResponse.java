package org.molgenis.data.rest;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class AttributeMetaDataResponse
{
	private final FieldTypeEnum fieldType;
	private String description = null;
	private boolean nillable = true;
	private boolean readOnly = false;
	private Object defaultValue = null;
	private boolean idAttribute = false;
	private boolean labelAttribute = false;
	private final Href refEntity = null;
	private String refThis = null;
	private String label = null;
	private boolean unique = false;
	private String name = null;
	private final List<?> attributes;

	public AttributeMetaDataResponse(String entityParentName, AttributeMetaData attr)
	{
		this(entityParentName, attr, null);
	}

	public AttributeMetaDataResponse(final String entityParentName, AttributeMetaData attr,
			final Set<String> expandFieldSet)
	{
		fieldType = attr.getDataType().getEnumType();
		description = attr.getDescription();
		nillable = attr.isNillable();
		readOnly = attr.isReadonly();
		defaultValue = attr.getDefaultValue();
		idAttribute = attr.isIdAtrribute();
		labelAttribute = attr.isLabelAttribute();
		name = attr.getName();
		setRefThis(String.format("%s/%s/meta/%s", RestController.BASE_URI, entityParentName, attr.getName()));

		label = attr.getLabel();
		unique = attr.isUnique();
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

	public String getRefThis()
	{
		return refThis;
	}

	public void setRefThis(String refThis)
	{
		this.refThis = refThis;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public List<?> getAttributes()
	{
		return attributes;
	}
}
