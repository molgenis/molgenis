package org.molgenis.data.meta.model;

import org.molgenis.data.Entity;
import org.molgenis.data.support.StaticEntity;

// TODO validate IRI
// TODO extends typed entity that stores entity and handles get/sets, also apply to other meta data entities
public class Tag extends StaticEntity
{
	public Tag(Entity entity)
	{
		super(entity);
	}

	public Tag(EntityType entityType)
	{
		super(entityType);
	}

	public Tag(String identifier, EntityType entityType)
	{
		super(entityType);
		setId(identifier);
	}

	public static Tag newInstance(Tag tag)
	{
		Tag tagCopy = new Tag(tag.getEntityType());
		tagCopy.setId(tag.getId());
		tagCopy.setObjectIri(tag.getObjectIri());
		tagCopy.setLabel(tag.getLabel());
		tagCopy.setRelationIri(tag.getRelationIri());
		tagCopy.setRelationLabel(tag.getRelationLabel());
		tagCopy.setCodeSystem(tag.getCodeSystem());
		return tagCopy;
	}

	public String getId()
	{
		return getString(TagMetadata.ID);
	}

	public Tag setId(String identifier)
	{
		set(TagMetadata.ID, identifier);
		return this;
	}

	public String getObjectIri()
	{
		return getString(TagMetadata.OBJECT_IRI);
	}

	public Tag setObjectIri(String objectIri)
	{
		set(TagMetadata.OBJECT_IRI, objectIri);
		return this;
	}

	public String getLabel()
	{
		return getString(TagMetadata.LABEL);
	}

	public Tag setLabel(String label)
	{
		set(TagMetadata.LABEL, label);
		return this;
	}

	public String getRelationIri()
	{
		return getString(TagMetadata.RELATION_IRI);
	}

	public Tag setRelationIri(String relationIri)
	{
		set(TagMetadata.RELATION_IRI, relationIri);
		return this;
	}

	public String getRelationLabel()
	{
		return getString(TagMetadata.RELATION_LABEL);
	}

	public Tag setRelationLabel(String relationLabel)
	{
		set(TagMetadata.RELATION_LABEL, relationLabel);
		return this;
	}

	public String getCodeSystem()
	{
		return getString(TagMetadata.CODE_SYSTEM);
	}

	public Tag setCodeSystem(String codeSystem)
	{
		set(TagMetadata.CODE_SYSTEM, codeSystem);
		return this;
	}
}
