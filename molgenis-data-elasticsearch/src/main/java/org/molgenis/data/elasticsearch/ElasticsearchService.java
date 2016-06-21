package org.molgenis.data.elasticsearch;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Stream.concat;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.elasticsearch.request.SourceFilteringGenerator.toFetchFields;
import static org.molgenis.data.elasticsearch.util.ElasticsearchEntityUtils.toElasticsearchId;
import static org.molgenis.data.elasticsearch.util.ElasticsearchEntityUtils.toElasticsearchIds;
import static org.molgenis.data.elasticsearch.util.MapperTypeSanitizer.sanitizeMapperType;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.collect.Iterators;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityStream;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.elasticsearch.index.ElasticsearchIndexCreator;
import org.molgenis.data.elasticsearch.index.MappingsBuilder;
import org.molgenis.data.elasticsearch.request.SearchRequestGenerator;
import org.molgenis.data.elasticsearch.response.ResponseParser;
import org.molgenis.data.elasticsearch.util.ElasticsearchUtils;
import org.molgenis.data.elasticsearch.util.SearchRequest;
import org.molgenis.data.elasticsearch.util.SearchResult;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.util.DependencyResolver;
import org.molgenis.util.EntityUtils;
import org.molgenis.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.AtomicLongMap;

/**
 * ElasticSearch implementation of the SearchService interface.
 * <p>
 * TODO use scroll-scan where possible:
 * http://www.elasticsearch.org/guide/en/elasticsearch /reference/current/search-request-scroll.html#scroll-scans
 *
 * @author erwin
 */
public class ElasticsearchService implements SearchService
{
	private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchService.class);

	private static final int BATCH_SIZE = 1000;

	public static final String CRUD_TYPE_FIELD_NAME = "MolgenisCrudType";

	public enum IndexingMode
	{
		ADD, UPDATE
	}

	private final DataService dataService;
	private final ElasticsearchEntityFactory elasticsearchEntityFactory;
	private final String indexName;
	private final ResponseParser responseParser = new ResponseParser();
	private final ElasticsearchUtils elasticsearchFacade;
	private final SearchRequestGenerator generator = new SearchRequestGenerator();

	public ElasticsearchService(Client client, String indexName, DataService dataService,
			ElasticsearchEntityFactory elasticsearchEntityFactory)
	{
		this(new ElasticsearchUtils(client), indexName, dataService, elasticsearchEntityFactory);
		new ElasticsearchIndexCreator(client).createIndexIfNotExists(indexName);
	}

	/**
	 * Constructor for testability.
	 */
	ElasticsearchService(ElasticsearchUtils elasticSearchFacade, String indexName, DataService dataService,
			ElasticsearchEntityFactory elasticsearchEntityFactory)
	{
		this.indexName = requireNonNull(indexName);
		this.dataService = requireNonNull(dataService);
		this.elasticsearchEntityFactory = requireNonNull(elasticsearchEntityFactory);
		this.elasticsearchFacade = elasticSearchFacade;
	}

	@Override
	public Iterable<String> getTypes()
	{
		return () -> elasticsearchFacade.getMappings(indexName).keysIt();
	}

	/**
	 * @deprecated see search(Query, EntityMetaData) or aggregate(AggregateQuery, EntityMetaData)
	 */
	@Override
	@Deprecated
	public SearchResult search(SearchRequest request)
	{
		// TODO : A quick fix now! Need to find a better way to get
		// EntityMetaData in ElasticSearchService, because ElasticSearchService should not be
		// aware of DataService. E.g. Put EntityMetaData in the SearchRequest object
		EntityMetaData entityMetaData = (request.getDocumentType() != null && dataService != null && dataService
				.hasRepository(request.getDocumentType())) ? dataService
				.getEntityMetaData(request.getDocumentType()) : null;
		String documentType = request.getDocumentType() == null ? null : sanitizeMapperType(request.getDocumentType());
		SearchResponse response = elasticsearchFacade
				.search(SearchType.QUERY_AND_FETCH, request, entityMetaData, documentType, indexName);
		return responseParser.parseSearchResponse(request, response, entityMetaData, dataService);
	}

	@Override
	public boolean hasMapping(EntityMetaData entityMetaData)
	{
		return elasticsearchFacade.getMappings(indexName).containsKey(sanitizeMapperType(entityMetaData.getName()));
	}

	@Override
	public void createMappings(EntityMetaData entityMetaData)
	{
		createMappings(entityMetaData, storeSource(entityMetaData), true, true);
	}

	private void createMappings(String index, EntityMetaData entityMetaData, boolean storeSource, boolean enableNorms,
			boolean createAllIndex)
	{
		try (XContentBuilder jsonBuilder = XContentFactory.jsonBuilder())
		{
			MappingsBuilder.buildMapping(jsonBuilder, entityMetaData, storeSource, enableNorms, createAllIndex);
			elasticsearchFacade.putMapping(index, jsonBuilder, entityMetaData.getName());
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
	public void refresh()
	{
		refreshIndex();
	}

	@Override
	public void refreshIndex()
	{
		elasticsearchFacade.refresh(indexName);
	}

	@Override
	public long count(EntityMetaData entityMetaData)
	{
		return count(null, entityMetaData);
	}

	@Override
	public long count(Query<Entity> q, EntityMetaData entityMetaData)
	{
		String entityName = entityMetaData.getName();
		String type = sanitizeMapperType(entityName);
		return elasticsearchFacade.getCount(q, entityMetaData, type, indexName);
	}

	@Override
	public void index(Entity entity, EntityMetaData entityMetaData, IndexingMode indexingMode)
	{
		index(Stream.of(entity), entityMetaData, indexingMode == IndexingMode.UPDATE);
	}

	@Override
	public long index(Iterable<? extends Entity> entities, EntityMetaData entityMetaData, IndexingMode indexingMode)
	{
		return index(stream(entities.spliterator(), false), entityMetaData, indexingMode == IndexingMode.UPDATE);
	}

	@Override
	public long index(Stream<? extends Entity> entities, EntityMetaData entityMetaData, IndexingMode indexingMode)
	{
		return index(entities, entityMetaData, indexingMode == IndexingMode.UPDATE);
	}

	private long index(Stream<? extends Entity> entityStream, EntityMetaData entityMetaData, boolean addReferences)
	{
		String entityName = entityMetaData.getName();
		String type = sanitizeMapperType(entityName);

		Stream<IndexRequest> indexRequestStream = entityStream
				.flatMap(entity -> createIndexRequestStreamForEntity(entity, entityMetaData, type, addReferences));

		AtomicLongMap<String> counts = elasticsearchFacade.index(indexRequestStream, true);
		return counts.get(type);
	}

	/**
	 * Creates an {@link IndexRequest} to reindex an entity. Optionally also creates {@link IndexRequest}s for referencing
	 * entities.
	 *
	 * @param entity                            the entity that should be indexed
	 * @param entityMetaData                    the {@link EntityMetaData} of the entity
	 * @param type                              the sanitized mapping type of the entity
	 * @param addRequestsForReferencingEntities boolean indicating if {@link IndexRequest}s should be added for all
	 *                                          referencing entities.
	 * @return Stream of {@link IndexRequest}s for the entity
	 */
	private Stream<IndexRequest> createIndexRequestStreamForEntity(Entity entity, EntityMetaData entityMetaData,
			String type, boolean addRequestsForReferencingEntities)
	{
		Stream<IndexRequest> result = Stream.of(createIndexRequestForEntity(entity, entityMetaData, type));
		if (addRequestsForReferencingEntities)
		{
			result = concat(result, createIndexRequestsForReferencingEntities(entity, entityMetaData));
		}
		return result;
	}

	/**
	 * Creates {@link IndexRequest}s for {@link Entity}s that have a reference to a particular entity instance
	 *
	 * @param entity         the entity that is referenced by the entities that need to be updated
	 * @param entityMetaData {@link EntityMetaData} of the referenced entity
	 * @return Stream of {@link IndexRequest}s for the entities that reference entity.
	 */
	private Stream<IndexRequest> createIndexRequestsForReferencingEntities(Entity entity, EntityMetaData entityMetaData)
	{
		Stream<IndexRequest> references = Stream.of();
		// Find entity metadata that is currently, in the database, referring to the entity we're reindexing
		for (Pair<EntityMetaData, List<AttributeMetaData>> pair : EntityUtils
				.getReferencingEntityMetaData(entityMetaData, dataService))
		{
			EntityMetaData refEntityMetaData = pair.getA();
			String refEntityType = sanitizeMapperType(refEntityMetaData.getName());

			// Search the index for referring documents of this type
			Stream<Entity> referringEntitiesStream = findReferringDocuments(entity, refEntityMetaData, pair.getB());

			// Get actual entities from the dataservice, skipping the ones that no longer exist and
			// fetching all of their attributes in one go
			referringEntitiesStream = dataService
					.findAll(refEntityMetaData.getName(), referringEntitiesStream.map(Entity::getIdValue),
							createFetchForReindexing(refEntityMetaData));

			references = concat(references, referringEntitiesStream
					.map(referencingEntity -> createIndexRequestForEntity(referencingEntity, refEntityMetaData,
							refEntityType)));
		}
		return references;
	}

	private Fetch createFetchForReindexing(EntityMetaData refEntityMetaData)
	{
		Fetch fetch = new Fetch();
		for (AttributeMetaData attr : refEntityMetaData.getAtomicAttributes())
		{
			if (attr.getRefEntity() != null)
			{
				Fetch attributeFetch = new Fetch();
				for (AttributeMetaData refAttr : attr.getRefEntity().getAtomicAttributes())
				{
					attributeFetch.field(refAttr.getName());
				}
				fetch.field(attr.getName(), attributeFetch);
			}
			else
			{
				fetch.field(attr.getName());
			}

		}
		return fetch;
	}

	/**
	 * Searches the index for documents of a certain type that contain a reference to a specific entity.
	 * Uses searchInternal to create a batched stream.
	 *
	 * @param referredEntity          the entity that should be referred to in the documents
	 * @param referringEntityMetaData {@link EntityMetaData} of the referring documents
	 * @param referringAttributes     {@link List} of {@link AttributeMetaData} of attributes that may reference the #referredEntity
	 * @return Stream of {@link Entity} references representing the documents.
	 */
	private Stream<Entity> findReferringDocuments(Entity referredEntity, EntityMetaData referringEntityMetaData,
			List<AttributeMetaData> referringAttributes)
	{
		// Find out which documents of this type currently, in ElasticSearch, contain a reference to
		// the entity we're reindexing
		QueryImpl<Entity> q = null;
		for (AttributeMetaData attributeMetaData : referringAttributes)
		{
			if (q == null)
			{
				q = new QueryImpl<>();
			}
			else
			{
				q.or();
			}
			q.eq(attributeMetaData.getName(), referredEntity);
		}
		return searchInternal(q, referringEntityMetaData).stream();
	}

	/**
	 * Creates an IndexRequest for an entity in index {@link #indexName}.
	 *
	 * @param entity         the entity that will be indexed
	 * @param entityMetaData {@link EntityMetaData} of the entity
	 * @param type           sanitized mapper type of the entity, so it need not be recomputed
	 */
	private IndexRequest createIndexRequestForEntity(Entity entity, EntityMetaData entityMetaData, String type)
	{
		String id = toElasticsearchId(entity, entityMetaData);
		Map<String, Object> source = elasticsearchEntityFactory.create(entityMetaData, entity);
		LOG.debug("Indexing [{}] with id [{}] in index [{}]...", type, id, indexName);
		return new IndexRequest().index(indexName).type(type).id(id).source(source);
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
		deleteById(indexName, id, entityMetaData.getName());
	}

	private void deleteById(String index, String id, String entityFullName)
	{
		String type = sanitizeMapperType(entityFullName);
		elasticsearchFacade.deleteById(index, id, type);
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
		Stream<Object> entityIds = entities.map(Entity::getIdValue);
		Iterators.partition(entityIds.iterator(), BATCH_SIZE).forEachRemaining(
				batchEntityIds -> deleteById(toElasticsearchIds(batchEntityIds.stream()), entityMetaData));
	}

	@Override
	public void delete(String entityName)
	{
		String type = sanitizeMapperType(entityName);

		if (elasticsearchFacade.isTypeExists(type, indexName) && !elasticsearchFacade.deleteMapping(type, indexName))
		{
			throw new ElasticsearchException("Delete of mapping for type '" + type + "' failed.");
		}

		if (!elasticsearchFacade.deleteAllDocumentsOfType(type, indexName))
		{
			throw new ElasticsearchException("Deleting all documents of type '" + type + "' failed.");
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

		Optional<Map<String, Object>> document;
		if (fetch == null)
		{
			document = elasticsearchFacade.getDocument(type, id, indexName);
		}
		else
		{
			document = elasticsearchFacade.getDocument(type, id, toFetchFields(fetch), indexName);
		}
		return document.map(s -> elasticsearchEntityFactory.create(entityMetaData, s, fetch)).orElse(null);
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
		return () -> {
			Stream<Object> stream = stream(entityIds.spliterator(), false);
			return get(stream, entityMetaData, fetch).iterator();
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
		Stream<Map<String, Object>> sourceStream;
		if (fetch == null)
		{
			sourceStream = elasticsearchFacade.getDocuments(type, entityIds, indexName);
		}
		else
		{
			sourceStream = elasticsearchFacade.getDocuments(type, toFetchFields(fetch), entityIds, indexName);
		}
		return sourceStream.map(source -> elasticsearchEntityFactory.create(entityMetaData, source, fetch));
	}

	@Override
	public Iterable<Entity> search(Query<Entity> q, final EntityMetaData entityMetaData)
	{
		return searchInternal(q, entityMetaData);
	}

	@Override
	public Stream<Entity> searchAsStream(Query<Entity> q, EntityMetaData entityMetaData)
	{
		ElasticsearchEntityIterable searchInternal = searchInternal(q, entityMetaData);
		return new EntityStream(searchInternal.stream(), true);
	}

	private ElasticsearchEntityIterable searchInternal(Query<Entity> q, EntityMetaData entityMetaData)
	{
		return new ElasticsearchEntityIterable(q, entityMetaData, elasticsearchFacade, elasticsearchEntityFactory,
				generator, indexName);
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery, final EntityMetaData entityMetaData)
	{
		Query<Entity> q = aggregateQuery.getQuery();
		AttributeMetaData xAttr = aggregateQuery.getAttributeX();
		AttributeMetaData yAttr = aggregateQuery.getAttributeY();
		AttributeMetaData distinctAttr = aggregateQuery.getAttributeDistinct();
		SearchRequest searchRequest = new SearchRequest(entityMetaData.getName(), q, xAttr, yAttr,
				distinctAttr);
		SearchResult searchResults = search(searchRequest);
		return searchResults.getAggregate();
	}

	@Override
	public void flush()
	{
		elasticsearchFacade.flushIndex(indexName);
	}

	@Override
	public void rebuildIndex(Iterable<? extends Entity> entities, EntityMetaData entityMetaData)
	{
		if (storeSource(entityMetaData))
		{
			throw new UnsupportedOperationException("Elasticsearch does not store data"); // FIXME
		}
		else
		{
			this.rebuildIndexGeneric(entities, entityMetaData);
		}
	}

	/**
	 * Rebuild Elasticsearch index when the source is living in another backend than the Elasticsearch itself.
	 *
	 * @param entities       entities that will be reindexed.
	 * @param entityMetaData meta data information about the entities that will be reindexed.
	 */
	private void rebuildIndexGeneric(Iterable<? extends Entity> entities, EntityMetaData entityMetaData)
	{
		Iterable<? extends Entity> entitiesToIndex = entities;
		if (DependencyResolver.hasSelfReferences(entityMetaData))
		{
			Iterable<Entity> iterable = Iterables.transform(entities, input -> input);
			entitiesToIndex = new DependencyResolver().resolveSelfReferences(iterable, entityMetaData);
		}
		if (hasMapping(entityMetaData))
		{
			delete(entityMetaData.getName());
		}
		createMappings(entityMetaData);
		index(entitiesToIndex, entityMetaData, IndexingMode.ADD);
	}

	@Override
	public void optimizeIndex()
	{
		elasticsearchFacade.optimizeIndex(indexName);
	}

	/**
	 * Entities are stored (in addition to indexed) in Elasticsearch only if the entity backend is Elasticsearch
	 *
	 * @param entityMeta {@link EntityMetaData} to check
	 * @return whether or not this entity class is stored in Elasticsearch
	 */
	private boolean storeSource(EntityMetaData entityMeta)
	{
		return ElasticsearchRepositoryCollection.NAME.equals(entityMeta.getBackend());
	}
}
