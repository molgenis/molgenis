package org.molgenis.data.meta;

import static org.molgenis.data.meta.TagMetaData.CODE_SYSTEM;
import static org.molgenis.data.meta.TagMetaData.IDENTIFIER;
import static org.molgenis.data.meta.TagMetaData.LABEL;
import static org.molgenis.data.meta.TagMetaData.OBJECT_IRI;
import static org.molgenis.data.meta.TagMetaData.RELATION_IRI;
import static org.molgenis.data.meta.TagMetaData.RELATION_LABEL;

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

	public Tag(EntityMetaData entityMeta)
	{
		super(entityMeta);
	}

	public Tag(String identifier, EntityMetaData entityMeta)
	{
		super(entityMeta);
		setIdentifier(identifier);
	}

	public static Tag newInstance(Tag tag)
	{
		Tag tagCopy = new Tag(tag.getEntityMetaData());
		tagCopy.setIdentifier(tag.getIdentifier());
		tagCopy.setObjectIri(tag.getObjectIri());
		tagCopy.setLabel(tag.getLabel());
		tagCopy.setRelationIri(tag.getRelationIri());
		tagCopy.setRelationLabel(tag.getRelationLabel());
		tagCopy.setCodeSystem(tag.getCodeSystem());
		return tagCopy;
	}

	public String getIdentifier()
	{
		return getString(IDENTIFIER);
	}

	public Tag setIdentifier(String identifier)
	{
		set(IDENTIFIER, identifier);
		return this;
	}

	public String getObjectIri()
	{
		return getString(OBJECT_IRI);
	}

	public Tag setObjectIri(String objectIri)
	{
		set(OBJECT_IRI, objectIri);
		return this;
	}

	public String getLabel()
	{
		return getString(LABEL);
	}

	public Tag setLabel(String label)
	{
		set(LABEL, label);
		return this;
	}

	public String getRelationIri()
	{
		return getString(RELATION_IRI);
	}

	public Tag setRelationIri(String relationIri)
	{
		set(RELATION_IRI, relationIri);
		return this;
	}

	public String getRelationLabel()
	{
		return getString(RELATION_LABEL);
	}

	public Tag setRelationLabel(String relationLabel)
	{
		set(RELATION_LABEL, relationLabel);
		return this;
	}

	public String getCodeSystem()
	{
		return getString(CODE_SYSTEM);
	}

	public Tag setCodeSystem(String codeSystem)
	{
		set(CODE_SYSTEM, codeSystem);
		return this;
	}
}
