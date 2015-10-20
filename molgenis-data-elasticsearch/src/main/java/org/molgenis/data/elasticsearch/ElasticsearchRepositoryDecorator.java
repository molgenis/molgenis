package org.molgenis.data.elasticsearch;

import java.util.Iterator;
import java.util.Set;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.IndexedRepository;
import org.molgenis.data.Manageable;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.support.QueryImpl;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository that wraps an existing repository and retrieves count/aggregate information from a Elasticsearch index
 */
public class ElasticsearchRepositoryDecorator extends AbstractElasticsearchRepository implements IndexedRepository
{
	private final Repository repository;

	public ElasticsearchRepositoryDecorator(Repository repository, SearchService elasticSearchService)
	{
		super(elasticSearchService);
		if (repository == null) throw new IllegalArgumentException("repository is null");
		this.repository = repository;
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return repository.getEntityMetaData();
	}

	@Override
	@Transactional
	public void add(Entity entity)
	{
		repository.add(entity);
		super.add(entity);
	}

	@Override
	@Transactional
	public Integer add(Iterable<? extends Entity> entities)
	{
		Integer count = repository.add(entities);
		super.add(entities);

		return count;
	}

	@Override
	public void flush()
	{
		repository.flush();
		super.flush();
	}

	@Override
	public void clearCache()
	{
		repository.clearCache();
		super.clearCache();
	}

	@Override
	@Transactional
	public void update(Entity entity)
	{
		repository.update(entity);
		super.update(entity);
	}

	@Override
	@Transactional
	public void update(Iterable<? extends Entity> entities)
	{
		repository.update(entities);
		super.update(entities);
	}

	@Override
	@Transactional
	public void delete(Entity entity)
	{
		repository.delete(entity);
		super.delete(entity);
	}

	@Override
	@Transactional
	public void delete(Iterable<? extends Entity> entities)
	{
		repository.delete(entities);
		super.delete(entities);
	}

	@Override
	@Transactional
	public void deleteById(Object id)
	{
		repository.deleteById(id);
		super.deleteById(id);
	}

	@Override
	@Transactional
	public void deleteById(Iterable<Object> ids)
	{
		repository.deleteById(ids);
		super.deleteById(ids);
	}

	@Override
	@Transactional
	public void deleteAll()
	{
		repository.deleteAll();
		super.deleteAll();
	}

	// retrieve entity by id via decorated repository
	@Override
	public Entity findOne(Object id)
	{
		return repository.findOne(id);
	}

	// retrieve entities by id via decorated repository
	@Override
	public Iterable<Entity> findAll(Iterable<Object> ids)
	{
		return repository.findAll(ids);
	}

	// retrieve all entities via decorated repository
	@Override
	public Iterator<Entity> iterator()
	{
		return repository.iterator();
	}

	@Override
	public void rebuildIndex()
	{
		elasticSearchService.rebuildIndex(repository, getEntityMetaData());
	}

	@Override
	public void drop()
	{
		if (!(repository instanceof Manageable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Manageable");
		}
		((Manageable) repository).drop();

		super.drop();
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		Set<RepositoryCapability> capabilities = repository.getCapabilities();
		capabilities.add(RepositoryCapability.QUERYABLE);
		capabilities.add(RepositoryCapability.AGGREGATEABLE);

		return capabilities;
	}
}
