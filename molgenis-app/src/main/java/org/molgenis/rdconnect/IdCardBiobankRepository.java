package org.molgenis.rdconnect;

import static java.util.Objects.requireNonNull;

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
		repoCapabilities.add(RepositoryCapability.AGGREGATEABLE);
		repoCapabilities.add(RepositoryCapability.QUERYABLE);
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
		return metaDataService.getEntityMetaData("rdconnect_regbb");
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
		EntityMetaData entityMeta = getEntityMetaData();
		// entities containing only id
		Iterable<Entity> idCardBiobankIds = elasticSearchService.search(q, entityMeta);
		// retrieve entities for ids
		Iterable<String> idCardBiobanks = Iterables.transform(idCardBiobankIds, new Function<Entity, String>()
		{
			@Override
			public String apply(Entity entity)
			{
				return entity.getIdValue().toString();
			}
		});
		return idCardBiobankService.getIdCardBiobanks(idCardBiobanks);
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
	public void add(Entity entity)
	{
		elasticSearchService.index(entity, getEntityMetaData(), IndexingMode.UPDATE); // FIXME
	}

	@Override
	public Integer add(Iterable<? extends Entity> entities)
	{
		return Long.valueOf(elasticSearchService.index(entities, getEntityMetaData(), IndexingMode.UPDATE)).intValue(); // FIXME
	}

	@Override
	public void flush()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void clearCache()
	{
		throw new UnsupportedOperationException();
	}

}
