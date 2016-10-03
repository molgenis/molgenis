package org.molgenis.ontology.core.meta;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.StaticEntity;

public class SemanticTypeEntity extends StaticEntity
{
	public SemanticTypeEntity(Entity entity)
	{
		super(entity);
	}

	public SemanticTypeEntity(EntityMetaData entityMeta)
	{
		super(entityMeta);
	}

	public SemanticTypeEntity(String id, EntityMetaData entityMeta)
	{
		super(entityMeta);
		setId(id);
	}

	public String getIdentifier()
	{
		return getString(SemanticTypeMetaData.ID);
	}

	public void setId(String id)
	{
		set(SemanticTypeMetaData.ID, id);
	}

	public String getSemanticTypeName()
	{
		return getString(SemanticTypeMetaData.SEMANTIC_TYPE_NAME);
	}

	public void setSemanticTypeName(String semanticTypeName)
	{
		set(SemanticTypeMetaData.SEMANTIC_TYPE_NAME, semanticTypeName);
	}

	public String getSemanticTypeGroup()
	{
		return getString(SemanticTypeMetaData.SEMANTIC_TYPE_GROUP);
	}

	public void setSemanticTypeGroup(String semanticTypeGroup)
	{
		set(SemanticTypeMetaData.SEMANTIC_TYPE_GROUP, semanticTypeGroup);
	}

	public Boolean isGlobalKeyConcept()
	{
		return getBoolean(SemanticTypeMetaData.SEMANTIC_TYPE_GLOBAL_KEY_CONCEPT);
	}

	public void setGlobalKeyConcept(Boolean isGlobalConcept)
	{
		set(SemanticTypeMetaData.SEMANTIC_TYPE_GLOBAL_KEY_CONCEPT, isGlobalConcept);
	}
}
