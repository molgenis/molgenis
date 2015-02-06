package org.molgenis.ontology.repository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.OntologyService;
import org.molgenis.ontology.beans.OntologyTermEntity;
import org.molgenis.ontology.beans.OntologyTermTransformer;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Iterables;

public class OntologyTermQueryRepository extends AbstractOntologyQueryRepository
{
	public final static String DEFAULT_ONTOLOGY_TERM_REPO = "ontologytermindex";
	private final static String BASE_URL = "ontologytermindex://";
	private final static List<String> reservedAttributeName = Arrays.asList("score");

	@Autowired
	public OntologyTermQueryRepository(String entityName, SearchService searchService, DataService dataService,
			OntologyService ontologyService)
	{
		super(entityName, searchService);
		dynamicEntityMetaData();
	}

	// FIXME: please document this piece of sorcery
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
			for (Entity entity : searchService.search(new QueryImpl().pageSize(1), entityMetaData))
			{
				for (String attributeName : entity.getAttributeNames())
				{
					if (!availableAttributes.contains(attributeName.toLowerCase())
							&& !reservedAttributeName.contains(attributeName))
					{
						availableAttributes.add(attributeName.toLowerCase());
						defaultEntityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(attributeName));
					}
				}
			}
		}
	}

	@Override
	public Iterable<Entity> findAll(Query q)
	{
		if (q.getRules().size() > 0) q.and();
		q.eq(OntologyTermQueryRepository.ENTITY_TYPE, OntologyTermQueryRepository.TYPE_ONTOLOGYTERM);
		return Iterables.transform(searchService.search(q, entityMetaData), new OntologyTermTransformer(entityMetaData,
				searchService));
	}

	@Override
	public Entity findOne(Query q)
	{
		if (q.getRules().size() > 0) q.and();
		q.eq(OntologyTermIndexRepository.ENTITY_TYPE, OntologyTermIndexRepository.TYPE_ONTOLOGYTERM);
		Entity entity = findOneInternal(q);
		return entity != null ? new OntologyTermEntity(entity, entityMetaData, searchService) : null;
	}

	@Override
	public Entity findOne(Object id)
	{
		for (Entity entity : searchService.search(new QueryImpl().eq(OntologyTermQueryRepository.ID, id),
				entityMetaData))
		{
			return new OntologyTermEntity(entity, entityMetaData, searchService);
		}
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
		return searchService.count(q.pageSize(Integer.MAX_VALUE).offset(Integer.MIN_VALUE), entityMetaData);
	}

	@Override
	public String getUrl()
	{
		return BASE_URL + getName();
	}
}
