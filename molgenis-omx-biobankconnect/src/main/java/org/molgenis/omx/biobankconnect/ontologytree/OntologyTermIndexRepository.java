package org.molgenis.omx.biobankconnect.ontologytree;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.omx.biobankconnect.utils.OntologyTermRepository;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;

public class OntologyTermIndexRepository extends AbstractOntologyIndexRepository
{
	public final static String DEFAULT_ONTOLOGY_TERM_REPO = "ontologytermindex";
	private final static String BASE_URL = "ontologytermindex://";
	private final OntologyTermRepository ontologyTermRepository;
	private final String ontologyUrl;
	{
		attributeMap.put("name", OntologyTermRepository.ONTOLOGY_TERM_IRI);
		attributeMap.put("label", OntologyTermRepository.ONTOLOGY_TERM);
		attributeMap.put("description", OntologyTermRepository.ONTOLOGY_TERM_DEFINITION);
		attributeMap.put("ontologyUrl", OntologyTermRepository.ONTOLOGY_IRI);
	}

	@Autowired
	public OntologyTermIndexRepository(String entityName, String ontologyUrl, SearchService searchService)
	{
		super(entityName, searchService);
		this.ontologyUrl = ontologyUrl;
		this.ontologyTermRepository = new OntologyTermRepository(null, entityName);
	}

	@Override
	public Iterable<Entity> findAll(Query q)
	{
		List<Entity> entities = new ArrayList<Entity>();
		if (q.getRules().size() > 0) q.and();
		q.eq(OntologyTermRepository.ENTITY_TYPE, OntologyTermRepository.TYPE_ONTOLOGYTERM);
		for (Hit hit : searchService.search(new SearchRequest(null, mapAttribute(q), null)).getSearchHits())
		{
			String id = hit.getId();
			int hashCode = id.hashCode();
			if (!identifierMap.containsKey(hashCode))
			{
				identifierMap.put(hashCode, id);
			}
			entities.add(new OntologyTermIndexEntity(hit, getEntityMetaData(), identifierMap, searchService));
		}
		return entities;
	}

	@Override
	public Entity findOne(Query q)
	{
		Hit hit = findOneInternal(q);
		if (hit != null) return new OntologyTermIndexEntity(hit, getEntityMetaData(), identifierMap, searchService);
		return null;
	}

	@Override
	public Entity findOne(Integer id)
	{
		Hit hit = findOneInternal(id);
		if (hit != null) return new OntologyTermIndexEntity(hit, getEntityMetaData(), identifierMap, searchService);
		return null;
	}

	@Override
	public long count(Query q)
	{
		String documentType = "ontologyTerm-" + ontologyUrl;
		SearchResult result = searchService.search(new SearchRequest(documentType, q, null));

		return result.getTotalHitCount();
	}

	@Override
	public String getUrl()
	{
		return BASE_URL + ontologyTermRepository.getName();
	}
}
