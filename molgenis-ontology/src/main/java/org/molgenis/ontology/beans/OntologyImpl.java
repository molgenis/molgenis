package org.molgenis.ontology.beans;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.Entity;
import org.molgenis.ontology.model.OntologyMetaData;

public class OntologyImpl implements Ontology
{
	private final String name;
	private final String iri;

	public OntologyImpl(Entity entity)
	{
		this.name = entity.getString(OntologyMetaData.ONTOLOGY_NAME);
		this.iri = entity.getString(OntologyMetaData.ONTOLOGY_IRI);
	}

	@Override
	public String getLabel()
	{
		return name;
	}

	@Override
	public String getIri()
	{
		return iri;
	}

	@Override
	public String getVersion()
	{
		return StringUtils.EMPTY;
	}

	@Override
	public String toString()
	{
		return name;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((iri == null) ? 0 : iri.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		OntologyImpl other = (OntologyImpl) obj;
		if (iri == null)
		{
			if (other.iri != null) return false;
		}
		else if (!iri.equals(other.iri)) return false;
		if (name == null)
		{
			if (other.name != null) return false;
		}
		else if (!name.equals(other.name)) return false;
		return true;
	}
}