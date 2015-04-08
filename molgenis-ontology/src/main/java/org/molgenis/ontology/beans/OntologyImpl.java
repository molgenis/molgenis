package org.molgenis.ontology.beans;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.Entity;
import org.molgenis.ontology.core.meta.OntologyMetaData;

public class OntologyImpl implements Ontology
{
	private final String label;
	private final String iri;
	private final String description;

	public OntologyImpl(String label, String iri, String description)
	{
		super();
		this.label = label;
		this.iri = iri;
		this.description = description;
	}

	public OntologyImpl(Entity entity)
	{
		this.label = entity.getString(OntologyMetaData.ONTOLOGY_NAME);
		this.iri = entity.getString(OntologyMetaData.ONTOLOGY_IRI);
		// TODO : FIXME
		this.description = StringUtils.EMPTY;
	}

	@Override
	public String getLabel()
	{
		return label;
	}

	@Override
	public String getIri()
	{
		return iri;
	}

	@Override
	public String getDescription()
	{
		return description;
	}

	@Override
	public String getVersion()
	{
		return StringUtils.EMPTY;
	}

	@Override
	public String toString()
	{
		return label;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((iri == null) ? 0 : iri.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
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
		if (label == null)
		{
			if (other.label != null) return false;
		}
		else if (!label.equals(other.label)) return false;
		return true;
	}
}