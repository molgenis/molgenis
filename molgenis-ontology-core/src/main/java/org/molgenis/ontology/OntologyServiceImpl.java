package org.molgenis.ontology;

import java.util.List;

import org.elasticsearch.common.collect.Lists;
import org.molgenis.ontology.repository.OntologyRepository;
import org.molgenis.ontology.repository.OntologyTermRepository;
import org.molgenis.ontology.repository.model.Ontology;
import org.molgenis.ontology.repository.model.OntologyTerm;

public class OntologyServiceImpl implements OntologyService
{
	private OntologyRepository ontologyRepository;

	private OntologyTermRepository ontologyTermRepository;

	@Override
	public List<Ontology> getOntologies()
	{
		return Lists.newArrayList(ontologyRepository.getOntologies());
	}

	@Override
	public Ontology getOntology(String name)
	{
		return ontologyRepository.getOntology(name);
	}

	@Override
	public OntologyTerm findOntologyTerm(List<Ontology> ontologies, String search)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OntologyTerm getOntologyTerm(String ontology, String iri)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
