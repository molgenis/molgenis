package org.molgenis.ontology.core.meta;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.StaticEntity;

import static org.molgenis.ontology.core.meta.OntologyTermSynonymMetaData.ID;
import static org.molgenis.ontology.core.meta.OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM_ATTR;

public class OntologyTermSynonym extends StaticEntity
{
	public OntologyTermSynonym(Entity entity)
	{
		super(entity);
	}

	public OntologyTermSynonym(EntityMetaData entityMeta)
	{
		super(entityMeta);
	}

	public OntologyTermSynonym(String id, EntityMetaData entityMeta)
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

	public String getOntologyTermSynonym()
	{
		return getString(ONTOLOGY_TERM_SYNONYM_ATTR);
	}

	public void setOntologyTermSynonym(String ontologyTermSynonym)
	{
		set(ONTOLOGY_TERM_SYNONYM_ATTR, ontologyTermSynonym);
	}
}
