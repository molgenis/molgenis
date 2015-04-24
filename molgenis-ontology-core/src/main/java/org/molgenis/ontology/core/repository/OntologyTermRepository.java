package org.molgenis.ontology.core.repository;

import static org.molgenis.data.support.QueryImpl.IN;
import static org.molgenis.ontology.core.meta.OntologyTermMetaData.ENTITY_NAME;
import static org.molgenis.ontology.core.meta.OntologyTermMetaData.ONTOLOGY;
import static org.molgenis.ontology.core.meta.OntologyTermMetaData.ONTOLOGY_TERM_IRI;
import static org.molgenis.ontology.core.meta.OntologyTermMetaData.ONTOLOGY_TERM_NAME;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.collect.Iterables;
import org.elasticsearch.common.collect.Lists;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.core.meta.OntologyTermMetaData;
import org.molgenis.ontology.core.meta.OntologyTermNodePathMetaData;
import org.molgenis.ontology.core.meta.OntologyTermSynonymMetaData;
import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.FluentIterable;

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
		return Lists.newArrayList(Iterables.transform(termEntities, OntologyTermRepository::toOntologyTerm));
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

	/**
	 * Calculate the distance between any two ontology terms in the ontology tree structure by calculating the
	 * difference in nodePaths.
	 * 
	 * @param ontologyTerm1
	 * @param ontologyTerm2
	 * 
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
		Entity ontologyTermEntity = dataService.findOne(ENTITY_NAME,
				new QueryImpl().eq(ONTOLOGY_TERM_IRI, ontologyTerm.getIRI()));

		Iterable<Entity> ontologyTermNodePathEntities = ontologyTermEntity
				.getEntities(OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH);

		for (Entity ontologyTermNodePathEntity : ontologyTermNodePathEntities)
		{
			return ontologyTermNodePathEntity.getString(OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH);
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
		Entity ontologyTermEntity = dataService.findOne(ENTITY_NAME,
				QueryImpl.EQ(ONTOLOGY_TERM_IRI, ontologyTerm.getIRI()));

		List<OntologyTerm> children = new ArrayList<OntologyTerm>();
		ontologyTermEntity.getEntities(OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH).forEach(
				ontologyTermNodePathEntity -> children
						.addAll(getChildOntologyTermsByNodePath(ontologyTermNodePathEntity)));
		return children;
	}

	public List<OntologyTerm> getChildOntologyTermsByNodePath(Entity nodePathEntity)
	{
		String nodePath = nodePathEntity.getString(OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH);

		Iterable<Entity> allNodePathEntities = dataService.findAll(OntologyTermNodePathMetaData.ENTITY_NAME,
				new QueryImpl().like(OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH, nodePath));

		List<Entity> childNodePathEntities = FluentIterable.from(allNodePathEntities)
				.filter(entity -> qualifiedNodePath(nodePath, entity)).toList();

		Iterable<Entity> childOntologyTermEntities = dataService.findAll(OntologyTermMetaData.ENTITY_NAME,
				new QueryImpl().in(OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH, childNodePathEntities));

		return Lists.newArrayList(Iterables
				.transform(childOntologyTermEntities, OntologyTermRepository::toOntologyTerm));
	}

	private boolean qualifiedNodePath(String nodePath, Entity entity)
	{
		String childNodePath = entity.getString(OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH);
		return !StringUtils.equals(nodePath, childNodePath) && childNodePath.contains(nodePath);
	}

	private static OntologyTerm toOntologyTerm(Entity entity)
	{
		if (entity == null)
		{
			return null;
		}

		// Collect synonyms if there are any
		List<String> synonyms = new ArrayList<String>();
		Iterable<Entity> ontologyTermSynonymEntities = entity.getEntities(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM);
		if (ontologyTermSynonymEntities != null)
		{
			ontologyTermSynonymEntities.forEach(synonymEntity -> synonyms.add(synonymEntity
					.getString(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM)));
		}
		if (!synonyms.contains(entity.getString(ONTOLOGY_TERM_NAME)))
		{
			synonyms.add(entity.getString(ONTOLOGY_TERM_NAME));
		}

		return OntologyTerm.create(entity.getString(ONTOLOGY_TERM_IRI), entity.getString(ONTOLOGY_TERM_NAME), null,
				synonyms);
	}
}
