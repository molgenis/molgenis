package org.molgenis.data.elasticsearch.util;

import com.codepoetics.protonpack.StreamUtils;
import com.google.common.util.concurrent.AtomicLongMap;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.byscroll.BulkByScrollResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.elasticsearch.request.SearchRequestGenerator;
import org.molgenis.data.meta.model.EntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.elasticsearch.client.Requests.refreshRequest;

/**
 * Facade in front of the ElasticSearch client.
 */
public class ElasticsearchUtils
{
	private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchUtils.class);
	private static final TimeValue SCROLL_KEEP_ALIVE = new TimeValue(5, TimeUnit.MINUTES);
	private static final int SCROLL_SIZE = 1000;
	private final Client client;
	private final SearchRequestGenerator generator = new SearchRequestGenerator(
			new DocumentIdGenerator()); // TODO use ElasticsearchNameGenerator bean instead of creating a new instance
	private final BulkProcessorFactory bulkProcessorFactory;

	public ElasticsearchUtils(Client client)
	{
		this(client, new BulkProcessorFactory());
	}

	private ElasticsearchUtils(Client client, BulkProcessorFactory bulkProcessorFactory)
	{
		this.client = client;
		this.bulkProcessorFactory = bulkProcessorFactory;
	}

	public boolean indexExists(String index)
	{
		return client.admin().indices().prepareExists(index).execute().actionGet().isExists();
	}

	// Wait until elasticsearch is ready
	public void waitForYellowStatus()
	{
		client.admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();
	}

	private void refreshIndex(String index)
	{
		client.admin().indices().refresh(refreshRequest(index)).actionGet();
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

	public ImmutableOpenMap<String, MappingMetaData> getMappings(String indexName)
	{
		LOG.trace("Retrieving Elasticsearch mappings ...");
		GetMappingsResponse mappingsResponse = client.admin().indices().prepareGetMappings(indexName).get();
		LOG.debug("Retrieved Elasticsearch mappings");
		return mappingsResponse.getMappings().get(indexName);
	}

	public void putMapping(String index, XContentBuilder jsonBuilder, String type) throws IOException
	{
		if (LOG.isTraceEnabled()) LOG.trace("Creating Elasticsearch mapping [{}] ...", jsonBuilder.string());

		PutMappingResponse response = client.admin().indices().preparePutMapping(index).setType(type)
				.setSource(jsonBuilder).get();

		if (!response.isAcknowledged())
		{
			throw new ElasticsearchException(
					"Creation of mapping for documentType [" + type + "] failed. Response=" + response);
		}
		if (LOG.isDebugEnabled()) LOG.debug("Created Elasticsearch mapping [{}]", jsonBuilder.string());
	}

	public void refresh(String index)
	{
		LOG.trace("Refreshing Elasticsearch index [{}] ...", index);
		refreshIndex(index);
		LOG.debug("Refreshed Elasticsearch index [{}]", index);
	}

	public long getCount(Query<Entity> q, EntityType entityType, String type, String indexName)
	{
		if (q != null)
		{
			LOG.trace("Counting Elasticsearch [{}] docs using query [{}] ...", type, q);
		}
		else
		{
			LOG.trace("Counting Elasticsearch [{}] docs", type);
		}
		SearchRequestBuilder searchRequestBuilder = client.prepareSearch(indexName);
		// FIXME search type should be count
		generator.buildSearchRequest(searchRequestBuilder, SearchType.DEFAULT, entityType, q, null, null, null);
		SearchResponse searchResponse = searchRequestBuilder.get();
		if (searchResponse.getFailedShards() > 0)
		{
			// FIXME .getHeaders()
			throw new ElasticsearchException("Search failed. Returned headers:" + searchResponse);
		}
		long count = searchResponse.getHits().totalHits();
		long ms = searchResponse.getTookInMillis();
		if (q != null)
		{
			LOG.debug("Counted {} Elasticsearch [{}] docs using query [{}] in {}ms", count, type, q, ms);
		}
		else
		{
			LOG.debug("Counted {} Elasticsearch [{}] docs in {}ms", count, type, ms);
		}
		return count;
	}

	/**
	 * Deletes a document from an index.
	 *
	 * @param index the name of the index
	 * @param id    the ID of the document
	 * @param type  tye type of the document
	 */
	public void deleteById(String index, String id, String type)
	{
		LOG.trace("Deleting Elasticsearch '{}' doc with id [{}] ...", type, id);
		GetResponse response = client.prepareGet(index, type, id).get();
		LOG.debug("Retrieved document type [{}] with id [{}] in index [{}]", type, id, index);
		if (response.isExists())
		{
			client.prepareDelete(index, type, id).get();
		}
		LOG.debug("Deleted Elasticsearch '{}' doc with id [{}]", type, id);
	}

	/**
	 * Checks if a type exists in an index.
	 *
	 * @param type      the name of the type
	 * @param indexName the name of the index
	 * @return boolean indicating if the type exists in the index
	 */
	public boolean isTypeExists(String type, String indexName)
	{
		LOG.trace("Check whether type [{}] exists in index [{}]...", type, indexName);
		TypesExistsResponse typesExistsResponse = client.admin().indices().prepareTypesExists(indexName).setTypes(type)
				.get();
		boolean typeExists = typesExistsResponse.isExists();
		LOG.trace("Checked whether type [{}] exists in index [{}]", type, indexName);
		return typeExists;
	}

	/**
	 * Tries to delete the mapping for a type in an index.
	 *
	 * @param type      name of the type
	 * @param indexName name of the index
	 * @return boolean indicating success of the deletion
	 */
	public boolean deleteMapping(String type, String indexName)
	{
		// FIXME
		//throw new UnsupportedOperationException("FIXME");
		//		DeleteMappingResponse deleteMappingResponse = client.admin().indices().prepareDeleteMapping(indexName)
		//				.setType(type).get();
		//		return deleteMappingResponse.isAcknowledged();
		return true;
	}

	/**
	 * Deletes all documents of a type in an index.
	 *
	 * @param type      tye name of the type of the documents
	 * @param indexName the name of the index
	 * @return boolean indicating success of the deletion
	 */
	public boolean deleteAllDocumentsOfType(String type, String indexName)
	{
		LOG.trace("Deleting all Elasticsearch '{}' docs ...", type);
		BulkByScrollResponse response = DeleteByQueryAction.INSTANCE.newRequestBuilder(client)
				.filter(QueryBuilders.termQuery("_type", type)).source(indexName).get();
		if (!response.getBulkFailures().isEmpty())
		{
			return false;
		}
		LOG.debug("Deleted all Elasticsearch '{}' docs.", type);
		return true;
	}

	public void flushIndex(String indexName)
	{
		LOG.trace("Flushing Elasticsearch index [{}] ...", indexName);
		client.admin().indices().prepareFlush(indexName).get();
		LOG.debug("Flushed Elasticsearch index [{}]", indexName);
	}

	public SearchResponse search(SearchType searchType, SearchRequest request, String indexName)
	{
		SearchRequestBuilder builder = client.prepareSearch(indexName);
		generator.buildSearchRequest(builder, searchType, request.getEntityType(), request.getQuery(),
				request.getAggregateAttribute1(), request.getAggregateAttribute2(),
				request.getAggregateAttributeDistinct());
		LOG.trace("*** REQUEST\n{}", builder);
		SearchResponse response = builder.get();
		LOG.trace("*** RESPONSE\n{}", response);
		return response;
	}

	/**
	 * Performs a search query and returns the result as a {@link Stream} of ID strings.
	 */
	public Stream<String> searchForIds(Consumer<SearchRequestBuilder> queryBuilder, String queryToString, String type,
			String indexName)
	{
		SearchHits searchHits = search(queryBuilder, queryToString, type, indexName);
		return Arrays.stream(searchHits.hits()).map(SearchHit::getId);
	}

	/**
	 * Performs a search query and returns the result as a {@link Stream} of ID strings.
	 */
	public Stream<String> searchForIdsWithScanScroll(Consumer<SearchRequestBuilder> queryBuilder, String queryToString,
			String type, String indexName)
	{
		LOG.trace("Searching Elasticsearch '{}' docs using query [{}] ...", type, queryToString);
		SearchRequestBuilder searchRequestBuilder = client.prepareSearch(indexName);
		queryBuilder.accept(searchRequestBuilder);
		searchRequestBuilder.setScroll(SCROLL_KEEP_ALIVE).setSize(SCROLL_SIZE);
		LOG.trace("SearchRequest: {}", searchRequestBuilder);
		SearchResponse originalSearchResponse = searchRequestBuilder.execute().actionGet();

		LOG.debug("Searched Elasticsearch '{}' docs using query [{}] in {}ms", type, queryToString,
				originalSearchResponse.getTookInMillis());

		Stream<SearchResponse> infiniteResponses = Stream.iterate(originalSearchResponse,
				searchResponse -> client.prepareSearchScroll(searchResponse.getScrollId()).setScroll(SCROLL_KEEP_ALIVE)
						.execute().actionGet());
		return StreamUtils.takeWhile(infiniteResponses, searchResponse -> searchResponse.getHits().getHits().length > 0)
				.flatMap(searchResponse -> Arrays.stream(searchResponse.getHits().getHits())).map(SearchHit::getId);
	}

	private SearchHits search(Consumer<SearchRequestBuilder> queryBuilder, String queryToString, String type,
			String indexName)
	{
		LOG.trace("Searching Elasticsearch '{}' docs using query [{}] ...", type, queryToString);
		SearchRequestBuilder searchRequestBuilder = client.prepareSearch(indexName);
		queryBuilder.accept(searchRequestBuilder);
		LOG.trace("SearchRequest: {}", searchRequestBuilder);
		SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();

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
		finally
		{
			if (awaitCompletion)
			{
				waitForCompletion(bulkProcessor);
			}
		}
	}
}
