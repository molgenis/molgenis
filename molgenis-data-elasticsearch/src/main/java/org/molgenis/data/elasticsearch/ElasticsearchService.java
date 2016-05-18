package org.molgenis.data.elasticsearch;

import com.google.common.collect.Iterables;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.collect.Iterators;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.molgenis.data.*;
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
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.support.UuidGenerator;
import org.molgenis.util.DependencyResolver;
import org.molgenis.util.EntityUtils;
import org.molgenis.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.StreamSupport.stream;
import static org.elasticsearch.action.search.SearchType.QUERY_AND_FETCH;
import static org.molgenis.data.elasticsearch.request.SourceFilteringGenerator.toFetchFields;
import static org.molgenis.data.elasticsearch.util.ElasticsearchEntityUtils.toElasticsearchId;
import static org.molgenis.data.elasticsearch.util.ElasticsearchEntityUtils.toElasticsearchIds;
import static org.molgenis.data.elasticsearch.util.MapperTypeSanitizer.sanitizeMapperType;

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
	private static BulkProcessorFactory BULK_PROCESSOR_FACTORY = new BulkProcessorFactory();

	public enum IndexingMode
	{
		ADD, UPDATE
	}

	enum CrudType
	{
		ADD, UPDATE, DELETE
	}

	private final DataService dataService;
	private final ElasticsearchEntityFactory elasticsearchEntityFactory;
	private final String indexName;
	private final Client client;
	private final ResponseParser responseParser = new ResponseParser();
	private final ElasticsearchUtils elasticsearchUtils;
	private final SearchRequestGenerator generator = new SearchRequestGenerator();

	public ElasticsearchService(Client client, String indexName, DataService dataService,
			ElasticsearchEntityFactory elasticsearchEntityFactory)
	{
		this(client, indexName, dataService, elasticsearchEntityFactory, true);
	}

	/**
	 * Constructor for testability.
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
		return () -> elasticsearchUtils.getMappings(indexName).keysIt();
	}

	@Override
	@Deprecated
	public SearchResult search(SearchRequest request)
	{
		return search(QUERY_AND_FETCH, request);
	}

	private SearchResult search(SearchType searchType, SearchRequest request)
	{

		// TODO : A quick fix now! Need to find a better way to get
		// EntityMetaData in ElasticSearchService, because ElasticSearchService should not be
		// aware of DataService. E.g. Put EntityMetaData in the SearchRequest object
		EntityMetaData entityMetaData = (request.getDocumentType() != null && dataService != null && dataService
				.hasRepository(request.getDocumentType())) ? dataService
				.getEntityMetaData(request.getDocumentType()) : null;
		String documentType = request.getDocumentType() == null ? null : sanitizeMapperType(request.getDocumentType());
		SearchResponse response = elasticsearchUtils
				.search(searchType, request, entityMetaData, documentType, indexName);
		return responseParser.parseSearchResponse(request, response, entityMetaData, dataService);
	}

	@Override
	public boolean hasMapping(EntityMetaData entityMetaData)
	{
		return elasticsearchUtils.getMappings(indexName).containsKey(sanitizeMapperType(entityMetaData.getName()));
	}

	@Override
	public void createMappings(EntityMetaData entityMetaData)
	{
		createMappings(entityMetaData, storeSource(entityMetaData), true, true);
	}

	private void createMappings(String index, EntityMetaData entityMetaData, boolean storeSource, boolean enableNorms,
			boolean createAllIndex)
	{
		try
		{
			XContentBuilder jsonBuilder = MappingsBuilder
					.buildMapping(entityMetaData, storeSource, enableNorms, createAllIndex);
			elasticsearchUtils.putMapping(index, jsonBuilder, entityMetaData.getName());
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
		elasticsearchUtils.refresh(indexName);
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
		return elasticsearchUtils.getCount(q, entityMetaData, type, indexName);
	}

	@Override
	public void index(Entity entity, EntityMetaData entityMetaData, IndexingMode indexingMode)
	{
		index(entity, entityMetaData, indexingMode, true);
	}

	private void index(Entity entity, EntityMetaData entityMetaData, IndexingMode indexingMode, boolean updateIndex)
	{
		CrudType crudType = indexingMode == IndexingMode.ADD ? CrudType.ADD : CrudType.UPDATE;

		index(indexName, Collections.singleton(entity).iterator(), entityMetaData, crudType, updateIndex);
	}

	@Override
	public long index(Iterable<? extends Entity> entities, EntityMetaData entityMetaData, IndexingMode indexingMode)
	{
		CrudType crudType = indexingMode == IndexingMode.ADD ? CrudType.ADD : CrudType.UPDATE;
		return index(indexName, entities.iterator(), entityMetaData, crudType, true);
	}

	@Override
	public long index(Stream<? extends Entity> entities, EntityMetaData entityMetaData, IndexingMode indexingMode)
	{
		CrudType crudType = indexingMode == IndexingMode.ADD ? CrudType.ADD : CrudType.UPDATE;
		return index(indexName, entities.iterator(), entityMetaData, crudType, true);
	}

	long index(String index, Iterator<? extends Entity> it, EntityMetaData entityMetaData, CrudType crudType,
			boolean updateIndex)
	{
		String entityName = entityMetaData.getName();
		String type = sanitizeMapperType(entityName);

		long nrIndexedEntities = 0;
		BulkProcessor bulkProcessor = BULK_PROCESSOR_FACTORY.create(client);

		try
		{
			while (it.hasNext())
			{
				Entity entity = it.next();
				String id = toElasticsearchId(entity, entityMetaData);
				Map<String, Object> source = elasticsearchEntityFactory.create(entityMetaData, entity);

				LOG.debug("Indexing [{}] with id [{}] in index [{}] mode [{}] ...", type, id, index, crudType);

				bulkProcessor.add(new IndexRequest().index(index).type(type).id(id).source(source));
				++nrIndexedEntities;

				if (updateIndex && crudType == CrudType.UPDATE)
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
		if (!canBeDeleted(singletonList(id), entityMetaData))
		{
			throw new MolgenisDataException(
					"Cannot delete entity because there are other entities referencing it. Delete these first.");
		}

		deleteById(indexName, id, entityMetaData.getName());
	}

	private void deleteById(String index, String id, String entityFullName)
	{
		String type = sanitizeMapperType(entityFullName);
		elasticsearchUtils.deleteById(index, id, type);
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

		if (elasticsearchUtils.isTypeExists(type, indexName))
		{
			if (!elasticsearchUtils.deleteMapping(type, indexName))
			{
				throw new ElasticsearchException("Delete of mapping '" + entityName + "' failed.");
			}
		}

		if (!elasticsearchUtils.deleteAllDocumentsOfType(type, indexName))
		{
			throw new ElasticsearchException("Delete all entities of type '" + entityName + "' failed.");
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
			document = elasticsearchUtils.getDocument(type, id, indexName);
		}
		else
		{
			document = elasticsearchUtils.getDocument(type, id, toFetchFields(fetch), indexName);
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
			sourceStream = elasticsearchUtils.getDocuments(type, entityIds, indexName);
		}
		else
		{
			sourceStream = elasticsearchUtils.getDocuments(type, toFetchFields(fetch), entityIds, indexName);
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
		return new ElasticsearchEntityIterable(q, entityMetaData, client, elasticsearchEntityFactory, generator,
				new String[] { indexName });
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery, final EntityMetaData entityMetaData)
	{
		Query<Entity> q = aggregateQuery.getQuery();
		AttributeMetaData xAttr = aggregateQuery.getAttributeX();
		AttributeMetaData yAttr = aggregateQuery.getAttributeY();
		AttributeMetaData distinctAttr = aggregateQuery.getAttributeDistinct();
		SearchRequest searchRequest = new SearchRequest(entityMetaData.getName(), q, emptyList(),
				xAttr, yAttr, distinctAttr);
		SearchResult searchResults = search(searchRequest);
		return searchResults.getAggregate();
	}

	@Override
	public void flush()
	{
		elasticsearchUtils.flushIndex(indexName);
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
			tempEntityMetaData.setPackage(new PackageImpl("elasticsearch_temporary_entity",
					"This entity (Original: " + entityMetaData.getName()
							+ ") is temporary build to make rebuilding of Elasticsearch entities possible."));

			// Add temporary repository into Elasticsearch
			Repository<Entity> tempRepository = dataService.getMeta().addEntityMeta(tempEntityMetaData);

			// Add temporary repository entities into Elasticsearch
			dataService.add(tempRepository.getName(), stream(entities.spliterator(), false));

			// Find the temporary saved entities
			Iterable<? extends Entity> tempEntities = (Iterable<Entity>) () -> dataService
					.findAll(tempEntityMetaData.getName()).iterator();

			this.rebuildIndexGeneric(tempEntities, entityMetaData);

			// Remove temporary entity
			dataService.delete(tempEntityMetaData.getName(), stream(tempEntities.spliterator(), false));

			// Remove temporary repository from Elasticsearch
			dataService.getMeta().deleteEntityMeta(tempEntityMetaData.getName());

			LOG.info("Finished rebuilding index of entity: [{}] with backend ElasticSearch", entityMetaData.getName());
		}
		else
		{
			LOG.debug("Rebuild index of entity: [{}] is skipped because the {} backend is unknown",
					entityMetaData.getName(), ElasticsearchRepositoryCollection.NAME);
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
		if (DependencyResolver.hasSelfReferences(entityMetaData))
		{
			Iterable<Entity> iterable = Iterables.transform(entities, input -> input);
			entities = new DependencyResolver().resolveSelfReferences(iterable, entityMetaData);
		}
		if (hasMapping(entityMetaData))
		{
			delete(entityMetaData.getName());
		}
		createMappings(entityMetaData);
		index(entities, entityMetaData, IndexingMode.ADD);
	}

	@Override
	public void optimizeIndex()
	{
		elasticsearchUtils.optimizeIndex(indexName);
	}

	private void updateReferences(Entity refEntity, EntityMetaData refEntityMetaData)
	{
		for (Pair<EntityMetaData, List<AttributeMetaData>> pair : EntityUtils
				.getReferencingEntityMetaData(refEntityMetaData, dataService))
		{
			EntityMetaData entityMetaData = pair.getA();

			QueryImpl<Entity> q = null;
			for (AttributeMetaData attributeMetaData : pair.getB())
			{
				if (q == null) q = new QueryImpl<>();
				else q.or();
				q.eq(attributeMetaData.getName(), refEntity);
			}

			Iterable<Entity> entities = new ElasticsearchEntityIterable(q, entityMetaData, client,
					elasticsearchEntityFactory, generator, new String[] { indexName });

			// TODO discuss whether this is still required
			// Don't use cached ref entities but make new ones
			entities = Iterables.transform(entities, entity -> new DefaultEntity(entityMetaData, dataService, entity));

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

	// Checks if entities can be deleted, have no ref entities pointing to it
	private boolean canBeDeleted(Iterable<?> ids, EntityMetaData meta)
	{
		List<Pair<EntityMetaData, List<AttributeMetaData>>> referencingMetas = EntityUtils
				.getReferencingEntityMetaData(meta, dataService);
		if (referencingMetas.isEmpty()) return true;

		for (Pair<EntityMetaData, List<AttributeMetaData>> pair : referencingMetas)
		{
			EntityMetaData refEntityMetaData = pair.getA();

			if (!refEntityMetaData.getName().equals(EntityMetaDataMetaData.ENTITY_NAME) && !refEntityMetaData.getName()
					.equals(AttributeMetaDataMetaData.ENTITY_NAME))
			{
				QueryImpl<Entity> q = null;
				for (AttributeMetaData attributeMetaData : pair.getB())
				{
					if (q == null) q = new QueryImpl<>();
					else q.or();
					q.in(attributeMetaData.getName(), ids);
				}

				if (dataService.count(refEntityMetaData.getName(), q) > 0) return false;
			}
		}

		return true;
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
