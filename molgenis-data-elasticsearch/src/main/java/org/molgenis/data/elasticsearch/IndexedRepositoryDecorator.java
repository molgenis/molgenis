package org.molgenis.data.elasticsearch;

import com.google.common.collect.Sets;
import org.elasticsearch.common.collect.Iterators;
import org.molgenis.data.*;
import org.molgenis.data.QueryRule.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.QueryUtils.containsAnyOperator;
import static org.molgenis.data.QueryUtils.containsComputedAttribute;
import static org.molgenis.data.RepositoryCapability.*;

/**
 * Decorator for indexed repositories. Sends all queries with operators that are not supported by the decorated
 * repository to the index.
 */
public class IndexedRepositoryDecorator implements Repository<Entity>
{
	private static final Logger LOG = LoggerFactory.getLogger(IndexedRepositoryDecorator.class);
	private static final String INDEX_REPOSITORY = "Index Repository";
	private static final String DECORATED_REPOSITORY = "Decorated Repository";

	private static final int BATCH_SIZE = 1000;

	private final Repository<Entity> decoratedRepository;
	private final Repository<Entity> indexRepository;
	private SearchService elasticSearchService;

	private Set<Operator> unsupportedOperators;

	public IndexedRepositoryDecorator(Repository<Entity> decoratedRepo, SearchService elasticSearchService)
	{
		this.elasticSearchService = requireNonNull(elasticSearchService);
		this.decoratedRepository = requireNonNull(decoratedRepo);
		this.indexRepository = this.getReadOnlyElasticsearchRepository();
		Set<Operator> operators = indexRepository.getQueryOperators();
		operators.removeAll(decoratedRepository.getQueryOperators());
		unsupportedOperators = Collections.unmodifiableSet(operators);
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return decoratedRepository.getEntityMetaData();
	}

	@Override
	@Transactional
	public void add(Entity entity)
	{
		decoratedRepository.add(entity);
	}

	@Override
	@Transactional
	public Integer add(Stream<Entity> entities)
	{
		// TODO look into performance improvements
		AtomicInteger count = new AtomicInteger();
		Iterators.partition(entities.iterator(), BATCH_SIZE).forEachRemaining(batch -> {
			Integer batchCount = decoratedRepository.add(batch.stream());
			count.addAndGet(batchCount);
		});
		return count.get();
	}

	@Override
	public void flush()
	{
		decoratedRepository.flush();
	}

	@Override
	public void clearCache()
	{
		decoratedRepository.clearCache();
	}

	@Override
	@Transactional
	public void update(Entity entity)
	{
		decoratedRepository.update(entity);
	}

	@Override
	@Transactional
	public void update(Stream<Entity> entities)
	{
		// TODO look into performance improvements
		Iterators.partition(entities.iterator(), BATCH_SIZE)
				.forEachRemaining(batch -> decoratedRepository.update(batch.stream()));
	}

	@Override
	@Transactional
	public void delete(Entity entity)
	{
		decoratedRepository.delete(entity);
	}

	@Override
	@Transactional
	public void delete(Stream<Entity> entities)
	{
		// TODO look into performance improvements
		Iterators.partition(entities.iterator(), BATCH_SIZE)
				.forEachRemaining(batch -> decoratedRepository.delete(batch.stream()));
	}

	@Override
	@Transactional
	public void deleteById(Object id)
	{
		decoratedRepository.deleteById(id);
	}

	@Override
	@Transactional
	public void deleteAll(Stream<Object> ids)
	{
		// TODO look into performance improvements
		Iterators.partition(ids.iterator(), BATCH_SIZE).forEachRemaining(decoratedRepository::deleteById);
	}

	@Override
	@Transactional
	public void deleteAll()
	{
		decoratedRepository.deleteAll();
	}

	@Override
	public Entity findOneById(Object id)
	{
		return decoratedRepository.findOneById(id);
	}

	@Override
	public Entity findOneById(Object id, Fetch fetch)
	{
		return decoratedRepository.findOneById(id, fetch);
	}

	@Override
	public Entity findOne(Query<Entity> q)
	{
		if (querySupported(q))
		{
			LOG.debug("public Entity findOne({}) entityName: [{}] repository: [{}]", q, getEntityMetaData().getName(),
					DECORATED_REPOSITORY);
			return decoratedRepository.findOne(q);
		}
		else
		{
			LOG.debug("public Entity findOne({}) entityName: [{}] repository: [{}]", q, getEntityMetaData().getName(),
					INDEX_REPOSITORY);
			return indexRepository.findOne(q);
		}

	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids)
	{
		return decoratedRepository.findAll(ids);
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids, Fetch fetch)
	{
		return decoratedRepository.findAll(ids, fetch);
	}

	@Override
	public Stream<Entity> findAll(Query<Entity> q)
	{
		if (querySupported(q))
		{
			LOG.debug("public Entity findAll({}) entityName: [{}] repository: [{}]", q, getEntityMetaData().getName(),
					DECORATED_REPOSITORY);
			return decoratedRepository.findAll(q);
		}
		else
		{
			LOG.debug("public Entity findAll({}) entityName: [{}] repository: [{}]", q, getEntityMetaData().getName(),
					INDEX_REPOSITORY);
			return indexRepository.findAll(q);
		}
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return decoratedRepository.iterator();
	}

	@Override
	public void forEachBatched(Fetch fetch, Consumer<List<Entity>> consumer, int batchSize)
	{
		decoratedRepository.forEachBatched(fetch, consumer, 1000);
	}

	@Override
	public void rebuildIndex()
	{
		elasticSearchService.rebuildIndex(decoratedRepository);
	}

	@Override
	public void create()
	{
		if (!decoratedRepository.getCapabilities().contains(MANAGABLE))
		{
			throw new MolgenisDataAccessException(
					"Repository '" + decoratedRepository.getName() + "' is not Manageable");
		}
		decoratedRepository.create();
	}

	@Override
	public void drop()
	{
		if (!decoratedRepository.getCapabilities().contains(MANAGABLE))
		{
			throw new MolgenisDataAccessException(
					"Repository '" + decoratedRepository.getName() + "' is not Manageable");
		}
		decoratedRepository.drop();
	}

	/**
	 * Gets the capabilities of the underlying repository and adds three read capabilities provided by the index:
	 * {@link RepositoryCapability#INDEXABLE}, {@link RepositoryCapability#QUERYABLE} and {@link RepositoryCapability#AGGREGATEABLE}.
	 * Does not add other index capabilities like{@link RepositoryCapability#WRITABLE} because those might conflict with the underlying repository.
	 */
	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		Set<RepositoryCapability> capabilities = new HashSet<>();
		capabilities.addAll(decoratedRepository.getCapabilities());
		capabilities.addAll(EnumSet.of(QUERYABLE, AGGREGATEABLE));
		return unmodifiableSet(capabilities);
	}

	@Override
	public Set<Operator> getQueryOperators()
	{
		return indexRepository.getQueryOperators();
	}

	@Override
	public void close() throws IOException
	{
		decoratedRepository.close();
	}

	@Override
	public String getName()
	{
		return decoratedRepository.getName();
	}

	@Override
	public long count()
	{
		return decoratedRepository.count();
	}

	@Override
	public Query<Entity> query()
	{
		return decoratedRepository.query();
	}

	@Override
	public long count(Query<Entity> q)
	{
		// TODO check if the index is stable. If index is stable you can better check index for count results
		if (querySupported(q))
		{
			LOG.debug("public long count({}) entityName: [{}] repository: [{}]", q, getEntityMetaData().getName(),
					DECORATED_REPOSITORY);
			return decoratedRepository.count(q);
		}
		else
		{
			LOG.debug("public long count({}) entityName: [{}] repository: [{}]", q, getEntityMetaData().getName(),
					INDEX_REPOSITORY);
			return indexRepository.count(q);
		}
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		return indexRepository.aggregate(aggregateQuery);
	}

	@Override
	public void addEntityListener(EntityListener entityListener)
	{
		decoratedRepository.addEntityListener(entityListener);
	}

	@Override
	public void removeEntityListener(EntityListener entityListener)
	{
		decoratedRepository.removeEntityListener(entityListener);
	}

	/**
	 * Checks if the underlying repository can handle this query. Queries with unsupported operators or queries that use
	 * attributes with computed values are delegated to the index.
	 */
	private boolean querySupported(Query<Entity> q)
	{
		return !containsAnyOperator(q, unsupportedOperators) && !containsComputedAttribute(q.getRules(),
				getEntityMetaData());

	}

	private ElasticsearchRepository getReadOnlyElasticsearchRepository()
	{
		return new IndexRepository(getEntityMetaData(), elasticSearchService);
	}

	private static class IndexRepository extends ElasticsearchRepository
	{

		private IndexRepository(EntityMetaData entityMetaData, SearchService searchService)
		{
			super(entityMetaData, searchService);
		}

		@Override
		public Set<RepositoryCapability> getCapabilities()
		{
			return Sets.newHashSet(AGGREGATEABLE, QUERYABLE);
		}

		@Override
		public void close() throws IOException
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void add(Entity entity)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Integer add(Stream<Entity> entities)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void flush()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void clearCache()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void update(Entity entity)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void update(Stream<Entity> entities)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void delete(Entity entity)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void delete(Stream<Entity> entities)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void deleteById(Object id)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void deleteAll(Stream<Object> ids)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void deleteAll()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void create()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void drop()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void addEntityListener(EntityListener entityListener)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void removeEntityListener(EntityListener entityListener)
		{
			throw new UnsupportedOperationException();
		}
	}
}
