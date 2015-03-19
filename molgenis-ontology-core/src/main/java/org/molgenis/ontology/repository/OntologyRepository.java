package org.molgenis.ontology.repository;

import static org.molgenis.ontology.model.OntologyMetaData.ID;
import static org.molgenis.ontology.model.OntologyMetaData.ONTOLOGY_IRI;
import static org.molgenis.ontology.model.OntologyMetaData.ONTOLOGY_NAME;

import org.elasticsearch.common.collect.Iterables;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.ontology.model.OntologyMetaData;
import org.molgenis.ontology.repository.model.Ontology;
import org.springframework.beans.factory.annotation.Autowired;

public class OntologyRepository
{
	@Autowired
	private DataService dataService;

	public Iterable<Ontology> getOntologies()
	{
		return Iterables.transform(dataService.findAll(OntologyMetaData.ENTITY_NAME), OntologyRepository::toOntology);
	}

	private static Ontology toOntology(Entity entity)
	{
		return Ontology.create(entity.getString(ID), entity.getString(ONTOLOGY_IRI), entity.getString(ONTOLOGY_NAME));
	}

	public Ontology getOntology(String name)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
