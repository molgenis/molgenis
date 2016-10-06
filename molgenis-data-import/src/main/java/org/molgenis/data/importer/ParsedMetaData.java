package org.molgenis.data.importer;

import com.google.common.collect.*;
import org.molgenis.data.i18n.model.I18nString;
import org.molgenis.data.i18n.model.Language;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semantic.SemanticTag;

import java.util.List;
import java.util.Map;

/**
 * Value object to store the result of parsing a source.
 */
public final class ParsedMetaData
{
	private final ImmutableMap<String, EntityType> entities;
	private final ImmutableMap<String, Package> packages;
	private final ImmutableSetMultimap<EntityType, SemanticTag<Attribute, LabeledResource, LabeledResource>> attributeTags;
	private final ImmutableList<SemanticTag<EntityType, LabeledResource, LabeledResource>> entityTags;
	private final ImmutableMap<String, Language> languages;
	private final ImmutableMap<String, I18nString> i18nStrings;

	public ParsedMetaData(List<? extends EntityType> entities, Map<String, ? extends Package> packages,
			SetMultimap<String, SemanticTag<Attribute, LabeledResource, LabeledResource>> attributeTags,
			List<SemanticTag<EntityType, LabeledResource, LabeledResource>> entityTags, Map<String, Language> languages,
			ImmutableMap<String, I18nString> i18nStrings)
	{
		if (entities == null)
		{
			throw new NullPointerException("Null entities");
		}

		ImmutableMap.Builder<String, EntityType> builder = ImmutableMap.builder();
		for (EntityType emd : entities)
		{
			builder.put(emd.getName(), emd);
		}
		this.entities = builder.build();
		if (packages == null)
		{
			throw new NullPointerException("Null packages");
		}
		this.packages = ImmutableMap.copyOf(packages);
		ImmutableSetMultimap.Builder<EntityType, SemanticTag<Attribute, LabeledResource, LabeledResource>> attrTagBuilder = ImmutableSetMultimap
				.builder();
		for (String simpleEntityName : attributeTags.keys())
		{
			EntityType emd = this.entities.get(simpleEntityName);
			if (emd == null)
			{
				throw new NullPointerException("Unknown entity [" + simpleEntityName + "]");
			}
			for (SemanticTag<Attribute, LabeledResource, LabeledResource> tag : attributeTags.get(simpleEntityName))
			{
				attrTagBuilder.put(emd, tag);
			}
		}
		this.attributeTags = attrTagBuilder.build();
		this.entityTags = ImmutableList.copyOf(entityTags);
		this.languages = ImmutableMap.copyOf(languages);
		this.i18nStrings = ImmutableMap.copyOf(i18nStrings);
	}

	public ImmutableCollection<EntityType> getEntities()
	{
		return entities.values();
	}

	public ImmutableMap<String, EntityType> getEntityMap()
	{
		return entities;
	}

	public ImmutableMap<String, Package> getPackages()
	{
		return packages;
	}

	public SetMultimap<EntityType, SemanticTag<Attribute, LabeledResource, LabeledResource>> getAttributeTags()
	{
		return attributeTags;
	}

	public ImmutableList<SemanticTag<EntityType, LabeledResource, LabeledResource>> getEntityTags()
	{
		return entityTags;
	}

	public ImmutableMap<String, Language> getLanguages()
	{
		return languages;
	}

	public ImmutableMap<String, I18nString> getI18nStrings()
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
	 * @param name the name of the package
	 * @return
	 */
	public Package getPackage(String name)
	{
		return getPackages().get(name);
	}
}
