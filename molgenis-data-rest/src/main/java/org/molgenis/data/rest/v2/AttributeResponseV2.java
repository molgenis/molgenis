package org.molgenis.data.rest.v2;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.molgenis.data.DataService;
import org.molgenis.data.Fetch;
import org.molgenis.data.Range;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.EntityTypeUtils;
import org.molgenis.data.support.Href;
import org.molgenis.security.core.MolgenisPermissionService;

import java.util.List;

import static org.molgenis.data.meta.AttributeType.COMPOUND;

class AttributeResponseV2
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
	private Boolean isAggregatable;
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
	public AttributeResponseV2(final String entityParentName, EntityType entityType, Attribute attr, Fetch fetch,
			MolgenisPermissionService permissionService, DataService dataService, LanguageService languageService)
	{
		String attrName = attr.getName();
		this.href = Href.concatMetaAttributeHref(RestControllerV2.BASE_URI, entityParentName, attrName);

		this.fieldType = attr.getDataType();
		this.name = attrName;
		this.label = attr.getLabel(languageService.getCurrentUserLanguageCode());
		this.description = attr.getDescription(languageService.getCurrentUserLanguageCode());
		this.enumOptions = attr.getDataType() == AttributeType.ENUM ? attr.getEnumOptions() : null;
		this.maxLength = attr.getDataType().getMaxLength();
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
		Attribute mappedByAttr = attr.getMappedBy();
		this.mappedBy = mappedByAttr != null ? mappedByAttr.getName() : null;

		Iterable<Attribute> attrParts = attr.getChildren();
		if (attrParts != null)
		{
			// filter attribute parts
			attrParts = filterAttributes(fetch, attrParts);

			// create attribute response
			this.attributes = Lists.newArrayList(
					Iterables.transform(attrParts, attr1 ->
					{
						Fetch subAttrFetch;
						if (fetch != null)
						{
							if (attr1.getDataType() == AttributeType.COMPOUND)
							{
								subAttrFetch = fetch;
							}
							else
							{
								subAttrFetch = fetch.getFetch(attr1);
							}
						}
						else if (EntityTypeUtils.isReferenceType(attr1))
						{
							subAttrFetch = AttributeFilterToFetchConverter.createDefaultAttributeFetch(attr1,
									languageService.getCurrentUserLanguageCode());
						}
						else
						{
							subAttrFetch = null;
						}
						return new AttributeResponseV2(entityParentName, entityType, attr1, subAttrFetch,
								permissionService, dataService, languageService);
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
		this.isAggregatable = attr.isAggregatable();
		this.range = attr.getRange();
		this.visible = attr.isVisible();
		this.visibleExpression = attr.getVisibleExpression();
		this.validationExpression = attr.getValidationExpression();
	}

	public static Iterable<Attribute> filterAttributes(Fetch fetch, Iterable<Attribute> attrs)
	{
		if (fetch != null)
		{
			return Iterables.filter(attrs, attr -> filterAttributeRec(fetch, attr));
		}
		else
		{
			return attrs;
		}
	}

	public static boolean filterAttributeRec(Fetch fetch, Attribute attr)
	{
		if (attr.getDataType() == COMPOUND)
		{
			for (Attribute attrPart : attr.getChildren())
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

	public Boolean isAggregatable()
	{
		return isAggregatable;
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

	public Boolean getAggregatable()
	{
		return isAggregatable;
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
