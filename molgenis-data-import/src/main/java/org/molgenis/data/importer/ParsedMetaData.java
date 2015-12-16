package org.molgenis.data.importer;

import java.util.List;
import java.util.Map;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Package;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semantic.Tag;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;

/**
 * Value object to store the result of parsing a source.
 */
public final class ParsedMetaData
{
	private final ImmutableMap<String, EntityMetaData> entities;
	private final ImmutableMap<String, Package> packages;
	private final ImmutableSetMultimap<EntityMetaData, Tag<AttributeMetaData, LabeledResource, LabeledResource>> attributeTags;
	private final ImmutableList<Tag<EntityMetaData, LabeledResource, LabeledResource>> entityTags;
	private final ImmutableMap<String, Entity> languages;
	private final ImmutableMap<String, Entity> i18nStrings;

	public ParsedMetaData(List<? extends EntityMetaData> entities, Map<String, ? extends Package> packages,
			SetMultimap<String, Tag<AttributeMetaData, LabeledResource, LabeledResource>> attributeTags,
			List<Tag<EntityMetaData, LabeledResource, LabeledResource>> entityTags, Map<String, Entity> languages,
			ImmutableMap<String, Entity> i18nStrings)
	{
		if (entities == null)
		{
			throw new NullPointerException("Null entities");
		}

		ImmutableMap.Builder<String, EntityMetaData> builder = ImmutableMap.<String, EntityMetaData> builder();
		for (EntityMetaData emd : entities)
		{
			builder.put(emd.getName(), emd);
		}
		this.entities = builder.build();
		if (packages == null)
		{
			throw new NullPointerException("Null packages");
		}
		this.packages = ImmutableMap.copyOf(packages);
		ImmutableSetMultimap.Builder<EntityMetaData, Tag<AttributeMetaData, LabeledResource, LabeledResource>> attrTagBuilder = ImmutableSetMultimap
				.<EntityMetaData, Tag<AttributeMetaData, LabeledResource, LabeledResource>> builder();
		for (String simpleEntityName : attributeTags.keys())
		{
			EntityMetaData emd = this.entities.get(simpleEntityName);
			if (emd == null)
			{
				throw new NullPointerException("Unknown entity [" + simpleEntityName + "]");
			}
			for (Tag<AttributeMetaData, LabeledResource, LabeledResource> tag : attributeTags.get(simpleEntityName))
			{
				attrTagBuilder.put(emd, tag);
			}
		}
		this.attributeTags = attrTagBuilder.build();
		this.entityTags = ImmutableList.copyOf(entityTags);
		this.languages = ImmutableMap.copyOf(languages);
		this.i18nStrings = ImmutableMap.copyOf(i18nStrings);
	}

	public ImmutableCollection<EntityMetaData> getEntities()
	{
		return entities.values();
	}

	public ImmutableMap<String, EntityMetaData> getEntityMap()
	{
		return entities;
	}

	public ImmutableMap<String, Package> getPackages()
	{
		return packages;
	}

	public SetMultimap<EntityMetaData, Tag<AttributeMetaData, LabeledResource, LabeledResource>> getAttributeTags()
	{
		return attributeTags;
	}

	public ImmutableList<Tag<EntityMetaData, LabeledResource, LabeledResource>> getEntityTags()
	{
		return entityTags;
	}

	public ImmutableMap<String, Entity> getLanguages()
	{
		return languages;
	}

	public ImmutableMap<String, Entity> getI18nStrings()
	{
		return i18nStrings;
	}

	@Override
	public String toString()
	{
		return "ParsedMetaData [entities=" + entities + ", packages=" + packages + ", attributeTags=" + attributeTags
				+ ", entityTags=" + entityTags + ", languages=" + languages + ", i18nStrings=" + i18nStrings + "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attributeTags == null) ? 0 : attributeTags.hashCode());
		result = prime * result + ((entities == null) ? 0 : entities.hashCode());
		result = prime * result + ((entityTags == null) ? 0 : entityTags.hashCode());
		result = prime * result + ((i18nStrings == null) ? 0 : i18nStrings.hashCode());
		result = prime * result + ((languages == null) ? 0 : languages.hashCode());
		result = prime * result + ((packages == null) ? 0 : packages.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ParsedMetaData other = (ParsedMetaData) obj;
		if (attributeTags == null)
		{
			if (other.attributeTags != null) return false;
		}
		else if (!attributeTags.equals(other.attributeTags)) return false;
		if (entities == null)
		{
			if (other.entities != null) return false;
		}
		else if (!entities.equals(other.entities)) return false;
		if (entityTags == null)
		{
			if (other.entityTags != null) return false;
		}
		else if (!entityTags.equals(other.entityTags)) return false;
		if (i18nStrings == null)
		{
			if (other.i18nStrings != null) return false;
		}
		else if (!i18nStrings.equals(other.i18nStrings)) return false;
		if (languages == null)
		{
			if (other.languages != null) return false;
		}
		else if (!languages.equals(other.languages)) return false;
		if (packages == null)
		{
			if (other.packages != null) return false;
		}
		else if (!packages.equals(other.packages)) return false;
		return true;
	}

	/**
	 * Gets a specific package
	 * 
	 * @param name
	 *            the name of the package
	 * @return
	 */
	public org.molgenis.data.Package getPackage(String name)
	{
		return getPackages().get(name);
	}
}
