package org.molgenis.ontology.core.repository;

import static org.molgenis.data.support.QueryImpl.IN;
import static org.molgenis.ontology.core.meta.OntologyTermMetaData.ENTITY_NAME;
import static org.molgenis.ontology.core.meta.OntologyTermMetaData.ONTOLOGY;
import static org.molgenis.ontology.core.meta.OntologyTermMetaData.ONTOLOGY_TERM_IRI;
import static org.molgenis.ontology.core.meta.OntologyTermMetaData.ONTOLOGY_TERM_NAME;

import java.util.List;
import java.util.Set;

import org.elasticsearch.common.collect.Iterables;
import org.elasticsearch.common.collect.Lists;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.core.meta.OntologyTermMetaData;
import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Maps {@link OntologyTermMetaData} {@link Entity} <-> {@link OntologyTerm}
 */
public class OntologyTermRepository
{
	@Autowired
	private DataService dataService;

	/**
	 * Finds {@link OntologyTerm}s within {@link Ontology}s.
	 * 
	 * @param ontologyIds
	 *            IDs of the {@link Ontology}s to search in
	 * @param terms
	 *            {@link List} of search terms. the {@link OntologyTerm} must match at least one of these terms
	 * @param pageSize
	 *            max number of results
	 * @return {@link List} of {@link OntologyTerm}s
	 */
	public List<OntologyTerm> findOntologyTerms(List<String> ontologyIds, Set<String> terms, int pageSize)
	{
		Query termsQuery = IN(ONTOLOGY, ontologyIds).pageSize(pageSize).and().nest();
		int counter = 0;
		for (String term : terms)
		{
			counter = counter + 1;
			if (counter < terms.size())
			{
				termsQuery = termsQuery.search(term).or();
			}
			else
			{
				termsQuery = termsQuery.search(term);
			}
		}

		Iterable<Entity> termEntities = dataService.findAll(ENTITY_NAME, termsQuery.unnest());
		List<Entity> termEntitiesList = Lists.<Entity> newArrayList(termEntities);

		if (termEntitiesList.size() > 3)
		{
			return Lists.newArrayList(Iterables.transform(termEntitiesList.subList(0, 3),
					OntologyTermRepository::toOntologyTerm));
		}
		else
		{
			return Lists.newArrayList(Iterables.transform(termEntitiesList, OntologyTermRepository::toOntologyTerm));
		}

		// return Lists.newArrayList(Iterables.transform(Iterables.get(termEntities, 3),
		// OntologyTermRepository::toOntologyTerm));
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
			ontologyTerms.add(ontologyTerm);
		}
		return OntologyTerm.and(ontologyTerms.toArray(new OntologyTerm[0]));
	}

	private static OntologyTerm toOntologyTerm(Entity entity)
	{
		if (entity == null)
		{
			return null;
		}
		return OntologyTerm.create(entity.getString(ONTOLOGY_TERM_IRI), entity.getString(ONTOLOGY_TERM_NAME));
	}
}
