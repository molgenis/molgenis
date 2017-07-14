package org.molgenis.data.rest;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.molgenis.data.DataService;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.Href;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;

import java.util.*;

public class EntityTypeResponse
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
	private String languageCode;

	/**
	 * Is this user allowed to add/update/delete entities of this type and has the repo the capability?
	 */
	private final Boolean writable;

	/**
	 * @param meta
	 */
	public EntityTypeResponse(EntityType meta, MolgenisPermissionService permissionService, DataService dataService,
			LanguageService languageService)
	{
		this(meta, null, null, permissionService, dataService, languageService);
	}

	/**
	 * @param meta
	 * @param attributesSet       set of lowercase attribute names to include in response
	 * @param attributeExpandsSet set of lowercase attribute names to expand in response
	 */
	public EntityTypeResponse(EntityType meta, Set<String> attributesSet, Map<String, Set<String>> attributeExpandsSet,
			MolgenisPermissionService permissionService, DataService dataService, LanguageService languageService)
	{
		String name = meta.getId();
		this.href = Href.concatMetaEntityHref(RestController.BASE_URI, name);
		this.hrefCollection = String.format("%s/%s", RestController.BASE_URI, name); // FIXME apply Href escaping fix
		this.languageCode = languageService.getCurrentUserLanguageCode();

		if (attributesSet == null || attributesSet.contains("name".toLowerCase()))
		{
			this.name = name;
		}
		else this.name = null;

		if (attributesSet == null || attributesSet.contains("description".toLowerCase()))
		{
			this.description = meta.getDescription(languageService.getCurrentUserLanguageCode());
		}
		else this.description = null;

		if (attributesSet == null || attributesSet.contains("label".toLowerCase()))
		{
			label = meta.getLabel(languageService.getCurrentUserLanguageCode());
		}
		else this.label = null;

		if (attributesSet == null || attributesSet.contains("attributes".toLowerCase()))
		{
			this.attributes = new LinkedHashMap<>();
			//the newArraylist is a fix for concurrency trouble
			//FIXME properly fix this by making metadata immutable
			for (Attribute attr : Lists.newArrayList(meta.getAttributes()))
			{
				if (!attr.getName().equals("__Type"))
				{
					if (attributeExpandsSet != null && attributeExpandsSet.containsKey("attributes".toLowerCase()))
					{
						Set<String> subAttributesSet = attributeExpandsSet.get("attributes".toLowerCase());
						this.attributes.put(attr.getName(), new AttributeResponse(name, meta, attr, subAttributesSet,
								Collections.singletonMap("refEntity".toLowerCase(), Sets.newHashSet("idattribute")),
								permissionService, dataService, languageService));
					}
					else
					{
						String attrHref = Href.concatMetaAttributeHref(RestController.BASE_URI, name, attr.getName());
						this.attributes.put(attr.getName(), Collections.singletonMap("href", attrHref));
					}
				}
			}
		}
		else this.attributes = null;

		if (attributesSet == null || attributesSet.contains("labelAttribute".toLowerCase()))
		{
			Attribute labelAttribute = meta.getLabelAttribute(this.languageCode);
			this.labelAttribute = labelAttribute != null ? labelAttribute.getName() : null;
		}
		else this.labelAttribute = null;

		if (attributesSet == null || attributesSet.contains("idAttribute".toLowerCase()))
		{
			Attribute idAttribute = meta.getIdAttribute();
			this.idAttribute = idAttribute != null ? idAttribute.getName() : null;
		}
		else this.idAttribute = null;

		if (attributesSet == null || attributesSet.contains("lookupAttributes".toLowerCase()))
		{
			Iterable<Attribute> lookupAttributes = meta.getLookupAttributes();
			this.lookupAttributes = lookupAttributes != null ? Lists.newArrayList(
					Iterables.transform(lookupAttributes, Attribute::getName)) : null;
		}
		else this.lookupAttributes = null;

		if (attributesSet == null || attributesSet.contains("abstract".toLowerCase()))
		{
			isAbstract = meta.isAbstract();
		}
		else this.isAbstract = null;

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

	public String getLanguageCode()
	{
		return languageCode;
	}

}
