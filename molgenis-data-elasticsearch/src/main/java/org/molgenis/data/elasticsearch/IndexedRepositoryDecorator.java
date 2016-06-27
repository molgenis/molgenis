package org.molgenis.data.elasticsearch;

import org.elasticsearch.common.collect.Iterators;
import org.molgenis.data.*;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.meta.model.EntityMetaData;
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
import static org.molgenis.data.RepositoryCapability.AGGREGATEABLE;
import static org.molgenis.data.RepositoryCapability.QUERYABLE;

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
	private SearchService elasticSearchService;

	/**
	 * Operators NOT supported by the decorated repository.
	 */
	private Set<Operator> unsupportedOperators;

	public IndexedRepositoryDecorator(Repository<Entity> decoratedRepo, SearchService elasticSearchService)
	{
		this.elasticSearchService = requireNonNull(elasticSearchService);
		this.decoratedRepository = requireNonNull(decoratedRepo);
		Set<Operator> operators = getQueryOperators();
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
			return elasticSearchService.findOne(q, getEntityMetaData());
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
			return elasticSearchService.searchAsStream(q, getEntityMetaData());
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
		decoratedRepository.forEachBatched(fetch, consumer, batchSize);
	}

	@Override
	public void rebuildIndex()
	{
		elasticSearchService.rebuildIndex(decoratedRepository);
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
		return EnumSet.allOf(Operator.class);
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
			return elasticSearchService.count(q, getEntityMetaData());
		}
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		return elasticSearchService.aggregate(aggregateQuery, getEntityMetaData());
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
}
