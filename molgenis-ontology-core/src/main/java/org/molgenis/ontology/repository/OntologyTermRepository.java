package org.molgenis.ontology.repository;

import static org.molgenis.data.support.QueryImpl.IN;
import static org.molgenis.ontology.model.OntologyTermMetaData.ENTITY_NAME;
import static org.molgenis.ontology.model.OntologyTermMetaData.ONTOLOGY;
import static org.molgenis.ontology.model.OntologyTermMetaData.ONTOLOGY_TERM_IRI;
import static org.molgenis.ontology.model.OntologyTermMetaData.ONTOLOGY_TERM_NAME;

import java.util.List;

import org.elasticsearch.common.collect.Iterables;
import org.elasticsearch.common.collect.Lists;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.model.OntologyTermMetaData;
import org.molgenis.ontology.repository.model.OntologyTerm;

/**
 * Maps {@link OntologyTermMetaData} {@link Entity} <-> {@link OntologyTerm}
 */
public class OntologyTermRepository
{
	private DataService dataService;

	/**
	 * Finds
	 * 
	 * @param ontologyIds
	 * @param search
	 * @param pageSize
	 * @return
	 */
	public List<OntologyTerm> findOntologyTerms(List<String> ontologyIds, String search, int pageSize)
	{
		Iterable<Entity> termEntities = dataService.findAll(ENTITY_NAME,
				new QueryImpl(IN(ONTOLOGY, ontologyIds)).setPageSize(pageSize));
		return Lists.newArrayList(Iterables.transform(termEntities, OntologyTermRepository::toOntologyTerm));
	}

	private static OntologyTerm toOntologyTerm(Entity entity)
	{
		if (entity == null)
		{
			return null;
		}
		return OntologyTerm.create(entity.getString(ONTOLOGY_TERM_IRI), entity.getString(ONTOLOGY_TERM_NAME));
	}

	/**
	 * Retrieves an {@link OntologyTerm} for one or more IRIs
	 * 
	 * @param iris
	 *            Array of {@link OntologyTerm} IRIs
	 * @return combined {@link OntologyTerm} for the iris.
	 */
	public OntologyTerm getOntologyTerm(String[] iris)
	{
		List<OntologyTerm> ontologyTerms = Lists.newArrayList();
		for (String iri : iris)
		{
			OntologyTerm ontologyTerm = toOntologyTerm(dataService.findOne(ENTITY_NAME,
					QueryImpl.EQ(ONTOLOGY_TERM_IRI, iri)));
			if (ontologyTerm == null)
			{
				return null;
			}

		}
		return OntologyTerm.and(ontologyTerms.toArray(new OntologyTerm[0]));
	}
}
