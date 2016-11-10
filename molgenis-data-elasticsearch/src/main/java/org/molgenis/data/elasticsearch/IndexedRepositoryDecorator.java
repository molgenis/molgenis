package org.molgenis.data.elasticsearch;

import org.molgenis.data.*;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.meta.model.EntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
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

	private final Repository<Entity> decoratedRepo;
	private SearchService searchService;

	/**
	 * Operators NOT supported by the decorated repository.
	 */
	private Set<Operator> unsupportedOperators;

	public IndexedRepositoryDecorator(Repository<Entity> decoratedRepo, SearchService searchService)
	{
		this.searchService = requireNonNull(searchService);
		this.decoratedRepo = requireNonNull(decoratedRepo);
		Set<Operator> operators = getQueryOperators();
		operators.removeAll(this.decoratedRepo.getQueryOperators());
		unsupportedOperators = Collections.unmodifiableSet(operators);
	}

	public EntityType getEntityType()
	{
		return decoratedRepo.getEntityType();
	}

	@Override
	public void add(Entity entity)
	{
		decoratedRepo.add(entity);
	}

	@Override
	public Integer add(Stream<Entity> entities)
	{
		return decoratedRepo.add(entities);
	}

	@Override
	public void update(Entity entity)
	{
		decoratedRepo.update(entity);
	}

	@Override
	public void update(Stream<Entity> entities)
	{
		decoratedRepo.update(entities);
	}

	@Override
	public void delete(Entity entity)
	{
		decoratedRepo.delete(entity);
	}

	@Override
	public void delete(Stream<Entity> entities)
	{
		decoratedRepo.delete(entities);
	}

	@Override
	public void deleteById(Object id)
	{
		decoratedRepo.deleteById(id);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		decoratedRepo.deleteAll(ids);
	}

	@Override
	public void deleteAll()
	{
		decoratedRepo.deleteAll();
	}

	@Override
	public Entity findOneById(Object id)
	{
		return decoratedRepo.findOneById(id);
	}

	@Override
	public Entity findOneById(Object id, Fetch fetch)
	{
		return decoratedRepo.findOneById(id, fetch);
	}

	@Override
	public Entity findOne(Query<Entity> q)
	{
		if (querySupported(q))
		{
			LOG.debug("public Entity findOne({}) entityName: [{}] repository: [{}]", q, getEntityType().getName(),
					DECORATED_REPOSITORY);
			return decoratedRepo.findOne(q);
		}
		else
		{
			LOG.debug("public Entity findOne({}) entityName: [{}] repository: [{}]", q, getEntityType().getName(),
					INDEX_REPOSITORY);
			return searchService.findOne(q, getEntityType());
		}

	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids)
	{
		return decoratedRepo.findAll(ids);
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids, Fetch fetch)
	{
		return decoratedRepo.findAll(ids, fetch);
	}

	@Override
	public Stream<Entity> findAll(Query<Entity> q)
	{
		if (querySupported(q))
		{
			LOG.debug("public Entity findAll({}) entityName: [{}] repository: [{}]", q, getEntityType().getName(),
					DECORATED_REPOSITORY);
			return decoratedRepo.findAll(q);
		}
		else
		{
			LOG.debug("public Entity findAll({}) entityName: [{}] repository: [{}]", q, getEntityType().getName(),
					INDEX_REPOSITORY);
			return searchService.searchAsStream(q, getEntityType());
		}
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return decoratedRepo.iterator();
	}

	@Override
	public void forEachBatched(Fetch fetch, Consumer<List<Entity>> consumer, int batchSize)
	{
		decoratedRepo.forEachBatched(fetch, consumer, batchSize);
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
		capabilities.addAll(decoratedRepo.getCapabilities());
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
		decoratedRepo.close();
	}

	@Override
	public String getName()
	{
		return decoratedRepo.getName();
	}

	@Override
	public long count()
	{
		return decoratedRepo.count();
	}

	@Override
	public Query<Entity> query()
	{
		return decoratedRepo.query();
	}

	@Override
	public long count(Query<Entity> q)
	{
		// TODO check if the index is stable. If index is stable you can better check index for count results
		if (querySupported(q))
		{
			LOG.debug("public long count({}) entityName: [{}] repository: [{}]", q, getEntityType().getName(),
					DECORATED_REPOSITORY);
			return decoratedRepo.count(q);
		}
		else
		{
			LOG.debug("public long count({}) entityName: [{}] repository: [{}]", q, getEntityType().getName(),
					INDEX_REPOSITORY);
			return searchService.count(q, getEntityType());
		}
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		return searchService.aggregate(aggregateQuery, getEntityType());
	}

	/**
	 * Checks if the underlying repository can handle this query. Queries with unsupported operators or queries that use
	 * attributes with computed values are delegated to the index.
	 */
	private boolean querySupported(Query<Entity> q)
	{
		return !containsAnyOperator(q, unsupportedOperators) && !containsComputedAttribute(q, getEntityType());

	}
}
