package org.molgenis.data.importer;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import org.molgenis.data.i18n.model.L10nString;
import org.molgenis.data.i18n.model.Language;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.Tag;

import java.util.List;
import java.util.Map;

/**
 * Value object to store the result of parsing a source.
 */
public class ParsedMetaData
{
	private final ImmutableMap<String, EntityType> entities;
	private final ImmutableMap<String, Package> packages;
	private final ImmutableMap<String, Tag> tags;
	private final ImmutableMap<String, Language> languages;
	private final ImmutableMap<String, L10nString> l10nStrings;

	public ParsedMetaData(List<? extends EntityType> entities, Map<String, ? extends Package> packages,
			ImmutableMap<String, Tag> tags, Map<String, Language> languages,
			ImmutableMap<String, L10nString> l10nStrings)
	{
		if (entities == null)
		{
			throw new NullPointerException("Null entities");
		}

		ImmutableMap.Builder<String, EntityType> builder = ImmutableMap.builder();
		for (EntityType emd : entities)
		{
			builder.put(emd.getId(), emd);
		}
		this.entities = builder.build();
		if (packages == null)
		{
			throw new NullPointerException("Null packages");
		}
		this.packages = ImmutableMap.copyOf(packages);
		this.tags = ImmutableMap.copyOf(tags);
		this.languages = ImmutableMap.copyOf(languages);
		this.l10nStrings = ImmutableMap.copyOf(l10nStrings);
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

	public ImmutableMap<String, Tag> getTags()
	{
		return tags;
	}

	public ImmutableMap<String, Language> getLanguages()
	{
		return languages;
	}

	public ImmutableMap<String, L10nString> getL10nStrings()
	{
		return l10nStrings;
	}

	@Override
	public String toString()
	{
		return "ParsedMetaData [entities=" + entities + ", packages=" + packages + ", tags=" + tags + ", languages="
				+ languages + ", l10nStrings=" + l10nStrings + ']';
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((tags == null) ? 0 : tags.hashCode());
		result = prime * result + ((entities == null) ? 0 : entities.hashCode());
		result = prime * result + ((l10nStrings == null) ? 0 : l10nStrings.hashCode());
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
		if (entities == null)
		{
			if (other.entities != null) return false;
		}
		else if (!entities.equals(other.entities)) return false;
		if (tags == null)
		{
			if (other.tags != null) return false;
		}
		else if (!tags.equals(other.tags)) return false;
		if (l10nStrings == null)
		{
			if (other.l10nStrings != null) return false;
		}
		else if (!l10nStrings.equals(other.l10nStrings)) return false;
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
}
