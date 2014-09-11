package org.molgenis.elasticsearch;

import static org.elasticsearch.client.Requests.refreshRequest;
import static org.molgenis.elasticsearch.util.ElasticsearchEntityUtils.toElasticsearchId;
import static org.molgenis.elasticsearch.util.MapperTypeSanitizer.sanitizeMapperType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkProcessor.Listener;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.deletebyquery.DeleteByQueryResponse;
import org.elasticsearch.action.deletebyquery.IndexDeleteByQueryResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.elasticsearch.index.EntityToSourceConverter;
import org.molgenis.elasticsearch.index.IndexRequestGenerator;
import org.molgenis.elasticsearch.index.MappingsBuilder;
import org.molgenis.elasticsearch.request.SearchRequestGenerator;
import org.molgenis.elasticsearch.response.ResponseParser;
import org.molgenis.search.Hit;
import org.molgenis.search.MultiSearchRequest;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;
import org.molgenis.util.Pair;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

/**
 * ElasticSearch implementation of the SearchService interface
 * 
 * @author erwin
 * 
 */
public class ElasticSearchService implements SearchService
{
	private static final Logger LOG = Logger.getLogger(ElasticSearchService.class);

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

		if (createIndexIfNotExists) createIndexIfNotExists();
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

	// TODO this method is only used by BiobankConnect and should be removed in
	// the future
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
				request.getFieldsToReturn(), null, null, null);

		if (LOG.isDebugEnabled())
		{
			LOG.debug("SearchRequestBuilder:" + builder);
		}

		SearchResponse response = builder.execute().actionGet();
		if (LOG.isDebugEnabled())
		{
			LOG.debug("SearchResponse:" + response);
		}

		return responseParser.parseSearchResponse(response, null);
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

		generator
				.buildSearchRequest(builder, documentType, searchType, request.getQuery(), request.getFieldsToReturn(),
						request.getAggregateField1(), request.getAggregateField2(), entityMetaData);

		if (LOG.isDebugEnabled())
		{
			LOG.debug("SearchRequestBuilder:" + builder);
		}

		SearchResponse response = builder.execute().actionGet();
		if (LOG.isDebugEnabled())
		{
			LOG.debug("SearchResponse:" + response);
		}

		return responseParser.parseSearchResponse(response, entityMetaData);
	}

	@Override
	public void indexRepository(Repository repository)
	{
		if (!repository.iterator().hasNext())
		{
			return;
		}

		try
		{
			LOG.info("Going to create mapping for repository [" + repository.getName() + "]");
			createMappings(repository, true);
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
				throw new ElasticsearchException(response.buildFailureMessage());
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
				throw new ElasticsearchException("Delete failed. Returned headers:" + idbqr.getHeaders());
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
				if (!deleteResponse.isFound())
				{
					throw new ElasticsearchException("Delete failed. Returned headers:" + deleteResponse.getHeaders());
				}
			}
		}
		LOG.info("Delete done.");
	}

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
			createMappings(repository, true);
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
				throw new ElasticsearchException(response.buildFailureMessage());
			}
		}
	}

	@Override
	public void updateDocumentById(String documentType, String documentId, String updateScript)
	{
		LOG.info("Going to update document of type [" + documentType + "] with Id : " + documentId);

		String documentTypeSantized = sanitizeMapperType(documentType);
		UpdateResponse updateResponse = client.prepareUpdate(indexName, documentTypeSantized, documentId)
				.setScript("ctx._source." + updateScript).execute().actionGet();

		if (updateResponse == null)
		{
			throw new ElasticsearchException("update failed.");
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
				throw new ElasticsearchException("Creation of index [" + indexName + "] failed. Response=" + response);
			}
			LOG.info("Index [" + indexName + "] created");
		}
	}

	public boolean hasMapping(Repository repository)
	{
		String docType = sanitizeMapperType(repository.getName());

		GetMappingsResponse getMappingsResponse = client.admin().indices().prepareGetMappings("molgenis").execute()
				.actionGet();
		ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> allMappings = getMappingsResponse
				.getMappings();
		final ImmutableOpenMap<String, MappingMetaData> indexMappings = allMappings.get("molgenis");
		return indexMappings.containsKey(docType);
	}

	public void createMappings(Repository repository, boolean storeSource) throws IOException
	{
		createMappings(repository.getEntityMetaData(), storeSource);
	}

	public void createMappings(EntityMetaData entityMetaData, boolean storeSource) throws IOException
	{
		XContentBuilder jsonBuilder = MappingsBuilder.buildMapping(entityMetaData, storeSource);
		LOG.info("Going to create mapping [" + jsonBuilder.string() + "]");
		String entityName = entityMetaData.getName();

		PutMappingResponse response = client.admin().indices().preparePutMapping(indexName)
				.setType(sanitizeMapperType(entityName)).setSource(jsonBuilder).execute().actionGet();

		if (!response.isAcknowledged())
		{
			throw new ElasticsearchException("Creation of mapping for documentType [" + entityName
					+ "] failed. Response=" + response);
		}

		LOG.info("Mapping for documentType [" + entityName + "] created");
	}

	@Override
	public void refresh()
	{
		if (LOG.isDebugEnabled()) LOG.debug("Refreshing Elasticsearch index [" + indexName + "]");
		RefreshResponse refreshResponse = client.admin().indices().refresh(refreshRequest(indexName)).actionGet();
		if (refreshResponse == null || refreshResponse.getFailedShards() > 0)
		{
			throw new ElasticsearchException("Delete failed. Returned headers:" + refreshResponse.getHeaders());
		}
		if (LOG.isDebugEnabled()) LOG.debug("Refreshed Elasticsearch index [" + indexName + "]");
	}

	public long count(Query q, EntityMetaData entityMetaData)
	{
		String entityName = entityMetaData.getName();
		String type = sanitizeMapperType(entityName);
		List<String> fieldsToReturn = Collections.<String> emptyList();

		SearchRequestBuilder searchRequestBuilder = client.prepareSearch(indexName);
		generator.buildSearchRequest(searchRequestBuilder, type, SearchType.COUNT, q, fieldsToReturn, null, null,
				entityMetaData);
		SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
		if (searchResponse.getFailedShards() > 0)
		{
			throw new ElasticsearchException("Search failed. Returned headers:" + searchResponse.getHeaders());
		}
		return searchResponse.getHits().totalHits();
	}

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

		SynchronizedBulkProcessor bulkProcessor = new SynchronizedBulkProcessor(client);
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
			bulkProcessor.close();
		}

		if (updateIndex == true && indexingMode == IndexingMode.UPDATE) updateReferences(entities, entityMetaData);
	}

	public Iterable<String> search(Query q, final EntityMetaData entityMetaData)
	{
		String entityName = entityMetaData.getName();
		String type = sanitizeMapperType(entityName);
		List<String> fieldsToReturn = Collections.<String> emptyList();

		SearchRequestBuilder searchRequestBuilder = client.prepareSearch(indexName);
		generator.buildSearchRequest(searchRequestBuilder, type, SearchType.QUERY_AND_FETCH, q, fieldsToReturn, null,
				null, entityMetaData);

		SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
		if (searchResponse.getFailedShards() > 0)
		{
			throw new ElasticsearchException("Search failed. Returned headers:" + searchResponse.getHeaders());
		}

		return Iterables.transform(searchResponse.getHits(), new Function<SearchHit, String>()
		{
			@Override
			public String apply(SearchHit searchHit)
			{
				return searchHit.getId();
			}
		});
	}

	public void delete(Entity entity, EntityMetaData entityMetaData)
	{
		String elasticsearchId = toElasticsearchId(entity, entityMetaData);
		deleteById(elasticsearchId, entityMetaData);
	}

	public void deleteById(String id, EntityMetaData entityMetaData)
	{
		String entityName = entityMetaData.getName();
		String type = sanitizeMapperType(entityName);

		DeleteResponse deleteResponse = client.prepareDelete(indexName, type, id.toString()).setRefresh(true).execute()
				.actionGet();
		if (!deleteResponse.isFound())
		{
			throw new ElasticsearchException("Delete failed. Returned headers:" + deleteResponse.getHeaders());
		}
	}

	static class SynchronizedBulkProcessor
	{
		private Semaphore sem;
		private BulkProcessor bulkProcessor;

		public SynchronizedBulkProcessor(Client client)
		{
			bulkProcessor = BulkProcessor.builder(client, new Listener()
			{
				@Override
				public void beforeBulk(long executionId, BulkRequest request)
				{
					if (LOG.isDebugEnabled())
					{
						LOG.debug("Going to execute new bulk composed of " + request.numberOfActions() + " actions");
					}
				}

				@Override
				public void afterBulk(long executionId, BulkRequest request, BulkResponse response)
				{
					if (LOG.isDebugEnabled())
					{
						LOG.debug("Executed bulk composed of " + request.numberOfActions() + " actions");
					}
					sem.release();
				}

				@Override
				public void afterBulk(long executionId, BulkRequest request, Throwable failure)
				{
					LOG.warn("Error executing bulk", failure);
					sem.release();
				}
			}).build();
			sem = new Semaphore(1);
		}

		public int hashCode()
		{
			return bulkProcessor.hashCode();
		}

		public boolean equals(Object obj)
		{
			return bulkProcessor.equals(obj);
		}

		public void close()
		{
			bulkProcessor.close();
			awaitCompletion();
		}

		public BulkProcessor add(IndexRequest request)
		{
			return bulkProcessor.add(request);
		}

		public BulkProcessor add(DeleteRequest request)
		{
			return bulkProcessor.add(request);
		}

		private void awaitCompletion()
		{
			try
			{
				sem.acquire();
			}
			catch (InterruptedException e)
			{
				throw new RuntimeException(e);
			}
		}

	}

	public void deleteById(Iterable<String> ids, EntityMetaData entityMetaData)
	{
		String entityName = entityMetaData.getName();
		String type = sanitizeMapperType(entityName);

		Semaphore sem = new Semaphore(1);
		try
		{
			sem.acquire();

			SynchronizedBulkProcessor bulkProcessor = new SynchronizedBulkProcessor(client);
			try
			{
				for (Object id : ids)
				{
					bulkProcessor.add(new DeleteRequest(indexName, type, id.toString()));
				}
			}
			finally
			{
				bulkProcessor.close();
			}

			sem.acquire();
		}
		catch (InterruptedException e)
		{
			throw new RuntimeException(e);
		}
		refresh();
	}

	public void delete(Iterable<? extends Entity> entities, EntityMetaData entityMetaData)
	{
		String entityName = entityMetaData.getName();
		String type = sanitizeMapperType(entityName);

		Semaphore sem = new Semaphore(1);
		try
		{
			sem.acquire();

			SynchronizedBulkProcessor bulkProcessor = new SynchronizedBulkProcessor(client);
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
				bulkProcessor.close();
			}

			sem.acquire();
		}
		catch (InterruptedException e)
		{
			throw new RuntimeException(e);
		}
		refresh();
	}

	public void delete(EntityMetaData entityMetaData)
	{
		String type = sanitizeMapperType(entityMetaData.getName());
		DeleteByQueryResponse deleteByQueryResponse = client.prepareDeleteByQuery(indexName)
				.setQuery(new TermQueryBuilder("_type", type)).execute().actionGet();

		if (deleteByQueryResponse != null)
		{
			IndexDeleteByQueryResponse idbqr = deleteByQueryResponse.getIndex(indexName);
			if (idbqr != null && idbqr.getFailedShards() > 0)
			{
				throw new ElasticsearchException("Delete failed. Returned headers:" + idbqr.getHeaders());
			}
		}
		refresh();
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
}
