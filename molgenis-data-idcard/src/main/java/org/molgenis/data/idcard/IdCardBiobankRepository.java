package org.molgenis.data.idcard;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.RepositoryCapability.AGGREGATEABLE;
import static org.molgenis.data.RepositoryCapability.QUERYABLE;
import static org.molgenis.data.RepositoryCapability.WRITABLE;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.elasticsearch.ElasticsearchService;
import org.molgenis.data.elasticsearch.ElasticsearchService.IndexingMode;
import org.molgenis.data.idcard.client.IdCardClient;
import org.molgenis.data.idcard.model.IdCardBiobank;
import org.molgenis.data.idcard.model.IdCardBiobankFactory;
import org.molgenis.data.idcard.model.IdCardBiobankMetaData;
import org.molgenis.data.idcard.settings.IdCardIndexerSettings;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.AbstractRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Repository
public class IdCardBiobankRepository extends AbstractRepository
{
	private static final Logger LOG = LoggerFactory.getLogger(IdCardBiobankRepository.class);

	private final IdCardBiobankMetaData idCardBiobankMetaData;
	private final IdCardClient idCardClient;
	private final ElasticsearchService elasticsearchService;
	private final DataService dataService;
	private final IdCardIndexerSettings idCardIndexerSettings;
	private final IdCardBiobankFactory idCardBiobankFactory;

	@Autowired
	public IdCardBiobankRepository(IdCardBiobankMetaData idCardBiobankMetaData, IdCardClient idCardClient,
			ElasticsearchService elasticsearchService, DataService dataService,
			IdCardIndexerSettings idCardIndexerSettings, IdCardBiobankFactory idCardBiobankFactory)
	{
		this.idCardBiobankMetaData = idCardBiobankMetaData;
		this.idCardClient = requireNonNull(idCardClient);
		this.elasticsearchService = requireNonNull(elasticsearchService);
		this.dataService = requireNonNull(dataService);
		this.idCardIndexerSettings = requireNonNull(idCardIndexerSettings);
		this.idCardBiobankFactory = requireNonNull(idCardBiobankFactory);
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return idCardClient.getIdCardBiobanks().iterator();
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
	public EntityMetaData getEntityMetaData()
	{
		return idCardBiobankMetaData;
	}

	@Override
	public long count(Query<Entity> q)
	{
		return elasticsearchService.count(q, getEntityMetaData());
	}

	@Override
	public Stream<Entity> findAll(Query<Entity> q)
	{
		return elasticsearchService.searchAsStream(q, getEntityMetaData());
	}

	@Override
	public Entity findOne(Query<Entity> q)
	{
		Iterator<Entity> it = findAll(q).iterator();
		return it.hasNext() ? it.next() : null;
	}

	@Override
	public Entity findOneById(Object id)
	{
		try
		{
			return idCardClient.getIdCardBiobank(id.toString());
		}
		catch (RuntimeException e)
		{
			return createErrorIdCardBiobank(id);
		}
	}

	@Override
	public Entity findOneById(Object id, Fetch fetch)
	{
		return findOneById(id);
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		return elasticsearchService.aggregate(aggregateQuery, getEntityMetaData());
	}

	@Override
	public void update(Entity entity)
	{
		throw new UnsupportedOperationException(
				String.format("Repository [%s] is not %s", getName(), WRITABLE.toString()));
	}

	@Override
	public void delete(Entity entity)
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
	public void deleteAll(Stream<Object> ids)
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
	public void flush()
	{
		elasticsearchService.flush();
	}

	@Override
	public void rebuildIndex()
	{
		LOG.trace("Indexing ID-Card biobanks ...");
		Iterable<? extends Entity> entities = idCardClient
				.getIdCardBiobanks(idCardIndexerSettings.getIndexRebuildTimeout());

		EntityMetaData entityMeta = getEntityMetaData();
		if (!elasticsearchService.hasMapping(entityMeta))
		{
			elasticsearchService.createMappings(entityMeta);
		}
		elasticsearchService.index(entities, entityMeta, IndexingMode.UPDATE);
		LOG.debug("Indexed ID-Card biobanks");
	}

	private IdCardBiobank createErrorIdCardBiobank(Object id)
	{
		IdCardBiobank idCardBiobank = idCardBiobankFactory.create(Integer.valueOf(id.toString()));
		idCardBiobank.set(IdCardBiobankMetaData.NAME, "Error loading data");
		return idCardBiobank;
	}
}
