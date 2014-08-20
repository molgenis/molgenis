package org.molgenis.omx.biobankconnect.ontology.repository;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.biobankconnect.ontologyservice.OntologyService;
import org.molgenis.omx.biobankconnect.ontologytree.OntologyEntity;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;

public class OntologyQueryRepository extends AbstractOntologyQueryRepository
{
	public final static String DEFAULT_ONTOLOGY_REPO = "ontologyindex";
	private final static String BASE_URL = "ontologyindex://";
	private final OntologyService ontologySerivce;

	@Autowired
	public OntologyQueryRepository(String entityName, OntologyService ontologyService, SearchService searchService)
	{
		super(entityName, searchService);
		this.ontologySerivce = ontologyService;
	}

	@Override
	public Iterable<Entity> findAll(Query q)
	{
		List<Entity> entities = new ArrayList<Entity>();
		if (q.getRules().size() > 0) q.and();
		q.eq(OntologyIndexRepository.ENTITY_TYPE, OntologyIndexRepository.TYPE_ONTOLOGY);
		for (Hit hit : searchService.search(new SearchRequest(null, q, null)).getSearchHits())
		{
			entities.add(new OntologyEntity(hit, getEntityMetaData(), ontologySerivce, searchService));
		}
		return entities;
	}

	@Override
	public Entity findOne(Query q)
	{
		if (q.getRules().size() > 0) q.and();
		q.eq(OntologyIndexRepository.ENTITY_TYPE, OntologyIndexRepository.TYPE_ONTOLOGY);
		Hit hit = findOneInternal(null, q);
		if (hit != null) return new OntologyEntity(hit, getEntityMetaData(), ontologySerivce, searchService);
		return null;
	}

	@Override
	public Entity findOne(Object id)
	{
		Hit hit = searchService.searchById(null, id.toString());
		if (hit != null) return new OntologyEntity(hit, getEntityMetaData(), ontologySerivce, searchService);
		return null;
	}

	@Override
	public Query query()
	{
		return new QueryImpl(this);
	}

	@Override
	public long count(Query q)
	{
		if (q.getRules().size() > 0)
		{
			q.and();
		}
		q.eq(OntologyIndexRepository.ENTITY_TYPE, OntologyIndexRepository.TYPE_ONTOLOGY);
		return searchService.count(null, q.pageSize(Integer.MAX_VALUE).offset(Integer.MIN_VALUE));
	}

	@Override
	public String getUrl()
	{
		return BASE_URL + entityName;
	}
}
