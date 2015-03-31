package org.molgenis.ontology.core.repository;

import static org.molgenis.ontology.core.meta.OntologyMetaData.ENTITY_NAME;
import static org.molgenis.ontology.core.meta.OntologyMetaData.ID;
import static org.molgenis.ontology.core.meta.OntologyMetaData.ONTOLOGY_IRI;
import static org.molgenis.ontology.core.meta.OntologyMetaData.ONTOLOGY_NAME;

import org.elasticsearch.common.collect.Iterables;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.core.model.Ontology;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Maps OntologyMetaData {@link Entity} <-> {@link Ontology}
 * 
 */
public class OntologyRepository
{
	@Autowired
	private DataService dataService;

	/**
	 * Retrieves all {@link Ontology}s.
	 */
	public Iterable<Ontology> getOntologies()
	{
		return Iterables.transform(dataService.findAll(ENTITY_NAME), OntologyRepository::toOntology);
	}

	/**
	 * Retrieves an ontology with a specific IRI.
	 * 
	 * @param IRI
	 *            the IRI of the ontology
	 * @return
	 */
	public Ontology getOntology(String IRI)
	{
		return toOntology(dataService.findOne(ENTITY_NAME, QueryImpl.EQ(ONTOLOGY_IRI, IRI)));
	}

	private static Ontology toOntology(Entity entity)
	{
		if (entity == null)
		{
			return null;
		}
		return Ontology.create(entity.getString(ID), entity.getString(ONTOLOGY_IRI), entity.getString(ONTOLOGY_NAME));
	}

}
