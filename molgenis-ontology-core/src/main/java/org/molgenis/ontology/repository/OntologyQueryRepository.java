package org.molgenis.ontology.repository;

import java.util.Set;

import javax.annotation.Nullable;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.OntologyService;
import org.molgenis.ontology.beans.OntologyEntity;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class OntologyQueryRepository extends AbstractOntologyQueryRepository
{
	public final static String DEFAULT_ONTOLOGY_REPO = "ontologyindex";
	private final OntologyService ontologyService;
	private final DataService dataService;

	@Autowired
	public OntologyQueryRepository(String entityName, OntologyService ontologyService, SearchService searchService,
			DataService dataService)
	{
		super(entityName, searchService);
		this.ontologyService = ontologyService;
		this.dataService = dataService;
	}

	@Override
	public Iterable<Entity> findAll(Query query)
	{
		if (query.getRules().size() > 0) query.and();
		query.eq(OntologyQueryRepository.ENTITY_TYPE, OntologyIndexRepository.TYPE_ONTOLOGY);
		return Iterables.transform(searchService.search(query, getEntityMetaData()), new Function<Entity, Entity>()
		{
			@Override
			@Nullable
			public Entity apply(@Nullable Entity entity)
			{
				return new OntologyEntity(entity, entityMetaData, dataService, searchService, ontologyService);
			}
		});

	}

	@Override
	public Entity findOne(Query q)
	{
		if (q.getRules().size() > 0) q.and();
		q.eq(OntologyQueryRepository.ENTITY_TYPE, OntologyIndexRepository.TYPE_ONTOLOGY);
		Entity entity = findOneInternal(q);
		return entity != null ? new OntologyEntity(entity, entityMetaData, dataService, searchService, ontologyService) : null;
	}

	@Override
	public Entity findOne(Object id)
	{
		for (Entity entity : searchService.search(new QueryImpl().eq(OntologyTermQueryRepository.ID, id),
				getEntityMetaData()))
		{
			return new OntologyEntity(entity, entityMetaData, dataService, searchService, ontologyService);
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
	public Set<RepositoryCapability> getCapabilities()
	{
		return Sets.newHashSet(RepositoryCapability.QUERYABLE);
	}

	@Override
	public long count()
	{
		return count(new QueryImpl());
	}

}