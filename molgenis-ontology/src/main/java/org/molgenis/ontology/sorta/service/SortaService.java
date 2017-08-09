package org.molgenis.ontology.sorta.service;

import org.molgenis.data.Entity;

public interface SortaService
{
	/**
	 * Get all ontology entities in generic type
	 *
	 * @return list of untyped ontology entities
	 */
	Iterable<Entity> getAllOntologyEntities();

	/**
	 * Get a specified ontology entity in generic type based on the given ontologyIri
	 *
	 * @param ontologyIri
	 * @return specified ontology entity in generic type
	 */
	Entity getOntologyEntity(String ontologyIri);

	/**
	 * Get a specified ontologyterm in generic type based on the given ontologyIri and ontologyTermIri
	 *
	 * @param ontologyTermIri
	 * @param ontologyIri
	 * @return specifieid ontologyterm entity in a generic type
	 */
	Entity getOntologyTermEntity(String ontologyTermIri, String ontologyIri);

	/**
	 * Find a list of relevant ontologyterm typed entities using lexical matching (elasticsearch + ngram) in generic
	 * type based on given ontologyIri and a set of query inputs (name, synonym, ontology database id, e.g. hpo, omim)
	 *
	 * @param ontologyIri
	 * @param inputEntity
	 * @return a list of ontologyterm entities in generic type
	 */
	Iterable<Entity> findOntologyTermEntities(String ontologyIri, Entity inputEntity);

}