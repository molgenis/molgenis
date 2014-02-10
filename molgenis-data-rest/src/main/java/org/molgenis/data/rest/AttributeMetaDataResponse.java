package org.molgenis.data.rest;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;

public class AttributeMetaDataResponse
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
