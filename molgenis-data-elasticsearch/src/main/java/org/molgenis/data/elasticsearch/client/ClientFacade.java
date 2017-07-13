package org.molgenis.data.elasticsearch.client;

import org.apache.lucene.search.Explanation;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.ResourceAlreadyExistsException;
import org.elasticsearch.ResourceNotFoundException;
import org.elasticsearch.action.DocWriteRequest;
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
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.explain.ExplainRequestBuilder;
import org.elasticsearch.action.explain.ExplainResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.sort.SortBuilder;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.MolgenisQueryException;
import org.molgenis.data.elasticsearch.client.model.SearchHit;
import org.molgenis.data.elasticsearch.client.model.SearchHits;
import org.molgenis.data.elasticsearch.generator.model.*;
import org.molgenis.data.index.exception.IndexAlreadyExistsException;
import org.molgenis.data.index.exception.IndexException;
import org.molgenis.data.index.exception.UnknownIndexException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.*;
import static org.elasticsearch.action.DocWriteRequest.OpType.INDEX;
import static org.molgenis.data.elasticsearch.ElasticsearchService.MAX_BATCH_SIZE;

/**
 * Elasticsearch client facade:
 * - Provides simplified interface to Elasticsearch transport client
 * - Reduces Elasticsearch transport client dependencies
 * - Translates Elasticsearch transport client exceptions to MOLGENIS indexing exceptions
 * - Logs requests and responses
 */
public class ClientFacade implements Closeable
{
	private static final Logger LOG = LoggerFactory.getLogger(ClientFacade.class);

	private final Client client;
	private final SettingsContentBuilder settingsBuilder;
	private final MappingContentBuilder mappingSourceBuilder;
	private final SortContentBuilder sortContentBuilder;
	private final BulkProcessorFactory bulkProcessorFactory;

	public ClientFacade(Client client)
	{
		this.client = requireNonNull(client);
		this.settingsBuilder = new SettingsContentBuilder();
		this.mappingSourceBuilder = new MappingContentBuilder();
		this.sortContentBuilder = new SortContentBuilder();
		this.bulkProcessorFactory = new BulkProcessorFactory();
	}

	public void createIndex(Index index, IndexSettings indexSettings, Stream<Mapping> mappingStream)
	{
		if (LOG.isTraceEnabled())
		{
			LOG.trace("Creating index '{}' ...", index.getName());
		}

		CreateIndexRequestBuilder createIndexRequest = createIndexRequest(index, indexSettings, mappingStream);

		CreateIndexResponse createIndexResponse;
		try
		{
			createIndexResponse = createIndexRequest.get();
		}
		catch (ResourceAlreadyExistsException e)
		{
			LOG.debug("", e);
			throw new IndexAlreadyExistsException(index.getName());
		}
		catch (ElasticsearchException e)
		{
			LOG.error("", e);
			throw new IndexException(format("Error creating index '%s'.", index.getName()));
		}

		// 'acknowledged' indicates whether the index was successfully created in the cluster before the request timeout
		if (!createIndexResponse.isAcknowledged())
		{
			LOG.warn("Index '{}' creation possibly failed (acknowledged=false)", index.getName());
		}
		// 'shards_acknowledged' indicates whether the requisite number of shard copies were started for each shard in the index before timing out
		if (!createIndexResponse.isShardsAcked())
		{
			LOG.warn("Index '{}' creation possibly failed (shards_acknowledged=false)", index.getName());
		}

		if (LOG.isDebugEnabled())
		{
			LOG.debug("Created index '{}'.", index.getName());
		}
	}

	private CreateIndexRequestBuilder createIndexRequest(Index index, IndexSettings indexSettings,
			Stream<Mapping> mappingStream)
	{
		XContentBuilder settings = settingsBuilder.createSettings(indexSettings);
		Map<String, XContentBuilder> mappings = mappingStream.collect(
				toMap(Mapping::getType, mappingSourceBuilder::createMapping, (u, v) ->
				{
					throw new IllegalStateException(String.format("Duplicate key %s", u));
				}, LinkedHashMap::new));

		CreateIndexRequestBuilder createIndexRequest = client.admin().indices().prepareCreate(index.getName());
		createIndexRequest.setSettings(settings);
		mappings.forEach(createIndexRequest::addMapping);
		return createIndexRequest;
	}

	public boolean indexesExist(Index index)
	{
		return indexesExist(singletonList(index));
	}

	private boolean indexesExist(List<Index> indexes)
	{
		if (LOG.isTraceEnabled())
		{
			LOG.trace("Determining index(es) '{}' existence ...", toString(indexes));
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
			throw new IndexException(format("Error determining index(es) '%s' existence.", toString(indexes)));
		}

		boolean exists = indicesExistsResponse.isExists();
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Determined index(es) '{}' existence: {}.", toString(indexes), exists);
		}
		return exists;
	}

	public void deleteIndex(Index index)
	{
		deleteIndexes(singletonList(index));
	}

	private void deleteIndexes(List<Index> indexes)
	{
		if (LOG.isTraceEnabled())
		{
			LOG.trace("Deleting index(es) '{}' ...", toString(indexes));
		}

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
			throw new IndexException(format("Error deleting index(es) '%s'.", toString(indexes)));
		}

		if (!deleteIndexResponse.isAcknowledged())
		{
			throw new IndexException(format("Error deleting index(es) '%s'.", toString(indexes)));
		}
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Deleted index(es) '{}'.", toString(indexes));
		}
	}

	public void refreshIndexes()
	{
		refreshIndexes(singletonList(Index.create("_all")));
	}

	private void refreshIndexes(List<Index> indexes)
	{
		if (LOG.isTraceEnabled())
		{
			LOG.trace("Refreshing index(es) '{}' ...", toString(indexes));
		}

		String[] indexNames = toIndexNames(indexes);
		RefreshRequestBuilder refreshRequest = client.admin().indices().prepareRefresh(indexNames);

		RefreshResponse refreshResponse;
		try
		{
			refreshResponse = refreshRequest.get();
		}
		catch (ResourceNotFoundException e)
		{
			LOG.debug("", e);
			throw new UnknownIndexException(toIndexNames(indexes));
		}
		catch (ElasticsearchException e)
		{
			LOG.error("", e);
			throw new IndexException(format("Error refreshing index(es) '%s'.", toString(indexes)));
		}

		if (refreshResponse.getFailedShards() > 0)
		{
			LOG.error(stream(refreshResponse.getShardFailures()).map(ShardOperationFailedException::toString)
																.collect(joining("\n")));
			throw new IndexException(format("Error refreshing index(es) '%s'.", toString(indexes)));
		}

		if (LOG.isDebugEnabled())
		{
			LOG.debug("Refreshed index(es) '{}'", toString(indexes));
		}
	}

	public long getCount(Index index)
	{
		return getCount(singletonList(index));
	}

	private long getCount(List<Index> indexes)
	{
		return getCount(null, indexes);
	}

	public long getCount(QueryBuilder query, Index index)
	{
		return getCount(query, singletonList(index));
	}

	private long getCount(QueryBuilder query, List<Index> indexes)
	{
		if (LOG.isTraceEnabled())
		{
			if (query != null)
			{
				LOG.trace("Counting docs in index(es) '{}' with query '{}' ...", toString(indexes), query);
			}
			else
			{
				LOG.trace("Counting docs in index(es) '{}' ...", toString(indexes));
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
			throw new IndexException(format("Error counting docs in index(es) '%s'.", toString(indexes)));
		}

		if (searchResponse.getFailedShards() > 0)
		{
			LOG.error(
					stream(searchResponse.getShardFailures()).map(ShardSearchFailure::toString).collect(joining("\n")));
			throw new IndexException(format("Error counting docs in index(es) '%s'.", toString(indexes)));
		}
		if (searchResponse.isTimedOut())
		{
			throw new IndexException(format("Timeout while counting docs in index(es) '%s'.", toString(indexes)));
		}

		long totalHits = searchResponse.getHits().getTotalHits();
		if (LOG.isDebugEnabled())
		{
			if (query != null)
			{
				LOG.debug("Counted {} docs in index(es) '{}' with query '{}' in {}ms.", totalHits, toString(indexes),
						query, searchResponse.getTookInMillis());
			}
			else
			{
				LOG.debug("Counted {} docs in index(es) '{}' in {}ms.", totalHits, toString(indexes),
						searchResponse.getTookInMillis());
			}
		}
		return totalHits;
	}

	public SearchHits search(QueryBuilder query, int from, int size, List<Index> indexes)
	{
		return search(query, from, size, null, indexes);
	}

	public SearchHits search(QueryBuilder query, int from, int size, Sort sort, Index index)
	{
		return search(query, from, size, sort, singletonList(index));
	}

	private SearchHits search(QueryBuilder query, int from, int size, Sort sort, List<Index> indexes)
	{
		if (size > 10000)
		{
			throw new MolgenisQueryException(
					String.format("Batch size of %s exceeds the maximum batch size of %s for search queries", size,
							MAX_BATCH_SIZE));
		}

		if (LOG.isTraceEnabled())
		{
			if (sort != null)
			{
				LOG.trace("Searching docs [{}-{}] in index(es) '{}' with query '{}' sorted by '{}' ...", from,
						from + size, toString(indexes), query, sort);
			}
			else
			{
				LOG.trace("Searching docs [{}-{}] in index(es) '{}' with query '{}' ...", from, from + size,
						toString(indexes), query);
			}
		}

		SearchRequestBuilder searchRequest = createSearchRequest(query, from, size, sort, null, indexes);

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
					format("Error searching docs in index(es) '%s' with query '%s'.", toString(indexes), query));
		}
		if (searchResponse.getFailedShards() > 0)
		{
			LOG.error(
					stream(searchResponse.getShardFailures()).map(ShardSearchFailure::toString).collect(joining("\n")));
			throw new IndexException(
					format("Error searching docs in index(es) '%s' with query '%s'.", toString(indexes), query));
		}
		if (searchResponse.isTimedOut())
		{
			throw new IndexException(
					format("Timeout searching counting docs in index(es) '%s'  with query '%s'.", toString(indexes),
							query));
		}

		if (LOG.isDebugEnabled())
		{
			if (sort != null)
			{
				LOG.debug("Searched {} docs in index(es) '{}' with query '{}' sorted by '{}' in {}ms.",
						searchResponse.getHits().getTotalHits(), toString(indexes), query, sort,
						searchResponse.getTookInMillis());
			}
			else
			{
				LOG.debug("Searched {} docs in index(es) '{}' with query '{}' in {}ms.",
						searchResponse.getHits().getTotalHits(), toString(indexes), query,
						searchResponse.getTookInMillis());
			}
		}
		return createSearchResponse(searchResponse);
	}

	private SearchRequestBuilder createSearchRequest(QueryBuilder query, Integer from, Integer size, Sort sort,
			List<AggregationBuilder> aggregations, List<Index> indexes)
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
		if (sort != null)
		{
			List<SortBuilder> sorts = sortContentBuilder.createSorts(sort);
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
		List<SearchHit> searchHitList = stream(searchHits.getHits()).map(
				hit -> SearchHit.create(hit.getId(), hit.getIndex())).collect(toList());
		return SearchHits.create(searchHits.getTotalHits(), searchHitList);
	}

	public Aggregations aggregate(List<AggregationBuilder> aggregations, QueryBuilder query, Index index)
	{
		return aggregate(aggregations, query, singletonList(index));
	}

	private Aggregations aggregate(List<AggregationBuilder> aggregations, QueryBuilder query, List<Index> indexes)
	{
		if (LOG.isTraceEnabled())
		{
			if (query != null)
			{
				LOG.trace("Aggregating docs in index(es) '{}' with aggregations '{}' and query '{}' ...",
						toString(indexes), aggregations, query);
			}
			else
			{
				LOG.trace("Aggregating docs in index(es) '{}' with aggregations '{}' ...", toString(indexes),
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
			throw new IndexException(format("Error aggregating docs in index(es) '%s'.", toString(indexes)));
		}
		if (searchResponse.getFailedShards() > 0)
		{
			LOG.error(
					stream(searchResponse.getShardFailures()).map(ShardSearchFailure::toString).collect(joining("\n")));
			throw new IndexException(format("Error aggregating docs in index(es) '%s'.", toString(indexes)));
		}
		if (searchResponse.isTimedOut())
		{
			throw new IndexException(format("Timeout aggregating docs in index(es) '%s'.", toString(indexes)));
		}

		if (LOG.isDebugEnabled())
		{
			if (query != null)
			{
				LOG.debug("Aggregated docs in index(es) '{}' with aggregations '{}' and query '{}' in {}ms.",
						toString(indexes), aggregations, query, searchResponse.getTookInMillis());
			}
			else
			{
				LOG.debug("Aggregated docs in index(es) '{}' with aggregations '{}' in {}ms.", toString(indexes),
						aggregations, searchResponse.getTookInMillis());
			}
		}
		return searchResponse.getAggregations();
	}

	public Explanation explain(SearchHit searchHit, QueryBuilder query)
	{
		if (LOG.isTraceEnabled())
		{
			LOG.trace("Explaining doc with id '{}' in index '{}' for query '{}' ...", searchHit.getId(),
					searchHit.getIndex(), query);
		}

		String indexName = searchHit.getIndex();
		//FIXME: ClientFacade shouldn't assume that typename equals typename
		ExplainRequestBuilder explainRequestBuilder = client.prepareExplain(indexName, indexName, searchHit.getId())
															.setQuery(query);
		ExplainResponse explainResponse;
		try
		{
			explainResponse = explainRequestBuilder.get();
		}
		catch (ElasticsearchException e)
		{
			LOG.error("", e);
			throw new IndexException(
					format("Error explaining doc with id '%s' in index '%s' for query '%s'.", searchHit.getId(),
							searchHit.getIndex(), query));
		}

		if (LOG.isDebugEnabled())
		{
			LOG.debug("Explained doc with id '{}' in index '{}' for query.", searchHit.getId(), searchHit.getIndex(),
					query);
		}
		return explainResponse.getExplanation();
	}

	public void index(Index index, Document document)
	{
		if (LOG.isTraceEnabled())
		{
			LOG.trace("Indexing doc with id '{}' in index '{}' ...", document.getId(), index.getName());
		}

		String indexName = index.getName();
		String documentId = document.getId();
		XContentBuilder source = document.getContent();
		IndexRequestBuilder indexRequest = client.prepareIndex()
												 .setIndex(indexName)
												 .setType(indexName)
												 .setId(documentId)
												 .setSource(source);

		IndexResponse indexResponse;
		try
		{
			indexResponse = indexRequest.get();
		}
		catch (ResourceNotFoundException e)
		{
			LOG.error("", e);
			throw new UnknownIndexException(index.getName());
		}
		catch (ElasticsearchException e)
		{
			LOG.debug("", e);
			throw new IndexException(format("Error indexing doc with id '%s' in index '%s'.", documentId, indexName));
		}

		//TODO: Is it good enough if at least one shard succeeds? Shouldn't we at least log something if failures > 0?
		if (indexResponse.getShardInfo().getSuccessful() == 0)
		{
			LOG.error(Arrays.stream(indexResponse.getShardInfo().getFailures())
							//FIXME: logs Object.toString()
							.map(ReplicationResponse.ShardInfo.Failure::toString)
							.collect(joining("\n")));
			throw new IndexException(format("Error indexing doc with id '%s' in index '%s'.", documentId, indexName));
		}

		if (LOG.isDebugEnabled())
		{
			LOG.debug("Indexed doc with id '{}' in index '{}'.", documentId, indexName);
		}
	}

	public void deleteById(Index index, Document document)
	{
		if (LOG.isTraceEnabled())
		{
			LOG.trace("Deleting doc with id '{}' in index '{}' ...", document.getId(), index.getName());
		}

		String indexName = index.getName();
		String documentId = document.getId();
		DeleteRequestBuilder deleteRequest = client.prepareDelete()
												   .setIndex(indexName)
												   .setType(indexName)
												   .setId(documentId);

		DeleteResponse deleteResponse;
		try
		{
			deleteResponse = deleteRequest.get();
		}
		catch (ResourceNotFoundException e)
		{
			LOG.error("", e);
			throw new UnknownIndexException(index.getName());
		}
		catch (ElasticsearchException e)
		{
			LOG.debug("", e);
			throw new IndexException(format("Error deleting doc with id '%s' in index '%s'.", documentId, indexName));
		}

		//TODO: Check why not check shardinfo?

		if (LOG.isDebugEnabled())
		{
			LOG.debug("Deleted doc with id '{}' in index '{}' and status '{}'", documentId, indexName,
					deleteResponse.getResult());
		}
	}

	public void processDocumentActions(Stream<DocumentAction> documentActions)
	{
		LOG.trace("Processing document actions ...");
		BulkProcessor bulkProcessor = bulkProcessorFactory.create(client);
		try
		{
			documentActions.forEachOrdered(documentAction ->
			{
				DocWriteRequest docWriteRequest = toDocWriteRequest(documentAction);
				bulkProcessor.add(docWriteRequest);
			});
		}
		finally
		{
			waitForCompletion(bulkProcessor);
			LOG.debug("Processed document actions.");
		}
	}

	private DocWriteRequest toDocWriteRequest(DocumentAction documentAction)
	{
		String indexName = documentAction.getIndex().getName();
		String documentId = documentAction.getDocument().getId();

		DocWriteRequest docWriteRequest;
		switch (documentAction.getOperation())
		{
			case INDEX:
				XContentBuilder source = documentAction.getDocument().getContent();
				if (source == null)
				{
					throw new IndexException(format("Document action is missing document source '%s'", documentAction));
				}
				docWriteRequest = Requests.indexRequest(indexName)
										  .type(indexName)
										  .id(documentId)
										  .source(source)
										  .opType(INDEX);
				break;
			case DELETE:
				docWriteRequest = Requests.deleteRequest(indexName).type(indexName).id(documentId);
				break;
			default:
				throw new RuntimeException(format("Unknown document operation '%s'", documentAction.getOperation()));
		}
		return docWriteRequest;
	}

	private void waitForCompletion(BulkProcessor bulkProcessor)
	{
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
	}

	private String[] toIndexNames(List<Index> indexes)
	{
		return indexes.stream().map(Index::getName).toArray(String[]::new);
	}

	private String toString(List<Index> indexes)
	{
		return indexes.stream().map(Index::getName).collect(joining(", "));
	}

	@Override
	public void close() throws IOException
	{
		try
		{
			client.close();
		}
		catch (ElasticsearchException e)
		{
			LOG.error("Error closing Elasticsearch client", e);
		}
	}
}
