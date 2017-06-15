package org.molgenis.data.elasticsearch.client;

import com.google.common.util.concurrent.AtomicLongMap;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.ResourceAlreadyExistsException;
import org.elasticsearch.ResourceNotFoundException;
import org.elasticsearch.action.ShardOperationFailedException;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequestBuilder;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequestBuilder;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.sort.SortBuilder;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.elasticsearch.client.model.SearchHit;
import org.molgenis.data.elasticsearch.client.model.SearchHits;
import org.molgenis.data.elasticsearch.generator.model.Index;
import org.molgenis.data.elasticsearch.generator.model.Mapping;
import org.molgenis.data.elasticsearch.settings.IndexSettings;
import org.molgenis.data.index.IndexAlreadyExistsException;
import org.molgenis.data.index.IndexException;
import org.molgenis.data.index.UnknownIndexException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.*;

/**
 * Facade in front of the Elasticsearch client.
 */
public class ElasticsearchClientFacade
{
	private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchClientFacade.class);

	private final Client client;
	private final SettingsContentBuilder settingsBuilder;
	private final MappingContentBuilder mappingSourceBuilder;
	private final BulkProcessorFactory bulkProcessorFactory;

	public ElasticsearchClientFacade(Client client)
	{
		this.client = requireNonNull(client);
		this.settingsBuilder = new SettingsContentBuilder();
		this.mappingSourceBuilder = new MappingContentBuilder();
		this.bulkProcessorFactory = new BulkProcessorFactory();
	}

	public void createIndex(Index index, IndexSettings indexSettings, Stream<Mapping> mappingStream)
	{
		LOG.trace("Creating index '{}' ...", toString(index));

		CreateIndexRequestBuilder createIndexRequest = createIndexRequest(index, indexSettings, mappingStream);

		CreateIndexResponse createIndexResponse;
		try
		{
			createIndexResponse = createIndexRequest.get();
		}
		catch (ResourceAlreadyExistsException e)
		{
			LOG.debug("", e);
			throw new IndexAlreadyExistsException(toString(index));
		}
		catch (ElasticsearchException e)
		{
			LOG.error("", e);
			throw new IndexException(format("Error creating index '%s'.", toString(index)));
		}

		// 'acknowledged' indicates whether the index was successfully created in the cluster before the request timeout
		if (!createIndexResponse.isAcknowledged())
		{
			LOG.warn("Index '{}' creation possibly failed (acknowledged=false)", toString(index));
		}
		// 'shards_acknowledged' indicates whether the requisite number of shard copies were started for each shard in the index before timing out
		if (!createIndexResponse.isShardsAcked())
		{
			LOG.warn("Index '{}' creation possibly failed (shards_acknowledged=false)", toString(index));
		}

		LOG.debug("Created index '{}'.", toString(index));
	}

	private CreateIndexRequestBuilder createIndexRequest(Index index, IndexSettings indexSettings,
			Stream<Mapping> mappingStream)
	{
		XContentBuilder settings = settingsBuilder.createSettings(indexSettings);
		Map<String, XContentBuilder> mappings = mappingStream
				.collect(toMap(Mapping::getType, mappingSourceBuilder::createMapping, (u, v) ->
				{
					throw new IllegalStateException(String.format("Duplicate key %s", u));
				}, LinkedHashMap::new));

		CreateIndexRequestBuilder createIndexRequest = client.admin().indices().prepareCreate(index.getName());
		createIndexRequest.setSettings(settings);
		mappings.forEach(createIndexRequest::addMapping);
		return createIndexRequest;
	}

	public boolean indexesExist(Index... indexes) // FIXME replace varargs with collection
	{
		if (LOG.isTraceEnabled())
		{
			LOG.trace("Determining indexes '{}' existence ...", toString(indexes));
		}

		String[] indexNames = toIndexNames(indexes);
		IndicesExistsRequestBuilder indicesExistsRequest = client.admin().indices().prepareExists(indexNames);

		IndicesExistsResponse indicesExistsResponse;
		try
		{
			indicesExistsResponse = indicesExistsRequest.get();
		}
		catch (ElasticsearchException e)
		{
			LOG.error("", e);
			throw new IndexException(format("Error determining indexes '%s' existence.", toString(indexes)));
		}

		boolean exists = indicesExistsResponse.isExists();
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Determined indexes '{}' existence: {}.", toString(indexes), exists);
		}
		return exists;
	}

	public void deleteIndexes(Index... indexes)
	{
		LOG.trace("Deleting indexes '{}' ...", toString(indexes));

		String[] indexNames = toIndexNames(indexes);
		DeleteIndexRequestBuilder deleteIndexRequest = client.admin().indices().prepareDelete(indexNames);

		DeleteIndexResponse deleteIndexResponse;
		try
		{
			deleteIndexResponse = deleteIndexRequest.get();
		}
		catch (ResourceNotFoundException e)
		{
			LOG.debug("", e);
			throw new UnknownIndexException(toString(indexes));
		}
		catch (ElasticsearchException e)
		{
			LOG.error("", e);
			throw new IndexException(format("Error deleting indexes '%s'.", toString(indexes)));
		}

		if (!deleteIndexResponse.isAcknowledged())
		{
			throw new IndexException(format("Error deleting indexes '%s'", toString(indexes)));
		}
		LOG.debug("Deleted indexes '{}'.", toString(indexes));
	}

	public void refreshIndexes()
	{
		refreshIndexes("_all");
	}

	private void refreshIndexes(String... indexNames)
	{
		if (LOG.isTraceEnabled())
		{
			LOG.trace("Refreshing indexes '{}' ...", indexNames);
		}

		RefreshRequestBuilder refreshRequest = client.admin().indices().prepareRefresh(indexNames);

		RefreshResponse refreshResponse;
		try
		{
			refreshResponse = refreshRequest.get();
		}
		catch (ResourceNotFoundException e)
		{
			LOG.debug("", e);
			throw new UnknownIndexException(indexNames);
		}
		catch (ElasticsearchException e)
		{
			LOG.error("", e);
			throw new IndexException(format("Error refreshing indexes '%s'.", indexNames));
		}

		if (refreshResponse.getFailedShards() > 0)
		{
			LOG.error(stream(refreshResponse.getShardFailures()).map(ShardOperationFailedException::toString)
					.collect(joining("\n")));
			throw new IndexException(format("Error refreshing index '%s'.", indexNames));
		}

		if (LOG.isDebugEnabled())
		{
			LOG.debug("Refreshed indexes '{}'", indexNames);
		}
	}

	public long getCount(Index... indexes)
	{
		return getCount(null, indexes);
	}

	public long getCount(QueryBuilder query, Index... indexes)
	{
		if (LOG.isTraceEnabled())
		{
			if (query != null)
			{
				LOG.trace("Counting docs in indexes '{}' with query '{}' ...", toString(indexes), query);
			}
			else
			{
				LOG.trace("Counting docs in indexes '{}' ...", toString(indexes));
			}
		}

		SearchRequestBuilder searchRequest = createSearchRequest(query, null, 0, null, null, indexes);

		SearchResponse searchResponse;
		try
		{
			searchResponse = searchRequest.get();
		}
		catch (ResourceNotFoundException e)
		{
			LOG.error("", e);
			throw new UnknownIndexException(toIndexNames(indexes));
		}
		catch (ElasticsearchException e)
		{
			LOG.error("", e);
			throw new IndexException(format("Error counting docs in indexes '%s'.", toString(indexes)));
		}

		if (searchResponse.getFailedShards() > 0)
		{
			LOG.error(
					stream(searchResponse.getShardFailures()).map(ShardSearchFailure::toString).collect(joining("\n")));
			throw new IndexException(format("Error counting docs in indexes '%s'.", toString(indexes)));
		}
		if (searchResponse.isTimedOut())
		{
			throw new IndexException(format("Timeout while counting docs in indexes '%s'.", toString(indexes)));
		}

		long totalHits = searchResponse.getHits().getTotalHits();
		if (LOG.isDebugEnabled())
		{
			if (query != null)
			{
				LOG.debug("Counted {} docs in indexes '{}' with query '{}' in {}ms.", totalHits, toString(indexes),
						query, searchResponse.getTookInMillis());
			}
			else
			{
				LOG.debug("Counted {} docs in indexes '{}' in {}ms.", totalHits, toString(indexes),
						searchResponse.getTookInMillis());
			}
		}
		return totalHits;
	}

	public SearchHits search(QueryBuilder query, int from, int size, Index... indexes)
	{
		return search(query, from, size, emptyList(), indexes);
	}

	// TODO replace List<SortBuilder> sorts with Sort (not data.Sort)
	public SearchHits search(QueryBuilder query, int from, int size, List<SortBuilder> sorts, Index... indexes)
	{
		if (LOG.isTraceEnabled())
		{
			if (sorts.isEmpty())
			{
				LOG.trace("Searching docs [{}-{}] in indexes '{}' with query '{}' ...", from, from + size,
						toString(indexes), query);
			}
			else
			{
				LOG.trace("Searching docs [{}-{}] in indexes '{}' with query '{}' sorted by '{}' ...", from,
						from + size, toString(indexes), query, sorts);
			}
		}

		SearchRequestBuilder searchRequest = createSearchRequest(query, from, size, sorts, null, indexes);

		SearchResponse searchResponse;
		try
		{
			searchResponse = searchRequest.get();
		}
		catch (ResourceNotFoundException e)
		{
			LOG.error("", e);
			throw new UnknownIndexException(toIndexNames(indexes));
		}
		catch (ElasticsearchException e)
		{
			LOG.error("", e);
			throw new IndexException(
					format("Error searching docs in indexes '%s' with query '%s'.", toString(indexes), query));
		}
		if (searchResponse.getFailedShards() > 0)
		{
			LOG.error(
					stream(searchResponse.getShardFailures()).map(ShardSearchFailure::toString).collect(joining("\n")));
			throw new IndexException(
					format("Error searching docs in indexes '%s' with query '%s'.", toString(indexes), query));
		}
		if (searchResponse.isTimedOut())
		{
			throw new IndexException(
					format("Timeout searching counting docs in indexes '%s'  with query '%s'.", toString(indexes),
							query));
		}

		if (LOG.isDebugEnabled())
		{
			if (sorts.isEmpty())
			{
				LOG.debug("Searched {} docs in indexes '{}' with query '{}' in {}ms.",
						searchResponse.getHits().getTotalHits(), toString(indexes), query,
						searchResponse.getTookInMillis());
			}
			else
			{
				LOG.debug("Searched {} docs in indexes '{}' with query '{}' sorted by '{}' in {}ms.",
						searchResponse.getHits().getTotalHits(), toString(indexes), query, sorts,
						searchResponse.getTookInMillis());
			}
		}
		return createSearchResponse(searchResponse);
	}

	private SearchRequestBuilder createSearchRequest(QueryBuilder query, Integer from, Integer size,
			List<SortBuilder> sorts, List<AggregationBuilder> aggregations, Index... indexes)
	{
		String[] indexNames = toIndexNames(indexes);
		SearchRequestBuilder searchRequest = client.prepareSearch(indexNames);
		if (query != null)
		{
			searchRequest.setQuery(query);
		}
		if (from != null)
		{
			searchRequest.setFrom(from);
		}
		if (size != null)
		{
			searchRequest.setSize(size);
		}
		if (sorts != null)
		{
			sorts.forEach(searchRequest::addSort);
		}
		if (aggregations != null)
		{
			aggregations.forEach(searchRequest::addAggregation);
		}
		return searchRequest;
	}

	private SearchHits createSearchResponse(SearchResponse searchResponse)
	{
		org.elasticsearch.search.SearchHits searchHits = searchResponse.getHits();
		List<SearchHit> searchHitList = stream(searchHits.getHits())
				.map(hit -> SearchHit.create(hit.getId(), hit.getIndex())).collect(toList());
		return SearchHits.create(searchHits.getTotalHits(), searchHitList);
	}

	// TODO how hard is it to use own class instead of List<AggregationBuilder>?
	public Aggregations aggregate(List<AggregationBuilder> aggregations, QueryBuilder query, Index... indexes)
	{
		if (LOG.isTraceEnabled())
		{
			if (query != null)
			{
				LOG.trace("Aggregating docs in indexes '{}' with aggregations '{}' and query '{}' ...",
						toString(indexes), aggregations, query);
			}
			else
			{
				LOG.trace("Aggregating docs in indexes '{}' with aggregations '{}' ...", toString(indexes),
						aggregations);
			}
		}

		SearchRequestBuilder searchRequest = createSearchRequest(query, null, 0, null, aggregations, indexes);
		SearchResponse searchResponse;
		try
		{
			searchResponse = searchRequest.get();
		}
		catch (ResourceNotFoundException e)
		{
			LOG.error("", e);
			throw new UnknownIndexException(toIndexNames(indexes));
		}
		catch (ElasticsearchException e)
		{
			LOG.error("", e);
			throw new IndexException(format("Error aggregating docs in indexes '%s'.", toString(indexes)));
		}
		if (searchResponse.getFailedShards() > 0)
		{
			LOG.error(
					stream(searchResponse.getShardFailures()).map(ShardSearchFailure::toString).collect(joining("\n")));
			throw new IndexException(format("Error aggregating docs in indexes '%s'.", toString(indexes)));
		}
		if (searchResponse.isTimedOut())
		{
			throw new IndexException(format("Timeout aggregating docs in indexes '%s'.", toString(indexes)));
		}

		if (LOG.isDebugEnabled())
		{
			if (query != null)
			{
				LOG.debug("Aggregated docs in indexes '{}' with aggregations '{}' and query '{}' in {}ms.",
						toString(indexes), aggregations, query, searchResponse.getTookInMillis());
			}
			else
			{
				LOG.debug("Aggregated docs in indexes '{}' with aggregations '{}' in {}ms.", toString(indexes),
						aggregations, searchResponse.getTookInMillis());
			}
		}
		return searchResponse.getAggregations();
	}

	public void deleteById(Index index, String id)
	{
		LOG.trace("Deleting doc with id '{}' in index '{}' ...", id, toString(index));
		DeleteResponse deleteResponse;
		try
		{
			deleteResponse = client.prepareDelete().setIndex(index.getName()).setId(id).get();
		}
		catch (ElasticsearchException e)
		{
			LOG.debug("", e);
			throw new IndexException(format("Error deleting doc with id '%s' in index '%s'.", id, toString(index)));
		}
		LOG.debug("Deleted doc with id '{}' in index '{}' and status '{}'", id, toString(index),
				deleteResponse.getResult());
	}

	// TODO create IndexRequests here instead of in Service, supply stream of docs
	/**
	 * Creates a {@link BulkProcessor} and adds a stream of {@link IndexRequest}s to it.
	 * Counts how many requests of each type were added to the {@link BulkProcessor}.
	 *
	 * @param requests        the {@link IndexRequest}s to add
	 * @param awaitCompletion indication if the completion of the requests should be awaited synchronously
	 * @return AtomicLongMap containing per type how many requests of that type were added.
	 */
	public AtomicLongMap<String> index(Stream<IndexRequest> requests, boolean awaitCompletion)
	{
		AtomicLongMap<String> nrIndexedEntitiesPerType = AtomicLongMap.create();
		BulkProcessor bulkProcessor = bulkProcessorFactory.create(client);
		try
		{
			requests.forEachOrdered(request ->
			{
				LOG.trace("Indexing [{}] with id [{}] in index [{}]...", request.type(), request.id(), request.index());
				nrIndexedEntitiesPerType.incrementAndGet(request.type());
				bulkProcessor.add(request);
			});
			return nrIndexedEntitiesPerType;
		}
		catch (ElasticsearchException e)
		{
			LOG.debug("", e);
			throw new IndexException("");
		}
		finally
		{
			if (awaitCompletion)
			{
				waitForCompletion(bulkProcessor);
			}
		}
	}

	private void waitForCompletion(BulkProcessor bulkProcessor)
	{
		LOG.trace("waitForCompletion...");
		try
		{
			boolean isCompleted = bulkProcessor.awaitClose(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			if (!isCompleted)
			{
				throw new MolgenisDataException("Failed to complete bulk request within the given time");
			}
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		}
		finally
		{
			LOG.debug("bulkProcessor closed.");
		}
	}

	private String[] toIndexNames(Index... indexes)
	{
		return Arrays.stream(indexes).map(Index::getName).toArray(String[]::new);
	}

	private String toString(Index... indexes)
	{
		return stream(indexes).map(Index::getName).collect(joining(", "));
	}
}
