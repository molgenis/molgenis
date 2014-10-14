package org.molgenis.ontology.tree;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.collect.Iterables;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.semantic.Ontology;
import org.molgenis.data.semantic.OntologyService;
import org.molgenis.data.semantic.OntologyTerm;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.repository.OntologyTermIndexRepository;
import org.molgenis.ontology.repository.OntologyTermQueryRepository;

public class OntologyTermEntity extends AbstractSemanticEntity implements OntologyTerm
{
	private static final long serialVersionUID = 1L;
	private BigDecimal score = null;

	public OntologyTermEntity(Entity entity, EntityMetaData entityMetaData, SearchService searchService,
			DataService dataService, OntologyService ontologyService)
	{
		super(entity, entityMetaData, searchService, dataService, ontologyService);
	}

	@Override
	public Object get(String attributeName)
	{
		if (attributeName.equalsIgnoreCase(OntologyTermQueryRepository.FIELDTYPE))
		{
			Iterable<Entity> listOfOntologyTerms = searchService.search(new QueryImpl().eq(
					OntologyTermQueryRepository.ENTITY_TYPE, OntologyTermQueryRepository.TYPE_ONTOLOGYTERM),
					entityMetaData);
			return Iterables.size(listOfOntologyTerms) == 0 ? MolgenisFieldTypes.STRING.toString().toUpperCase() : MolgenisFieldTypes.COMPOUND
					.toString().toUpperCase();
		}

		if (attributeName.equalsIgnoreCase("attributes"))
		{
			if (!Boolean.parseBoolean(entity.getString(OntologyTermQueryRepository.LAST)))
			{
				String currentNodePath = entity.getString(OntologyTermQueryRepository.NODE_PATH);
				String currentOntologyTermIri = entity.getString(OntologyTermQueryRepository.ONTOLOGY_TERM_IRI);
				Query q = new QueryImpl().eq(OntologyTermIndexRepository.PARENT_NODE_PATH, currentNodePath).and()
						.eq(OntologyTermIndexRepository.PARENT_ONTOLOGY_TERM_IRI, currentOntologyTermIri)
						.pageSize(Integer.MAX_VALUE);
				return searchService.search(q, entityMetaData);
			}
		}

		return entity.get(attributeName);
	}

	@Override
	public String getIRI()
	{
		return getValueInternal(OntologyTermQueryRepository.ONTOLOGY_TERM_IRI);
	}

	@Override
	public String getLabel()
	{
		return getValueInternal(OntologyTermQueryRepository.ONTOLOGY_TERM);
	}

	@Override
	public String getDescription()
	{
		return getValueInternal(OntologyTermQueryRepository.ONTOLOGY_TERM_DEFINITION);
	}

	@Override
	public String getTermAccession()
	{
		return StringUtils.EMPTY;
	}

	@Override
	public Set<String> getSynonyms()
	{
		Set<String> synonyms = new HashSet<String>();
		synonyms.add(entity.getString(OntologyTermQueryRepository.SYNONYMS));
		Iterable<Entity> entities = searchService.search(
				new QueryImpl().eq(OntologyTermQueryRepository.ONTOLOGY_TERM_IRI, getIRI()), entityMetaData);
		for (Entity entity : entities)
		{
			synonyms.add(entity.getString(OntologyTermQueryRepository.SYNONYMS));
		}
		return synonyms;
	}

	@Override
	public Ontology getOntology()
	{
		return ontologyService.getOntology(getValueInternal(OntologyTermQueryRepository.ONTOLOGY_IRI));
	}

	public BigDecimal getScore()
	{
		return score;
	}

	public void setScore(BigDecimal score)
	{
		this.score = score;
	}
}