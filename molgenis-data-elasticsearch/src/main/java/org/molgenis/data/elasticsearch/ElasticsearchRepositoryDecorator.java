package org.molgenis.data.elasticsearch;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.QueryRule.Operator.EQUALS;
import static org.molgenis.data.RepositoryCapability.MANAGABLE;
import static org.molgenis.data.RepositoryCapability.WRITABLE;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.elasticsearch.common.collect.Iterators;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Fetch;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;

/**
 * Repository that wraps an existing repository and retrieves count/aggregate information from a Elasticsearch index
 */
public class ElasticsearchRepositoryDecorator extends AbstractElasticsearchRepository
{
	private static final int BATCH_SIZE = 1000;

	private final Repository decoratedRepo;

	public ElasticsearchRepositoryDecorator(Repository decoratedRepo, SearchService elasticSearchService)
	{
		super(elasticSearchService);
		this.decoratedRepo = requireNonNull(decoratedRepo);
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return decoratedRepo.getEntityMetaData();
	}

	@Override
	@Transactional
	public void add(Entity entity)
	{
		decoratedRepo.add(entity);
		super.add(entity);
	}

	@Override
	@Transactional
	public Integer add(Stream<? extends Entity> entities)
	{
		// TODO look into performance improvements
		AtomicInteger count = new AtomicInteger();
		Iterators.partition(entities.iterator(), BATCH_SIZE).forEachRemaining(batch -> {
			Integer batchCount = decoratedRepo.add(batch.stream());
			super.add(batch.stream());
			count.addAndGet(batchCount);
		});
		return count.get();
	}

	@Override
	public void flush()
	{
		decoratedRepo.flush();
		super.flush();
	}

	@Override
	public void clearCache()
	{
		decoratedRepo.clearCache();
		super.clearCache();
	}

	@Override
	@Transactional
	public void update(Entity entity)
	{
		decoratedRepo.update(entity);
		super.update(entity);
	}

	@Override
	@Transactional
	public void update(Stream<? extends Entity> entities)
	{
		// TODO look into performance improvements
		Iterators.partition(entities.iterator(), BATCH_SIZE).forEachRemaining(batch -> {
			decoratedRepo.update(batch.stream());
			super.update(batch.stream());
		});
	}

	@Override
	@Transactional
	public void delete(Entity entity)
	{
		decoratedRepo.delete(entity);
		super.delete(entity);
	}

	@Override
	@Transactional
	public void delete(Stream<? extends Entity> entities)
	{
		// TODO look into performance improvements
		Iterators.partition(entities.iterator(), BATCH_SIZE).forEachRemaining(batch -> {
			decoratedRepo.delete(batch.stream());
			super.delete(batch.stream());
		});
	}

	@Override
	@Transactional
	public void deleteById(Object id)
	{
		decoratedRepo.deleteById(id);
		super.deleteById(id);
	}

	@Override
	@Transactional
	public void deleteById(Stream<Object> ids)
	{
		// TODO look into performance improvements
		Iterators.partition(ids.iterator(), BATCH_SIZE).forEachRemaining(batch -> {
			decoratedRepo.deleteById(batch);
			super.deleteById(batch);
		});
	}

	@Override
	@Transactional
	public void deleteAll()
	{
		decoratedRepo.deleteAll();
		super.deleteAll();
	}

	// retrieve entity by id via decorated repository
	@Override
	public Entity findOne(Object id)
	{
		return decoratedRepo.findOne(id);
	}

	// retrieve entity by id via decorated repository
	@Override
	public Entity findOne(Object id, Fetch fetch)
	{
		return decoratedRepo.findOne(id, fetch);
	}

	@Override
	public Entity findOne(Query q)
	{
		// optimization:
		// retrieve entity by id via decorated repository in case query is of the form: <id attribute> EQUALS <id>
		List<QueryRule> queryRules = q.getRules();
		if (queryRules != null && queryRules.size() == 1)
		{
			QueryRule queryRule = queryRules.get(0);
			if (queryRule.getOperator() == EQUALS)
			{
				String idAttrName = getEntityMetaData().getIdAttribute().getName();
				if (queryRule.getField().equals(idAttrName))
				{
					return decoratedRepo.findOne(queryRule.getValue(), q.getFetch());
				}
			}
		}

		return super.findOne(q);
	}

	// retrieve entities by id via decorated repository
	@Override
	public Stream<Entity> findAll(Stream<Object> ids)
	{
		return decoratedRepo.findAll(ids);
	}

	// retrieve entities by id via decorated repository
	@Override
	public Stream<Entity> findAll(Stream<Object> ids, Fetch fetch)
	{
		return decoratedRepo.findAll(ids, fetch);
	}

	@Override
	public Stream<Entity> findAll(Query q)
	{
		return super.findAll(q);
	}

	// retrieve all entities via decorated repository
	@Override
	public Iterator<Entity> iterator()
	{
		return decoratedRepo.iterator();
	}

	@Override
	public void rebuildIndex()
	{
		elasticSearchService.rebuildIndex(decoratedRepo, getEntityMetaData());
	}

	@Override
	public void create()
	{
		if (!decoratedRepo.getCapabilities().contains(MANAGABLE))
		{
			throw new MolgenisDataAccessException("Repository '" + decoratedRepo.getName() + "' is not Manageable");
		}
		decoratedRepo.create();

		super.create();
	}

	@Override
	public void drop()
	{
		if (!decoratedRepo.getCapabilities().contains(MANAGABLE))
		{
			throw new MolgenisDataAccessException("Repository '" + decoratedRepo.getName() + "' is not Manageable");
		}
		decoratedRepo.drop();

		super.drop();
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		Set<RepositoryCapability> capabilities = Sets.newHashSet(decoratedRepo.getCapabilities());
		super.getCapabilities().forEach(capability -> {
			// Elasticsearch can write and update documents, but the parent repository might not
			if (capability != WRITABLE && capability != MANAGABLE)
			{
				capabilities.add(capability);
			}
		});
		return capabilities;
	}
}
