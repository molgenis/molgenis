package org.molgenis.elasticsearch;

import static org.molgenis.elasticsearch.util.MapperTypeSanitizer.sanitizeMapperType;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.deletebyquery.DeleteByQueryResponse;
import org.elasticsearch.action.deletebyquery.IndexDeleteByQueryResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.molgenis.elasticsearch.index.IndexRequestGenerator;
import org.molgenis.elasticsearch.index.MappingsBuilder;
import org.molgenis.elasticsearch.request.SearchRequestGenerator;
import org.molgenis.elasticsearch.response.ResponseParser;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.tupletable.TableException;
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

		String documentType = request.getDocumentType() == null ? null : sanitizeMapperType(request.getDocumentType());
		SearchRequestBuilder requestBuilder = generator.buildSearchRequest(documentType, searchType,
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
		if (!entities.iterator().hasNext())
		{
			return;
		}

		String documentTypeSantized = sanitizeMapperType(documentType);

		LOG.info("Going to update index [" + indexName + "] for document type [" + documentType + "]");
		deleteDocumentsByType(documentTypeSantized);

		LOG.info("Going to insert documents of type [" + documentType + "]");
		IndexRequestGenerator requestGenerator = new IndexRequestGenerator(client, indexName);

		BulkRequestBuilder request = requestGenerator.buildIndexRequest(documentTypeSantized, entities);
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
	public void indexTupleTable(String documentType, TupleTable tupleTable)
	{
		System.out.println("Indexing " + documentType);

		try
		{
			System.out.println("Count = " + tupleTable.getCount());

			if (tupleTable.getCount() == 0)
			{
				return;
			}
		}
		catch (TableException e)
		{
			throw new RuntimeException(e);
		}

		String documentTypeSantized = sanitizeMapperType(documentType);

		LOG.info("Going to create mapping for documentType [" + documentType + "]");
		createMappings(documentTypeSantized, tupleTable);

		LOG.info("Going to update index [" + indexName + "] for document type [" + documentType + "]");
		deleteDocumentsByType(documentTypeSantized);

		LOG.info("Going to insert documents of type [" + documentType + "]");
		IndexRequestGenerator requestGenerator = new IndexRequestGenerator(client, indexName);

		Iterable<BulkRequestBuilder> requests = requestGenerator.buildIndexRequest(documentTypeSantized, tupleTable);
		for (BulkRequestBuilder request : requests)
		{
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
	}

	@Override
	public boolean documentTypeExists(String documentType)
	{
		String documentTypeSantized = sanitizeMapperType(documentType);

		return client.admin().indices().typesExists(new TypesExistsRequest(new String[]
		{ indexName }, documentTypeSantized)).actionGet().exists();
	}

	@Override
	public void deleteDocumentsByType(String documentType)
	{
		LOG.info("Going to delete all documents of type [" + documentType + "]");

		String documentTypeSantized = sanitizeMapperType(documentType);

		DeleteByQueryResponse deleteResponse = client.prepareDeleteByQuery(indexName)
				.setQuery(new TermQueryBuilder("_type", documentTypeSantized)).execute().actionGet();

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

	@Override
	public void deleteDocumentByIds(String documentType, List<String> documentIds)
	{
		LOG.info("Going to delete document of type [" + documentType + "] with Id : " + documentIds);

		String documentTypeSantized = sanitizeMapperType(documentType);

		for (String documentId : documentIds)
		{
			DeleteResponse deleteResponse = client.prepareDelete(indexName, documentTypeSantized, documentId)
					.setRefresh(true).execute().actionGet();
			if (deleteResponse != null)
			{
				if (deleteResponse.isNotFound())
				{
					throw new ElasticSearchException("Delete failed. Returned headers:" + deleteResponse.getHeaders());
				}
			}
		}
		LOG.info("Delete done.");
	}

	@Override
	public void updateDocumentById(String documentType, String documentId, String updateScript)
	{
		LOG.info("Going to delete document of type [" + documentType + "] with Id : " + documentId);

		String documentTypeSantized = sanitizeMapperType(documentType);
		UpdateResponse updateResponse = client.prepareUpdate(indexName, documentTypeSantized, documentId)
				.setScript("ctx._source." + updateScript).execute().actionGet();

		if (updateResponse == null)
		{
			throw new ElasticSearchException("update failed.");
		}

		LOG.info("Update done.");
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

	private void createMappings(String documentType, TupleTable tupleTable)
	{
		String documentTypeSantized = sanitizeMapperType(documentType);

		XContentBuilder jsonBuilder;
		try
		{
			jsonBuilder = MappingsBuilder.buildMapping(documentTypeSantized, tupleTable);
		}
		catch (Exception e)
		{
			String msg = "Exception creating mapping for documentType [" + documentType + "]";
			LOG.error(msg, e);
			throw new ElasticSearchException(msg, e);
		}

		try
		{
			LOG.info("Going to create mapping [" + jsonBuilder.string() + "]");
		}
		catch (IOException e)
		{
			LOG.error(e);
		}

		PutMappingResponse response = client.admin().indices().preparePutMapping(indexName)
				.setType(documentTypeSantized).setSource(jsonBuilder).execute().actionGet();

		if (!response.acknowledged())
		{
			throw new ElasticSearchException("Creation of mapping for documentType [" + documentType
					+ "] failed. Response=" + response);
		}
		LOG.info("Mapping for documentType [" + documentType + "] created");
	}
}
