package org.molgenis.ontology.beans;

import java.util.Collections;
import java.util.Set;

import org.molgenis.data.Entity;
import org.molgenis.ontology.Ontology;
import org.molgenis.ontology.OntologyService;
import org.molgenis.ontology.OntologyTerm;
import org.molgenis.ontology.repository.OntologyTermQueryRepository;

public class OntologyTermImpl implements OntologyTerm
{
	private final String iri;
	private final String label;
	private final String description;
	private final String termAccession;
	private final Ontology ontology;

	public OntologyTermImpl(Entity entity, OntologyService ontologyService)
	{
		this.iri = entity.getString(OntologyTermQueryRepository.ONTOLOGY_TERM_IRI);
		this.label = entity.getString(OntologyTermQueryRepository.ONTOLOGY_TERM);
		this.description = entity.getString(OntologyTermQueryRepository.ONTOLOGY_TERM_DEFINITION);
		this.termAccession = entity.getString(OntologyTermQueryRepository.ID);
		this.ontology = ontologyService.getOntology(entity.getString(OntologyTermQueryRepository.ONTOLOGY_IRI));
	}

	public OntologyTermImpl(String iri, String label, String description, String termAccession, Ontology ontology)
	{
		this.iri = iri;
		this.label = label;
		this.description = description;
		this.termAccession = termAccession;
		this.ontology = ontology;
	}

	@Override
	public String getIRI()
	{
		return iri;
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
	public String getTermAccession()
	{
		return termAccession;
	}

	@Override
	public Set<String> getSynonyms()
	{
		// TODO : implement in the future
		return Collections.emptySet();
	}

	@Override
	public Ontology getOntology()
	{
		return ontology;
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
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((iri == null) ? 0 : iri.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((ontology == null) ? 0 : ontology.hashCode());
		result = prime * result + ((termAccession == null) ? 0 : termAccession.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		OntologyTermImpl other = (OntologyTermImpl) obj;
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
		if (ontology == null)
		{
			if (other.ontology != null) return false;
		}
		else if (!ontology.equals(other.ontology)) return false;
		if (termAccession == null)
		{
			if (other.termAccession != null) return false;
		}
		else if (!termAccession.equals(other.termAccession)) return false;
		return true;
	}

}
