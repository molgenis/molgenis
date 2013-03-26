package org.molgenis.elasticsearch;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.deletebyquery.DeleteByQueryResponse;
import org.elasticsearch.action.deletebyquery.IndexDeleteByQueryResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.molgenis.elasticsearch.index.IndexRequestGenerator;
import org.molgenis.elasticsearch.request.SearchRequestGenerator;
import org.molgenis.elasticsearch.response.ResponseParser;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.tupletable.TupleTable;
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

		createIndexIfNotExists();
	}

	@Override
	public SearchResult search(SearchRequest request)
	{
		return search(SearchType.QUERY_AND_FETCH, request);
	}

	@Override
	public long count(String documentType, List<QueryRule> queryRules)
	{
		SearchRequest request = new SearchRequest(documentType, queryRules, Collections.<String> emptyList());
		SearchResult result = search(SearchType.COUNT, request);

		return result.getTotalHitCount();
	}

	private SearchResult search(SearchType searchType, SearchRequest request)
	{

		SearchRequestGenerator generator = new SearchRequestGenerator(client.prepareSearch(indexName));
		SearchRequestBuilder requestBuilder = generator.buildSearchRequest(request.getDocumentType(), searchType,
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
	public void updateIndex(String documentType, Iterable<? extends Entity> entities)
	{
		LOG.info("Going to update index [" + indexName + "] for document type [" + documentType + "]");
		deleteDocumentsByType(documentType);

		LOG.info("Going to insert documents of type [" + documentType + "]");
		IndexRequestGenerator requestGenerator = new IndexRequestGenerator(client, indexName);

		BulkRequestBuilder request = requestGenerator.buildIndexRequest(documentType, entities);
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

		try
		{
			for (Class<? extends Entity> clazz : db.getEntityClasses())
			{
				String simpleName = clazz.getSimpleName();
				List<? extends Entity> entities = db.find(clazz);
				if ((entities != null) && !entities.isEmpty())
				{
					LOG.info("Indexing [" + simpleName + "]. Count=" + entities.size());

					updateIndex(simpleName, db.find(clazz));
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		LOG.info("Indexing done");
	}

	@Override
	public void indexTupleTable(String documentType, TupleTable tupleTable)
	{
		if (!tupleTable.iterator().hasNext())
		{
			return;
		}
		LOG.info("Going to update index [" + indexName + "] for document type [" + documentType + "]");
		deleteDocumentsByType(documentType);

		LOG.info("Going to insert documents of type [" + documentType + "]");
		IndexRequestGenerator requestGenerator = new IndexRequestGenerator(client, indexName);

		BulkRequestBuilder request = requestGenerator.buildIndexRequest(documentType, tupleTable);
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
	public boolean documentTypeExists(String documentType)
	{
		return client.admin().indices().typesExists(new TypesExistsRequest(new String[]
		{ indexName }, documentType)).actionGet().exists();
	}

	private void deleteDocumentsByType(String documentType)
	{
		LOG.info("Going to delete all documents of type [" + documentType + "]");
		DeleteByQueryResponse deleteResponse = client.prepareDeleteByQuery(indexName)
				.setQuery(new TermQueryBuilder("_type", documentType)).execute().actionGet();

		if (deleteResponse != null)
		{
			IndexDeleteByQueryResponse idbqr = deleteResponse.index(indexName);
			if ((idbqr != null) && (idbqr.failedShards() > 0))
			{
				throw new ElasticSearchException("Delete failed. Returned headers:" + idbqr.getHeaders());
			}
		}

		LOG.info("Delete done.");
	}

	private void createIndexIfNotExists()
	{
		// Wait until elasticsearch is ready
		client.admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();
		boolean hasIndex = client.admin().indices().exists(new IndicesExistsRequest(indexName)).actionGet().exists();
		if (!hasIndex)
		{
			CreateIndexResponse response = client.admin().indices().prepareCreate(indexName).execute().actionGet();
			if (!response.acknowledged())
			{
				throw new ElasticSearchException("Creation of index [" + indexName + "] failed. Response=" + response);
			}
			LOG.info("Index [" + indexName + "] created");
		}
	}

}
