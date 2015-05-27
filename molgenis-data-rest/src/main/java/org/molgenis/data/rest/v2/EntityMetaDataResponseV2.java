package org.molgenis.data.rest.v2;

import static org.molgenis.data.rest.v2.RestControllerV2.BASE_URI;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.common.collect.Lists;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.rest.Href;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

class EntityMetaDataResponseV2
{
	private final String href;
	private final String hrefCollection;
	private final String name;
	private final String label;
	private final String description;
	private final Map<String, Object> attributes;
	private final String labelAttribute;
	private final String idAttribute;
	private final List<String> lookupAttributes;
	private final Boolean isAbstract;
	/**
	 * Is this user allowed to add/update/delete entities of this type?
	 */
	private final Boolean writable;

	/**
	 * @param meta
	 */
	public EntityMetaDataResponseV2(EntityMetaData meta, MolgenisPermissionService permissionService)
	{
		this(meta, null, permissionService);
	}

	/**
	 * 
	 * @param meta
	 * @param attributes
	 *            set of lowercase attribute names to include in response
	 */
	public EntityMetaDataResponseV2(EntityMetaData meta, AttributeFilter attributes,
			MolgenisPermissionService permissionService)
	{
		String name = meta.getName();
		this.href = Href.concatMetaEntityHref(BASE_URI, name);
		this.hrefCollection = String.format("%s/%s", BASE_URI, name); // FIXME apply Href escaping fix

		this.name = name;
		this.description = meta.getDescription();
		this.label = meta.getLabel();

		this.attributes = new LinkedHashMap<String, Object>();

		for (AttributeMetaData attr : meta.getAttributes())
		{
			String attrName = attr.getName();
			if (!attrName.equals("__Type")) // FIXME check if still needed for JPA
			{
				if (attributes == null || attributes.contains(attrName))
				{
					AttributeFilter subAttributes = attributes != null ? attributes.getAttributeFilter(attrName) : null;
					this.attributes.put(attrName, new AttributeMetaDataResponseV2(name, attr, subAttributes,
							permissionService));
				}
			}
		}

		AttributeMetaData labelAttribute = meta.getLabelAttribute();
		this.labelAttribute = labelAttribute != null ? labelAttribute.getName() : null;

		AttributeMetaData idAttribute = meta.getIdAttribute();
		this.idAttribute = idAttribute != null ? idAttribute.getName() : null;

		Iterable<AttributeMetaData> lookupAttributes = meta.getLookupAttributes();
		this.lookupAttributes = lookupAttributes != null ? Lists.newArrayList(Iterables.transform(lookupAttributes,
				new Function<AttributeMetaData, String>()
				{
					@Override
					public String apply(AttributeMetaData attribute)
					{
						return attribute.getName();
					}
				})) : null;

		this.isAbstract = meta.isAbstract();

		this.writable = permissionService.hasPermissionOnEntity(name, Permission.WRITE);
	}

	public String getHref()
	{
		return href;
	}

	public String getHrefCollection()
	{
		return hrefCollection;
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

	public List<String> getLookupAttributes()
	{
		return lookupAttributes;
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

	public Boolean getWritable()
	{
		return writable;
	}
}
