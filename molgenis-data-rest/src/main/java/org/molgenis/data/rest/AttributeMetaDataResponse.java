package org.molgenis.data.rest;

import java.util.List;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Range;
import org.molgenis.security.core.MolgenisPermissionService;

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
	private final List<String> enumOptions;
	private final Long maxLength;
	private final Object refEntity;
	private final Boolean auto;
	private final Boolean nillable;
	private final Boolean readOnly;
	private final Object defaultValue;
	private final Boolean labelAttribute;
	private final Boolean unique;
	private final Boolean visible;
	private Boolean lookupAttribute;
	private Boolean aggregateable;
	private Range range;
	private String expression;
	private String visibleExpression;
	private String validationExpression;

	/**
	 * 
	 * @param entityParentName
	 * @param attr
	 * @param attributes
	 *            set of lowercase attribute names to include in response
	 * @param attributeExpandsSet
	 *            set of lowercase attribute names to expand in response
	 */
	public AttributeMetaDataResponse(final String entityParentName, AttributeMetaData attr, Attributes attributes,
			MolgenisPermissionService permissionService)
	{
		String attrName = attr.getName();
		this.href = Href.concatMetaAttributeHref(RestController.BASE_URI, entityParentName, attrName);

		this.fieldType = attr.getDataType().getEnumType();
		this.name = attrName;
		this.label = attr.getLabel();
		this.description = attr.getDescription();
		this.enumOptions = attr.getEnumOptions();
		this.maxLength = attr.getDataType().getMaxLength();
		this.expression = attr.getExpression();

		EntityMetaData refEntity = attr.getRefEntity();
		if (refEntity != null)
		{
			Attributes subAttributes = attributes != null ? attributes.getAttribute(attrName).getAttributes() : null;
			this.refEntity = new EntityMetaDataResponse(refEntity, subAttributes, permissionService);
		}
		else
		{
			this.refEntity = null;
		}

		// }
		// else
		// {
		// this.refEntity = refEntity != null ? new Href(Href.concatMetaEntityHref(RestController.BASE_URI,
		// refEntity.getName()), String.format("%s/%s", RestController.BASE_URI, refEntity.getName())) : null; //
		// FIXME
		// // apply
		// // Href
		// // escaping
		// // fix
		// }

		Iterable<AttributeMetaData> attributeParts = attr.getAttributeParts();
		this.attributes = attributeParts != null ? Lists.newArrayList(Iterables.transform(attributeParts,
				new Function<AttributeMetaData, Object>()
				{

					@Override
					public Object apply(AttributeMetaData attributeMetaData)
					{
						String attrName = attributeMetaData.getName();
						Attributes subAttributes = attributes != null ? attributes.getAttribute(attrName)
								.getAttributes() : null;
						// if (attributeExpandsSet != null
						// && attributeExpandsSet.containsKey("attributes"))
						// {
						return new AttributeMetaDataResponse(entityParentName, attributeMetaData, subAttributes,
								permissionService);
						// }
						// else
						// {
						// return Collections.<String, Object> singletonMap("href", Href.concatMetaAttributeHref(
						// RestController.BASE_URI, entityParentName, attributeMetaData.getName()));
						// }
					}
				})) : null;

		this.auto = attr.isAuto();
		this.nillable = attr.isNillable();
		this.readOnly = attr.isReadonly();
		this.defaultValue = attr.getDefaultValue();
		this.labelAttribute = attr.isLabelAttribute();
		this.unique = attr.isUnique();
		this.lookupAttribute = attr.isLookupAttribute();
		this.aggregateable = attr.isAggregateable();
		this.range = attr.getRange();
		this.visible = attr.isVisible();
		this.visibleExpression = attr.getVisibleExpression();
		this.validationExpression = attr.getValidationExpression();
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

	public List<String> getEnumOptions()
	{
		return enumOptions;
	}

	public Object getRefEntity()
	{
		return refEntity;
	}

	public boolean isAuto()
	{
		return auto;
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

	public boolean isVisible()
	{
		return visible;
	}

	public Boolean getLookupAttribute()
	{
		return lookupAttribute;
	}

	public Boolean isAggregateable()
	{
		return aggregateable;
	}

	public Boolean getNillable()
	{
		return nillable;
	}

	public Boolean getReadOnly()
	{
		return readOnly;
	}

	public Boolean getLabelAttribute()
	{
		return labelAttribute;
	}

	public Boolean getUnique()
	{
		return unique;
	}

	public Boolean getAggregateable()
	{
		return aggregateable;
	}

	public Range getRange()
	{
		return range;
	}

	public String getExpression()
	{
		return expression;
	}

	public String getVisibleExpression()
	{
		return visibleExpression;
	}

	public String getValidationExpression()
	{
		return validationExpression;
	}

}
