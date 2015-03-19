package org.molgenis.ontology.repository;

import org.molgenis.data.DataService;
import org.molgenis.ontology.repository.model.Ontology;

public class OntologyRepository
{
	private DataService dataService;

	public Iterable<Ontology> getOntologies()
	{
		return dataService.findAll();
	}

	public Ontology getOntology(String name)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
