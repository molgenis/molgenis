package org.molgenis.data.rest.v2;

import static org.molgenis.data.rest.v2.RestControllerV2.BASE_URI;

import java.util.List;

import org.elasticsearch.common.collect.Lists;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.rest.Href;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

class EntityMetaDataResponseV2
{
	private final String href;
	private final String hrefCollection;
	private final String name;
	private final String label;
	private final String description;
	private final List<AttributeMetaDataResponseV2> attributes;
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
	public EntityMetaDataResponseV2(EntityMetaData meta, MolgenisPermissionService permissionService,
			DataService dataService)
	{
		this(meta, null, permissionService, dataService);
	}

	/**
	 * 
	 * @param meta
	 * @param attrFilter
	 *            set of lowercase attribute names to include in response
	 */
	public EntityMetaDataResponseV2(EntityMetaData meta, AttributeFilter attrFilter,
			MolgenisPermissionService permissionService, DataService dataService)
	{
		String name = meta.getName();
		this.href = Href.concatMetaEntityHrefV2(BASE_URI, name);
		this.hrefCollection = String.format("%s/%s", BASE_URI, name); // FIXME apply Href escaping fix

		this.name = name;
		this.description = meta.getDescription();
		this.label = meta.getLabel();

		// filter attribute parts
		Iterable<AttributeMetaData> filteredAttrs = attrFilter != null ? Iterables.filter(meta.getAttributes(),
				new Predicate<AttributeMetaData>()
				{
					@Override
					public boolean apply(AttributeMetaData attr)
					{
						return attrFilter.includeAttribute(attr);
					}
				}) : meta.getAttributes();

		this.attributes = Lists.newArrayList(Iterables.transform(filteredAttrs,
				new Function<AttributeMetaData, AttributeMetaDataResponseV2>()
				{
					@Override
					public AttributeMetaDataResponseV2 apply(AttributeMetaData attr)
					{
						AttributeFilter subAttrFilter;
						if (attrFilter != null)
						{
							subAttrFilter = attrFilter.getAttributeFilter(attr);
						}
						else
						{
							subAttrFilter = null;
						}
						return new AttributeMetaDataResponseV2(name, attr, subAttrFilter, permissionService,
								dataService);
					}
				}));

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

		this.writable = permissionService.hasPermissionOnEntity(name, Permission.WRITE)
				&& dataService.getCapabilities(name).contains(RepositoryCapability.WRITABLE);
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

	public List<AttributeMetaDataResponseV2> getAttributes()
	{
		return attributes;
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
