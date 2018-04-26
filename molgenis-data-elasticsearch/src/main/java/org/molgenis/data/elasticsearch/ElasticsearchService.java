package org.molgenis.data.elasticsearch;

import com.google.common.collect.Iterators;
import com.google.common.collect.Streams;
import org.apache.lucene.search.Explanation;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.Aggregations;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.elasticsearch.client.ClientFacade;
import org.molgenis.data.elasticsearch.client.model.SearchHit;
import org.molgenis.data.elasticsearch.client.model.SearchHits;
import org.molgenis.data.elasticsearch.generator.ContentGenerators;
import org.molgenis.data.elasticsearch.generator.model.*;
import org.molgenis.data.index.IndexService;
import org.molgenis.data.index.SearchService;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.EntityType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.support.EntityTypeUtils.createFetchForReindexing;

/**
 * Elasticsearch search service that executes all requests using the elasticsearch client facade.
 */
@Component
public class ElasticsearchService implements SearchService, IndexService
{
	private static final int BATCH_SIZE = 1000;
	public static final int MAX_BATCH_SIZE = 10000;

	private final ClientFacade clientFacade;
	private final ContentGenerators contentGenerators;
	private final DataService dataService;

	public ElasticsearchService(ClientFacade clientFacade, ContentGenerators contentGenerators, DataService dataService)
	{
		this.clientFacade = requireNonNull(clientFacade);
		this.contentGenerators = requireNonNull(contentGenerators);
		this.dataService = requireNonNull(dataService);
	}

	@Override
	public void createIndex(EntityType entityType)
	{
		Index index = contentGenerators.createIndex(entityType);
		IndexSettings indexSettings = IndexSettings.create();
		Mapping mapping = contentGenerators.createMapping(entityType);
		clientFacade.createIndex(index, indexSettings, Stream.of(mapping));
	}

	@Override
	public boolean hasIndex(EntityType entityType)
	{
		Index index = contentGenerators.createIndex(entityType);
		return clientFacade.indexesExist(index);
	}

	@Override
	public void deleteIndex(EntityType entityType)
	{
		Index index = contentGenerators.createIndex(entityType);
		clientFacade.deleteIndex(index);
	}

	@Override
	public void rebuildIndex(Repository<? extends Entity> repository)
	{
		EntityType entityType = repository.getEntityType();

		if (hasIndex(entityType))
		{
			deleteIndex(entityType);
		}

		createIndex(entityType);
		repository.forEachBatched(createFetchForReindexing(entityType),
				entities -> index(entityType, entities.stream()), BATCH_SIZE);
	}

	@Override
	public void refreshIndex()
	{
		clientFacade.refreshIndexes();
	}

	@Override
	public long count(EntityType entityType)
	{
		Index index = contentGenerators.createIndex(entityType);
		return clientFacade.getCount(index);
	}

	@Override
	public long count(EntityType entityType, Query<Entity> q)
	{
		Index index = contentGenerators.createIndex(entityType);
		QueryBuilder queryBuilder = contentGenerators.createQuery(q, entityType);
		return clientFacade.getCount(queryBuilder, index);
	}

	@Override
	public Stream<Object> search(EntityType entityType, Query<Entity> q)
	{
		int from = q.getOffset();

		return search(entityType, q, from, q.getPageSize());
	}

	private Stream<Object> search(EntityType entityType, Query<Entity> q, int offset, int pageSize)
	{
		QueryBuilder query = contentGenerators.createQuery(q, entityType);
		Sort sort = q.getSort() != null ? contentGenerators.createSorts(q.getSort(), entityType) : null;
		Index index = contentGenerators.createIndex(entityType);
		Stream<SearchHit> searchHits = Stream.empty();

		boolean done = false;
		int currentOffset;
		int i = 0;
		while (!done)
		{
			int batchSize = pageSize < MAX_BATCH_SIZE && pageSize != 0 ? pageSize : MAX_BATCH_SIZE;
			currentOffset = offset + (i * MAX_BATCH_SIZE);

			SearchHits currentSearchHits = clientFacade.search(query, currentOffset, batchSize, sort, index);
			searchHits = Streams.concat(searchHits, currentSearchHits.getHits().stream());

			if (currentSearchHits.getHits().size() < MAX_BATCH_SIZE)
			{
				done = true;
			}

			if (pageSize != 0) pageSize -= MAX_BATCH_SIZE;
			i++;
		}

		return toEntityIds(entityType, searchHits.map(SearchHit::getId));
	}

	private static Stream<Object> toEntityIds(EntityType entityType, Stream<String> documentIdStream)
	{
		return documentIdStream.map(documentId -> ElasticsearchService.toEntityId(entityType, documentId));
	}

	private static Object toEntityId(EntityType entityType, String documentId)
	{
		AttributeType attributeType = entityType.getIdAttribute().getDataType();
		switch (attributeType)
		{
			case EMAIL:
			case HYPERLINK:
			case STRING:
				return documentId;
			case INT:
				return Integer.parseInt(documentId);
			case LONG:
				return Long.parseLong(documentId);
			default:
				throw new RuntimeException(format("Invalid id attribute type '%s'", attributeType));
		}
	}

	@Override
	public Object searchOne(EntityType entityType, Query<Entity> q)
	{
		return search(entityType, q, q.getOffset(), 1).findFirst().orElse(null);
	}

	@Override
	public AggregateResult aggregate(final EntityType entityType, AggregateQuery aggregateQuery)
	{
		List<AggregationBuilder> aggregationList = contentGenerators.createAggregations(aggregateQuery.getAttributeX(),
				aggregateQuery.getAttributeY(), aggregateQuery.getAttributeDistinct());
		QueryBuilder query = contentGenerators.createQuery(aggregateQuery.getQuery(), entityType);
		Index index = contentGenerators.createIndex(entityType);
		Aggregations aggregations = clientFacade.aggregate(aggregationList, query, index);
		return new AggregateResponseParser().parseAggregateResponse(aggregateQuery.getAttributeX(),
				aggregateQuery.getAttributeY(), aggregateQuery.getAttributeDistinct(), aggregations, dataService);
	}

	public Explanation explain(EntityType entityType, Object entityId, Query<Entity> q)
	{
		Index index = contentGenerators.createIndex(entityType);
		Document document = contentGenerators.createDocument(entityId);
		QueryBuilder query = contentGenerators.createQuery(q, entityType);
		return clientFacade.explain(SearchHit.create(document.getId(), index.getName()), query);
	}

	@Override
	public void index(EntityType entityType, Entity entity)
	{
		Index index = contentGenerators.createIndex(entityType);
		Document document = contentGenerators.createDocument(entity);
		clientFacade.index(index, document);
	}

	@Override
	public long index(EntityType entityType, Stream<? extends Entity> entities)
	{
		Index index = contentGenerators.createIndex(entityType);
		Stream<DocumentAction> documentActionStream = entities.map(entity -> this.toDocumentAction(index, entity));

		AtomicLong count = new AtomicLong(0L);
		clientFacade.processDocumentActions(documentActionStream.filter(documentAction ->
		{
			count.incrementAndGet();
			return true;
		}));
		return count.get();
	}

	private DocumentAction toDocumentAction(Index index, Entity entity)
	{
		Document document = contentGenerators.createDocument(entity);
		return DocumentAction.create(index, document, DocumentAction.Operation.INDEX);
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
		Document document = contentGenerators.createDocument(entityId);
		clientFacade.deleteById(index, document);
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
