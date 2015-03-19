package org.molgenis.ontology;

import java.util.List;

import org.elasticsearch.common.collect.Lists;
import org.molgenis.ontology.repository.OntologyRepository;
import org.molgenis.ontology.repository.OntologyTermRepository;
import org.molgenis.ontology.repository.model.Ontology;
import org.molgenis.ontology.repository.model.OntologyTerm;
import org.springframework.beans.factory.annotation.Autowired;

public class OntologyServiceImpl implements OntologyService
{
	@Autowired
	private OntologyRepository ontologyRepository;

	@Autowired
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
	public List<OntologyTerm> findOntologyTerms(List<Ontology> ontologies, String search)
	{
		return ontologyTermRepository.findOntologyTerms(ontologies, search);
	}

	@Override
	public OntologyTerm getOntologyTerm(String ontology, String iri)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
