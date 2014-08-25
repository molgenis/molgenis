package org.molgenis.omx.biobankconnect.ontology.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.biobankconnect.ontologyindexer.AsyncOntologyIndexer;
import org.molgenis.omx.biobankconnect.ontologytree.OntologyTermEntity;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;

public class OntologyTermQueryRepository extends AbstractOntologyQueryRepository
{
	public final static String DEFAULT_ONTOLOGY_TERM_REPO = "ontologytermindex";
	private final static String BASE_URL = "ontologytermindex://";
	private final static List<String> reservedAttributeName = Arrays.asList("score");
	private final String ontologyIri;

	@Autowired
	public OntologyTermQueryRepository(String entityName, String ontologyIri, SearchService searchService)
	{
		super(entityName, searchService);
		this.ontologyIri = ontologyIri;
		dynamicEntityMetaData();
	}

	private void dynamicEntityMetaData()
	{
		EntityMetaData entityMetaData = getEntityMetaData();
		if (entityMetaData instanceof DefaultEntityMetaData)
		{
			DefaultEntityMetaData defaultEntityMetaData = (DefaultEntityMetaData) entityMetaData;
			Set<String> availableAttributes = new HashSet<String>();
			for (AttributeMetaData attributeMetaData : entityMetaData.getAttributes())
			{
				availableAttributes.add(attributeMetaData.getName().toLowerCase());
			}
			SearchResult resultResult = searchService.search(new SearchRequest(AsyncOntologyIndexer
					.createOntologyTermDocumentType(ontologyIri),
					new QueryImpl().eq(OntologyTermQueryRepository.ENTITY_TYPE,
							OntologyTermQueryRepository.TYPE_ONTOLOGYTERM).pageSize(1), null));
			if (resultResult.getTotalHitCount() > 0)
			{
				Hit hit = resultResult.getSearchHits().get(0);
				for (String attributeName : hit.getColumnValueMap().keySet())
				{
					if (!availableAttributes.contains(attributeName.toLowerCase())
							&& !reservedAttributeName.contains(attributeName))
					{
						defaultEntityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(attributeName));
					}
				}
			}
		}
	}

	@Override
	public Iterable<Entity> findAll(Query q)
	{
		List<Entity> entities = new ArrayList<Entity>();
		if (q.getRules().size() > 0) q.and();
		q.eq(OntologyTermQueryRepository.ENTITY_TYPE, OntologyTermQueryRepository.TYPE_ONTOLOGYTERM);
		for (Hit hit : searchService.search(
				new SearchRequest(AsyncOntologyIndexer.createOntologyTermDocumentType(ontologyIri), q, null))
				.getSearchHits())
		{
			entities.add(new OntologyTermEntity(hit, getEntityMetaData(), searchService));
		}
		return entities;
	}

	@Override
	public Entity findOne(Query q)
	{
		if (q.getRules().size() > 0) q.and();
		q.eq(OntologyTermIndexRepository.ENTITY_TYPE, OntologyTermIndexRepository.TYPE_ONTOLOGYTERM);
		Hit hit = findOneInternal(AsyncOntologyIndexer.createOntologyTermDocumentType(ontologyIri), q);
		return hit != null ? new OntologyTermEntity(hit, getEntityMetaData(), searchService) : null;
	}

	@Override
	public Entity findOne(Object id)
	{
		Hit hit = searchService.searchById(AsyncOntologyIndexer.createOntologyTermDocumentType(ontologyIri),
				id.toString());
		return hit != null ? new OntologyTermEntity(hit, getEntityMetaData(), searchService) : null;
	}

	@Override
	public Query query()
	{
		return new QueryImpl(this);
	}

	@Override
	public long count(Query q)
	{
		return searchService.count(AsyncOntologyIndexer.createOntologyTermDocumentType(ontologyIri),
				q.pageSize(Integer.MAX_VALUE).offset(Integer.MIN_VALUE));
	}

	@Override
	public String getUrl()
	{
		return BASE_URL + getName();
	}
}
