package org.molgenis.data.rest.v2;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.molgenis.data.DataService;
import org.molgenis.data.Fetch;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.EntityTypeUtils;
import org.molgenis.data.support.Href;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;

import java.util.List;

import static org.molgenis.data.meta.AttributeType.COMPOUND;
import static org.molgenis.data.rest.v2.AttributeResponseV2.filterAttributes;
import static org.molgenis.data.rest.v2.RestControllerV2.BASE_URI;

class EntityTypeResponseV2
{
	private final String href;
	private final String hrefCollection;
	private final String name;
	private final String label;
	private final String description;
	private final List<AttributeResponseV2> attributes;
	private final String labelAttribute;
	private final String idAttribute;
	private final List<String> lookupAttributes;
	private final Boolean isAbstract;
	/**
	 * Is this user allowed to add/update/delete entities of this type?
	 */
	private final Boolean writable;
	private String languageCode;

	/**
	 * @param meta
	 */
	public EntityTypeResponseV2(EntityType meta, MolgenisPermissionService permissionService, DataService dataService,
			LanguageService languageService)
	{
		this(meta, null, permissionService, dataService, languageService);
	}

	/**
	 * @param meta
	 * @param fetch set of lowercase attribute names to include in response
	 */
	public EntityTypeResponseV2(EntityType meta, Fetch fetch, MolgenisPermissionService permissionService,
			DataService dataService, LanguageService languageService)
	{
		String name = meta.getId();
		this.href = Href.concatMetaEntityHrefV2(BASE_URI, name);
		this.hrefCollection = String.format("%s/%s", BASE_URI, name); // FIXME apply Href escaping fix

		this.name = name;
		this.description = meta.getDescription(languageService.getCurrentUserLanguageCode());
		this.label = meta.getLabel(languageService.getCurrentUserLanguageCode());

		// filter attribute parts
		Iterable<Attribute> filteredAttrs = filterAttributes(fetch, meta.getAttributes());

		this.attributes = Lists.newArrayList(Iterables.transform(filteredAttrs, attr ->
		{
			Fetch subAttrFetch;
			if (fetch != null)
			{
				if (attr.getDataType() == COMPOUND)
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
				subAttrFetch = AttributeFilterToFetchConverter.createDefaultAttributeFetch(attr, languageCode);
			}
			else
			{
				subAttrFetch = null;
			}
			return new AttributeResponseV2(name, meta, attr, subAttrFetch, permissionService, dataService,
					languageService);
		}));

		languageCode = languageService.getCurrentUserLanguageCode();

		Attribute labelAttribute = meta.getLabelAttribute(languageCode);
		this.labelAttribute = labelAttribute != null ? labelAttribute.getName() : null;

		Attribute idAttribute = meta.getIdAttribute();
		this.idAttribute = idAttribute != null ? idAttribute.getName() : null;

		Iterable<Attribute> lookupAttributes = meta.getLookupAttributes();
		this.lookupAttributes = lookupAttributes != null ? Lists.newArrayList(
				Iterables.transform(lookupAttributes, Attribute::getName)) : null;

		this.isAbstract = meta.isAbstract();

		this.writable =
				permissionService.hasPermissionOnEntity(name, Permission.WRITE) && dataService.getCapabilities(name)
																							  .contains(
																									  RepositoryCapability.WRITABLE);
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

	public List<AttributeResponseV2> getAttributes()
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

	public String getLanguageCode()
	{
		return languageCode;
	}

}
