package org.molgenis.data.rest.v2;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.molgenis.MolgenisFieldTypes.AttributeType;
import org.molgenis.data.DataService;
import org.molgenis.data.Fetch;
import org.molgenis.data.Range;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.rest.Href;
import org.molgenis.data.support.EntityTypeUtils;
import org.molgenis.security.core.MolgenisPermissionService;

import java.util.List;

import static org.molgenis.MolgenisFieldTypes.AttributeType.COMPOUND;
import static org.molgenis.MolgenisFieldTypes.AttributeType.getValueString;
import static org.molgenis.MolgenisFieldTypes.getType;

class AttributeMetaDataResponseV2
{
	private final String href;
	private final AttributeType fieldType;
	private final String name;
	private final String label;
	private final String description;
	private final List<?> attributes;
	private final List<String> enumOptions;
	private final Long maxLength;
	private final Object refEntity;
	private final String mappedBy;
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
	 * @param entityParentName
	 * @param entityType
	 * @param attr
	 * @param fetch             set of lowercase attribute names to include in response
	 * @param permissionService
	 */
	public AttributeMetaDataResponseV2(final String entityParentName, EntityType entityType, AttributeMetaData attr,
			Fetch fetch, MolgenisPermissionService permissionService, DataService dataService,
			LanguageService languageService)
	{
		String attrName = attr.getName();
		this.href = Href.concatMetaAttributeHref(RestControllerV2.BASE_URI, entityParentName, attrName);

		this.fieldType = attr.getDataType();
		this.name = attrName;
		this.label = attr.getLabel(languageService.getCurrentUserLanguageCode());
		this.description = attr.getDescription(languageService.getCurrentUserLanguageCode());
		this.enumOptions = attr.getDataType() == AttributeType.ENUM ? attr.getEnumOptions() : null;
		this.maxLength = getType(getValueString(attr.getDataType())).getMaxLength();
		this.expression = attr.getExpression();

		EntityType refEntity = attr.getRefEntity();
		if (refEntity != null)
		{
			this.refEntity = new EntityTypeResponseV2(refEntity, fetch, permissionService, dataService,
					languageService);
		}
		else
		{
			this.refEntity = null;
		}
		AttributeMetaData mappedByAttr = attr.getMappedBy();
		this.mappedBy = mappedByAttr != null ? mappedByAttr.getName() : null;

		Iterable<AttributeMetaData> attrParts = attr.getAttributeParts();
		if (attrParts != null)
		{
			// filter attribute parts
			attrParts = filterAttributes(fetch, attrParts);

			// create attribute response
			this.attributes = Lists.newArrayList(
					Iterables.transform(attrParts, new Function<AttributeMetaData, AttributeMetaDataResponseV2>()
					{
						@Override
						public AttributeMetaDataResponseV2 apply(AttributeMetaData attr)
						{
							Fetch subAttrFetch;
							if (fetch != null)
							{
								if (attr.getDataType() == AttributeType.COMPOUND)
								{
									subAttrFetch = fetch;
								}
								else
								{
									subAttrFetch = fetch.getFetch(attr);
								}
							}
							else if (EntityTypeUtils.isReferenceType(attr))
							{
								subAttrFetch = AttributeFilterToFetchConverter.createDefaultAttributeFetch(attr,
										languageService.getCurrentUserLanguageCode());
							}
							else
							{
								subAttrFetch = null;
							}
							return new AttributeMetaDataResponseV2(entityParentName, entityType, attr, subAttrFetch,
									permissionService, dataService, languageService);
						}
					}));
		}
		else
		{
			this.attributes = null;
		}

		this.auto = attr.isAuto();
		this.nillable = attr.isNillable();
		this.readOnly = attr.isReadOnly();
		this.defaultValue = attr.getDefaultValue();
		this.labelAttribute = attr.equals(entityType.getLabelAttribute());
		this.unique = attr.isUnique();
		this.lookupAttribute = entityType.getLookupAttribute(attr.getName()) != null;
		this.aggregateable = attr.isAggregatable();
		this.range = attr.getRange();
		this.visible = attr.isVisible();
		this.visibleExpression = attr.getVisibleExpression();
		this.validationExpression = attr.getValidationExpression();
	}

	public static Iterable<AttributeMetaData> filterAttributes(Fetch fetch, Iterable<AttributeMetaData> attrs)
	{
		if (fetch != null)
		{
			return Iterables.filter(attrs, new Predicate<AttributeMetaData>()
			{
				@Override
				public boolean apply(AttributeMetaData attr)
				{
					return filterAttributeRec(fetch, attr);
				}
			});
		}
		else
		{
			return attrs;
		}
	}

	public static boolean filterAttributeRec(Fetch fetch, AttributeMetaData attr)
	{
		if (attr.getDataType() == COMPOUND)
		{
			for (AttributeMetaData attrPart : attr.getAttributeParts())
			{
				if (filterAttributeRec(fetch, attrPart))
				{
					return true;
				}
			}
			return false;
		}
		else
		{
			return fetch.hasField(attr);
		}
	}

	public String getHref()
	{
		return href;
	}

	public AttributeType getFieldType()
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
