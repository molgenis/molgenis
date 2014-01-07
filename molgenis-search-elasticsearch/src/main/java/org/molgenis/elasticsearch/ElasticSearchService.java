package org.molgenis.elasticsearch;

import static org.molgenis.elasticsearch.util.MapperTypeSanitizer.sanitizeMapperType;

import java.io.IOException;
import java.util.ArrayList;
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
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.elasticsearch.index.IndexRequestGenerator;
import org.molgenis.elasticsearch.index.MappingsBuilder;
import org.molgenis.elasticsearch.request.SearchRequestGenerator;
import org.molgenis.elasticsearch.response.ResponseParser;
import org.molgenis.search.MultiSearchRequest;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;

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
	private final SearchRequestGenerator generator = new SearchRequestGenerator();

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
	public SearchResult multiSearch(MultiSearchRequest request)
	{
		return multiSearch(SearchType.QUERY_AND_FETCH, request);
	}

	@Override
	public long count(String documentType, Query q)
	{

		SearchRequest request = new SearchRequest(documentType, q, Collections.<String> emptyList());
		SearchResult result = search(SearchType.COUNT, request);

		return result.getTotalHitCount();
	}

	public SearchResult multiSearch(SearchType searchType, MultiSearchRequest request)
	{

		List<String> documentTypes = null;
		if (request.getDocumentType() != null)
		{
			documentTypes = new ArrayList<String>();
			for (String documentType : request.getDocumentType())
			{
				documentTypes.add(sanitizeMapperType(documentType));
			}
		}

		SearchRequestBuilder builder = client.prepareSearch(indexName);

		generator.buildSearchRequest(builder, documentTypes, searchType, request.getQuery(),
				request.getFieldsToReturn());

		if (LOG.isDebugEnabled())
		{
			LOG.debug("SearchRequestBuilder:" + builder);
		}

		SearchResponse response = builder.execute().actionGet();
		if (LOG.isDebugEnabled())
		{
			LOG.debug("SearchResponse:" + response);
		}

		return responseParser.parseSearchResponse(response);
	}

	private SearchResult search(SearchType searchType, SearchRequest request)
	{
		SearchRequestBuilder builder = client.prepareSearch(indexName);
		String documentType = request.getDocumentType() == null ? null : sanitizeMapperType(request.getDocumentType());

		generator
				.buildSearchRequest(builder, documentType, searchType, request.getQuery(), request.getFieldsToReturn());

		if (LOG.isDebugEnabled())
		{
			LOG.debug("SearchRequestBuilder:" + builder);
		}

		SearchResponse response = builder.execute().actionGet();
		if (LOG.isDebugEnabled())
		{
			LOG.debug("SearchResponse:" + response);
		}

		return responseParser.parseSearchResponse(response);
	}

	@Override
	public void indexRepository(Repository<? extends Entity> repository)
	{
		if (!repository.iterator().hasNext())
		{
			return;
		}

		try
		{
			LOG.info("Going to create mapping for repository [" + repository.getName() + "]");
			createMappings(repository);
		}
		catch (IOException e)
		{
			String msg = "Exception creating mapping for repository [" + repository.getName() + "]";
			LOG.error(msg, e);
			throw new ElasticSearchException(msg, e);
		}

		LOG.info("Going to update index [" + indexName + "] for repository type [" + repository.getName() + "]");
		deleteDocumentsByType(repository.getName());

		LOG.info("Going to insert documents of type [" + repository.getName() + "]");
		IndexRequestGenerator requestGenerator = new IndexRequestGenerator(client, indexName);
		Iterable<BulkRequestBuilder> requests = requestGenerator.buildIndexRequest(repository);
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
		{ indexName }, documentTypeSantized)).actionGet().isExists();
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
			IndexDeleteByQueryResponse idbqr = deleteResponse.getIndex(indexName);
			if ((idbqr != null) && (idbqr.getFailedShards() > 0))
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
	public void updateRepositoryIndex(Repository<? extends Entity> repository)
	{
		if (!repository.iterator().hasNext())
		{
			return;
		}

		try
		{
			LOG.info("Going to create mapping for repository [" + repository.getName() + "]");
			createMappings(repository);
		}
		catch (IOException e)
		{
			String msg = "Exception creating mapping for repository [" + repository.getName() + "]";
			LOG.error(msg, e);
			throw new ElasticSearchException(msg, e);
		}

		LOG.info("Going to insert documents of type [" + repository.getName() + "]");
		IndexRequestGenerator requestGenerator = new IndexRequestGenerator(client, indexName);
		Iterable<BulkRequestBuilder> requests = requestGenerator.buildIndexRequest(repository);
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
		boolean hasIndex = client.admin().indices().exists(new IndicesExistsRequest(indexName)).actionGet().isExists();
		if (!hasIndex)
		{
			CreateIndexResponse response = client.admin().indices().prepareCreate(indexName).execute().actionGet();
			if (!response.isAcknowledged())
			{
				throw new ElasticSearchException("Creation of index [" + indexName + "] failed. Response=" + response);
			}
			LOG.info("Index [" + indexName + "] created");
		}
	}

	private void createMappings(Repository<? extends Entity> repository) throws IOException
	{
		XContentBuilder jsonBuilder = MappingsBuilder.buildMapping(repository);
		LOG.info("Going to create mapping [" + jsonBuilder.string() + "]");

		PutMappingResponse response = client.admin().indices().preparePutMapping(indexName)
				.setType(sanitizeMapperType(repository.getName())).setSource(jsonBuilder).execute().actionGet();

		if (!response.isAcknowledged())
		{
			throw new ElasticSearchException("Creation of mapping for documentType [" + repository.getName()
					+ "] failed. Response=" + response);
		}

		LOG.info("Mapping for documentType [" + repository.getName() + "] created");
	}

}
