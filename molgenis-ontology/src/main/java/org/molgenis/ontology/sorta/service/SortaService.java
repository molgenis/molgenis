package org.molgenis.ontology.sorta.service;

import java.util.List;

import org.molgenis.data.Entity;
import org.molgenis.ontology.sorta.bean.SortaHit;

public interface SortaService
{
	/**
	 * Find a list of relevant ontologyterm entities using lexical matching (elasticsearch + ngram) in generic type
	 * based on given ontologyIri and queryString
	 * 
	 * @param ontologyUrl
	 * @param queryString
	 * @return a list of ontologyterm entities in generic type
	 */
	List<SortaHit> findOntologyTermEntities(String ontologyUrl, String queryString);

	/**
	 * Find a list of relevant ontologyterm typed entities using lexical matching (elasticsearch + ngram) in generic
	 * type based on given ontologyIri and a set of query inputs (name, synonym, ontology database id, e.g. hpo, omim)
	 * 
	 * @param ontologyIri
	 * @param inputEntity
	 * @return a list of ontologyterm entities in generic type
	 */
	List<SortaHit> findOntologyTermEntities(String ontologyIri, Entity inputEntity);

}