package org.molgenis.ontology.beans;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.Entity;
import org.molgenis.ontology.Ontology;
import org.molgenis.ontology.repository.OntologyQueryRepository;

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
		this.description = entity.getString(OntologyQueryRepository.ONTOLOGY_TERM_DEFINITION);
		this.label = entity.getString(OntologyQueryRepository.ONTOLOGY_NAME);
		this.iri = entity.getString(OntologyQueryRepository.ONTOLOGY_IRI);
	}

	@Override
	public String getLabel()
	{
		return label;
	}

	@Override
	public String getDescription()
	{
		return description;
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
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
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
		if (description == null)
		{
			if (other.description != null) return false;
		}
		else if (!description.equals(other.description)) return false;
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

	@Override
	public String toString()
	{
		return label;
	}

}
