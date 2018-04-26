package org.molgenis.ontology.core.meta;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import static org.molgenis.ontology.core.meta.OntologyTermDynamicAnnotationMetaData.*;

public class OntologyTermDynamicAnnotation extends StaticEntity
{
	public OntologyTermDynamicAnnotation(Entity entity)
	{
		super(entity);
	}

	public OntologyTermDynamicAnnotation(EntityType entityType)
	{
		super(entityType);
	}

	public OntologyTermDynamicAnnotation(String id, EntityType entityType)
	{
		super(entityType);
		setId(id);
	}

	public String getId()
	{
		return getString(ID);
	}

	public void setId(String id)
	{
		set(ID, id);
	}

	public String getName()
	{
		return getString(NAME);
	}

	public void setName(String name)
	{
		set(NAME, name);
	}

	public String getValue()
	{
		return getString(VALUE);
	}

	public void setValue(String value)
	{
		set(VALUE, value);
	}

	public String getLabel()
	{
		return getString(LABEL);
	}

	public void setLabel(String label)
	{
		set(LABEL, label);
	}
}
