package org.molgenis.data.elasticsearch;

import com.google.common.util.concurrent.AtomicLongMap;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.collect.FluentIterable;
import org.elasticsearch.common.collect.Iterators;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.molgenis.data.*;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.elasticsearch.index.ElasticsearchIndexCreator;
import org.molgenis.data.elasticsearch.index.MappingsBuilder;
import org.molgenis.data.elasticsearch.request.SearchRequestGenerator;
import org.molgenis.data.elasticsearch.response.ResponseParser;
import org.molgenis.data.elasticsearch.util.ElasticsearchUtils;
import org.molgenis.data.elasticsearch.util.SearchRequest;
import org.molgenis.data.elasticsearch.util.SearchResult;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.util.EntityUtils;
import org.molgenis.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Stream.concat;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.DataConverter.convert;
import static org.molgenis.data.elasticsearch.util.ElasticsearchEntityUtils.toElasticsearchId;
import static org.molgenis.data.elasticsearch.util.ElasticsearchEntityUtils.toElasticsearchIds;
import static org.molgenis.data.elasticsearch.util.MapperTypeSanitizer.sanitizeMapperType;
import static org.molgenis.data.support.EntityTypeUtils.createFetchForReindexing;

/**
 * ElasticSearch implementation of the SearchService interface.
 *
 * @author erwin
 */
public class ElasticsearchService implements SearchService
{
	private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchService.class);

	private static final int BATCH_SIZE = 1000;

	public enum IndexingMode
	{
		ADD, UPDATE
	}

	private final DataService dataService;
	private final ElasticsearchEntityFactory elasticsearchEntityFactory;
	private final String indexName;
	private final ResponseParser responseParser = new ResponseParser();
	private final ElasticsearchUtils elasticsearchFacade;
	private final SearchRequestGenerator searchRequestGenerator = new SearchRequestGenerator();

	public ElasticsearchService(Client client, String indexName, DataService dataService,
			ElasticsearchEntityFactory elasticsearchEntityFactory)
	{
		this.indexName = requireNonNull(indexName);
		this.dataService = requireNonNull(dataService);
		this.elasticsearchEntityFactory = requireNonNull(elasticsearchEntityFactory);
		this.elasticsearchFacade = new ElasticsearchUtils(client);
		new ElasticsearchIndexCreator(client).createIndexIfNotExists(indexName);
	}

	@Override
	public Iterable<String> getTypes()
	{
		return () -> elasticsearchFacade.getMappings(indexName).keysIt();
	}

	private SearchResult search(SearchRequest request)
	{
		// TODO : A quick fix now! Need to find a better way to get
		// EntityType in ElasticSearchService, because ElasticSearchService should not be
		// aware of DataService. E.g. Put EntityType in the SearchRequest object
		EntityType entityType = (request.getDocumentType() != null && dataService != null && dataService
				.hasRepository(request.getDocumentType())) ? dataService
				.getEntityType(request.getDocumentType()) : null;
		String documentType = request.getDocumentType() == null ? null : sanitizeMapperType(request.getDocumentType());
		SearchResponse response = elasticsearchFacade
				.search(SearchType.QUERY_AND_FETCH, request, entityType, documentType, indexName);
		return responseParser.parseSearchResponse(request, response, entityType, dataService);
	}

	@Override
	public boolean hasMapping(EntityType entityType)
	{
		return hasMapping(entityType.getName());
	}

	@Override
	public boolean hasMapping(String entityName)
	{
		return elasticsearchFacade.getMappings(indexName).containsKey(sanitizeMapperType(entityName));
	}

	@Override
	public void createMappings(EntityType entityType)
	{
		createMappings(entityType, true, true);
	}

	private void createMappings(String index, EntityType entityType, boolean enableNorms, boolean createAllIndex)
	{
		try (XContentBuilder jsonBuilder = XContentFactory.jsonBuilder())
		{
			MappingsBuilder.buildMapping(jsonBuilder, entityType, enableNorms, createAllIndex);
			elasticsearchFacade.putMapping(index, jsonBuilder, entityType.getName());
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public void createMappings(EntityType entityType, boolean enableNorms, boolean createAllIndex)
	{
		createMappings(indexName, entityType, enableNorms, createAllIndex);
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
	public long count(EntityType entityType)
	{
		return count(null, entityType);
	}

	@Override
	public long count(Query<Entity> q, EntityType entityType)
	{
		String entityName = entityType.getName();
		String type = sanitizeMapperType(entityName);
		return elasticsearchFacade.getCount(q, entityType, type, indexName);
	}

	@Override
	public void index(Entity entity, EntityType entityType, IndexingMode indexingMode)
	{
		LOG.debug("Indexing single {}.{} entity ...", entityType.getName(), entity.getIdValue());
		index(Stream.of(entity), entityType, indexingMode == IndexingMode.UPDATE);
	}

	@Override
	public long index(Iterable<? extends Entity> entities, EntityType entityType, IndexingMode indexingMode)
	{
		LOG.debug("Indexing multiple {} entities...", entityType.getName());
		return index(stream(entities.spliterator(), false), entityType, indexingMode == IndexingMode.UPDATE);
	}

	@Override
	public long index(Stream<? extends Entity> entities, EntityType entityType, IndexingMode indexingMode)
	{
		LOG.debug("Indexing multiple {} entities...", entityType.getName());
		return index(entities, entityType, indexingMode == IndexingMode.UPDATE);
	}

	private long index(Stream<? extends Entity> entityStream, EntityType entityType, boolean addReferences)
	{
		String entityName = entityType.getName();
		String type = sanitizeMapperType(entityName);

		Stream<IndexRequest> indexRequestStream = entityStream
				.flatMap(entity -> createIndexRequestStreamForEntity(entity, entityType, type, addReferences));

		AtomicLongMap<String> counts = elasticsearchFacade.index(indexRequestStream, true);
		return counts.get(type);
	}

	/**
	 * Creates an {@link IndexRequest} to reindex an entity. Optionally also creates {@link IndexRequest}s for referencing
	 * entities.
	 *
	 * @param entity                            the entity that should be indexed
	 * @param entityType                        the {@link EntityType} of the entity
	 * @param type                              the sanitized mapping type of the entity
	 * @param addRequestsForReferencingEntities boolean indicating if {@link IndexRequest}s should be added for all
	 *                                          referencing entities.
	 * @return Stream of {@link IndexRequest}s for the entity
	 */
	private Stream<IndexRequest> createIndexRequestStreamForEntity(Entity entity, EntityType entityType, String type,
			boolean addRequestsForReferencingEntities)
	{
		Stream<IndexRequest> result = Stream.of(createIndexRequestForEntity(entity, entityType, type));
		if (addRequestsForReferencingEntities)
		{
			result = concat(result, createIndexRequestsForReferencingEntities(entity, entityType));
		}
		return result;
	}

	/**
	 * Creates {@link IndexRequest}s for {@link Entity}s that have a reference to a particular entity instance
	 *
	 * @param entity     the entity that is referenced by the entities that need to be updated
	 * @param entityType {@link EntityType} of the referenced entity
	 * @return Stream of {@link IndexRequest}s for the entities that reference entity.
	 */
	private Stream<IndexRequest> createIndexRequestsForReferencingEntities(Entity entity, EntityType entityType)
	{
		Stream<IndexRequest> references = Stream.of();
		// Find entity metadata that is currently, in the database, referring to the entity we're reindexing
		for (Pair<EntityType, List<Attribute>> pair : EntityUtils.getReferencingEntityType(entityType, dataService))
		{
			EntityType refEntityType = pair.getA();

			// Search the index for referring documents of this type
			Stream<Entity> referringEntitiesStream = findReferringDocuments(entity, refEntityType, pair.getB());

			// Get actual entities from the dataservice, skipping the ones that no longer exist and
			// fetching all of their attributes in one go
			referringEntitiesStream = dataService
					.findAll(refEntityType.getName(), referringEntitiesStream.map(Entity::getIdValue),
							createFetchForReindexing(refEntityType));

			references = concat(references, referringEntitiesStream
					.map(referencingEntity -> createIndexRequestForEntity(referencingEntity, refEntityType,
							sanitizeMapperType(refEntityType.getName()))));
		}
		return references;
	}

	/**
	 * Searches the index for documents of a certain type that contain a reference to a specific entity.
	 * Uses {@link #searchInternalWithScanScroll(Query, EntityType)} to scroll through the existing referring
	 * entities in a context that remains valid even when the documents are getting updated.
	 *
	 * @param referredEntity      the entity that should be referred to in the documents
	 * @param referringEntityType {@link EntityType} of the referring documents
	 * @param referringAttributes {@link List} of {@link Attribute} of attributes that may reference the #referredEntity
	 * @return Stream of {@link Entity} references representing the documents.
	 */
	private Stream<Entity> findReferringDocuments(Entity referredEntity, EntityType referringEntityType,
			List<Attribute> referringAttributes)
	{
		// Find out which documents of this type currently, in ElasticSearch, contain a reference to
		// the entity we're reindexing
		QueryImpl<Entity> q = null;
		for (Attribute attribute : referringAttributes)
		{
			if (q == null)
			{
				q = new QueryImpl<>();
			}
			else
			{
				q.or();
			}
			q.eq(attribute.getName(), referredEntity);
		}
		LOG.debug("q: [{}], referringEntityType: [{}]", q.toString(), referringEntityType.getName());
		if (hasMapping(referringEntityType))
		{
			return searchInternalWithScanScroll(q, referringEntityType);
		}
		else
		{
			return Stream.empty();
		}
	}

	/**
	 * Creates an IndexRequest for an entity in index {@link #indexName}.
	 *
	 * @param entity     the entity that will be indexed
	 * @param entityType {@link EntityType} of the entity
	 * @param type       sanitized mapper type of the entity, so it need not be recomputed
	 */
	private IndexRequest createIndexRequestForEntity(Entity entity, EntityType entityType, String type)
	{
		String id = toElasticsearchId(entity, entityType);
		XContentBuilder xContentBuilder = elasticsearchEntityFactory.create(entity);
		LOG.trace("Indexing [{}] with id [{}] in index [{}]...", type, id, indexName);
		return new IndexRequest().index(indexName).type(type).id(id).source(xContentBuilder);
	}

	@Override
	public void delete(Entity entity, EntityType entityType)
	{
		String elasticsearchId = toElasticsearchId(entity, entityType);
		deleteById(elasticsearchId, entityType);
	}

	@Override
	public void deleteById(String id, EntityType entityType)
	{
		deleteById(indexName, id, entityType.getName());
	}

	private void deleteById(String index, String id, String entityFullName)
	{
		String type = sanitizeMapperType(entityFullName);
		elasticsearchFacade.deleteById(index, id, type);
	}

	@Override
	public void deleteById(Stream<String> ids, EntityType entityType)
	{
		ids.forEach(id -> deleteById(id, entityType));
	}

	@Override
	public void delete(Iterable<? extends Entity> entities, EntityType entityType)
	{
		delete(stream(entities.spliterator(), true), entityType);
	}

	@Override
	public void delete(Stream<? extends Entity> entities, EntityType entityType)
	{
		Stream<Object> entityIds = entities.map(Entity::getIdValue);
		Iterators.partition(entityIds.iterator(), BATCH_SIZE).forEachRemaining(
				batchEntityIds -> deleteById(toElasticsearchIds(batchEntityIds.stream()), entityType));
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

	@Override
	public Iterable<Entity> search(Query<Entity> q, final EntityType entityType)
	{
		return searchInternal(q, entityType);
	}

	@Override
	public Stream<Entity> searchAsStream(Query<Entity> q, EntityType entityType)
	{
		ElasticsearchEntityIterable searchInternal = searchInternal(q, entityType);
		return new EntityStream(searchInternal.stream(), true);
	}

	private ElasticsearchEntityIterable searchInternal(Query<Entity> q, EntityType entityType)
	{
		return new ElasticsearchEntityIterable(q, entityType, elasticsearchFacade, elasticsearchEntityFactory,
				searchRequestGenerator, indexName);
	}

	private Stream<Entity> searchInternalWithScanScroll(Query<Entity> query, EntityType entityType)
	{
		String type = sanitizeMapperType(entityType.getName());
		Consumer<SearchRequestBuilder> searchRequestBuilderConsumer = searchRequestBuilder -> searchRequestGenerator
				.buildSearchRequest(searchRequestBuilder, type, SearchType.QUERY_AND_FETCH, query, null, null, null,
						entityType);

		return elasticsearchFacade
				.searchForIdsWithScanScroll(searchRequestBuilderConsumer, query.toString(), type, indexName)
				.map(idString -> convert(idString, entityType.getIdAttribute()))
				.map(idObject -> elasticsearchEntityFactory.getReference(entityType, idObject));
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery, final EntityType entityType)
	{
		Query<Entity> q = aggregateQuery.getQuery();
		Attribute xAttr = aggregateQuery.getAttributeX();
		Attribute yAttr = aggregateQuery.getAttributeY();
		Attribute distinctAttr = aggregateQuery.getAttributeDistinct();
		SearchRequest searchRequest = new SearchRequest(entityType.getName(), q, xAttr, yAttr, distinctAttr);
		SearchResult searchResults = search(searchRequest);
		return searchResults.getAggregate();
	}

	@Override
	public void flush()
	{
		elasticsearchFacade.flushIndex(indexName);
	}

	@Override
	public void rebuildIndex(Repository<? extends Entity> repository)
	{
		EntityType entityType = repository.getEntityType();

		if (hasMapping(entityType))
		{
			LOG.debug("Delete index for repository {}...", repository.getName());
			delete(entityType.getName());
		}

		createMappings(entityType);
		LOG.trace("Indexing {} repository in batches of size {}...", repository.getName(), BATCH_SIZE);
		repository.forEachBatched(createFetchForReindexing(entityType),
				entities -> index(entities, entityType, IndexingMode.ADD), BATCH_SIZE);
		LOG.debug("Create index for repository {}...", repository.getName());
	}

	@Override
	public void optimizeIndex()
	{
		elasticsearchFacade.optimizeIndex(indexName);
	}

	@Override
	public Entity findOne(Query<Entity> q, EntityType entityType)
	{
		return FluentIterable.from(search(q, entityType)).first().orNull();
	}
}
