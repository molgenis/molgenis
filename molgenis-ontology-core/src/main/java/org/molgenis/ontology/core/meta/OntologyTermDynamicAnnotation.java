package org.molgenis.ontology.core.meta;

import static org.molgenis.ontology.core.meta.OntologyTermDynamicAnnotationMetaData.ID;
import static org.molgenis.ontology.core.meta.OntologyTermDynamicAnnotationMetaData.LABEL;
import static org.molgenis.ontology.core.meta.OntologyTermDynamicAnnotationMetaData.NAME;
import static org.molgenis.ontology.core.meta.OntologyTermDynamicAnnotationMetaData.VALUE;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.StaticEntity;

public class OntologyTermDynamicAnnotation extends StaticEntity
{
	public OntologyTermDynamicAnnotation(Entity entity)
	{
		super(entity);
	}

	public OntologyTermDynamicAnnotation(EntityMetaData entityMeta)
	{
		super(entityMeta);
	}

	public OntologyTermDynamicAnnotation(String id, EntityMetaData entityMeta)
	{
		super(entityMeta);
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
