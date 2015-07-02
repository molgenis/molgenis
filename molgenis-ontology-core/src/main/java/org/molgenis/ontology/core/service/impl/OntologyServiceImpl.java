package org.molgenis.ontology.core.service.impl;

import java.util.List;
import java.util.Set;

import org.elasticsearch.common.collect.Lists;
import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.repository.OntologyRepository;
import org.molgenis.ontology.core.repository.OntologyTermRepository;
import org.molgenis.ontology.core.service.OntologyService;
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
	public OntologyTerm getOntologyTerm(String iri)
	{
		return ontologyTermRepository.getOntologyTerm(iri.split(","));

	}

	@Override
	public List<OntologyTerm> findOntologyTerms(List<String> ontologyIds, Set<String> terms, int pageSize)
	{
		return ontologyTermRepository.findOntologyTerms(ontologyIds, terms, pageSize);
	}

	@Override
	public List<OntologyTerm> getChildren(OntologyTerm ontologyTerm)
	{
		return ontologyTermRepository.getChildren(ontologyTerm);
	}

	@Override
	public Integer getOntologyTermDistance(OntologyTerm ontologyTerm1, OntologyTerm ontologyTerm2)
	{
		return ontologyTermRepository.getOntologyTermDistance(ontologyTerm1, ontologyTerm2);
	}
}