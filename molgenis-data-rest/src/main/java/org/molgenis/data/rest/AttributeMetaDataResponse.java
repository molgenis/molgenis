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
	private final Object refEntity;
	private final Boolean nillable;
	private final Boolean readOnly;
	private final Object defaultValue;
	private final Boolean labelAttribute;
	private final Boolean unique;

	public AttributeMetaDataResponse(String entityParentName, AttributeMetaData attr)
	{
		this(entityParentName, attr, null, null);
	}

	public AttributeMetaDataResponse(final String entityParentName, AttributeMetaData attr, Set<String> attributesSet,
			final Set<String> attributeExpandsSet)
	{
		String attrName = attr.getName();

		this.href = String.format("%s/%s/meta/%s", RestController.BASE_URI, entityParentName, attrName);

		if (attributesSet == null || attributesSet.contains("fieldType".toLowerCase()))
		{
			this.fieldType = attr.getDataType().getEnumType();
		}
		else this.fieldType = null;

		if (attributesSet == null || attributesSet.contains("name".toLowerCase()))
		{
			this.name = attrName;
		}
		else this.name = null;

		if (attributesSet == null || attributesSet.contains("label".toLowerCase()))
		{
			this.label = attr.getLabel();
		}
		else this.label = null;

		if (attributesSet == null || attributesSet.contains("description".toLowerCase()))
		{
			this.description = attr.getDescription();
		}
		else this.description = null;

		if (attributesSet == null || attributesSet.contains("refEntity".toLowerCase()))
		{
			EntityMetaData refEntity = attr.getRefEntity();
			if (attributeExpandsSet != null && attributeExpandsSet.contains("refEntity"))
			{
				this.refEntity = refEntity != null ? new EntityMetaDataResponse(refEntity, null, null) : null;
			}
			else
			{
				this.refEntity = refEntity != null ? new Href(String.format("%s/%s/meta", RestController.BASE_URI,
						refEntity.getName())) : null;
			}
		}
		else this.refEntity = null;

		if (attributesSet == null || attributesSet.contains("attributes".toLowerCase()))
		{
			Iterable<AttributeMetaData> attributeParts = attr.getAttributeParts();
			this.attributes = attributeParts != null ? Lists.newArrayList(Iterables.transform(attributeParts,
					new Function<AttributeMetaData, Object>()
					{

						@Override
						public Object apply(AttributeMetaData attributeMetaData)
						{
							if (attributeExpandsSet != null && attributeExpandsSet.contains("attributes"))
							{
								return new AttributeMetaDataResponse(entityParentName, attributeMetaData, null, null);
							}
							else
							{
								return Collections.<String, Object> singletonMap("href", String.format("%s/%s/meta/%s",
										RestController.BASE_URI, entityParentName, attributeMetaData.getName()));
							}
						}
					})) : null;
		}
		else this.attributes = null;

		if (attributesSet == null || attributesSet.contains("nillable".toLowerCase()))
		{
			this.nillable = attr.isNillable();
		}
		else this.nillable = null;

		if (attributesSet == null || attributesSet.contains("readOnly".toLowerCase()))
		{
			this.readOnly = attr.isReadonly();
		}
		else this.readOnly = null;

		if (attributesSet == null || attributesSet.contains("defaultValue".toLowerCase()))
		{
			this.defaultValue = attr.getDefaultValue();
		}
		else this.defaultValue = null;

		if (attributesSet == null || attributesSet.contains("labelAttribute".toLowerCase()))
		{
			this.labelAttribute = attr.isLabelAttribute();
		}
		else this.labelAttribute = null;

		if (attributesSet == null || attributesSet.contains("unique".toLowerCase()))
		{
			this.unique = attr.isUnique();
		}
		else this.unique = null;
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

	public Object getRefEntity()
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

	public boolean isLabelAttribute()
	{
		return labelAttribute;
	}

	public boolean isUnique()
	{
		return unique;
	}
}
