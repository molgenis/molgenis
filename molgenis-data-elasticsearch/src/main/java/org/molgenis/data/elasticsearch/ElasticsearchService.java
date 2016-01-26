package org.molgenis.data.elasticsearch;

import static java.util.Objects.requireNonNull;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.elasticsearch.request.SourceFilteringGenerator.toFetchFields;
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
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.delete.DeleteMappingResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.admin.indices.optimize.OptimizeResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.deletebyquery.DeleteByQueryResponse;
import org.elasticsearch.action.deletebyquery.IndexDeleteByQueryResponse;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequest.Item;
import org.elasticsearch.action.get.MultiGetRequestBuilder;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.collect.Iterators;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.fetch.source.FetchSourceContext;
import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Fetch;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.elasticsearch.index.ElasticsearchIndexCreator;
import org.molgenis.data.elasticsearch.index.MappingsBuilder;
import org.molgenis.data.elasticsearch.request.SearchRequestGenerator;
import org.molgenis.data.elasticsearch.response.ResponseParser;
import org.molgenis.data.elasticsearch.util.ElasticsearchUtils;
import org.molgenis.data.elasticsearch.util.SearchRequest;
import org.molgenis.data.elasticsearch.util.SearchResult;
import org.molgenis.data.meta.AttributeMetaDataMetaData;
import org.molgenis.data.meta.EntityMetaDataMetaData;
import org.molgenis.data.meta.PackageImpl;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.support.UuidGenerator;
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
public class ElasticsearchService implements SearchService, MolgenisTransactionListener
{
	private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchService.class);

	private static final int BATCH_SIZE = 1000;

	public static final String CRUD_TYPE_FIELD_NAME = "MolgenisCrudType";
	private static BulkProcessorFactory BULK_PROCESSOR_FACTORY = new BulkProcessorFactory();
	private static List<String> NON_TRANSACTIONAL_ENTITIES = Arrays.asList(MolgenisTransactionLogMetaData.ENTITY_NAME,
			MolgenisTransactionLogEntryMetaData.ENTITY_NAME);

	public static enum IndexingMode
	{
		ADD, UPDATE
	};

	static enum CrudType
	{
		ADD, UPDATE, DELETE
	}

	private final DataService dataService;
	private final ElasticsearchEntityFactory elasticsearchEntityFactory;
	private final String indexName;
	private final Client client;
	private final ResponseParser responseParser = new ResponseParser();
	private final SearchRequestGenerator generator = new SearchRequestGenerator();
	private final ElasticsearchUtils elasticsearchUtils;

	public ElasticsearchService(Client client, String indexName, DataService dataService,
			ElasticsearchEntityFactory elasticsearchEntityFactory)
	{
		this(client, indexName, dataService, elasticsearchEntityFactory, true);
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
	ElasticsearchService(Client client, String indexName, DataService dataService,
			ElasticsearchEntityFactory elasticsearchEntityFactory, boolean createIndexIfNotExists)
	{
		this.client = requireNonNull(client);
		this.indexName = requireNonNull(indexName);
		this.dataService = requireNonNull(dataService);
		this.elasticsearchEntityFactory = requireNonNull(elasticsearchEntityFactory);
		this.elasticsearchUtils = new ElasticsearchUtils(client);

		if (createIndexIfNotExists)
		{
			new ElasticsearchIndexCreator(client).createIndexIfNotExists(indexName);
		}
	}

	@Override
	public Iterable<String> getTypes()
	{
		if (LOG.isTraceEnabled())
		{
			LOG.trace("Retrieving Elasticsearch mappings ...");
		}
		GetMappingsResponse mappingsResponse = client.admin().indices().prepareGetMappings(indexName).get();
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Retrieved Elasticsearch mappings");
		}

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
		EntityMetaData entityMetaData = (request.getDocumentType() != null && dataService != null
				&& dataService.hasRepository(request.getDocumentType()))
						? dataService.getEntityMetaData(request.getDocumentType()) : null;
		String documentType = request.getDocumentType() == null ? null : sanitizeMapperType(request.getDocumentType());
		if (LOG.isTraceEnabled())
		{
			LOG.trace("*** REQUEST\n" + builder);
		}
		generator.buildSearchRequest(builder, documentType, searchType, request.getQuery(),
				request.getAggregateField1(), request.getAggregateField2(), request.getAggregateFieldDistinct(),
				entityMetaData);
		SearchResponse response = builder.get();
		if (LOG.isTraceEnabled())
		{
			LOG.trace("*** RESPONSE\n" + response);
		}
		return responseParser.parseSearchResponse(request, response, entityMetaData, dataService);
	}

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

	@Override
	public void createMappings(EntityMetaData entityMetaData)
	{
		boolean storeSource = storeSource(entityMetaData);
		createMappings(entityMetaData, storeSource, true, true);
	}

	public void createMappings(String index, EntityMetaData entityMetaData)
	{
		boolean storeSource = storeSource(entityMetaData);
		createMappings(index, entityMetaData, storeSource, true, true);
	}

	private void createMappings(String index, EntityMetaData entityMetaData, boolean storeSource, boolean enableNorms,
			boolean createAllIndex)
	{
		try
		{
			XContentBuilder jsonBuilder = MappingsBuilder.buildMapping(entityMetaData, storeSource, enableNorms,
					createAllIndex);
			if (LOG.isTraceEnabled()) LOG.trace("Creating Elasticsearch mapping [{}] ...", jsonBuilder.string());
			String entityName = entityMetaData.getName();

			PutMappingResponse response = client.admin().indices().preparePutMapping(index)
					.setType(sanitizeMapperType(entityName)).setSource(jsonBuilder).get();

			if (!response.isAcknowledged())
			{
				throw new ElasticsearchException(
						"Creation of mapping for documentType [" + entityName + "] failed. Response=" + response);
			}

			if (LOG.isDebugEnabled()) LOG.debug("Created Elasticsearch mapping [{}]", jsonBuilder.string());
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public void createMappings(EntityMetaData entityMetaData, boolean storeSource, boolean enableNorms,
			boolean createAllIndex)
	{
		createMappings(indexName, entityMetaData, storeSource, enableNorms, createAllIndex);
	}

	@Override
	public void refresh(EntityMetaData entityMeta)
	{
		String transactionId = getCurrentTransactionId();
		if (transactionId != null && !NON_TRANSACTIONAL_ENTITIES.contains(entityMeta.getName()))
		{
			refresh(transactionId);
		}
		else
		{
			refresh(indexName);
		}
	}

	private void refresh(String index)
	{
		if (LOG.isTraceEnabled()) LOG.trace("Refreshing Elasticsearch index [{}] ...", index);
		elasticsearchUtils.refreshIndex(index);
		if (LOG.isDebugEnabled()) LOG.debug("Refreshed Elasticsearch index [{}]", index);
	}

	@Override
	public long count(EntityMetaData entityMetaData)
	{
		return count(null, entityMetaData);
	}

	@Override
	public long count(Query q, EntityMetaData entityMetaData)
	{
		String entityName = entityMetaData.getName();
		String type = sanitizeMapperType(entityName);

		if (LOG.isTraceEnabled())
		{
			if (q != null)
			{
				LOG.trace("Counting Elasticsearch [{}] docs using query [{}] ...", type, q);
			}
			else
			{
				LOG.trace("Counting Elasticsearch [{}] docs", type);
			}
		}
		SearchRequestBuilder searchRequestBuilder = client.prepareSearch(indexName);
		generator.buildSearchRequest(searchRequestBuilder, type, SearchType.COUNT, q, null, null, null, entityMetaData);
		SearchResponse searchResponse = searchRequestBuilder.get();
		if (searchResponse.getFailedShards() > 0)
		{
			throw new ElasticsearchException("Search failed. Returned headers:" + searchResponse.getHeaders());
		}
		long count = searchResponse.getHits().totalHits();
		if (LOG.isDebugEnabled())
		{
			long ms = searchResponse.getTookInMillis();
			if (q != null)
			{
				LOG.debug("Counted {} Elasticsearch [{}] docs using query [{}] in {}ms", count, type, q, ms);
			}
			else
			{
				LOG.debug("Counted {} Elasticsearch [{}] docs in {}ms", count, type, ms);
			}
		}

		String transactionId = getCurrentTransactionId();
		if (transactionId != null && !NON_TRANSACTIONAL_ENTITIES.contains(entityMetaData.getName()))
		{
			if (hasMapping(transactionId, entityMetaData))
			{
				// count added entities in transaction index
				Query countAddedQ = q != null ? new QueryImpl(q) : new QueryImpl();
				if (countAddedQ.getRules() != null && !countAddedQ.getRules().isEmpty())
				{
					countAddedQ.and();
				}
				countAddedQ.eq(CRUD_TYPE_FIELD_NAME, CrudType.ADD.toString());
				SearchRequestBuilder countAddSearchRequestBuilder = client.prepareSearch(transactionId);
				generator.buildSearchRequest(countAddSearchRequestBuilder, type, SearchType.COUNT, countAddedQ, null,
						null, null, entityMetaData);
				SearchResponse countAddSearchResponse = countAddSearchRequestBuilder.get();
				if (countAddSearchResponse.getFailedShards() > 0)
				{
					throw new ElasticsearchException(
							"Search failed. Returned headers:" + countAddSearchResponse.getHeaders());
				}
				long addedCount = countAddSearchResponse.getHits().totalHits();

				// count deleted entities in transaction index
				Query countDeletedQ = q != null ? new QueryImpl(q) : new QueryImpl();
				if (countDeletedQ.getRules() != null && !countDeletedQ.getRules().isEmpty())
				{
					countDeletedQ.and();
				}
				countDeletedQ.eq(CRUD_TYPE_FIELD_NAME, CrudType.DELETE.toString());
				SearchRequestBuilder countDeletedSearchRequestBuilder = client.prepareSearch(transactionId);
				generator.buildSearchRequest(countDeletedSearchRequestBuilder, type, SearchType.COUNT, countDeletedQ,
						null, null, null, entityMetaData);
				SearchResponse countDeletedSearchResponse = countDeletedSearchRequestBuilder.get();
				if (countDeletedSearchResponse.getFailedShards() > 0)
				{
					throw new ElasticsearchException(
							"Search failed. Returned headers:" + countDeletedSearchResponse.getHeaders());
				}
				long deletedCount = countDeletedSearchResponse.getHits().totalHits();

				count = count + addedCount - deletedCount;
			}
		}
		return count;
	}

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

		index(index, Collections.singleton(entity).iterator(), entityMetaData, crudType, updateIndex);
	}

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
		return index(index, entities.iterator(), entityMetaData, crudType, true);
	}

	@Override
	public long index(Stream<? extends Entity> entities, EntityMetaData entityMetaData, IndexingMode indexingMode)
	{
		String transactionId = null;
		if (!NON_TRANSACTIONAL_ENTITIES.contains(entityMetaData.getName()))
		{
			transactionId = getCurrentTransactionId();
		}
		String index = transactionId != null ? transactionId : indexName;

		CrudType crudType = indexingMode == IndexingMode.ADD ? CrudType.ADD : CrudType.UPDATE;
		return index(index, entities.iterator(), entityMetaData, crudType, true);
	}

	private String getCurrentTransactionId()
	{
		return (String) TransactionSynchronizationManager.getResource(TRANSACTION_ID_RESOURCE_NAME);
	}

	long index(String index, Iterator<? extends Entity> it, EntityMetaData entityMetaData, CrudType crudType,
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
				// store entities in the index related to this transaction even
				// if the entity should not be stored in
				// the index, after transaction commit the transaction index is
				// merged with the main index. Based on the
				// main index mapping the data is (not) stored. The transaction
				// index is removed after transaction
				// commit or rollback.
				if (!hasMapping(transactionId, entityMetaData))
				{
					createMappings(transactionId, entityMetaData, true, true, true);
				}
			}

			while (it.hasNext())
			{
				Entity entity = it.next();
				String id = toElasticsearchId(entity, entityMetaData);
				Map<String, Object> source = elasticsearchEntityFactory.create(entityMetaData, entity);
				if (transactionId != null)
				{
					if (crudType == CrudType.UPDATE)
					{
						// updating a document in the transactional index is the same as adding the new updated
						// document.
						GetResponse response = client.prepareGet(transactionId, type, id).get();
						if (LOG.isDebugEnabled())
						{
							LOG.debug("Retrieved document type [{}] with id [{}] in index [{}]", type, id,
									transactionId);
						}
						if (response.isExists())
						{
							crudType = CrudType.ADD;
						}
					}
					source.put(CRUD_TYPE_FIELD_NAME, crudType.name());
				}
				if (LOG.isDebugEnabled())
				{
					LOG.debug("Indexing [{}] with id [{}] in index [{}] mode [{}] ...", type, id, index, crudType);
				}

				bulkProcessor.add(new IndexRequest().index(index).type(type).id(id).source(source));
				++nrIndexedEntities;

				// If not in transaction, update references now, if in transaction the
				// references are updated in
				// the commitTransaction method
				if (updateIndex && (crudType == CrudType.UPDATE) && (transactionId == null))
				{
					updateReferences(entity, entityMetaData);
				}
			}
		}
		finally
		{
			elasticsearchUtils.waitForCompletion(bulkProcessor);
		}

		return nrIndexedEntities;
	}

	@Override
	public void delete(Entity entity, EntityMetaData entityMetaData)
	{
		String elasticsearchId = toElasticsearchId(entity, entityMetaData);
		deleteById(elasticsearchId, entityMetaData);
	}

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
			// Check if delete from main index or if it is delete from entity
			// that is not committed yet and is in the
			// temp index
			String type = sanitizeMapperType(entityMetaData.getName());
			GetResponse response = client.prepareGet(indexName, type, id).get();
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Retrieved document type [{}] with id [{}] in index [{}]", type, id, indexName);
			}
			if (response.isExists())
			{
				// Copy to temp transaction index and mark as deleted
				Entity entity = new MapEntity(entityMetaData);
				entity.set(entityMetaData.getIdAttribute().getName(), id);
				index(transactionId, Collections.singleton(entity).iterator(), entityMetaData, CrudType.DELETE, false);
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
		GetResponse response = client.prepareGet(index, type, id).get();
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Retrieved document type [{}] with id [{}] in index [{}]", type, id, index);
		}
		if (response.isExists())
		{
			client.prepareDelete(index, type, id).get();
		}

		if (LOG.isDebugEnabled())
		{
			LOG.debug("Deleted Elasticsearch '" + type + "' doc with id [" + id + "]");
		}
	}

	@Override
	public void deleteById(Stream<String> ids, EntityMetaData entityMetaData)
	{
		ids.forEach(id -> deleteById(id, entityMetaData));
	}

	@Override
	public void delete(Iterable<? extends Entity> entities, EntityMetaData entityMetaData)
	{
		delete(stream(entities.spliterator(), true), entityMetaData);
	}

	@Override
	public void delete(Stream<? extends Entity> entities, EntityMetaData entityMetaData)
	{
		Stream<Object> entityIds = entities.map(entity -> entity.getIdValue());
		Iterators.partition(entityIds.iterator(), BATCH_SIZE).forEachRemaining(batchEntityIds -> {
			if (!canBeDeleted(batchEntityIds, entityMetaData))
			{
				throw new MolgenisDataException(
						"Cannot delete entity because there are other entities referencing it. Delete these first.");
			}

			deleteById(toElasticsearchIds(batchEntityIds.stream()), entityMetaData);
		});
	}

	@Override
	public void delete(String entityName)
	{
		String type = sanitizeMapperType(entityName);

		if (LOG.isTraceEnabled())
		{
			LOG.trace("Deleting all Elasticsearch '" + type + "' docs ...");
		}
		TypesExistsResponse typesExistsResponse = client.admin().indices().prepareTypesExists(indexName).setTypes(type)
				.get();
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Checked whether type [{}] exists in index [{}]", type, indexName);
		}
		if (typesExistsResponse.isExists())
		{
			DeleteMappingResponse deleteMappingResponse = client.admin().indices().prepareDeleteMapping(indexName)
					.setType(type).get();
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
				.setQuery(new TermQueryBuilder("_type", type)).get();

		if (deleteByQueryResponse != null)
		{
			IndexDeleteByQueryResponse idbqr = deleteByQueryResponse.getIndex(indexName);
			if (idbqr != null && idbqr.getFailedShards() > 0)
			{
				throw new ElasticsearchException("Delete all entities of type '" + entityName + "' failed.");
			}
		}
	}

	/**
	 * Retrieve a stored entity from the index. Can only be used if the mapping was created with storeSource=true.
	 */
	@Override
	public Entity get(Object entityId, EntityMetaData entityMetaData)
	{
		return get(entityId, entityMetaData, null);
	}

	@Override
	public Entity get(Object entityId, EntityMetaData entityMetaData, Fetch fetch)
	{
		String entityName = entityMetaData.getName();
		String type = sanitizeMapperType(entityName);
		String id = toElasticsearchId(entityId);

		if (LOG.isTraceEnabled())
		{
			if (fetch == null)
			{
				LOG.trace("Retrieving Elasticsearch [{}] doc with id [{}] ...", type, id);
			}
			else
			{
				LOG.trace("Retrieving Elasticsearch [{}] doc with id [{}] and fetch [{}] ...", type, id, fetch);
			}
		}

		String transactionId = getCurrentTransactionId();
		if (transactionId != null)
		{
			Item transactionItem = createMultiGetItem(transactionId, type, id, fetch);
			Item indexItem = createMultiGetItem(indexName, type, id, fetch);
			MultiGetResponse response = client.prepareMultiGet().add(transactionItem).add(indexItem).execute()
					.actionGet();

			for (MultiGetItemResponse res : response.getResponses())
			{
				if ((res.getResponse() != null) && res.getResponse().isExists())
				{
					return elasticsearchEntityFactory.create(entityMetaData, res.getResponse().getSource(), fetch);
				}
			}

			return null;
		}
		else
		{
			GetRequestBuilder requestBuilder = client.prepareGet(indexName, type, id);
			if (fetch != null)
			{
				requestBuilder.setFetchSource(toFetchFields(fetch), null);
			}
			GetResponse response = requestBuilder.get();
			if (LOG.isDebugEnabled())
			{
				if (fetch == null)
				{
					LOG.debug("Retrieved Elasticsearch [{}] doc with id [{}]", type, id);
				}
				else
				{
					LOG.debug("Retrieved Elasticsearch [{}] doc with id [{}] and fetch [{}]", type, id, fetch);
				}
			}

			return response.isExists() ? elasticsearchEntityFactory.create(entityMetaData, response.getSource(), fetch)
					: null;
		}
	}

	/**
	 * Retrieve stored entities from the index. Can only be used if the mapping was created with storeSource=true.
	 */
	@Override
	public Iterable<Entity> get(Iterable<Object> entityIds, final EntityMetaData entityMetaData)
	{
		return get(entityIds, entityMetaData, null);
	}

	@Override
	public Iterable<Entity> get(Iterable<Object> entityIds, final EntityMetaData entityMetaData, Fetch fetch)
	{
		return new Iterable<Entity>()
		{
			@Override
			public Iterator<Entity> iterator()
			{
				Stream<Object> stream = stream(entityIds.spliterator(), false);
				return get(stream, entityMetaData, fetch).iterator();
			}

		};
	}

	/**
	 * Retrieve stored entities from the index. Can only be used if the mapping was created with storeSource=true.
	 */
	@Override
	public Stream<Entity> get(Stream<Object> entityIds, final EntityMetaData entityMetaData)
	{
		return get(entityIds, entityMetaData, null);
	}

	@Override
	public Stream<Entity> get(Stream<Object> entityIds, final EntityMetaData entityMetaData, Fetch fetch)
	{
		String entityName = entityMetaData.getName();
		String type = sanitizeMapperType(entityName);
		String transactionId = getCurrentTransactionId();

		if (LOG.isTraceEnabled())
		{
			if (fetch == null)
			{
				LOG.trace("Retrieving Elasticsearch [{}] docs with ids [{}] ...", type, entityIds);
			}
			else
			{
				LOG.trace("Retrieving Elasticsearch [{}] docs with ids [{}] and fetch [{}] ...", type, entityIds,
						fetch);
			}
		}

		MultiGetRequestBuilder request = client.prepareMultiGet();
		entityIds.forEach(id -> {
			request.add(createMultiGetItem(indexName, type, id, fetch));
			if (transactionId != null)
			{
				request.add(createMultiGetItem(transactionId, type, id, fetch));
			}
		});

		MultiGetResponse response = request.get();

		if (LOG.isDebugEnabled())
		{
			if (fetch == null)
			{
				LOG.debug("Retrieved Elasticsearch [{}] docs with ids [{}]", type, entityIds);
			}
			else
			{
				LOG.debug("Retrieved Elasticsearch [{}] docs with ids [{}] and fetch [{}]", type, entityIds, fetch);
			}
		}

		// If the document was not found in the molgenis index or transaction index a response is included that
		// states that the item doesn't exist. Filter out these responses, since the document should be located
		// in either of the indexes.
		return stream(response.spliterator(), false).flatMap(itemResponse -> {
			if (itemResponse.isFailed())
			{
				throw new ElasticsearchException("Search failed. Returned headers:" + itemResponse.getFailure());
			}
			GetResponse getResponse = itemResponse.getResponse();
			if (getResponse.isExists())
			{
				Map<String, Object> source = getResponse.getSource();
				Entity entity = elasticsearchEntityFactory.create(entityMetaData, source, fetch);
				return Stream.of(entity);
			}
			else
			{
				return Stream.<Entity> empty();
			}
		});
	}

	private Item createMultiGetItem(String indexName, String type, Object id, Fetch fetch)
	{
		Item item = new Item(indexName, type, toElasticsearchId(id));
		if (fetch != null)
		{
			item.fetchSourceContext(new FetchSourceContext(toFetchFields(fetch)));
		}
		return item;
	}

	@Override
	public Iterable<Entity> search(Query q, final EntityMetaData entityMetaData)
	{
		return searchInternal(q, entityMetaData);
	}

	@Override
	public Stream<Entity> searchAsStream(Query q, EntityMetaData entityMetaData)
	{
		return searchInternal(q, entityMetaData).stream();
	}

	private ElasticsearchEntityIterable searchInternal(Query q, EntityMetaData entityMetaData)
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

		return new ElasticsearchEntityIterable(q, entityMetaData, client, elasticsearchEntityFactory, generator,
				indexNames);
	}

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

	@Override
	public void flush()
	{
		if (LOG.isTraceEnabled()) LOG.trace("Flushing Elasticsearch index [" + indexName + "] ...");
		client.admin().indices().prepareFlush(indexName).get();
		if (LOG.isDebugEnabled()) LOG.debug("Flushed Elasticsearch index [" + indexName + "]");
	}

	@Override
	public void rebuildIndex(Iterable<? extends Entity> entities, EntityMetaData entityMetaData)
	{
		if (storeSource(entityMetaData))
		{
			this.rebuildIndexElasticSearchEntity(entities, entityMetaData);
		}
		else
		{
			this.rebuildIndexGeneric(entities, entityMetaData);
		}
	}

	/**
	 * Rebuild Elasticsearch index when the source is living in Elasticearch itself. This operation requires a way to
	 * temporary save the data so we can drop and rebuild the index for this document.
	 * 
	 * @param entities
	 * @param entityMetaData
	 */
	void rebuildIndexElasticSearchEntity(Iterable<? extends Entity> entities, EntityMetaData entityMetaData)
	{
		if (dataService.getMeta().hasBackend(ElasticsearchRepositoryCollection.NAME))
		{
			UuidGenerator uuidg = new UuidGenerator();
			DefaultEntityMetaData tempEntityMetaData = new DefaultEntityMetaData(uuidg.generateId(), entityMetaData);
			tempEntityMetaData
					.setPackage(
							new PackageImpl("elasticsearch_temporary_entity",
									"This entity (Original: " + entityMetaData
											.getName()
									+ ") is temporary build to make rebuilding of Elasticsearch entities posible."));

			// Add temporary repository into Elasticsearch
			Repository tempRepository = dataService.getMeta().addEntityMeta(tempEntityMetaData);

			// Add temporary repository entities into Elasticsearch
			dataService.add(tempRepository.getName(), stream(entities.spliterator(), false));

			// Find the temporary saved entities
			Iterable<? extends Entity> tempEntities = new Iterable<Entity>()
			{
				@Override
				public Iterator<Entity> iterator()
				{
					return dataService.findAll(tempEntityMetaData.getName()).iterator();
				}
			};

			this.rebuildIndexGeneric(tempEntities, entityMetaData);

			// Remove temporary entity
			dataService.delete(tempEntityMetaData.getName(), tempEntities);

			// Remove temporary repository from Elasticsearch
			dataService.getMeta().deleteEntityMeta(tempEntityMetaData.getName());

			if (LOG.isInfoEnabled()) LOG.info("Finished rebuilding index of entity: [" + entityMetaData.getName()
					+ "] with backend ElasticSearch");
		}
		else
		{
			if (LOG.isDebugEnabled()) LOG.debug("Rebuild index of entity: [" + entityMetaData.getName()
					+ "] is skipped because the " + ElasticsearchRepositoryCollection.NAME + " backend is unknown");
		}
	}

	/**
	 * Rebuild Elasticsearch index when the source is living in another backend than the Elasticsearch itself.
	 * 
	 * @param entities
	 *            entities that will be reindexed.
	 * @param entityMetaData
	 *            meta data information about the entities that will be reindexed.
	 */
	private void rebuildIndexGeneric(Iterable<? extends Entity> entities, EntityMetaData entityMetaData)
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

	@Override
	public void optimizeIndex()
	{
		LOG.trace("Optimizing Elasticsearch index [{}] ...", indexName);
		// setMaxNumSegments(1) fully optimizes the index
		OptimizeResponse response = client.admin().indices().prepareOptimize(indexName).setMaxNumSegments(1).get();
		if (response.getFailedShards() > 0)
		{
			throw new ElasticsearchException("Optimize failed. Returned headers:" + response.getHeaders());
		}
		LOG.debug("Optimized Elasticsearch index [{}]", indexName);
	}

	private void updateReferences(Entity refEntity, EntityMetaData refEntityMetaData)
	{
		for (Pair<EntityMetaData, List<AttributeMetaData>> pair : EntityUtils
				.getReferencingEntityMetaData(refEntityMetaData, dataService))
		{
			EntityMetaData entityMetaData = pair.getA();

			QueryImpl q = null;
			for (AttributeMetaData attributeMetaData : pair.getB())
			{
				if (q == null) q = new QueryImpl();
				else q.or();
				q.eq(attributeMetaData.getName(), refEntity);
			}

			Iterable<Entity> entities = new ElasticsearchEntityIterable(q, entityMetaData, client,
					elasticsearchEntityFactory, generator, new String[]
			{ indexName });

			// TODO discuss whether this is still required
			// Don't use cached ref entities but make new ones
			entities = Iterables.transform(entities, new Function<Entity, Entity>()
			{
				@Override
				public Entity apply(Entity entity)
				{
					return new DefaultEntity(entityMetaData, dataService, entity);
				}
			});

			index(indexName, entities.iterator(), entityMetaData, CrudType.UPDATE, false);
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
			}).setConcurrentRequests(0).setBulkActions(50).build();
		}
	}

	public GetMappingsResponse getMappings()
	{
		return client.admin().indices().prepareGetMappings(indexName).get();
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
				try
				{
					searchResponse = client.prepareSearchScroll(searchResponse.getScrollId())
							.setScroll(TimeValue.timeValueMinutes(5)).get();

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
								if (LOG.isDebugEnabled())
								{
									LOG.debug("Adding [{}] with id [{}] to index [{}] ...", entityName, hit.id(),
											indexName);
								}
								bulkProcessor.add(new IndexRequest(indexName, entityName, hit.id()).source(values));

								if (crudType == CrudType.UPDATE)
								{
									updateReferences(elasticsearchEntityFactory.create(entityMeta, values, null),
											entityMeta);
								}
							}
							else if (crudType == CrudType.DELETE)
							{
								deleteById(indexName, hit.id(), entityMeta);
							}
						}

						searchResponse = client.prepareSearchScroll(searchResponse.getScrollId())
								.setScroll(TimeValue.timeValueMinutes(5)).get();
					}
				}
				finally
				{
					elasticsearchUtils.waitForCompletion(bulkProcessor);
				}
				refresh(indexName);
			}
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

		flush(); // persist changes on disk
	}

	/**
	 * Entities are stored (in addition to indexed) in Elasticsearch only if the entity backend is Elasticsearch
	 * 
	 * @param entityMeta
	 * @return whether or not this entity class is stored in Elasticsearch
	 */
	private boolean storeSource(EntityMetaData entityMeta)
	{
		return ElasticsearchRepositoryCollection.NAME.equals(entityMeta.getBackend());
	}
}
