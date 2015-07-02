package org.molgenis.data.elasticsearch;

import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.elasticsearch.util.ElasticsearchEntityUtils.toElasticsearchId;
import static org.molgenis.data.elasticsearch.util.ElasticsearchEntityUtils.toElasticsearchIds;
import static org.molgenis.data.elasticsearch.util.MapperTypeSanitizer.sanitizeMapperType;
import static org.molgenis.data.transaction.MolgenisTransactionManager.TRANSACTION_ID_RESOURCE_NAME;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.delete.DeleteMappingResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.deletebyquery.DeleteByQueryResponse;
import org.elasticsearch.action.deletebyquery.IndexDeleteByQueryResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequestBuilder;
import org.elasticsearch.action.get.MultiGetResponse;
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
import org.molgenis.data.elasticsearch.index.ElasticsearchIndexCreator;
import org.molgenis.data.elasticsearch.index.EntityToSourceConverter;
import org.molgenis.data.elasticsearch.index.MappingsBuilder;
import org.molgenis.data.elasticsearch.request.SearchRequestGenerator;
import org.molgenis.data.elasticsearch.response.ResponseParser;
import org.molgenis.data.elasticsearch.util.ElasticsearchEntityUtils;
import org.molgenis.data.elasticsearch.util.ElasticsearchUtils;
import org.molgenis.data.elasticsearch.util.SearchRequest;
import org.molgenis.data.elasticsearch.util.SearchResult;
import org.molgenis.data.meta.AttributeMetaDataMetaData;
import org.molgenis.data.meta.EntityMetaDataMetaData;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.transaction.LockMetaData;
import org.molgenis.data.transaction.MolgenisTransactionListener;
import org.molgenis.data.transaction.MolgenisTransactionLogEntryMetaData;
import org.molgenis.data.transaction.MolgenisTransactionLogMetaData;
import org.molgenis.util.DependencyResolver;
import org.molgenis.util.EntityUtils;
import org.molgenis.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

/**
 * ElasticSearch implementation of the SearchService interface. TODO use scroll-scan where possible:
 * http://www.elasticsearch.org/guide/en/elasticsearch /reference/current/search-request-scroll.html#scroll-scans
 * 
 * @author erwin
 */
public class ElasticSearchService implements SearchService, MolgenisTransactionListener
{
	private static final Logger LOG = LoggerFactory.getLogger(ElasticSearchService.class);
	public static final String CRUD_TYPE_FIELD_NAME = "MolgenisCrudType";
	private static BulkProcessorFactory BULK_PROCESSOR_FACTORY = new BulkProcessorFactory();
	private static List<String> NON_TRANSACTIONAL_ENTITIES = Arrays.asList(MolgenisTransactionLogMetaData.ENTITY_NAME,
			MolgenisTransactionLogEntryMetaData.ENTITY_NAME, LockMetaData.ENTITY_NAME);

	public static enum IndexingMode
	{
		ADD, UPDATE
	};

	static enum CrudType
	{
		ADD, UPDATE, DELETE
	}

	private final DataService dataService;
	private final String indexName;
	private final Client client;
	private final ResponseParser responseParser = new ResponseParser();
	private final SearchRequestGenerator generator = new SearchRequestGenerator();
	private final EntityToSourceConverter entityToSourceConverter;
	private final ElasticsearchUtils elasticsearchUtils;

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
		this.elasticsearchUtils = new ElasticsearchUtils(client);

		if (createIndexIfNotExists)
		{
			new ElasticsearchIndexCreator(client).createIndexIfNotExists(indexName);
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
		if (LOG.isTraceEnabled())
		{
			LOG.trace("*** REQUEST\n" + builder);
		}
		generator.buildSearchRequest(builder, documentType, searchType, request.getQuery(),
				request.getFieldsToReturn(), request.getAggregateField1(), request.getAggregateField2(),
				request.getAggregateFieldDistinct(), entityMetaData);
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
	 * @see org.molgenis.data.elasticsearch.SearchService#hasMapping(org.molgenis .data.EntityMetaData)
	 */
	@Override
	public boolean hasMapping(EntityMetaData entityMetaData)
	{
		String docType = sanitizeMapperType(entityMetaData.getName());

		GetMappingsResponse getMappingsResponse = client.admin().indices().prepareGetMappings(indexName).execute()
				.actionGet();
		ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> allMappings = getMappingsResponse
				.getMappings();
		final ImmutableOpenMap<String, MappingMetaData> indexMappings = allMappings.get(indexName);
		return indexMappings.containsKey(docType);
	}

	public boolean hasMapping(String index, EntityMetaData entityMetaData)
	{
		String docType = sanitizeMapperType(entityMetaData.getName());

		GetMappingsResponse getMappingsResponse = client.admin().indices().prepareGetMappings(index).execute()
				.actionGet();
		ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> allMappings = getMappingsResponse
				.getMappings();
		final ImmutableOpenMap<String, MappingMetaData> indexMappings = allMappings.get(index);
		return indexMappings.containsKey(docType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.data.elasticsearch.SearchService#createMappings(org.molgenis .data.EntityMetaData)
	 */
	@Override
	public void createMappings(EntityMetaData entityMetaData)
	{
		createMappings(entityMetaData, true, true, true);
	}

	public void createMappings(String index, EntityMetaData entityMetaData)
	{
		createMappings(index, entityMetaData, true, true, true);
	}

	private void createMappings(String index, EntityMetaData entityMetaData, boolean storeSource, boolean enableNorms,
			boolean createAllIndex)
	{
		try
		{
			XContentBuilder jsonBuilder = MappingsBuilder.buildMapping(entityMetaData, storeSource, enableNorms,
					createAllIndex);
			if (LOG.isTraceEnabled()) LOG.trace("Creating Elasticsearch mapping [" + jsonBuilder.string() + "] ...");
			String entityName = entityMetaData.getName();

			PutMappingResponse response = client.admin().indices().preparePutMapping(index)
					.setType(sanitizeMapperType(entityName)).setSource(jsonBuilder).execute().actionGet();

			if (!response.isAcknowledged())
			{
				throw new ElasticsearchException("Creation of mapping for documentType [" + entityName
						+ "] failed. Response=" + response);
			}

			if (LOG.isDebugEnabled()) LOG.debug("Created Elasticsearch mapping [" + jsonBuilder.string() + "]");
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.data.elasticsearch.SearchService#createMappings(org.molgenis .data.EntityMetaData, boolean,
	 * boolean, boolean)
	 */
	@Override
	public void createMappings(EntityMetaData entityMetaData, boolean storeSource, boolean enableNorms,
			boolean createAllIndex)
	{
		createMappings(indexName, entityMetaData, storeSource, enableNorms, createAllIndex);
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
		refresh(indexName);
		if (LOG.isDebugEnabled()) LOG.debug("Refreshed Elasticsearch index [" + indexName + "]");
	}

	public void refresh(String index)
	{
		elasticsearchUtils.refreshIndex(index);
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
	}

	private void index(Entity entity, EntityMetaData entityMetaData, IndexingMode indexingMode, boolean updateIndex)
	{
		String transactionId = null;
		if (!NON_TRANSACTIONAL_ENTITIES.contains(entityMetaData.getName()))
		{
			transactionId = getCurrentTransactionId();
		}
		String index = transactionId != null ? transactionId : indexName;
		CrudType crudType = indexingMode == IndexingMode.ADD ? CrudType.ADD : CrudType.UPDATE;

		index(index, Arrays.asList(entity), entityMetaData, crudType, updateIndex);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.data.elasticsearch.SearchService#index(java.lang.Iterable, org.molgenis.data.EntityMetaData,
	 * org.molgenis.data.elasticsearch.ElasticSearchService.IndexingMode)
	 */
	@Override
	public long index(Iterable<? extends Entity> entities, EntityMetaData entityMetaData, IndexingMode indexingMode)
	{
		String transactionId = null;
		if (!NON_TRANSACTIONAL_ENTITIES.contains(entityMetaData.getName()))
		{
			transactionId = getCurrentTransactionId();
		}
		String index = transactionId != null ? transactionId : indexName;

		CrudType crudType = indexingMode == IndexingMode.ADD ? CrudType.ADD : CrudType.UPDATE;
		return index(index, entities, entityMetaData, crudType, true);
	}

	private String getCurrentTransactionId()
	{
		return (String) TransactionSynchronizationManager.getResource(TRANSACTION_ID_RESOURCE_NAME);
	}

	long index(String index, Iterable<? extends Entity> entities, EntityMetaData entityMetaData, CrudType crudType,
			boolean updateIndex)
	{
		String entityName = entityMetaData.getName();
		String type = sanitizeMapperType(entityName);
		String transactionId = null;

		if (!NON_TRANSACTIONAL_ENTITIES.contains(entityMetaData.getName()))
		{
			transactionId = getCurrentTransactionId();
		}

		long nrIndexedEntities = 0;
		BulkProcessor bulkProcessor = BULK_PROCESSOR_FACTORY.create(client);

		try
		{
			if (transactionId != null)
			{
				createMappings(transactionId, entityMetaData);
			}

			for (Entity entity : entities)
			{
				String id = toElasticsearchId(entity, entityMetaData);
				Map<String, Object> source = entityToSourceConverter.convert(entity, entityMetaData);
				if (transactionId != null)
				{
					source.put(CRUD_TYPE_FIELD_NAME, crudType.name());
				}
				bulkProcessor.add(new IndexRequest().index(index).type(type).id(id).source(source));
				++nrIndexedEntities;
			}
		}
		finally
		{
			elasticsearchUtils.waitForCompletion(bulkProcessor);
		}

		refresh(index);

		// If not in transaction, update references now, if in transaction the references are updated in
		// the commitTransaction method
		if (updateIndex && (crudType == CrudType.UPDATE) && (transactionId == null))
		{
			updateReferences(entities, entityMetaData);
		}

		return nrIndexedEntities;
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
		if (!canBeDeleted(Arrays.asList(id), entityMetaData))
		{
			throw new MolgenisDataException(
					"Cannot delete entity because there are other entities referencing it. Delete these first.");
		}

		String transactionId = getCurrentTransactionId();
		if (transactionId == null || NON_TRANSACTIONAL_ENTITIES.contains(entityMetaData.getName()))
		{
			deleteById(indexName, id, entityMetaData);
		}
		else
		{
			// Check if delete from main index or if it is delete from entity that is not committed yet and is in the
			// temp index
			String type = sanitizeMapperType(entityMetaData.getName());
			GetResponse response = client.prepareGet(indexName, type, id).execute().actionGet();
			if (response.isExists())
			{
				// Copy to temp transaction index and mark as deleted
				Entity entity = get(id, entityMetaData);
				index(transactionId, Arrays.asList(entity), entityMetaData, CrudType.DELETE, false);
			}
			else
			{
				deleteById(transactionId, id, entityMetaData);
			}
		}
	}

	private void deleteById(String index, String id, EntityMetaData entityMetaData)
	{
		String entityName = entityMetaData.getName();
		String type = sanitizeMapperType(entityName);

		if (LOG.isTraceEnabled())
		{
			LOG.trace("Deleting Elasticsearch '" + type + "' doc with id [" + id + "] ...");
		}

		GetResponse response = client.prepareGet(index, type, id).execute().actionGet();
		if (response.isExists())
		{
			client.prepareDelete(index, type, id).setRefresh(true).execute().actionGet();
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
		ids.forEach(id -> deleteById(id, entityMetaData));
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
		List<Object> ids = stream(entities.spliterator(), true).map(e -> e.getIdValue()).collect(Collectors.toList());
		if (!canBeDeleted(ids, entityMetaData))
		{
			throw new MolgenisDataException(
					"Cannot delete entity because there are other entities referencing it. Delete these first.");
		}

		deleteById(toElasticsearchIds(ids), entityMetaData);
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
				throw new ElasticsearchException("Delete of mapping '" + entityName + "' failed.");
			}
		}

		if (LOG.isDebugEnabled())
		{
			LOG.debug("Deleted all Elasticsearch '" + type + "' docs");
		}

		DeleteByQueryResponse deleteByQueryResponse = client.prepareDeleteByQuery(indexName)
				.setQuery(new TermQueryBuilder("_type", type)).execute().actionGet();

		if (deleteByQueryResponse != null)
		{
			IndexDeleteByQueryResponse idbqr = deleteByQueryResponse.getIndex(indexName);
			if (idbqr != null && idbqr.getFailedShards() > 0)
			{
				throw new ElasticsearchException("Delete all entities of type '" + entityName + "' failed.");
			}
		}
		refresh();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.data.elasticsearch.SearchService#get(java.lang.Object, org.molgenis.data.EntityMetaData)
	 */
	@Override
	public Entity get(Object entityId, EntityMetaData entityMetaData)
	{
		String entityName = entityMetaData.getName();
		String type = sanitizeMapperType(entityName);
		String id = ElasticsearchEntityUtils.toElasticsearchId(entityId);

		if (LOG.isTraceEnabled())
		{
			LOG.trace("Retrieving Elasticsearch '" + type + "' doc with id [" + id + "] ...");
		}

		String transactionId = getCurrentTransactionId();
		if (transactionId != null)
		{
			MultiGetResponse response = client.prepareMultiGet().add(transactionId, type, id).add(indexName, type, id)
					.execute().actionGet();

			for (MultiGetItemResponse res : response.getResponses())
			{
				if ((res.getResponse() != null) && res.getResponse().isExists())
				{
					return new DefaultEntity(entityMetaData, dataService, res.getResponse().getSource());
				}
			}

			return null;
		}
		else
		{
			GetResponse response = client.prepareGet(indexName, type, id).execute().actionGet();
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Retrieved Elasticsearch '" + type + "' doc with id [" + id + "]");
			}

			return response.isExists() ? new DefaultEntity(entityMetaData, dataService, response.getSource()) : null;
		}
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
		String transactionId = getCurrentTransactionId();

		if (LOG.isTraceEnabled())
		{
			LOG.trace("Retrieving Elasticsearch '" + type + "' docs with ids [" + entityIds + "] ...");
		}

		MultiGetRequestBuilder request = client.prepareMultiGet().add(indexName, type,
				ElasticsearchEntityUtils.toElasticsearchIds(entityIds));

		if (transactionId != null)
		{
			request.add(transactionId, type, ElasticsearchEntityUtils.toElasticsearchIds(entityIds));
		}

		MultiGetResponse response = request.execute().actionGet();

		if (LOG.isDebugEnabled())
		{
			LOG.debug("Retrieved Elasticsearch '" + type + "' docs with ids [" + entityIds + "] ...");
		}

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
				return getResponse.isExists() ? new DefaultEntity(entityMetaData, dataService, getResponse.getSource()) : null;
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
		String[] indexNames = new String[]
		{ indexName };

		String transactionId = null;
		if (!NON_TRANSACTIONAL_ENTITIES.contains(entityMetaData.getName()))
		{
			transactionId = getCurrentTransactionId();
		}

		if ((transactionId != null) && hasMapping(transactionId, entityMetaData))
		{
			indexNames = ArrayUtils.add(indexNames, transactionId);
		}

		return new ElasticsearchEntityIterable(q, entityMetaData, client, dataService, generator, indexNames);
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
		if (DependencyResolver.hasSelfReferences(entityMetaData))
		{
			Iterable<Entity> iterable = Iterables.transform(entities, new Function<Entity, Entity>()
			{
				@Override
				public Entity apply(Entity input)
				{
					return input;
				}
			});

			Iterable<Entity> resolved = new DependencyResolver().resolveSelfReferences(iterable, entityMetaData);
			if (hasMapping(entityMetaData))
			{
				delete(entityMetaData.getName());
			}
			createMappings(entityMetaData);

			for (Entity e : resolved)
			{
				index(e, entityMetaData, IndexingMode.ADD);
			}
		}
		else
		{
			if (hasMapping(entityMetaData))
			{
				delete(entityMetaData.getName());
			}
			createMappings(entityMetaData);

			index(entities, entityMetaData, IndexingMode.ADD);
		}
	}

	private void updateReferences(Entity refEntity, EntityMetaData refEntityMetaData)
	{
		for (Pair<EntityMetaData, List<AttributeMetaData>> pair : EntityUtils.getReferencingEntityMetaData(
				refEntityMetaData, dataService))
		{
			EntityMetaData entityMetaData = pair.getA();

			QueryImpl q = null;
			for (AttributeMetaData attributeMetaData : pair.getB())
			{
				if (q == null) q = new QueryImpl();
				else q.or();
				q.eq(attributeMetaData.getName(), refEntity);
			}

			Iterable<Entity> entities = new ElasticsearchEntityIterable(q, entityMetaData, client, dataService,
					generator, new String[]
					{ indexName });

			// Don't use cached ref entities but make new ones
			entities = Iterables.transform(entities, new Function<Entity, Entity>()
			{
				@Override
				public Entity apply(Entity entity)
				{
					return new DefaultEntity(entityMetaData, dataService, entity);
				}
			});

			index(indexName, entities, entityMetaData, CrudType.UPDATE, false);
		}
	}

	private void updateReferences(Iterable<? extends Entity> entities, EntityMetaData entityMetaData)
	{
		for (Entity entity : entities)
		{
			updateReferences(entity, entityMetaData);
		}
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

	// Checks if entities can be deleted, have no ref entities pointing to it
	private boolean canBeDeleted(Iterable<?> ids, EntityMetaData meta)
	{
		List<Pair<EntityMetaData, List<AttributeMetaData>>> referencingMetas = EntityUtils
				.getReferencingEntityMetaData(meta, dataService);
		if (referencingMetas.isEmpty()) return true;

		for (Pair<EntityMetaData, List<AttributeMetaData>> pair : referencingMetas)
		{
			EntityMetaData refEntityMetaData = pair.getA();

			if (!refEntityMetaData.getName().equals(EntityMetaDataMetaData.ENTITY_NAME)
					&& !refEntityMetaData.getName().equals(AttributeMetaDataMetaData.ENTITY_NAME))
			{
				QueryImpl q = null;
				for (AttributeMetaData attributeMetaData : pair.getB())
				{
					if (q == null) q = new QueryImpl();
					else q.or();
					q.in(attributeMetaData.getName(), ids);
				}

				if (dataService.count(refEntityMetaData.getName(), q) > 0) return false;
			}
		}

		return true;
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
		private final DataService dataService;;
		private final SearchRequestGenerator searchRequestGenerator;
		private final String[] indexNames;

		private final String type;
		private final List<String> fieldsToReturn;
		private final int offset;
		private final int pageSize;

		public ElasticsearchEntityIterable(Query q, EntityMetaData entityMetaData, Client client,
				DataService dataService, SearchRequestGenerator searchRequestGenerator, String[] indexNames)
		{
			this.client = client;
			this.q = q;
			this.entityMetaData = entityMetaData;
			this.dataService = dataService;
			this.searchRequestGenerator = searchRequestGenerator;
			this.indexNames = indexNames;

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
						return new DefaultEntity(entityMetaData, dataService, hit.getSource());
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

					SearchRequestBuilder searchRequestBuilder = client.prepareSearch(indexNames);
					searchRequestGenerator.buildSearchRequest(searchRequestBuilder, type, SearchType.QUERY_AND_FETCH,
							q, fieldsToReturn, null, null, null, entityMetaData);
					if (LOG.isTraceEnabled())
					{
						LOG.trace("SearchRequest: " + searchRequestBuilder);
					}
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

	@Override
	public void transactionStarted(String transactionId)
	{
		new ElasticsearchIndexCreator(client).createIndexIfNotExists(transactionId);
	}

	@Override
	public void commitTransaction(String transactionId)
	{
		try
		{
			SearchResponse searchResponse = client.prepareSearch(transactionId).setQuery(QueryBuilders.matchAllQuery())
					.setSearchType(SearchType.SCAN).setScroll(TimeValue.timeValueMinutes(5)).setSize(1000).execute()
					.actionGet();

			if (searchResponse.getHits().getTotalHits() > 0)
			{
				BulkProcessor bulkProcessor = BULK_PROCESSOR_FACTORY.create(client);

				searchResponse = client.prepareSearchScroll(searchResponse.getScrollId())
						.setScroll(TimeValue.timeValueMinutes(5)).execute().actionGet();

				while (searchResponse.getHits().getHits().length > 0)
				{
					for (SearchHit hit : searchResponse.getHits())
					{
						String entityName = hit.type();
						Map<String, Object> values = hit.getSource();
						CrudType crudType = CrudType.valueOf((String) values.remove(CRUD_TYPE_FIELD_NAME));
						EntityMetaData entityMeta = dataService.getEntityMetaData(entityName);

						if ((crudType == CrudType.UPDATE) || (crudType == CrudType.ADD))
						{
							bulkProcessor.add(new IndexRequest(indexName, entityName, hit.id()).source(values));

							if (crudType == CrudType.UPDATE)
							{
								updateReferences(new DefaultEntity(entityMeta, dataService, values), entityMeta);
							}
						}
						else if (crudType == CrudType.DELETE)
						{
							deleteById(indexName, hit.id(), entityMeta);
						}
					}

					searchResponse = client.prepareSearchScroll(searchResponse.getScrollId())
							.setScroll(TimeValue.timeValueMinutes(5)).execute().actionGet();
				}
				bulkProcessor.close();
			}
			refresh();
		}
		finally
		{
			cleanUpTrans(transactionId);
		}
	}

	@Override
	public void rollbackTransaction(String transactionId)
	{
		cleanUpTrans(transactionId);
	}

	private void cleanUpTrans(String transactionId)
	{
		if (elasticsearchUtils.indexExists(transactionId))
		{
			elasticsearchUtils.deleteIndex(transactionId);
		}

		flush();
	}

}
