package org.molgenis.data.elasticsearch;

import static org.elasticsearch.client.Requests.refreshRequest;
import static org.molgenis.data.elasticsearch.util.ElasticsearchEntityUtils.toElasticsearchId;
import static org.molgenis.data.elasticsearch.util.MapperTypeSanitizer.sanitizeMapperType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.delete.DeleteMappingResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.deletebyquery.DeleteByQueryResponse;
import org.elasticsearch.action.deletebyquery.IndexDeleteByQueryResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.elasticsearch.index.ElasticsearchIndexCreator;
import org.molgenis.data.elasticsearch.index.EntityToSourceConverter;
import org.molgenis.data.elasticsearch.index.IndexRequestGenerator;
import org.molgenis.data.elasticsearch.index.MappingsBuilder;
import org.molgenis.data.elasticsearch.request.SearchRequestGenerator;
import org.molgenis.data.elasticsearch.response.ResponseParser;
import org.molgenis.data.elasticsearch.util.BulkProcessor;
import org.molgenis.data.elasticsearch.util.ElasticsearchEntityUtils;
import org.molgenis.data.elasticsearch.util.Hit;
import org.molgenis.data.elasticsearch.util.MultiSearchRequest;
import org.molgenis.data.elasticsearch.util.SearchRequest;
import org.molgenis.data.elasticsearch.util.SearchResult;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

/**
 * ElasticSearch implementation of the SearchService interface. TODO use scroll-scan where possible:
 * http://www.elasticsearch.org/guide/en/elasticsearch /reference/current/search-request-scroll.html#scroll-scans
 * 
 * @author erwin
 */
public class ElasticSearchService implements SearchService
{
	private static final Logger LOG = LoggerFactory.getLogger(ElasticSearchService.class);

	private static BulkProcessorFactory BULK_PROCESSOR_FACTORY = new BulkProcessorFactory();

	public EntityMetaData deserializeEntityMeta(String name) throws IOException
	{
		return MappingsBuilder.deserializeEntityMeta(client, name);
	}

	public static enum IndexingMode
	{
		ADD, UPDATE
	};

	private final DataService dataService;
	private final String indexName;
	private final Client client;
	private final ResponseParser responseParser = new ResponseParser();
	private final SearchRequestGenerator generator = new SearchRequestGenerator();
	private final EntityToSourceConverter entityToSourceConverter;

	public ElasticSearchService(Client client, String indexName, DataService dataService,
			EntityToSourceConverter entityToSourceConverter)
	{
		this(client, indexName, dataService, entityToSourceConverter, true);
	}

	/**
	 * Testability
	 * 
	 * @param client
	 * @param indexName
	 * @param dataService
	 * @param entityToSourceConverter
	 * @param createIndexIfNotExists
	 */
	ElasticSearchService(Client client, String indexName, DataService dataService,
			EntityToSourceConverter entityToSourceConverter, boolean createIndexIfNotExists)
	{
		if (client == null) throw new IllegalArgumentException("Client is null");
		if (indexName == null) throw new IllegalArgumentException("IndexName is null");
		if (dataService == null) throw new IllegalArgumentException("DataService is null");
		if (entityToSourceConverter == null) throw new IllegalArgumentException("EntityToSourceConverter is null");
		this.indexName = indexName;
		this.client = client;
		this.dataService = dataService;
		this.entityToSourceConverter = entityToSourceConverter;

		if (createIndexIfNotExists)
		{
			try
			{
				new ElasticsearchIndexCreator(client).createIndexIfNotExists(indexName);
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.data.elasticsearch.SearchService#getTypes()
	 */
	@Override
	public Iterable<String> getTypes()
	{
		if (LOG.isTraceEnabled()) LOG.trace("Retrieving Elasticsearch type names ...");
		GetMappingsResponse mappingsResponse = client.admin().indices().prepareGetMappings(indexName).execute()
				.actionGet();
		if (LOG.isDebugEnabled()) LOG.debug("Retrieved Elasticsearch type names");

		final ImmutableOpenMap<String, MappingMetaData> indexMappings = mappingsResponse.getMappings().get(indexName);
		return new Iterable<String>()
		{

			@Override
			public Iterator<String> iterator()
			{
				return indexMappings.keysIt();
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.data.elasticsearch.SearchService#search(org.elasticsearch .action.search.SearchRequest)
	 */
	@Override
	@Deprecated
	public SearchResult search(SearchRequest request)
	{
		return search(SearchType.QUERY_AND_FETCH, request);
	}

	/*
	 * TODO this method is only used by BiobankConnect and should be removed in the future
	 */
	@Override
	@Deprecated
	public SearchResult multiSearch(MultiSearchRequest request)
	{
		return multiSearch(SearchType.QUERY_AND_FETCH, request);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.data.elasticsearch.SearchService#count(java.lang.String, org.molgenis.data.Query)
	 */
	@Override
	@Deprecated
	public long count(String documentType, Query q)
	{
		String type = sanitizeMapperType(documentType);

		if (LOG.isTraceEnabled())
		{
			LOG.trace("Counting Elasticsearch '" + type + "' docs using query [" + q + "] ...");
		}
		SearchRequest request = new SearchRequest(type, q, Collections.<String> emptyList());
		long count = search(SearchType.COUNT, request).getTotalHitCount();
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Counted " + count + " Elasticsearch '" + type + "' docs using query [" + q + "] ...");
		}

		return count;
	}

	@Override
	public Hit searchById(String documentType, String id)
	{
		GetResponse response = client.prepareGet(indexName, sanitizeMapperType(documentType), id).execute().actionGet();
		Hit hit = null;
		if (response.isExists())
		{
			hit = new Hit(response.getId(), response.getType(), response.getSourceAsMap());
		}
		return hit;
	}

	/*
	 * TODO this method is only used by BiobankConnect and should be removed in the future (non-Javadoc)
	 * 
	 * @see org.molgenis.data.elasticsearch.SearchService#multiSearch(org.elasticsearch .action.search.SearchType,
	 * org.elasticsearch.action.search.MultiSearchRequest)
	 */
	@Override
	@Deprecated
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
				request.getFieldsToReturn(), null, null, null, null);

		if (LOG.isTraceEnabled())
		{
			LOG.trace("SearchRequestBuilder:" + builder);
		}

		SearchResponse response = builder.execute().actionGet();
		if (LOG.isDebugEnabled())
		{
			LOG.debug("SearchResponse:" + response);
		}

		// FIXME passing null as request is not cool (and breaks aggregates)
		return responseParser.parseSearchResponse(null, response, null, dataService);
	}

	private SearchResult search(SearchType searchType, SearchRequest request)
	{
		SearchRequestBuilder builder = client.prepareSearch(indexName);
		// TODO : A quick fix now! Need to find a better way to get
		// EntityMetaData in
		// ElasticSearchService, because ElasticSearchService should not be
		// aware of DataService. E.g. Put EntityMetaData in the SearchRequest
		// object
		EntityMetaData entityMetaData = (request.getDocumentType() != null && dataService != null && dataService
				.hasRepository(request.getDocumentType())) ? dataService.getEntityMetaData(request.getDocumentType()) : null;
		String documentType = request.getDocumentType() == null ? null : sanitizeMapperType(request.getDocumentType());
		generator.buildSearchRequest(builder, documentType, searchType, request.getQuery(),
				request.getFieldsToReturn(), request.getAggregateField1(), request.getAggregateField2(),
				request.getAggregateFieldDistinct(), entityMetaData);
		if (LOG.isTraceEnabled())
		{
			LOG.trace("*** REQUEST\n" + builder);
		}		
		SearchResponse response = builder.execute().actionGet();
		if (LOG.isTraceEnabled())
		{
			LOG.trace("*** RESPONSE\n" + response);
		}
		return responseParser.parseSearchResponse(request, response, entityMetaData, dataService);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.data.elasticsearch.SearchService#indexRepository(org.molgenis .data.Repository)
	 */
	@Override
	@Deprecated
	public void indexRepository(Repository repository)
	{
		if (!repository.iterator().hasNext())
		{
			return;
		}

		try
		{
			LOG.info("Going to create mapping for repository [" + repository.getName() + "]");
			createMappings(repository, true, true, true);
		}
		catch (IOException e)
		{
			String msg = "Exception creating mapping for repository [" + repository.getName() + "]";
			LOG.error(msg, e);
			throw new ElasticsearchException(msg, e);
		}

		LOG.info("Going to update index [" + indexName + "] for repository type [" + repository.getName() + "]");
		deleteDocumentsByType(repository.getName());

		LOG.info("Going to insert documents of type [" + repository.getName() + "]");
		IndexRequestGenerator requestGenerator = new IndexRequestGenerator(client, indexName, entityToSourceConverter);
		Iterable<BulkRequestBuilder> requests = requestGenerator.buildIndexRequest(repository);
		for (BulkRequestBuilder request : requests)
		{
			LOG.info("Request created");
			if (LOG.isTraceEnabled())
			{
				LOG.trace("BulkRequest:" + request);
			}

			BulkResponse response = request.execute().actionGet();
			LOG.info("Request done");
			if (LOG.isDebugEnabled())
			{
				LOG.debug("BulkResponse:" + response);
			}

			if (response.hasFailures())
			{
				throw new ElasticsearchException(response.buildFailureMessage());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.data.elasticsearch.SearchService#documentTypeExists(java .lang.String)
	 */
	@Override
	public boolean documentTypeExists(String documentType)
	{
		String documentTypeSantized = sanitizeMapperType(documentType);

		return client.admin().indices().typesExists(new TypesExistsRequest(new String[]
		{ indexName }, documentTypeSantized)).actionGet().isExists();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.data.elasticsearch.SearchService#deleteDocumentsByType(java .lang.String)
	 */
	@Override
	@Deprecated
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
				throw new ElasticsearchException("Delete failed. Returned headers:" + idbqr.getHeaders());
			}
		}

		LOG.info("Delete done.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.data.elasticsearch.SearchService#deleteDocumentByIds(java .lang.String, java.util.List)
	 */
	@Override
	@Deprecated
	public void deleteDocumentByIds(String documentType, List<String> documentIds)
	{
		String documentTypeSantized = sanitizeMapperType(documentType);
		LOG.info("Deleting Elasticsearch '" + documentTypeSantized + "' docs with ids [" + documentIds + "]");

		for (String documentId : documentIds)
		{
			DeleteResponse deleteResponse = client.prepareDelete(indexName, documentTypeSantized, documentId)
					.setRefresh(true).execute().actionGet();
			if (deleteResponse != null)
			{
				if (!deleteResponse.isFound())
				{
					throw new ElasticsearchException("Delete failed. Returned headers:" + deleteResponse.getHeaders());
				}
			}
		}
		LOG.info("Deleted Elasticsearch '" + documentTypeSantized + "' docs with ids [" + documentIds + "]");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.data.elasticsearch.SearchService#updateRepositoryIndex(org .molgenis.data.Repository)
	 */
	@Override
	public void updateRepositoryIndex(Repository repository)
	{
		if (!repository.iterator().hasNext())
		{
			return;
		}

		try
		{
			LOG.info("Going to create mapping for repository [" + repository.getName() + "]");
			createMappings(repository, true, true, true);
		}
		catch (IOException e)
		{
			String msg = "Exception creating mapping for repository [" + repository.getName() + "]";
			LOG.error(msg, e);
			throw new ElasticsearchException(msg, e);
		}

		LOG.info("Going to insert documents of type [" + repository.getName() + "]");
		IndexRequestGenerator requestGenerator = new IndexRequestGenerator(client, indexName, entityToSourceConverter);
		Iterable<BulkRequestBuilder> requests = requestGenerator.buildIndexRequest(repository);
		for (BulkRequestBuilder request : requests)
		{
			if (LOG.isTraceEnabled())
			{
				LOG.trace("BulkRequest:" + request);
			}

			BulkResponse response = request.execute().actionGet();
			if (LOG.isDebugEnabled())
			{
				LOG.debug("BulkResponse:" + response);
			}

			if (response.hasFailures())
			{
				throw new ElasticsearchException(response.buildFailureMessage());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.data.elasticsearch.SearchService#updateDocumentById(java .lang.String, java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void updateDocumentById(String documentType, String documentId, String updateScript)
	{
		// LOG.info("Going to update document of type [" + documentType +
		// "] with Id : " + documentId);
		//
		// String documentTypeSantized = sanitizeMapperType(documentType);
		// UpdateResponse updateResponse = client.prepareUpdate(indexName,
		// documentTypeSantized, documentId)
		// .setScript("ctx._source." + updateScript).execute().actionGet();
		//
		// if (updateResponse == null)
		// {
		// throw new ElasticsearchException("update failed.");
		// }
		//
		// LOG.info("Update done.");
		// FIXME
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.data.elasticsearch.SearchService#hasMapping(org.molgenis .data.Repository)
	 */
	@Override
	public boolean hasMapping(Repository repository)
	{
		return hasMapping(repository.getEntityMetaData());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.data.elasticsearch.SearchService#hasMapping(org.molgenis .data.EntityMetaData)
	 */
	@Override
	public boolean hasMapping(EntityMetaData entityMetaData)
	{
		String docType = sanitizeMapperType(entityMetaData.getName());

		GetMappingsResponse getMappingsResponse = client.admin().indices().prepareGetMappings("molgenis").execute()
				.actionGet();
		ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> allMappings = getMappingsResponse
				.getMappings();
		final ImmutableOpenMap<String, MappingMetaData> indexMappings = allMappings.get("molgenis");
		return indexMappings.containsKey(docType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.data.elasticsearch.SearchService#createMappings(org.molgenis .data.Repository, boolean,
	 * boolean, boolean)
	 */
	@Override
	@Deprecated
	public void createMappings(Repository repository, boolean storeSource, boolean enableNorms, boolean createAllIndex)
			throws IOException
	{
		createMappings(repository.getEntityMetaData(), storeSource, enableNorms, createAllIndex);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.data.elasticsearch.SearchService#createMappings(org.molgenis .data.EntityMetaData)
	 */
	@Override
	public void createMappings(EntityMetaData entityMetaData) throws IOException
	{
		createMappings(entityMetaData, true, true, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.data.elasticsearch.SearchService#createMappings(org.molgenis .data.EntityMetaData, boolean,
	 * boolean, boolean)
	 */
	@Override
	public void createMappings(EntityMetaData entityMetaData, boolean storeSource, boolean enableNorms,
			boolean createAllIndex) throws IOException
	{
		createMappings(entityMetaData, storeSource, enableNorms, createAllIndex, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.data.elasticsearch.SearchService#createMappings(org.molgenis .data.EntityMetaData, boolean,
	 * boolean, boolean)
	 */
	@Override
	public void createMappings(EntityMetaData entityMetaData, boolean storeSource, boolean enableNorms,
			boolean createAllIndex, boolean storeFullMetadata) throws IOException
	{
		XContentBuilder jsonBuilder = MappingsBuilder.buildMapping(entityMetaData, storeSource, enableNorms,
				createAllIndex, storeFullMetadata);
		if (LOG.isTraceEnabled()) LOG.trace("Creating Elasticsearch mapping [" + jsonBuilder.string() + "] ...");
		String entityName = entityMetaData.getName();

		PutMappingResponse response = client.admin().indices().preparePutMapping(indexName)
				.setType(sanitizeMapperType(entityName)).setSource(jsonBuilder).execute().actionGet();

		if (!response.isAcknowledged())
		{
			throw new ElasticsearchException("Creation of mapping for documentType [" + entityName
					+ "] failed. Response=" + response);
		}

		if (LOG.isDebugEnabled()) LOG.debug("Created Elasticsearch mapping [" + jsonBuilder.string() + "]");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.data.elasticsearch.SearchService#refresh()
	 */
	@Override
	public void refresh()
	{
		if (LOG.isTraceEnabled()) LOG.trace("Refreshing Elasticsearch index [" + indexName + "]");
		RefreshResponse refreshResponse = client.admin().indices().refresh(refreshRequest(indexName)).actionGet();
		if (refreshResponse == null || refreshResponse.getFailedShards() > 0)
		{
			throw new ElasticsearchException("Delete failed. Returned headers:" + refreshResponse.getHeaders());
		}
		if (LOG.isDebugEnabled()) LOG.debug("Refreshed Elasticsearch index [" + indexName + "]");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.data.elasticsearch.SearchService#count(org.molgenis.data .EntityMetaData)
	 */
	@Override
	public long count(EntityMetaData entityMetaData)
	{
		return count(null, entityMetaData);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.data.elasticsearch.SearchService#count(org.molgenis.data .Query,
	 * org.molgenis.data.EntityMetaData)
	 */
	@Override
	public long count(Query q, EntityMetaData entityMetaData)
	{
		String entityName = entityMetaData.getName();
		String type = sanitizeMapperType(entityName);
		List<String> fieldsToReturn = Collections.<String> emptyList();

		if (LOG.isTraceEnabled())
		{
			if (q != null)
			{
				LOG.trace("Counting Elasticsearch '" + type + "' docs using query [" + q + "] ...");
			}
			else LOG.trace("Counting Elasticsearch '" + type + "' docs ...");
		}
		SearchRequestBuilder searchRequestBuilder = client.prepareSearch(indexName);
		generator.buildSearchRequest(searchRequestBuilder, type, SearchType.COUNT, q, fieldsToReturn, null, null, null,
				entityMetaData);
		SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
		if (searchResponse.getFailedShards() > 0)
		{
			throw new ElasticsearchException("Search failed. Returned headers:" + searchResponse.getHeaders());
		}
		long count = searchResponse.getHits().totalHits();
		if (LOG.isDebugEnabled())
		{
			if (q != null)
			{
				LOG.debug("Counted " + count + " Elasticsearch '" + type + "' docs using query [" + q + "] in "
						+ searchResponse.getTookInMillis() + "ms");
			}
			else
			{
				LOG.debug("Counted " + count + " Elasticsearch '" + type + "' docs in "
						+ searchResponse.getTookInMillis() + "ms");
			}
		}

		return count;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.data.elasticsearch.SearchService#index(org.molgenis.data .Entity,
	 * org.molgenis.data.EntityMetaData, org.molgenis.data.elasticsearch.ElasticSearchService.IndexingMode)
	 */
	@Override
	public void index(Entity entity, EntityMetaData entityMetaData, IndexingMode indexingMode)
	{
		index(entity, entityMetaData, indexingMode, true);
		refresh();
	}

	private void index(Entity entity, EntityMetaData entityMetaData, IndexingMode indexingMode, boolean updateIndex)
	{
		String type = sanitizeMapperType(entityMetaData.getName());
		String id = toElasticsearchId(entity, entityMetaData);
		Map<String, Object> source = entityToSourceConverter.convert(entity, entityMetaData);
		client.prepareIndex(indexName, type, id).setSource(source).execute().actionGet();

		if (updateIndex && indexingMode == IndexingMode.UPDATE) updateReferences(entity, entityMetaData);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.data.elasticsearch.SearchService#index(java.lang.Iterable, org.molgenis.data.EntityMetaData,
	 * org.molgenis.data.elasticsearch.ElasticSearchService.IndexingMode)
	 */
	@Override
	public void index(Iterable<? extends Entity> entities, EntityMetaData entityMetaData, IndexingMode indexingMode)
	{
		index(entities, entityMetaData, indexingMode, true);
		refresh();
	}

	void index(Iterable<? extends Entity> entities, EntityMetaData entityMetaData, IndexingMode indexingMode,
			boolean updateIndex)
	{
		String entityName = entityMetaData.getName();
		String type = sanitizeMapperType(entityName);

		BulkProcessor bulkProcessor = BULK_PROCESSOR_FACTORY.create(client);
		try
		{
			for (Entity entity : entities)
			{
				String id = toElasticsearchId(entity, entityMetaData);
				Map<String, Object> source = entityToSourceConverter.convert(entity, entityMetaData);
				bulkProcessor.add(new IndexRequest(indexName, type, id).source(source));
			}
		}
		finally
		{
			try
			{
				boolean isCompleted = bulkProcessor.awaitClose(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
				if (!isCompleted)
				{
					throw new MolgenisDataException("Failed to complete bulk delete within the given time");
				}
			}
			catch (InterruptedException e)
			{
				throw new RuntimeException(e);
			}
		}

		if (updateIndex == true && indexingMode == IndexingMode.UPDATE) updateReferences(entities, entityMetaData);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.data.elasticsearch.SearchService#delete(org.molgenis.data .Entity,
	 * org.molgenis.data.EntityMetaData)
	 */
	@Override
	public void delete(Entity entity, EntityMetaData entityMetaData)
	{
		String elasticsearchId = toElasticsearchId(entity, entityMetaData);
		deleteById(elasticsearchId, entityMetaData);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.data.elasticsearch.SearchService#deleteById(java.lang.String ,
	 * org.molgenis.data.EntityMetaData)
	 */
	@Override
	public void deleteById(String id, EntityMetaData entityMetaData)
	{
		String entityName = entityMetaData.getName();
		String type = sanitizeMapperType(entityName);

		if (LOG.isTraceEnabled())
		{
			LOG.trace("Deleting Elasticsearch '" + type + "' doc with id [" + id + "] ...");
		}
		DeleteResponse deleteResponse = client.prepareDelete(indexName, type, id.toString()).setRefresh(true).execute()
				.actionGet();
		if (!deleteResponse.isFound())
		{
			throw new ElasticsearchException("Delete failed. Returned headers:" + deleteResponse.getHeaders());
		}
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Deleted Elasticsearch '" + type + "' doc with id [" + id + "]");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.data.elasticsearch.SearchService#deleteById(java.lang.Iterable ,
	 * org.molgenis.data.EntityMetaData)
	 */
	@Override
	public void deleteById(Iterable<String> ids, EntityMetaData entityMetaData)
	{
		String entityName = entityMetaData.getName();
		String type = sanitizeMapperType(entityName);

		if (LOG.isTraceEnabled())
		{
			LOG.trace("Deleting Elasticsearch '" + type + "' docs with ids [" + ids + "] ...");
		}

		BulkProcessor bulkProcessor = BULK_PROCESSOR_FACTORY.create(client);
		try
		{
			for (Object id : ids)
			{
				bulkProcessor.add(new DeleteRequest(indexName, type, id.toString()));
			}
		}
		finally
		{
			try
			{
				boolean isCompleted = bulkProcessor.awaitClose(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
				if (!isCompleted)
				{
					throw new MolgenisDataException("Failed to complete bulk delete within the given time");
				}
			}
			catch (InterruptedException e)
			{
				throw new RuntimeException(e);
			}
		}

		if (LOG.isDebugEnabled())
		{
			LOG.debug("Deleted Elasticsearch '" + type + "' docs with ids [" + ids + "] ...");
		}
		refresh();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.data.elasticsearch.SearchService#delete(java.lang.Iterable, org.molgenis.data.EntityMetaData)
	 */
	@Override
	public void delete(Iterable<? extends Entity> entities, EntityMetaData entityMetaData)
	{
		String entityName = entityMetaData.getName();
		String type = sanitizeMapperType(entityName);

		if (LOG.isTraceEnabled())
		{
			LOG.trace("Bulk deleting Elasticsearch '" + type + "' docs ...");
		}

		BulkProcessor bulkProcessor = BULK_PROCESSOR_FACTORY.create(client);
		try
		{
			for (Entity entity : entities)
			{
				String elasticsearchId = toElasticsearchId(entity, entityMetaData);
				bulkProcessor.add(new DeleteRequest(indexName, type, elasticsearchId));
			}
		}
		finally
		{
			try
			{
				boolean isCompleted = bulkProcessor.awaitClose(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
				if (!isCompleted)
				{
					throw new MolgenisDataException("Failed to complete bulk delete within the given time");
				}
			}
			catch (InterruptedException e)
			{
				throw new RuntimeException(e);
			}
		}

		if (LOG.isDebugEnabled())
		{
			LOG.debug("Bulk deleted Elasticsearch '" + type + "' docs");
		}

		refresh();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.data.elasticsearch.SearchService#delete(org.molgenis.data .EntityMetaData)
	 */
	@Override
	public void delete(String entityName)
	{
		String type = sanitizeMapperType(entityName);

		if (LOG.isTraceEnabled())
		{
			LOG.trace("Deleting all Elasticsearch '" + type + "' docs ...");
		}

		TypesExistsResponse typesExistsResponse = client.admin().indices().prepareTypesExists(indexName).setTypes(type)
				.execute().actionGet();
		if (typesExistsResponse.isExists())
		{
			DeleteMappingResponse deleteMappingResponse = client.admin().indices().prepareDeleteMapping(indexName)
					.setType(type).execute().actionGet();
			if (!deleteMappingResponse.isAcknowledged())
			{
				throw new ElasticsearchException("Delete failed. Returned headers:"
						+ deleteMappingResponse.getHeaders());
			}
		}

		if (LOG.isDebugEnabled())
		{
			LOG.debug("Deleted all Elasticsearch '" + type + "' docs");
		}
		// FIXME only deletes mappings?
		// deleteMapping(request)
		// DeleteByQueryResponse deleteByQueryResponse =
		// client.prepareDeleteByQuery(indexName)
		// .setQuery(new TermQueryBuilder("_type", type)).execute().actionGet();
		//
		// if (deleteByQueryResponse != null)
		// {
		// IndexDeleteByQueryResponse idbqr =
		// deleteByQueryResponse.getIndex(indexName);
		// if (idbqr != null && idbqr.getFailedShards() > 0)
		// {
		// throw new ElasticsearchException("Delete failed. Returned headers:" +
		// idbqr.getHeaders());
		// }
		// }
		refresh();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.data.elasticsearch.SearchService#get(java.lang.Object, org.molgenis.data.EntityMetaData)
	 */
	@Override
	public ElasticsearchDocumentEntity get(Object entityId, EntityMetaData entityMetaData)
	{
		String entityName = entityMetaData.getName();
		String type = sanitizeMapperType(entityName);
		String id = ElasticsearchEntityUtils.toElasticsearchId(entityId);

		if (LOG.isTraceEnabled())
		{
			LOG.trace("Retrieving Elasticsearch '" + type + "' doc with id [" + id + "] ...");
		}
		GetResponse response = client.prepareGet(indexName, type, id).execute().actionGet();
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Retrieved Elasticsearch '" + type + "' doc with id [" + id + "]");
		}
		return response.isExists() ? new ElasticsearchDocumentEntity(response.getSource(), entityMetaData, this,
				entityToSourceConverter) : null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.data.elasticsearch.SearchService#get(java.lang.Iterable, org.molgenis.data.EntityMetaData)
	 */
	@Override
	public Iterable<Entity> get(Iterable<Object> entityIds, final EntityMetaData entityMetaData)
	{
		String entityName = entityMetaData.getName();
		String type = sanitizeMapperType(entityName);

		if (LOG.isTraceEnabled())
		{
			LOG.trace("Retrieving Elasticsearch '" + type + "' docs with ids [" + entityIds + "] ...");
		}
		MultiGetResponse response = client.prepareMultiGet()
				.add(indexName, type, ElasticsearchEntityUtils.toElasticsearchIds(entityIds)).execute().actionGet();
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Retrieved Elasticsearch '" + type + "' docs with ids [" + entityIds + "] ...");
		}

		final SearchService self = this;
		return Iterables.transform(response, new Function<MultiGetItemResponse, Entity>()
		{
			@Override
			public Entity apply(MultiGetItemResponse itemResponse)
			{
				if (itemResponse.isFailed())
				{
					throw new ElasticsearchException("Search failed. Returned headers:" + itemResponse.getFailure());
				}
				GetResponse getResponse = itemResponse.getResponse();
				return getResponse.isExists() ? new ElasticsearchDocumentEntity(getResponse.getSource(),
						entityMetaData, self, entityToSourceConverter) : null;
			}
		});
	}

	// TODO replace Iterable<Entity> with EntityCollection and add
	// EntityCollection.getTotal()
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.data.elasticsearch.SearchService#search(org.molgenis.data .Query,
	 * org.molgenis.data.EntityMetaData)
	 */
	@Override
	public Iterable<Entity> search(Query q, final EntityMetaData entityMetaData)
	{
		return new ElasticsearchEntityIterable(q, entityMetaData, client, this, generator, indexName,
				entityToSourceConverter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.data.elasticsearch.SearchService#aggregate(org.molgenis. data.AggregateQuery,
	 * org.molgenis.data.EntityMetaData)
	 */
	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery, final EntityMetaData entityMetaData)
	{
		Query q = aggregateQuery.getQuery();
		AttributeMetaData xAttr = aggregateQuery.getAttributeX();
		AttributeMetaData yAttr = aggregateQuery.getAttributeY();
		AttributeMetaData distinctAttr = aggregateQuery.getAttributeDistinct();
		SearchRequest searchRequest = new SearchRequest(entityMetaData.getName(), q, Collections.<String> emptyList(),
				xAttr, yAttr, distinctAttr);
		SearchResult searchResults = search(searchRequest);
		return searchResults.getAggregate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.data.elasticsearch.SearchService#flush()
	 */
	@Override
	public void flush()
	{
		if (LOG.isTraceEnabled()) LOG.trace("Flushing Elasticsearch index [" + indexName + "] ...");
		client.admin().indices().prepareFlush(indexName).execute().actionGet();
		if (LOG.isDebugEnabled()) LOG.debug("Flushed Elasticsearch index [" + indexName + "]");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.data.elasticsearch.SearchService#rebuildIndex(java.lang. Iterable,
	 * org.molgenis.data.EntityMetaData)
	 */
	@Override
	public void rebuildIndex(Iterable<? extends Entity> entities, EntityMetaData entityMetaData)
	{
		try
		{
			if (hasMapping(entityMetaData))
			{
				delete(entityMetaData.getName());
			}
			createMappings(entityMetaData);
			index(entities, entityMetaData, IndexingMode.ADD);
		}
		catch (IOException e)
		{
			throw new MolgenisDataException(e);
		}
	}

	private void updateReferences(Entity entity, EntityMetaData entityMetaData)
	{
		List<Pair<EntityMetaData, List<AttributeMetaData>>> referencingEntityMetaData = getReferencingEntityMetaData(entityMetaData);

		for (Pair<EntityMetaData, List<AttributeMetaData>> pair : referencingEntityMetaData)
		{
			EntityMetaData refEntityMetaData = pair.getA();
			QueryImpl q = null;
			for (AttributeMetaData attributeMetaData : pair.getB())
			{
				if (q == null) q = new QueryImpl();
				else q.or();
				q.eq(attributeMetaData.getName(), entity);
			}

			Iterable<Entity> refEntities = dataService.findAll(refEntityMetaData.getName(), q);
			if (!Iterables.isEmpty(refEntities))
			{
				index(refEntities, refEntityMetaData, IndexingMode.UPDATE, false);
			}
		}
	}

	private void updateReferences(Iterable<? extends Entity> entities, EntityMetaData entityMetaData)
	{
		for (Entity entity : entities)
		{
			updateReferences(entity, entityMetaData);
		}
	}

	private List<Pair<EntityMetaData, List<AttributeMetaData>>> getReferencingEntityMetaData(
			EntityMetaData entityMetaData)
	{
		List<Pair<EntityMetaData, List<AttributeMetaData>>> referencingEntityMetaData = null;

		// get entity types that referencing the given entity (including self)
		String entityName = entityMetaData.getName();
		for (String otherEntityName : dataService.getEntityNames())
		{
			EntityMetaData otherEntityMetaData = dataService.getEntityMetaData(otherEntityName);

			// get referencing attributes for other entity
			List<AttributeMetaData> referencingAttributes = null;
			for (AttributeMetaData attributeMetaData : otherEntityMetaData.getAtomicAttributes())
			{
				EntityMetaData refEntityMetaData = attributeMetaData.getRefEntity();
				if (refEntityMetaData != null && refEntityMetaData.getName().equals(entityName))
				{
					if (referencingAttributes == null) referencingAttributes = new ArrayList<AttributeMetaData>();
					referencingAttributes.add(attributeMetaData);
				}
			}

			// store references
			if (referencingAttributes != null)
			{
				if (referencingEntityMetaData == null) referencingEntityMetaData = new ArrayList<Pair<EntityMetaData, List<AttributeMetaData>>>();
				referencingEntityMetaData.add(new Pair<EntityMetaData, List<AttributeMetaData>>(otherEntityMetaData,
						referencingAttributes));
			}
		}

		return referencingEntityMetaData != null ? referencingEntityMetaData : Collections
				.<Pair<EntityMetaData, List<AttributeMetaData>>> emptyList();

	}

	/**
	 * Testability, using the real Elasticsearch BulkProcessor results in infinite waits on close
	 * 
	 * @param bulkProcessorFactory
	 */
	static void setBulkProcessorFactory(BulkProcessorFactory bulkProcessorFactory)
	{
		BULK_PROCESSOR_FACTORY = bulkProcessorFactory;
	}

	static class BulkProcessorFactory
	{
		public BulkProcessor create(Client client)
		{
			return BulkProcessor.builder(client, new BulkProcessor.Listener()
			{
				@Override
				public void beforeBulk(long executionId, BulkRequest request)
				{
					if (LOG.isTraceEnabled())
					{
						LOG.trace("Going to execute new bulk composed of " + request.numberOfActions() + " actions");
					}
				}

				@Override
				public void afterBulk(long executionId, BulkRequest request, BulkResponse response)
				{
					if (LOG.isTraceEnabled())
					{
						LOG.trace("Executed bulk composed of " + request.numberOfActions() + " actions");
					}
				}

				@Override
				public void afterBulk(long executionId, BulkRequest request, Throwable failure)
				{
					LOG.warn("Error executing bulk", failure);
				}
			}).setConcurrentRequests(0).build();
		}
	}

	public GetMappingsResponse getMappings()
	{
		return client.admin().indices().prepareGetMappings(indexName).execute().actionGet();
	}

	public EntityMetaData getEntityMetaData(String name)
	{
		EntityMetaData entityMetaData = null;
		try
		{
			entityMetaData = MappingsBuilder.deserializeEntityMeta(client, name);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return entityMetaData;
	}

	/**
	 * Retrieve search results in batches. Note: We do not use Elasticsearch scan & scroll, because scrolling is not
	 * intended for real time user request: http://www.elasticsearch.org/guide/en/elasticsearch/reference/current
	 * /search-request-scroll.html
	 */
	private static class ElasticsearchEntityIterable implements Iterable<Entity>
	{
		private static final int BATCH_SIZE = 1000;

		private final Query q;
		private final EntityMetaData entityMetaData;
		private final Client client;
		private final ElasticSearchService elasticSearchService;
		private final SearchRequestGenerator searchRequestGenerator;
		private final String indexName;

		private final String type;
		private final List<String> fieldsToReturn;
		private final int offset;
		private final int pageSize;
		private final EntityToSourceConverter entityToSourceConverter;

		public ElasticsearchEntityIterable(Query q, EntityMetaData entityMetaData, Client client,
				ElasticSearchService elasticSearchService, SearchRequestGenerator searchRequestGenerator,
				String indexName, EntityToSourceConverter entityToSourceConverter)
		{
			this.client = client;
			this.q = q;
			this.entityMetaData = entityMetaData;
			this.elasticSearchService = elasticSearchService;
			this.searchRequestGenerator = searchRequestGenerator;
			this.indexName = indexName;
			this.entityToSourceConverter = entityToSourceConverter;

			this.type = sanitizeMapperType(entityMetaData.getName());
			this.fieldsToReturn = Collections.<String> emptyList();
			this.offset = q.getOffset();
			this.pageSize = q.getPageSize();
		}

		@Override
		public Iterator<Entity> iterator()
		{
			return new Iterator<Entity>()
			{
				private long totalHits;
				private SearchHit[] batchHits;
				private int batchPos;

				private int currentOffset;

				@Override
				public boolean hasNext()
				{
					if (batchHits == null)
					{
						int batchOffset = offset;
						int batchSize = pageSize != 0 ? Math.min(pageSize - currentOffset, BATCH_SIZE) : BATCH_SIZE;
						doBatchSearch(batchOffset, batchSize);

					}
					if (batchHits.length == 0)
					{
						return false;
					}

					if (batchPos < batchHits.length)
					{
						return true;
					}
					else if (batchPos == batchHits.length)
					{
						long requestedHits = pageSize != 0 ? Math.min(pageSize, totalHits) : totalHits;
						if (currentOffset + batchHits.length < requestedHits)
						{
							int batchOffset = currentOffset + BATCH_SIZE;
							int batchSize = pageSize != 0 ? Math.min(pageSize - batchOffset, BATCH_SIZE) : BATCH_SIZE;
							doBatchSearch(batchOffset, batchSize);
							return true;
						}
						else
						{
							return false;
						}
					}
					else throw new RuntimeException();
				}

				@Override
				public Entity next()
				{
					if (hasNext())
					{
						SearchHit hit = batchHits[batchPos];
						++batchPos;
						return new ElasticsearchDocumentEntity(hit.getSource(), entityMetaData, elasticSearchService,
								entityToSourceConverter);
					}
					else throw new ArrayIndexOutOfBoundsException();
				}

				private void doBatchSearch(int from, int size)
				{
					q.offset(from);
					q.pageSize(size);

					if (LOG.isTraceEnabled())
					{
						LOG.trace("Searching Elasticsearch '" + type + "' docs using query [" + q + "] ...");
					}
					SearchRequestBuilder searchRequestBuilder = client.prepareSearch(indexName);
					searchRequestGenerator.buildSearchRequest(searchRequestBuilder, type, SearchType.QUERY_AND_FETCH,
							q, fieldsToReturn, null, null, null, entityMetaData);
					SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
					if (searchResponse.getFailedShards() > 0)
					{
						throw new ElasticsearchException("Search failed. Returned headers:"
								+ searchResponse.getHeaders());
					}
					if (LOG.isDebugEnabled())
					{
						LOG.debug("Searched Elasticsearch '" + type + "' docs using query [" + q + "] in "
								+ searchResponse.getTotalShards() + "ms");
					}
					SearchHits searchHits = searchResponse.getHits();
					this.totalHits = searchHits.getTotalHits();
					this.batchHits = searchHits.getHits();
					this.batchPos = 0;

					this.currentOffset = from;
				}

				@Override
				public void remove()
				{
					throw new UnsupportedOperationException();
				}
			};
		}
	}
}
