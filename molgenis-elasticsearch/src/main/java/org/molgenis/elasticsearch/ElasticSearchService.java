package org.molgenis.elasticsearch;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.molgenis.elasticsearch.index.IndexRequestGenerator;
import org.molgenis.elasticsearch.request.SearchRequestGenerator;
import org.molgenis.elasticsearch.response.ResponseParser;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;
import org.molgenis.util.Entity;

/**
 * ElasticSearch implementation of the SearchService interface
 * 
 * @author erwin
 * 
 */
public class ElasticSearchService implements SearchService
{
	public static final String REST_API_BASE_URL = "/api/v1";
	private static final Logger LOG = Logger.getLogger(ElasticSearchService.class);
	private final String indexName;
	private final Client client;
	private final ResponseParser responseParser = new ResponseParser(REST_API_BASE_URL);

	public ElasticSearchService(Client client, String indexName)
	{
		if (client == null)
		{
			throw new IllegalArgumentException("Client is null");
		}

		if (indexName == null)
		{
			throw new IllegalArgumentException("IndexName is null");
		}

		this.indexName = indexName;
		this.client = client;
	}

	@Override
	public SearchResult search(SearchRequest request)
	{
		return search(SearchType.QUERY_AND_FETCH, request);
	}

	@Override
	public long count(String entityName, List<QueryRule> queryRules)
	{
		SearchRequest request = new SearchRequest(entityName, queryRules, Collections.<String> emptyList());
		SearchResult result = search(SearchType.COUNT, request);

		return result.getTotalHitCount();
	}

	private SearchResult search(SearchType searchType, SearchRequest request)
	{

		SearchRequestGenerator generator = new SearchRequestGenerator(client.prepareSearch(indexName));
		SearchRequestBuilder requestBuilder = generator.buildSearchRequest(request.getEntityName(), searchType,
				request.getQueryRules(), request.getFieldsToReturn());
		if (LOG.isDebugEnabled())
		{
			LOG.debug("SearchRequestBuilder:" + requestBuilder);
		}

		SearchResponse response = requestBuilder.execute().actionGet();
		if (LOG.isDebugEnabled())
		{
			LOG.debug("SearchResponse:" + response);
		}

		return responseParser.parseSearchResponse(response);

	}

	@Override
	public void updateIndex(String documentName, Iterable<? extends Entity> entities)
	{
		LOG.info("Going to update index [" + indexName + "]");

		IndexRequestGenerator requestGenerator = new IndexRequestGenerator(client, indexName);

		BulkRequestBuilder request = requestGenerator.buildIndexRequest(documentName, entities);
		LOG.info("Request created");
		if (LOG.isDebugEnabled())
		{
			LOG.debug("BulkRequest:" + request);
		}

		BulkResponse response = request.execute().actionGet();
		LOG.info("Request done");
		if (LOG.isDebugEnabled())
		{
			LOG.debug("BulkResponse:" + response);
		}

		if (response.hasFailures())
		{
			throw new ElasticSearchException(response.buildFailureMessage());
		}

	}

	@Override
	public void indexDatabase(Database db) throws DatabaseException
	{
		LOG.info("Start indexing database");

		for (String entityName : db.getEntityNames())
		{
			LOG.info("Indexing [" + entityName + "]");
			Class<? extends Entity> clazz = db.getClassForName(entityName);
			List<? extends Entity> entities = db.find(clazz);

			updateIndex(entityName, entities);
		}

		LOG.info("Indexing ready");
	}

}
