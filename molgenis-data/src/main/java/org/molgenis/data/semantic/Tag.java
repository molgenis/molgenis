package org.molgenis.data.semantic;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.meta.PackageMetaData;
import org.molgenis.data.meta.TagMetaData;
import org.molgenis.data.support.AbstractEntity;
import org.molgenis.util.ApplicationContextProvider;

// TODO validate IRI
// TODO extends typed entity that stores entity and handles get/sets, also apply to other meta data entities
public class Tag extends AbstractEntity
{
	private final Entity entity;

	public Tag(Entity entity)
	{
		this.entity = requireNonNull(entity);
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return ApplicationContextProvider.getApplicationContext().getBean(TagMetaData.class);
	}

	public String getIdentifier()
	{
		return getString(TagMetaData.IDENTIFIER);
	}

	public Tag setIdentifier(String identifier)
	{
		set(TagMetaData.IDENTIFIER, identifier);
		return this;
	}

	public String getObjectIri()
	{
		return getString(TagMetaData.OBJECT_IRI);
	}

	public Tag setObjectIri(String objectIri)
	{
		set(TagMetaData.OBJECT_IRI, objectIri);
		return this;
	}

	public String getLabel()
	{
		return getString(TagMetaData.LABEL);
	}

	public Tag setLabel(String label)
	{
		set(TagMetaData.LABEL, label);
		return this;
	}

	public String getRelationIri()
	{
		return getString(TagMetaData.RELATION_IRI);
	}

	public Tag setRelationIri(String relationIri)
	{
		set(TagMetaData.RELATION_IRI, relationIri);
		return this;
	}

	public String getRelationLabel()
	{
		return getString(TagMetaData.RELATION_LABEL);
	}

	public Tag setRelationLabel(String relationLabel)
	{
		set(TagMetaData.RELATION_LABEL, relationLabel);
		return this;
	}

	public String getCodeSystem()
	{
		return getString(TagMetaData.CODE_SYSTEM);
	}

	public Tag setCodeSystem(String codeSystem)
	{
		set(TagMetaData.CODE_SYSTEM, codeSystem);
		return this;
	}

	@Override
	public Object get(String attributeName)
	{
		return entity.get(attributeName);
	}

	@Override
	public void set(String attributeName, Object value)
	{
		entity.set(attributeName, value);
	}

	@Override
	public void set(Entity values)
	{
		entity.set(values);
	}
}
