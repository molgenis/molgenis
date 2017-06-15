package org.molgenis.data.elasticsearch;

import com.google.common.collect.Iterators;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.sort.SortBuilder;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.elasticsearch.client.ElasticsearchClientFacade;
import org.molgenis.data.elasticsearch.client.model.SearchHit;
import org.molgenis.data.elasticsearch.client.model.SearchHits;
import org.molgenis.data.elasticsearch.generator.ContentGenerators;
import org.molgenis.data.elasticsearch.generator.model.Index;
import org.molgenis.data.elasticsearch.generator.model.Mapping;
import org.molgenis.data.elasticsearch.settings.IndexSettings;
import org.molgenis.data.elasticsearch.util.ElasticsearchEntityUtils;
import org.molgenis.data.index.IndexingMode;
import org.molgenis.data.index.SearchService;
import org.molgenis.data.meta.model.EntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.elasticsearch.util.ElasticsearchEntityUtils.toElasticsearchId;
import static org.molgenis.data.support.EntityTypeUtils.createFetchForReindexing;

/**
 * Elasticsearch implementation of the SearchService interface.
 *
 * @author erwin
 */
@Component
public class ElasticsearchService implements SearchService
{
	private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchService.class);

	private static final int BATCH_SIZE = 1000;

	private final ElasticsearchClientFacade elasticsearchClientFacade;
	private final ContentGenerators contentGenerators;

	public ElasticsearchService(ElasticsearchClientFacade elasticsearchClientFacade,
			ContentGenerators contentGenerators)
	{
		this.elasticsearchClientFacade = requireNonNull(elasticsearchClientFacade);
		this.contentGenerators = requireNonNull(contentGenerators);
	}

	@Override
	public void createIndex(EntityType entityType)
	{
		Index index = contentGenerators.createIndex(entityType);
		IndexSettings indexSettings = IndexSettings.create();
		Mapping mapping = contentGenerators.createMapping(entityType);
		elasticsearchClientFacade.createIndex(index, indexSettings, Stream.of(mapping));
	}

	@Override
	public boolean hasIndex(EntityType entityType)
	{
		Index index = contentGenerators.createIndex(entityType);
		return elasticsearchClientFacade.indexesExist(index);
	}

	@Override
	public void deleteIndex(EntityType entityType)
	{
		Index index = contentGenerators.createIndex(entityType);
		elasticsearchClientFacade.deleteIndexes(index);
	}

	@Override
	public void rebuildIndex(Repository<? extends Entity> repository)
	{
		EntityType entityType = repository.getEntityType();

		if (hasIndex(entityType)) // TODO just perform delete, index should exist
		{
			LOG.debug("Delete index for repository {}...", repository.getName());
			deleteIndex(entityType);
		}

		createIndex(entityType);
		LOG.trace("Indexing {} repository in batches of size {}...", repository.getName(), BATCH_SIZE);
		repository.forEachBatched(createFetchForReindexing(entityType),
				entities -> index(entityType, entities.stream(), IndexingMode.ADD), BATCH_SIZE);
		LOG.debug("Create index for repository {}...", repository.getName());
	}

	@Override
	public void refreshIndex()
	{
		elasticsearchClientFacade.refreshIndexes();
	}

	@Override
	public long count(EntityType entityType)
	{
		Index index = contentGenerators.createIndex(entityType);
		return elasticsearchClientFacade.getCount(index);
	}

	@Override
	public long count(EntityType entityType, Query<Entity> q)
	{
		Index index = contentGenerators.createIndex(entityType);
		QueryBuilder queryBuilder = contentGenerators.createQuery(q, entityType);
		return elasticsearchClientFacade.getCount(queryBuilder, index);
	}

	@Override
	public Stream<Object> search(EntityType entityType, Query<Entity> q)
	{
		return search(entityType, q, q.getOffset(), q.getPageSize());
	}

	private Stream<Object> search(EntityType entityType, Query<Entity> q, int from, int size)
	{
		QueryBuilder query = contentGenerators.createQuery(q, entityType);
		List<SortBuilder> sortList =
				q.getSort() != null ? contentGenerators.createSorts(q.getSort(), entityType) : null;
		Index index = contentGenerators.createIndex(entityType);
		SearchHits searchHits = elasticsearchClientFacade.search(query, from, size, sortList, index);
		return ElasticsearchEntityUtils.toEntityIds(searchHits.getHits().stream().map(SearchHit::getId));
	}

	@Override
	public Object searchOne(EntityType entityType, Query<Entity> q)
	{
		return search(entityType, q, q.getOffset(), 1).findFirst().orElse(null);
	}

	@Override
	public AggregateResult aggregate(final EntityType entityType, AggregateQuery aggregateQuery)
	{
		List<AggregationBuilder> aggregationList = contentGenerators
				.createAggregations(aggregateQuery.getAttributeX(), aggregateQuery.getAttributeY(),
						aggregateQuery.getAttributeDistinct());
		QueryBuilder query = contentGenerators.createQuery(aggregateQuery.getQuery(), entityType);
		Index index = contentGenerators.createIndex(entityType);
		Aggregations aggregations = elasticsearchClientFacade.aggregate(aggregationList, query, index);

		//		return aggregateResponseParser
		//				.parseAggregateResponse(request.getAggregateAttribute1(), request.getAggregateAttribute2(),
		//						request.getAggregateAttributeDistinct(), aggregations, dataService);
		throw new UnsupportedOperationException("FIXME"); // FIXME create response
	}

	// TODO can we get rid of indexing mode?
	@Override
	public void index(EntityType entityType, Entity entity, IndexingMode indexingMode)
	{
		throw new UnsupportedOperationException(); // FIXME
	}

	@Override
	public long index(EntityType entityType, Stream<? extends Entity> entities, IndexingMode indexingMode)
	{
		throw new UnsupportedOperationException(); // FIXME
	}

	@Override
	public void delete(EntityType entityType, Entity entity)
	{
		deleteById(entityType, entity.getIdValue());
	}

	@Override
	public void deleteById(EntityType entityType, Object entityId)
	{
		Index index = contentGenerators.createIndex(entityType);
		elasticsearchClientFacade.deleteById(index, toElasticsearchId(entityId));
	}

	@Override
	public void deleteAll(EntityType entityType, Stream<Object> entityIds)
	{
		entityIds.forEach(entityId -> deleteById(entityType, entityId));
	}

	@Override
	public void delete(EntityType entityType, Stream<? extends Entity> entities)
	{
		Stream<Object> entityIds = entities.map(Entity::getIdValue);
		Iterators.partition(entityIds.iterator(), BATCH_SIZE)
				.forEachRemaining(batchEntityIds -> deleteAll(entityType, batchEntityIds.stream()));
	}
}
