package org.molgenis.ontology.beans;

import java.util.Collections;
import java.util.Set;

import org.molgenis.data.Entity;
import org.molgenis.data.semantic.Ontology;
import org.molgenis.data.semantic.OntologyService;
import org.molgenis.data.semantic.OntologyTerm;
import org.molgenis.ontology.repository.OntologyTermQueryRepository;

public class OntologyTermImpl implements OntologyTerm
{
	private final Entity entity;
	private final OntologyService ontologyService;

	public OntologyTermImpl(Entity entity, OntologyService ontologyService)
	{
		this.entity = entity;
		this.ontologyService = ontologyService;
	}

	@Override
	public String getIRI()
	{
		return entity.getString(OntologyTermQueryRepository.ONTOLOGY_TERM_IRI);
	}

	@Override
	public String getLabel()
	{
		return entity.getString(OntologyTermQueryRepository.ONTOLOGY_TERM);
	}

	@Override
	public String getDescription()
	{
		return entity.getString(OntologyTermQueryRepository.ONTOLOGY_TERM_DEFINITION);
	}

	@Override
	public String getTermAccession()
	{
		return entity.getString(OntologyTermQueryRepository.ID);
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
		return ontologyService.getOntology(entity.getString(OntologyTermQueryRepository.ONTOLOGY_IRI));
	}
}
