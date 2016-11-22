package org.molgenis.data.importer.emx;

import com.google.common.collect.*;
import org.molgenis.data.Entity;
import org.molgenis.data.i18n.model.I18nString;
import org.molgenis.data.i18n.model.Language;
import org.molgenis.data.importer.emx.EmxMetaDataParser.EmxAttribute;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semantic.SemanticTag;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.ImmutableMap.copyOf;
import static com.google.common.collect.ImmutableSetMultimap.copyOf;

/**
 * Mutable bean to store intermediate parse results. Uses lookup tables to map simple names to the parsed objects. Is
 * used by the {@link EmxMetaDataParser}
 */
public final class IntermediateParseResults
{
	/**
	 * Maps full name to EntityType
	 */
	private final Map<String, EntityType> entities;
	/**
	 * Maps full name to PackageImpl (with tags)
	 */
	private final Map<String, Package> packages;
	/**
	 * Contains all Attribute tags
	 */
	private final SetMultimap<String, SemanticTag<Attribute, LabeledResource, LabeledResource>> attributeTags;
	/**
	 * Contains all {@link Entity} tags
	 */
	private final List<SemanticTag<EntityType, LabeledResource, LabeledResource>> entityTags;
	/**
	 * Contains all tag entities from the tag sheet
	 */
	private final Map<String, Entity> tags;
	/**
	 * Contains all language enities from the languages sheet
	 */
	private final Map<String, Language> languages;
	/**
	 * Contains all i18nString entities from the i18nstrings sheet
	 */
	private final Map<String, I18nString> i18nStrings;
	private final EntityTypeFactory entityTypeFactory;

	public IntermediateParseResults(EntityTypeFactory entityTypeFactory)
	{
		this.tags = new LinkedHashMap<>();
		this.entities = new LinkedHashMap<>();
		this.packages = new LinkedHashMap<>();
		this.attributeTags = LinkedHashMultimap.create();
		this.entityTags = new ArrayList<>();
		this.languages = new LinkedHashMap<>();
		this.i18nStrings = new LinkedHashMap<>();
		this.entityTypeFactory = entityTypeFactory;
	}

	public void addTag(String identifier, Entity tag)
	{
		tags.put(identifier, tag);
	}

	public boolean hasTag(String identifier)
	{
		return tags.containsKey(identifier);
	}

	public Entity getTagEntity(String tagIdentifier)
	{
		return tags.get(tagIdentifier);
	}

	public void addEntityTag(SemanticTag tag)
	{
		entityTags.add(tag);
	}

	public void addAttributeTag(String entityName, SemanticTag tag)
	{
		attributeTags.put(entityName, tag);
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

	public void addAttributes(String entityName, List<EmxAttribute> emxAttrs)
	{
		EntityType entityType = getEntityType(entityName);
		if (entityType == null) entityType = addEntityType(entityName);

		for (EmxAttribute emxAttr : emxAttrs)
		{
			Attribute attr = emxAttr.getAttr();
			entityType.addAttribute(attr);

			// set attribute roles
			if (emxAttr.isIdAttr()) attr.setIdAttribute(true);
			if (emxAttr.isLabelAttr()) attr.setLabelAttribute(true);
			if (emxAttr.isLookupAttr()) attr.setLookupAttributeIndex(0); // FIXME assign unique index
		}
	}

	public EntityType addEntityType(String name)
	{
		String simpleName = name;
		for (String packageName : packages.keySet())
		{
			if (name.toLowerCase().startsWith(packageName.toLowerCase()))
			{
				simpleName = name.substring(packageName.length() + 1);// package_entity
			}
		}

		EntityType emd = entityTypeFactory.create().setName(name).setSimpleName(simpleName);
		entities.put(name, emd);
		return emd;
	}

	public EntityType getEntityType(String name)
	{
		return entities.get(name);
	}

	public void addLanguage(Language language)
	{
		languages.put(language.getCode(), language);
	}

	public void addI18nString(I18nString i18nString)
	{
		i18nStrings.put(i18nString.getMessageId(), i18nString);
	}

	/**
	 * Checks if it knows entity with given simple name.
	 *
	 * @param name simple name of the entity
	 * @return true if entity with simple name name is known, false otherwise
	 */
	public boolean knowsEntity(String name)
	{
		return entities.containsKey(name);
	}

	public void addPackage(String name, Package p)
	{
		packages.put(name, p);
	}

	public ImmutableMap<String, EntityType> getEntityMap()
	{
		return copyOf(entities);
	}

	public ImmutableList<EntityType> getEntities()
	{
		return copyOf(entities.values());
	}

	public ImmutableMap<String, Package> getPackages()
	{
		return copyOf(packages);
	}

	public ImmutableSetMultimap<String, SemanticTag<Attribute, LabeledResource, LabeledResource>> getAttributeTags()
	{
		return copyOf(attributeTags);
	}

	public ImmutableList<SemanticTag<EntityType, LabeledResource, LabeledResource>> getEntityTags()
	{
		return copyOf(entityTags);
	}

	public ImmutableMap<String, Language> getLanguages()
	{
		return copyOf(languages);
	}

	public ImmutableMap<String, I18nString> getI18nStrings()
	{
		return copyOf(i18nStrings);
	}

	@Override
	public String toString()
	{
		return "IntermediateParseResults [entities=" + entities + ", packages=" + packages + ", attributeTags="
				+ attributeTags + ", entityTags=" + entityTags + ", tags=" + tags + ", languages=" + languages
				+ ", i18nStrings=" + i18nStrings + "]";
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
		result = prime * result + ((tags == null) ? 0 : tags.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		IntermediateParseResults other = (IntermediateParseResults) obj;
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
		if (tags == null)
		{
			if (other.tags != null) return false;
		}
		else if (!tags.equals(other.tags)) return false;
		return true;
	}
}
