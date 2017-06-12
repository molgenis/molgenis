package org.molgenis.data.elasticsearch.util;

import com.codepoetics.protonpack.StreamUtils;
import com.google.common.util.concurrent.AtomicLongMap;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.elasticsearch.request.SearchRequestGenerator;
import org.molgenis.data.index.IndexException;
import org.molgenis.data.meta.model.EntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.elasticsearch.client.Requests.refreshRequest;

/**
 * Facade in front of the ElasticSearch client.
 */
public class ElasticsearchClientFacade
{
	private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchClientFacade.class);
	private static final TimeValue SCROLL_KEEP_ALIVE = new TimeValue(5, TimeUnit.MINUTES);
	private static final int SCROLL_SIZE = 1000;
	private final Client client;
	private final SearchRequestGenerator generator = new SearchRequestGenerator(
			new DocumentIdGenerator()); // TODO use ElasticsearchNameGenerator bean instead of creating a new instance
	private final BulkProcessorFactory bulkProcessorFactory;

	public ElasticsearchClientFacade(Client client)
	{
		this(client, new BulkProcessorFactory());
	}

	private ElasticsearchClientFacade(Client client, BulkProcessorFactory bulkProcessorFactory)
	{
		this.client = client;
		this.bulkProcessorFactory = bulkProcessorFactory;
	}

	public void createIndex(String indexName, Settings settings)
	{
		LOG.trace("Creating Elasticsearch index '{}' ...", indexName);
		CreateIndexResponse createIndexResponse;
		try
		{
			createIndexResponse = client.admin().indices().prepareCreate(indexName).setSettings(settings).get();
		}
		catch (ElasticsearchException e)
		{
			LOG.debug("", e);
			throw new IndexException(format("Error creating index '%s'.", indexName));
		}

		if (!createIndexResponse.isAcknowledged())
		{
			throw new IndexException(format("Error creating index '%s'.", indexName));
		}
		LOG.debug("Created Elasticsearch index '{}'.", indexName);
	}

	public boolean indexExists(String indexName)
	{
		LOG.trace("Determining Elasticsearch index '{}' existence ...", indexName);
		IndicesExistsResponse indicesExistsResponse;
		try
		{
			indicesExistsResponse = client.admin().indices().prepareExists(indexName).get();
		}
		catch (ElasticsearchException e)
		{
			LOG.debug("", e);
			throw new IndexException(format("Error determining index '%s' existence.", indexName));
		}
		LOG.debug("Determined Elasticsearch index '{}' existence.", indexName);
		return indicesExistsResponse.isExists();
	}

	public void deleteIndex(String indexName)
	{
		LOG.trace("Deleting Elasticsearch index '{}' ...", indexName);
		DeleteIndexResponse deleteIndexResponse;
		try
		{
			deleteIndexResponse = client.admin().indices().prepareDelete(indexName).get();
		}
		catch (ElasticsearchException e)
		{
			LOG.debug("", e);
			throw new IndexException(format("Error deleting index '%s'.", indexName));
		}

		if (!deleteIndexResponse.isAcknowledged())
		{
			throw new ElasticsearchException(format("Error deleting index '%s'", indexName));
		}
		LOG.debug("Deleted Elasticsearch index '{}'.", indexName);
	}

	public void refreshIndex(String indexName)
	{
		LOG.trace("Refreshing Elasticsearch index [{}] ...", indexName);
		if (indexName == null)
		{
			indexName = "_all";
		}
		try
		{
			client.admin().indices().refresh(refreshRequest(indexName)).actionGet();
		}
		catch (ElasticsearchException e)
		{
			LOG.debug("", e);
			throw new IndexException(format("Error refreshing index '%s'.", indexName));
		}
		LOG.debug("Refreshed Elasticsearch index [{}]", indexName);
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

	// 0) String     : bad json --> exception
	// 1) JsonObject
	// 2) AutoValue  : easier for testing
	// 1) or 2)? see what is most practical
	public void createMapping(String indexName, String type, XContentBuilder jsonBuilder) throws IOException
	{
		if (LOG.isTraceEnabled())
		{
			LOG.trace("Creating Elasticsearch mapping in index '{}' for type '{}' with content '{}' ...", indexName,
					type, jsonBuilder.string());
		}

		PutMappingResponse response;
		try
		{
			response = client.admin().indices().preparePutMapping(indexName).setType(type).setSource(jsonBuilder).get();
		}
		catch (ElasticsearchException e)
		{
			LOG.debug("", e);
			throw new IndexException(format("Error creating index metadata '%s'.", indexName));
		}

		if (!response.isAcknowledged())
		{
			throw new IndexException(format("Error creating index metadata '%s'.", indexName));
		}

		if (LOG.isDebugEnabled())
		{
			LOG.debug("Creating Elasticsearch mapping in index '{}' for type '{}' with content '{}'", indexName, type,
					jsonBuilder.string());
		}
	}

	public long getCount(Query<Entity> q, EntityType entityType, String type, String indexName)
	{
		if (q != null)
		{
			LOG.trace("Counting Elasticsearch docs in index '{}' for type '{}' using query [{}] ...", indexName, type,
					q);
		}
		else
		{
			LOG.trace("Counting Elasticsearch docs in index '{}' for type '{}' ...", indexName, type);
		}

		SearchResponse searchResponse;
		try
		{
			SearchRequestBuilder searchRequestBuilder = client.prepareSearch(indexName).setSize(0);
			generator.buildSearchRequest(searchRequestBuilder, SearchType.DEFAULT, entityType, q, null, null, null);
			searchResponse = searchRequestBuilder.get();
			if (searchResponse.getFailedShards() > 0)
			{
				throw new ElasticsearchException("Search failed:\n" + Arrays.stream(searchResponse.getShardFailures())
						.map(ShardSearchFailure::toString).collect(joining("\n")));
			}
		}
		catch (ElasticsearchException e)
		{
			LOG.debug("", e);
			throw new IndexException(format("Error counting documents in index '%s'.", indexName));
		}
		long count = searchResponse.getHits().getTotalHits();

		if (q != null)
		{
			LOG.debug("Counted {} Elasticsearch docs in index '{}' for type '{}' using query [{}] in {}ms", count,
					indexName, type, q, searchResponse.getTookInMillis());
		}
		else
		{
			LOG.debug("Counted {} Elasticsearch docs in index '{}' for type '{}' in {}ms", count, indexName, type,
					searchResponse.getTookInMillis());
		}
		return count;
	}

	/**
	 * Deletes a document from an index.
	 *
	 * @param indexName the name of the index
	 * @param id        the ID of the document
	 * @param type      tye type of the document
	 */
	public void deleteById(String indexName, String id, String type)
	{
		LOG.trace("Deleting Elasticsearch doc with id '{}' in index '{}' for type '{}' ...", id, indexName, type);
		try
		{
			client.prepareDelete(indexName, type, id).get();
		}
		catch (ElasticsearchException e)
		{
			LOG.debug("", e);
			throw new IndexException(format("Error deleting doc '%s' from index '%s'.", id, indexName));
		}
		LOG.debug("Deleted Elasticsearch '{}' doc with id [{}]", type, id);
	}

	public SearchResponse search(SearchType searchType, SearchRequest request, String indexName)
	{
		LOG.trace("Searching Elasticsearch docs in index '{}' ...", indexName);
		SearchResponse response;
		try
		{
			SearchRequestBuilder builder = client.prepareSearch(indexName);
			generator.buildSearchRequest(builder, searchType, request.getEntityType(), request.getQuery(),
					request.getAggregateAttribute1(), request.getAggregateAttribute2(),
					request.getAggregateAttributeDistinct());
			LOG.trace("*** REQUEST\n{}", builder);
			response = builder.get();
			LOG.trace("*** RESPONSE\n{}", response);
			if (response.getFailedShards() > 0)
			{
				throw new ElasticsearchException("Search failed:\n" + Arrays.stream(response.getShardFailures())
						.map(ShardSearchFailure::toString).collect(joining("\n")));
			}
		}
		catch (ElasticsearchException e)
		{
			LOG.debug("", e);
			throw new IndexException(format("Error executing search in index '%s'", indexName));
		}
		LOG.trace("Searched Elasticsearch docs in index '{}' in {}ms.", indexName, response.getTookInMillis());
		return response;
	}

	/**
	 * Performs a search query and returns the result as a {@link Stream} of ID strings.
	 */
	public Stream<String> searchForIds(Consumer<SearchRequestBuilder> queryBuilder, String queryToString, String type,
			String indexName)
	{
		SearchHits searchHits = search(queryBuilder, queryToString, type, indexName);
		return Arrays.stream(searchHits.getHits()).map(SearchHit::getId);
	}

	/**
	 * Performs a search query and returns the result as a {@link Stream} of ID strings.
	 */
	public Stream<String> searchForIdsWithScanScroll(Consumer<SearchRequestBuilder> queryBuilder, String queryToString,
			String type, String indexName)
	{
		LOG.trace("Searching Elasticsearch '{}' docs using query [{}] ...", type, queryToString);
		try
		{
			SearchRequestBuilder searchRequestBuilder = client.prepareSearch(indexName);
			queryBuilder.accept(searchRequestBuilder);
			searchRequestBuilder.setScroll(SCROLL_KEEP_ALIVE).setSize(SCROLL_SIZE);
			LOG.trace("SearchRequest: {}", searchRequestBuilder);
			SearchResponse originalSearchResponse = searchRequestBuilder.execute().actionGet();

			LOG.debug("Searched Elasticsearch '{}' docs using query [{}] in {}ms", type, queryToString,
					originalSearchResponse.getTookInMillis());

			Stream<SearchResponse> infiniteResponses = Stream.iterate(originalSearchResponse,
					searchResponse -> client.prepareSearchScroll(searchResponse.getScrollId())
							.setScroll(SCROLL_KEEP_ALIVE).execute().actionGet());
			return StreamUtils
					.takeWhile(infiniteResponses, searchResponse -> searchResponse.getHits().getHits().length > 0)
					.flatMap(searchResponse -> Arrays.stream(searchResponse.getHits().getHits())).map(SearchHit::getId);
		}
		catch (ElasticsearchException e)
		{
			LOG.debug("", e);
			throw new IndexException("");
		}
	}

	private SearchHits search(Consumer<SearchRequestBuilder> queryBuilder, String queryToString, String type,
			String indexName)
	{
		LOG.trace("Searching Elasticsearch '{}' docs using query [{}] ...", type, queryToString);
		SearchResponse searchResponse;
		try
		{
			SearchRequestBuilder searchRequestBuilder = client.prepareSearch(indexName);
			queryBuilder.accept(searchRequestBuilder);
			LOG.trace("SearchRequest: {}", searchRequestBuilder);
			searchResponse = searchRequestBuilder.execute().actionGet();

			if (searchResponse.getFailedShards() > 0)
			{
				StringBuilder sb = new StringBuilder("Search failed.");
				for (ShardSearchFailure failure : searchResponse.getShardFailures())
				{
					sb.append("\n").append(failure.reason());
				}
				throw new ElasticsearchException(sb.toString());
			}
			LOG.debug("Searched Elasticsearch '{}' docs using query [{}] in {}ms", type, queryToString,
					searchResponse.getTookInMillis());
		}
		catch (ElasticsearchException e)
		{
			LOG.debug("", e);
			throw new IndexException("");
		}
		return searchResponse.getHits();
	}

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
}
