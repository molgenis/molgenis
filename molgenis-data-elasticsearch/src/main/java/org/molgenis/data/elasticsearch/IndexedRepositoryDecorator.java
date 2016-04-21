package org.molgenis.data.elasticsearch;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.RepositoryCapability.AGGREGATEABLE;
import static org.molgenis.data.RepositoryCapability.INDEXABLE;
import static org.molgenis.data.RepositoryCapability.MANAGABLE;
import static org.molgenis.data.RepositoryCapability.QUERYABLE;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.elasticsearch.common.collect.Iterators;
import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityListener;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Fetch;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.QueryUtils;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.springframework.transaction.annotation.Transactional;

public class IndexedRepositoryDecorator implements Repository<Entity>
{
	private static final int BATCH_SIZE = 1000;

	private final Repository<Entity> decoratedRepository;
	private final Repository<Entity> indexRepository;
	private SearchService elasticSearchService;

	private Set<Operator> unsupportedOperators;

	public IndexedRepositoryDecorator(Repository<Entity> decoratedRepo, SearchService elasticSearchService)
	{
		this.elasticSearchService = requireNonNull(elasticSearchService);
		this.decoratedRepository = requireNonNull(decoratedRepo);
		this.indexRepository = new ElasticsearchRepository(getEntityMetaData(), elasticSearchService);

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
		Iterators.partition(entities.iterator(), BATCH_SIZE).forEachRemaining(batch -> {
			decoratedRepository.update(batch.stream());
		});
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
		Iterators.partition(entities.iterator(), BATCH_SIZE).forEachRemaining(batch -> {
			decoratedRepository.delete(batch.stream());
		});
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
		Iterators.partition(ids.iterator(), BATCH_SIZE).forEachRemaining(batch -> {
			decoratedRepository.deleteById(batch);
		});
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
			return decoratedRepository.findOne(q);
		}
		else
		{
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
			return decoratedRepository.findAll(q);
		}
		else
		{
			return indexRepository.findAll(q);
		}
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return decoratedRepository.iterator();
	}

	@Override
	public Stream<Entity> stream(Fetch fetch)
	{
		return decoratedRepository.stream(fetch);
	}

	@Override
	public void rebuildIndex()
	{
		elasticSearchService.rebuildIndex(decoratedRepository, getEntityMetaData());
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

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		Set<RepositoryCapability> capabilities = decoratedRepository.getCapabilities();

		// when a repository is indexed, it is automatically indexable, queryable and aggregateable
		capabilities.addAll(EnumSet.of(INDEXABLE, QUERYABLE, AGGREGATEABLE));
		return capabilities;
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
		if (querySupported(q))
		{
			return decoratedRepository.count(q);
		}
		else
		{
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

	private boolean querySupported(Query<Entity> q)
	{
		if (QueryUtils.containsAnyOperator(q, unsupportedOperators))
		{
			return false;
		}

		if (QueryUtils.containsComputedAttribute(q.getRules(), getEntityMetaData()))
		{
			return false;
		}

		return true;
	}
}
