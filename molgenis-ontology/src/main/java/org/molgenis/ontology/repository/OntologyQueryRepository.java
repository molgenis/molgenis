package org.molgenis.ontology.repository;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.semantic.OntologyService;
import org.molgenis.data.support.QueryImpl;
import org.springframework.beans.factory.annotation.Autowired;

public class OntologyQueryRepository extends AbstractOntologyQueryRepository
{
	public final static String DEFAULT_ONTOLOGY_REPO = "ontologyindex";
	private final static String BASE_URL = "ontologyindex://";

	@Autowired
	public OntologyQueryRepository(String entityName, OntologyService ontologyService, SearchService searchService,
			DataService dataService)
	{
		super(entityName, searchService);
	}

	@Override
	public Iterable<Entity> findAll(Query query)
	{
		if (query.getRules().size() > 0) query.and();
		query.eq(OntologyQueryRepository.ENTITY_TYPE, OntologyIndexRepository.TYPE_ONTOLOGY);
		return searchService.search(query, entityMetaData);
	}

	@Override
	public Entity findOne(Query q)
	{
		if (q.getRules().size() > 0) q.and();
		q.eq(OntologyQueryRepository.ENTITY_TYPE, OntologyIndexRepository.TYPE_ONTOLOGY);
		return findOneInternal(q);
	}

	@Override
	public Entity findOne(Object id)
	{
		for (Entity entity : searchService.search(new QueryImpl().eq(OntologyTermQueryRepository.ID, id),
				entityMetaData))
		{
			return entity;
		}
		return null;
	}

	@Override
	public Query query()
	{
		return new QueryImpl(this);
	}

	@Override
	public long count(Query query)
	{
		if (query.getRules().size() > 0)
		{
			query.and();
		}
		query.eq(OntologyIndexRepository.ENTITY_TYPE, OntologyIndexRepository.TYPE_ONTOLOGY);
		return searchService.count(query.pageSize(Integer.MAX_VALUE).offset(Integer.MIN_VALUE), entityMetaData);
	}

	@Override
	public String getUrl()
	{
		return BASE_URL + entityName;
	}
}