package org.molgenis.ontology.core.repository;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.*;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.core.meta.OntologyMetaData;
import org.molgenis.ontology.core.meta.OntologyTermMetaData;
import org.molgenis.ontology.core.meta.OntologyTermNodePathMetaData;
import org.molgenis.ontology.core.meta.OntologyTermSynonymMetaData;
import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.molgenis.ontology.core.meta.OntologyTermMetaData.*;

/**
 * Maps {@link OntologyTermMetaData} {@link Entity} <-> {@link OntologyTerm}
 */
public class OntologyTermRepository
{
	private final DataService dataService;

	@Autowired
	public OntologyTermRepository(DataService dataService)
	{
		this.dataService = requireNonNull(dataService);
	}

	/**
	 * FIXME write docs
	 *
	 * @param term
	 * @param pageSize
	 * @return
	 */
	public List<OntologyTerm> findOntologyTerms(String term, int pageSize)
	{
		Iterable<Entity> ontologyTermEntities;

		// #1 find exact match
		Query<Entity> termNameQuery = new QueryImpl<>().eq(OntologyTermMetaData.ONTOLOGY_TERM_NAME, term)
													   .pageSize(pageSize);
		ontologyTermEntities = () -> dataService.findAll(ONTOLOGY_TERM, termNameQuery).iterator();

		if (!ontologyTermEntities.iterator().hasNext())
		{
			Query<Entity> termsQuery = new QueryImpl<>().search(term).pageSize(pageSize);
			ontologyTermEntities = () -> dataService.findAll(ONTOLOGY_TERM, termsQuery).iterator();
		}
		return Lists.newArrayList(Iterables.transform(ontologyTermEntities, OntologyTermRepository::toOntologyTerm));
	}

	/**
	 * Finds exact {@link OntologyTerm}s within {@link Ontology}s.
	 *
	 * @param ontologyIds IDs of the {@link Ontology}s to search in
	 * @param terms       {@link List} of search terms. the {@link OntologyTerm} must match at least one of these terms
	 * @param pageSize    max number of results
	 * @return {@link List} of {@link OntologyTerm}s
	 */
	public List<OntologyTerm> findExcatOntologyTerms(List<String> ontologyIds, Set<String> terms, int pageSize)
	{
		List<OntologyTerm> findOntologyTerms = findOntologyTerms(ontologyIds, terms, pageSize);
		return findOntologyTerms.stream()
								.filter(ontologyTerm -> isOntologyTermExactMatch(terms, ontologyTerm))
								.collect(Collectors.toList());
	}

	private boolean isOntologyTermExactMatch(Set<String> terms, OntologyTerm ontologyTerm)
	{
		Set<String> lowerCaseSearchTerms = terms.stream().map(StringUtils::lowerCase).collect(Collectors.toSet());
		for (String synonym : ontologyTerm.getSynonyms())
		{
			if (lowerCaseSearchTerms.contains(synonym.toLowerCase()))
			{
				return true;
			}
		}
		if (lowerCaseSearchTerms.contains(ontologyTerm.getLabel().toLowerCase()))
		{
			return true;
		}
		return false;
	}

	/**
	 * Finds {@link OntologyTerm}s within {@link Ontology}s.
	 *
	 * @param ontologyIds IDs of the {@link Ontology}s to search in
	 * @param terms       {@link List} of search terms. the {@link OntologyTerm} must match at least one of these terms
	 * @param pageSize    max number of results
	 * @return {@link List} of {@link OntologyTerm}s
	 */
	public List<OntologyTerm> findOntologyTerms(List<String> ontologyIds, Set<String> terms, int pageSize)
	{
		List<QueryRule> rules = new ArrayList<>();
		for (String term : terms)
		{
			if (rules.size() > 0)
			{
				rules.add(new QueryRule(Operator.OR));
			}
			rules.add(new QueryRule(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM, Operator.FUZZY_MATCH, term));
		}
		rules = Arrays.asList(new QueryRule(ONTOLOGY, Operator.IN, ontologyIds), new QueryRule(Operator.AND),
				new QueryRule(rules));

		final List<QueryRule> finalRules = rules;
		Iterable<Entity> termEntities = () -> dataService.findAll(ONTOLOGY_TERM, new QueryImpl<>(finalRules).pageSize(pageSize))
												 .iterator();

		return Lists.newArrayList(Iterables.transform(termEntities, OntologyTermRepository::toOntologyTerm));
	}

	public List<OntologyTerm> getAllOntologyTerms(String ontologyId)
	{
		Entity ontologyEntity = dataService.findOne(OntologyMetaData.ONTOLOGY,
				new QueryImpl<>().eq(OntologyMetaData.ONTOLOGY_IRI, ontologyId));

		if (ontologyEntity != null)
		{
			Iterable<Entity> ontologyTermEntities = () -> dataService.findAll(OntologyTermMetaData.ONTOLOGY_TERM,
					new QueryImpl<>().eq(OntologyTermMetaData.ONTOLOGY, ontologyEntity)
									 .pageSize(Integer.MAX_VALUE)).iterator();

			return Lists.newArrayList(
					Iterables.transform(ontologyTermEntities, OntologyTermRepository::toOntologyTerm));
		}

		return Collections.emptyList();
	}

	/**
	 * Retrieves an {@link OntologyTerm} for one or more IRIs
	 *
	 * @param iris Array of {@link OntologyTerm} IRIs
	 * @return combined {@link OntologyTerm} for the iris.
	 */
	public OntologyTerm getOntologyTerm(String[] iris)
	{
		List<OntologyTerm> ontologyTerms = Lists.newArrayList();
		for (String iri : iris)
		{
			OntologyTerm ontologyTerm = toOntologyTerm(
					dataService.findOne(ONTOLOGY_TERM, QueryImpl.EQ(ONTOLOGY_TERM_IRI, iri)));
			if (ontologyTerm == null)
			{
				return null;
			}
			ontologyTerms.add(ontologyTerm);
		}
		return OntologyTerm.and(ontologyTerms.toArray(new OntologyTerm[0]));
	}

	/**
	 * Calculate the distance between any two ontology terms in the ontology tree structure by calculating the
	 * difference in nodePaths.
	 *
	 * @param ontologyTerm1
	 * @param ontologyTerm2
	 * @return the distance between two ontology terms
	 */
	public int getOntologyTermDistance(OntologyTerm ontologyTerm1, OntologyTerm ontologyTerm2)
	{
		String nodePath1 = getOntologyTermNodePath(ontologyTerm1);
		String nodePath2 = getOntologyTermNodePath(ontologyTerm2);

		if (StringUtils.isEmpty(nodePath1))
		{
			throw new MolgenisDataAccessException("The nodePath cannot be null : " + ontologyTerm1.toString());
		}

		if (StringUtils.isEmpty(nodePath2))
		{
			throw new MolgenisDataAccessException("The nodePath cannot be null : " + ontologyTerm2.toString());
		}

		return calculateNodePathDistance(nodePath1, nodePath2);
	}

	private String getOntologyTermNodePath(OntologyTerm ontologyTerm)
	{
		Entity ontologyTermEntity = dataService.findOne(ONTOLOGY_TERM,
				new QueryImpl<>().eq(ONTOLOGY_TERM_IRI, ontologyTerm.getIRI()));

		Iterable<Entity> ontologyTermNodePathEntities = ontologyTermEntity.getEntities(
				OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH);

		for (Entity ontologyTermNodePathEntity : ontologyTermNodePathEntities)
		{
			return ontologyTermNodePathEntity.getString(OntologyTermNodePathMetaData.NODE_PATH);
		}

		return null;
	}

	/**
	 * Calculate the distance between nodePaths, e.g. 0[0].1[1].2[2], 0[0].2[1].2[2]. The distance is the non-overlap
	 * part of the strings
	 *
	 * @param nodePath1
	 * @param nodePath2
	 * @return distance
	 */
	public int calculateNodePathDistance(String nodePath1, String nodePath2)
	{
		String[] nodePathFragment1 = nodePath1.split("\\.");
		String[] nodePathFragment2 = nodePath2.split("\\.");

		int overlapBlock = 0;
		while (overlapBlock < nodePathFragment1.length && overlapBlock < nodePathFragment2.length
				&& nodePathFragment1[overlapBlock].equals(nodePathFragment2[overlapBlock]))
		{
			overlapBlock++;
		}

		return nodePathFragment1.length + nodePathFragment2.length - overlapBlock * 2;
	}

	/**
	 * Retrieve all descendant ontology terms
	 *
	 * @param ontologyTerm
	 * @return a list of {@link OntologyTerm}
	 */
	public List<OntologyTerm> getChildren(OntologyTerm ontologyTerm)
	{
		Iterable<Entity> ontologyTermEntities = () -> dataService.findAll(ONTOLOGY_TERM, QueryImpl.EQ(ONTOLOGY_TERM_IRI, ontologyTerm.getIRI()))
														 .iterator();

		List<OntologyTerm> children = new ArrayList<>();
		for (Entity ontologyTermEntity : ontologyTermEntities)
		{
			Entity ontologyEntity = ontologyTermEntity.getEntity(OntologyTermMetaData.ONTOLOGY);
			ontologyTermEntity.getEntities(OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH)
							  .forEach(ontologyTermNodePathEntity -> children.addAll(
									  getChildOntologyTermsByNodePath(ontologyEntity, ontologyTermNodePathEntity)));
		}
		return children;
	}

	public List<OntologyTerm> getChildOntologyTermsByNodePath(Entity ontologyEntity, Entity nodePathEntity)
	{
		String nodePath = nodePathEntity.getString(OntologyTermNodePathMetaData.NODE_PATH);

		Iterable<Entity> relatedOntologyTermEntities = () -> dataService.findAll(OntologyTermMetaData.ONTOLOGY_TERM, new QueryImpl<>(
				new QueryRule(OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH, Operator.FUZZY_MATCH,
						"\"" + nodePath + "\"")).and().eq(OntologyTermMetaData.ONTOLOGY, ontologyEntity))
																.iterator();
		Iterable<Entity> childOntologyTermEntities = FluentIterable.from(relatedOntologyTermEntities)
																   .filter(entity -> qualifiedNodePath(nodePath,
																		   entity))
																   .toList();

		return Lists.newArrayList(
				Iterables.transform(childOntologyTermEntities, OntologyTermRepository::toOntologyTerm));
	}

	private boolean qualifiedNodePath(String nodePath, Entity entity)
	{
		Iterable<Entity> nodePathEntities = entity.getEntities(OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH);
		return Lists.newArrayList(nodePathEntities).stream().anyMatch(nodePathEntity ->
		{
			String childNodePath = nodePathEntity.getString(OntologyTermNodePathMetaData.NODE_PATH);
			return !StringUtils.equals(nodePath, childNodePath) && childNodePath.startsWith(nodePath);
		});
	}

	private static OntologyTerm toOntologyTerm(Entity entity)
	{
		if (entity == null)
		{
			return null;
		}

		// Collect synonyms if there are any
		List<String> synonyms = new ArrayList<>();
		Iterable<Entity> ontologyTermSynonymEntities = entity.getEntities(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM);
		if (ontologyTermSynonymEntities != null)
		{
			ontologyTermSynonymEntities.forEach(synonymEntity -> synonyms.add(
					synonymEntity.getString(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM_ATTR)));
		}
		if (!synonyms.contains(entity.getString(ONTOLOGY_TERM_NAME)))
		{
			synonyms.add(entity.getString(ONTOLOGY_TERM_NAME));
		}

		return OntologyTerm.create(entity.getString(ONTOLOGY_TERM_IRI), entity.getString(ONTOLOGY_TERM_NAME), null,
				synonyms);
	}
}
