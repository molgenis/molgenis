package org.molgenis.data.rest;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Range;
import org.molgenis.data.i18n.LanguageService;
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

	public AttributeMetaDataResponse(String entityParentName, AttributeMetaData attr,
			MolgenisPermissionService permissionService, DataService dataService, LanguageService languageService)
	{
		this(entityParentName, attr, null, null, permissionService, dataService, languageService);
	}

	/**
	 * 
	 * @param entityParentName
	 * @param attr
	 * @param attributesSet
	 *            set of lowercase attribute names to include in response
	 * @param attributeExpandsSet
	 *            set of lowercase attribute names to expand in response
	 */
	public AttributeMetaDataResponse(final String entityParentName, AttributeMetaData attr, Set<String> attributesSet,
			final Map<String, Set<String>> attributeExpandsSet, MolgenisPermissionService permissionService,
			DataService dataService, LanguageService languageService)
	{
		String attrName = attr.getName();
		this.href = Href.concatMetaAttributeHref(RestController.BASE_URI, entityParentName, attrName);

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
			this.label = attr.getLabel(languageService.getCurrentUserLanguageCode());
		}
		else this.label = null;

		if (attributesSet == null || attributesSet.contains("description".toLowerCase()))
		{
			this.description = attr.getDescription(languageService.getCurrentUserLanguageCode());
		}
		else this.description = null;

		if (attributesSet == null || attributesSet.contains("enumOptions".toLowerCase()))
		{
			this.enumOptions = attr.getEnumOptions();
		}
		else this.enumOptions = null;

		if (attributesSet == null || attributesSet.contains("maxLength".toLowerCase()))
		{
			this.maxLength = attr.getDataType().getMaxLength();
		}
		else this.maxLength = null;

		if (attributesSet == null || attributesSet.contains("expression".toLowerCase()))
		{
			this.expression = attr.getExpression();
		}
		else this.expression = null;

		if (attributesSet == null || attributesSet.contains("refEntity".toLowerCase()))
		{
			EntityMetaData refEntity = attr.getRefEntity();
			if (attributeExpandsSet != null && attributeExpandsSet.containsKey("refEntity".toLowerCase()))
			{
				Set<String> subAttributesSet = attributeExpandsSet.get("refEntity".toLowerCase());
				this.refEntity = refEntity != null ? new EntityMetaDataResponse(refEntity, subAttributesSet,
						Collections.singletonMap("attributes".toLowerCase(), null), permissionService, dataService,
						languageService) : null;
			}
			else
			{
				this.refEntity = refEntity != null ? new Href(Href.concatMetaEntityHref(RestController.BASE_URI,
						refEntity.getName()), String.format("%s/%s", RestController.BASE_URI, refEntity.getName())) : null; // FIXME
																															// apply
																															// Href
																															// escaping
																															// fix
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
							if (attributeExpandsSet != null
									&& attributeExpandsSet.containsKey("attributes".toLowerCase()))
							{
								Set<String> subAttributesSet = attributeExpandsSet.get("attributes".toLowerCase());
								return new AttributeMetaDataResponse(entityParentName, attributeMetaData,
										subAttributesSet, Collections.singletonMap("refEntity".toLowerCase(), null),
										permissionService, dataService, languageService);
							}
							else
							{
								return Collections.<String, Object> singletonMap("href", Href.concatMetaAttributeHref(
										RestController.BASE_URI, entityParentName, attributeMetaData.getName()));
							}
						}
					})) : null;
		}
		else this.attributes = null;

		if (attributesSet == null || attributesSet.contains("auto".toLowerCase()))
		{
			this.auto = attr.isAuto();
		}
		else this.auto = null;

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

		if (attributesSet == null || attributesSet.contains("lookupAttribute".toLowerCase()))
		{
			this.lookupAttribute = attr.isLookupAttribute();
		}
		else this.lookupAttribute = null;

		if (attributesSet == null || attributesSet.contains("aggregateable".toLowerCase()))
		{
			this.aggregateable = attr.isAggregateable();
		}
		else this.aggregateable = null;

		if (attributesSet == null || attributesSet.contains("range".toLowerCase()))
		{
			this.range = attr.getRange();
		}
		else this.range = null;

		if (attributesSet == null || attributesSet.contains("isVisible".toLowerCase()))
		{
			this.visible = attr.isVisible();
		}
		else this.visible = null;

		if (attributesSet == null || attributesSet.contains("visibleExpression".toLowerCase()))
		{
			this.visibleExpression = attr.getVisibleExpression();
		}
		else this.visibleExpression = null;

		if (attributesSet == null || attributesSet.contains("validationExpression".toLowerCase()))
		{
			this.validationExpression = attr.getValidationExpression();
		}
		else this.validationExpression = null;
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

	public Long getMaxLength()
	{
		return maxLength;
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
