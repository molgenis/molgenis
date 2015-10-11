package org.molgenis.rdconnect;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.RepositoryCapability.AGGREGATEABLE;
import static org.molgenis.data.RepositoryCapability.QUERYABLE;
import static org.molgenis.data.RepositoryCapability.UPDATEABLE;
import static org.molgenis.data.RepositoryCapability.WRITABLE;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.elasticsearch.ElasticSearchService;
import org.molgenis.data.elasticsearch.ElasticSearchService.IndexingMode;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.support.QueryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

@org.springframework.stereotype.Repository
public class IdCardBiobankRepository implements Repository
{
	private static final Logger LOG = LoggerFactory.getLogger(IdCardBiobankRepository.class);

	private final IdCardBiobankService idCardBiobankService;
	private final ElasticSearchService elasticSearchService;
	private final MetaDataService metaDataService;

	@Autowired
	public IdCardBiobankRepository(IdCardBiobankService idCardBiobankService, ElasticSearchService elasticSearchService,
			MetaDataService metaDataService)
	{
		this.idCardBiobankService = requireNonNull(idCardBiobankService);
		this.elasticSearchService = requireNonNull(elasticSearchService);
		this.metaDataService = requireNonNull(metaDataService);
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return idCardBiobankService.getIdCardBiobanks().iterator();
	}

	@Override
	public void close() throws IOException
	{
		// noop
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		Set<RepositoryCapability> repoCapabilities = new HashSet<>();
		repoCapabilities.add(AGGREGATEABLE);
		repoCapabilities.add(QUERYABLE);
		return repoCapabilities;
	}

	@Override
	public String getName()
	{
		return getEntityMetaData().getName();
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return metaDataService.getEntityMetaData(IdCardBiobank.ENTITY_NAME);
	}

	@Override
	public long count()
	{
		return elasticSearchService.count(getEntityMetaData());
	}

	@Override
	public Query query()
	{
		return new QueryImpl();
	}

	@Override
	public long count(Query q)
	{
		return elasticSearchService.count(q, getEntityMetaData());
	}

	@Override
	public Iterable<Entity> findAll(Query q)
	{
		return elasticSearchService.search(q, getEntityMetaData());
	}

	@Override
	public Entity findOne(Query q)
	{
		Iterator<Entity> it = findAll(q).iterator();
		return it.hasNext() ? it.next() : null;
	}

	@Override
	public Entity findOne(Object id)
	{
		return idCardBiobankService.getIdCardBiobank(id.toString());
	}

	@Override
	public Iterable<Entity> findAll(Iterable<Object> ids)
	{
		return idCardBiobankService.getIdCardBiobanks(Iterables.transform(ids, new Function<Object, String>()
		{
			@Override
			public String apply(Object id)
			{
				return id.toString();
			}
		}));
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		return elasticSearchService.aggregate(aggregateQuery, getEntityMetaData());
	}

	@Override
	public void update(Entity entity)
	{
		throw new UnsupportedOperationException(
				String.format("Repository [%s] is not %s", getName(), UPDATEABLE.toString()));
	}

	@Override
	public void update(Iterable<? extends Entity> records)
	{
		throw new UnsupportedOperationException(
				String.format("Repository [%s] is not %s", getName(), UPDATEABLE.toString()));
	}

	@Override
	public void delete(Entity entity)
	{
		throw new UnsupportedOperationException(
				String.format("Repository [%s] is not %s", getName(), WRITABLE.toString()));
	}

	@Override
	public void delete(Iterable<? extends Entity> entities)
	{
		throw new UnsupportedOperationException(
				String.format("Repository [%s] is not %s", getName(), WRITABLE.toString()));
	}

	@Override
	public void deleteById(Object id)
	{
		throw new UnsupportedOperationException(
				String.format("Repository [%s] is not %s", getName(), WRITABLE.toString()));
	}

	@Override
	public void deleteById(Iterable<Object> ids)
	{
		throw new UnsupportedOperationException(
				String.format("Repository [%s] is not %s", getName(), WRITABLE.toString()));
	}

	@Override
	public void deleteAll()
	{
		throw new UnsupportedOperationException(
				String.format("Repository [%s] is not %s", getName(), WRITABLE.toString()));
	}

	@Override
	public void add(Entity entity)
	{
		throw new UnsupportedOperationException(
				String.format("Repository [%s] is not %s", getName(), WRITABLE.toString()));
	}

	@Override
	public Integer add(Iterable<? extends Entity> entities)
	{
		throw new UnsupportedOperationException(
				String.format("Repository [%s] is not %s", getName(), WRITABLE.toString()));
	}

	@Override
	public void flush()
	{
		elasticSearchService.flush();
	}

	@Override
	public void clearCache()
	{
		// noop
	}

	public void index(Iterable<? extends Entity> entities)
	{
		LOG.trace("Indexing ID-Card biobanks ...");
		EntityMetaData entityMeta = getEntityMetaData();
		if (!elasticSearchService.hasMapping(entityMeta))
		{
			elasticSearchService.createMappings(entityMeta);
		}
		elasticSearchService.index(entities, entityMeta, IndexingMode.UPDATE);
		LOG.debug("Indexed ID-Card biobanks");
	}
}
