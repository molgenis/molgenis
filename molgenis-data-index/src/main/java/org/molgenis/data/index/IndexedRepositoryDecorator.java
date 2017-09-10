package org.molgenis.data.index;

import org.molgenis.data.*;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.index.exception.UnknownIndexException;
import org.molgenis.data.index.job.IndexJobScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.QueryUtils.*;
import static org.molgenis.data.RepositoryCapability.AGGREGATEABLE;
import static org.molgenis.data.RepositoryCapability.QUERYABLE;

/**
 * Decorator for indexed repositories. Sends all queries with operators that are not supported by the decorated
 * repository to the index.
 */
class IndexedRepositoryDecorator extends AbstractRepositoryDecorator<Entity>
{
	private static final Logger LOG = LoggerFactory.getLogger(IndexedRepositoryDecorator.class);
	private static final String INDEX_REPOSITORY = "Index Repository";
	private static final String DECORATED_REPOSITORY = "Decorated Repository";

	private final SearchService searchService;
	private final IndexJobScheduler indexJobScheduler;

	/**
	 * Operators NOT supported by the decorated repository.
	 */
	private Set<Operator> unsupportedOperators;

	IndexedRepositoryDecorator(Repository<Entity> delegateRepository, SearchService searchService,
			IndexJobScheduler indexJobScheduler)
	{
		super(delegateRepository);
		this.searchService = requireNonNull(searchService);
		this.indexJobScheduler = requireNonNull(indexJobScheduler);
		Set<Operator> operators = getQueryOperators();
		operators.removeAll(delegate().getQueryOperators());
		unsupportedOperators = Collections.unmodifiableSet(operators);
	}

	@Override
	public Entity findOne(Query<Entity> q)
	{
		if (querySupported(q))
		{
			LOG.debug("public Entity findOne({}) entityTypeId: [{}] repository: [{}]", q, getEntityType().getId(),
					DECORATED_REPOSITORY);
			return delegate().findOne(q);
		}
		else
		{
			LOG.debug("public Entity findOne({}) entityTypeId: [{}] repository: [{}]", q, getEntityType().getId(),
					INDEX_REPOSITORY);
			Object entityId = tryTwice(() -> searchService.searchOne(getEntityType(), q));
			return entityId != null ? delegate().findOneById(entityId, q.getFetch()) : null;
		}

	}

	@Override
	public Stream<Entity> findAll(Query<Entity> q)
	{
		if (querySupported(q))
		{
			LOG.debug("public Entity findAll({}) entityTypeId: [{}] repository: [{}]", q, getEntityType().getId(),
					DECORATED_REPOSITORY);
			return delegate().findAll(q);
		}
		else
		{
			LOG.debug("public Entity findAll({}) entityTypeId: [{}] repository: [{}]", q, getEntityType().getId(),
					INDEX_REPOSITORY);
			Stream<Object> entityIds = tryTwice(() -> searchService.search(getEntityType(), q));
			return delegate().findAll(entityIds, q.getFetch());
		}
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
		capabilities.addAll(delegate().getCapabilities());
		capabilities.addAll(EnumSet.of(QUERYABLE, AGGREGATEABLE));
		return unmodifiableSet(capabilities);
	}

	@Override
	public Set<Operator> getQueryOperators()
	{
		return EnumSet.allOf(Operator.class);
	}

	@Override
	public long count(Query<Entity> q)
	{
		// TODO check if the index is stable. If index is stable you can better check index for count results
		if (querySupported(q))
		{
			LOG.debug("public long count({}) entityTypeId: [{}] repository: [{}]", q, getEntityType().getId(),
					DECORATED_REPOSITORY);
			return delegate().count(q);
		}
		else
		{
			LOG.debug("public long count({}) entityTypeId: [{}] repository: [{}]", q, getEntityType().getId(),
					INDEX_REPOSITORY);
			return tryTwice(() -> searchService.count(getEntityType(), q));
		}
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		return tryTwice(() -> searchService.aggregate(getEntityType(), aggregateQuery));
	}

	/**
	 * Executes an action on an index that may be unstable.
	 *
	 * If the Index was unknown, waits for the index to be stable and then tries again.
	 * @param action the action that gets executed
	 * @param <R> the result type of the action
	 * @return the result
	 * @throws MolgenisDataException if the action still failed when the index was stable, with a translated error message.
	 */
	private <R> R tryTwice(Supplier<R> action)
	{
		try
		{
			return action.get();
		}
		catch (UnknownIndexException e)
		{
			waitForIndexToBeStable();
			try
			{
				return action.get();
			}
			catch (UnknownIndexException e1)
			{
				throw new MolgenisDataException(
						format("Error executing query, index for entity type '%s' with id '%s' does not exist",
								getEntityType().getLabel(), getEntityType().getId()));
			}
		}
	}

	/**
	 * Checks if the underlying repository can handle this query. Queries with unsupported operators, queries that use
	 * attributes with computed values or queries with nested query rule field are delegated to the index.
	 */
	private boolean querySupported(Query<Entity> q)
	{
		return !containsAnyOperator(q, unsupportedOperators) && !containsComputedAttribute(q, getEntityType())
				&& !containsNestedQueryRuleField(q);
	}

	private void waitForIndexToBeStable()
	{
		try
		{
			indexJobScheduler.waitForIndexToBeStableIncludingReferences(getEntityType());
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
		}
	}

}
