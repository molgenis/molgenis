package org.molgenis.data.elasticsearch;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.RepositoryCapability.MANAGABLE;
import static org.molgenis.data.RepositoryCapability.UPDATEABLE;
import static org.molgenis.data.RepositoryCapability.WRITABLE;

import java.util.Iterator;
import java.util.Set;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;

/**
 * Repository that wraps an existing repository and retrieves count/aggregate information from a Elasticsearch index
 */
public class ElasticsearchRepositoryDecorator extends AbstractElasticsearchRepository
{
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
	public Integer add(Iterable<? extends Entity> entities)
	{
		Integer count = decoratedRepo.add(entities);
		super.add(entities);

		return count;
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
	public void update(Iterable<? extends Entity> entities)
	{
		decoratedRepo.update(entities);
		super.update(entities);
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
	public void delete(Iterable<? extends Entity> entities)
	{
		decoratedRepo.delete(entities);
		super.delete(entities);
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
	public void deleteById(Iterable<Object> ids)
	{
		decoratedRepo.deleteById(ids);
		super.deleteById(ids);
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

	// retrieve entities by id via decorated repository
	@Override
	public Iterable<Entity> findAll(Iterable<Object> ids)
	{
		return decoratedRepo.findAll(ids);
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
			if (capability != WRITABLE && capability != UPDATEABLE && capability != MANAGABLE)
			{
				capabilities.add(capability);
			}
		});
		return capabilities;
	}
}
