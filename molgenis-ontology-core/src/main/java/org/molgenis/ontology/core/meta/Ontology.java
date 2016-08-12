package org.molgenis.ontology.core.meta;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.StaticEntity;

import static org.molgenis.ontology.core.meta.OntologyMetaData.*;

public class Ontology extends StaticEntity
{
	public Ontology(Entity entity)
	{
		super(entity);
	}

	public Ontology(EntityMetaData entityMeta)
	{
		super(entityMeta);
	}

	public Ontology(String id, EntityMetaData entityMeta)
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

	public String getOntologyIri()
	{
		return getString(ONTOLOGY_IRI);
	}

	public void setOntologyIri(String ontologyIri)
	{
		set(ONTOLOGY_IRI, ontologyIri);
	}

	public String getOntologyName()
	{
		return getString(ONTOLOGY_NAME);
	}

	public void setOntologyName(String ontologyName)
	{
		set(ONTOLOGY_NAME, ontologyName);
	}
}
