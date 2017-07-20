package org.molgenis.data.index;

import org.molgenis.data.*;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.QueryUtils.*;
import static org.molgenis.data.RepositoryCapability.AGGREGATEABLE;
import static org.molgenis.data.RepositoryCapability.QUERYABLE;

/**
 * Decorator for indexed repositories. Sends all queries with operators that are not supported by the decorated
 * repository to the index.
 */
public class IndexedRepositoryDecorator extends AbstractRepositoryDecorator<Entity>
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

	@Override
	protected Repository<Entity> delegate()
	{
		return decoratedRepo;
	}

	@Override
	public Entity findOne(Query<Entity> q)
	{
		if (querySupported(q))
		{
			LOG.debug("public Entity findOne({}) entityTypeId: [{}] repository: [{}]", q, getEntityType().getId(),
					DECORATED_REPOSITORY);
			return decoratedRepo.findOne(q);
		}
		else
		{
			LOG.debug("public Entity findOne({}) entityTypeId: [{}] repository: [{}]", q, getEntityType().getId(),
					INDEX_REPOSITORY);
			Object entityId = searchService.searchOne(getEntityType(), q);
			return entityId != null ? decoratedRepo.findOneById(entityId, q.getFetch()) : null;
		}

	}

	@Override
	public Stream<Entity> findAll(Query<Entity> q)
	{
		if (querySupported(q))
		{
			LOG.debug("public Entity findAll({}) entityTypeId: [{}] repository: [{}]", q, getEntityType().getId(),
					DECORATED_REPOSITORY);
			return decoratedRepo.findAll(q);
		}
		else
		{
			LOG.debug("public Entity findAll({}) entityTypeId: [{}] repository: [{}]", q, getEntityType().getId(),
					INDEX_REPOSITORY);
			Stream<Object> entityIds = searchService.search(getEntityType(), q);
			return decoratedRepo.findAll(entityIds, q.getFetch());
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
	public long count(Query<Entity> q)
	{
		// TODO check if the index is stable. If index is stable you can better check index for count results
		if (querySupported(q))
		{
			LOG.debug("public long count({}) entityTypeId: [{}] repository: [{}]", q, getEntityType().getId(),
					DECORATED_REPOSITORY);
			return decoratedRepo.count(q);
		}
		else
		{
			LOG.debug("public long count({}) entityTypeId: [{}] repository: [{}]", q, getEntityType().getId(),
					INDEX_REPOSITORY);
			return searchService.count(getEntityType(), q);
		}
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		return searchService.aggregate(getEntityType(), aggregateQuery);
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
}
