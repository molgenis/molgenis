package org.molgenis.ontology.repository;

import javax.annotation.Nullable;

import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.OntologyService;
import org.molgenis.ontology.beans.OntologyEntity;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

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
	public long count()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(Query q, Class<E> clazz)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterable<Entity> findAll(Iterable<Object> ids)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(Iterable<Object> ids, Class<E> clazz)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Entity> E findOne(Object id, Class<E> clazz)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Entity> E findOne(Query q, Class<E> clazz)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void update(Entity entity)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void update(Iterable<? extends Entity> records)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(Entity entity)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(Iterable<? extends Entity> entities)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteById(Object id)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteById(Iterable<Object> ids)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteAll()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(Entity entity)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Integer add(Iterable<? extends Entity> entities)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void flush()
	{
	}

	@Override
	public void clearCache()
	{
	}

	@Override
	public <E extends Entity> Iterable<E> iterator(Class<E> clazz)
	{
		throw new UnsupportedOperationException();
	}
}