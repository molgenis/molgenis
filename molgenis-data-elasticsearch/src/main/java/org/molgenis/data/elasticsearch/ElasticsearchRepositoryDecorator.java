package org.molgenis.data.elasticsearch;

import java.io.IOException;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.IndexedCrudRepository;
import org.molgenis.data.Manageable;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.Updateable;
import org.molgenis.data.Writable;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository that wraps an existing repository and retrieves count/aggregate information from a Elasticsearch index
 */
public class ElasticsearchRepositoryDecorator extends AbstractElasticsearchRepository implements IndexedCrudRepository
{
	public static final String BASE_URL = "elasticsearch://";

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
		if (!(repository instanceof Writable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Writable");
		}
		((Writable) repository).add(entity);

		super.add(entity);
	}

	@Override
	@Transactional
	public Integer add(Iterable<? extends Entity> entities)
	{
		if (!(repository instanceof Writable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Writable");
		}
		Integer count = ((Writable) repository).add(entities);

		super.add(entities);
		return count;
	}

	@Override
	public void flush()
	{
		if (!(repository instanceof Writable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Updateable");
		}
		((Writable) repository).flush();

		super.flush();
	}

	@Override
	public void clearCache()
	{
		if (!(repository instanceof Writable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Updateable");
		}
		((Writable) repository).clearCache();

		super.clearCache();
	}

	@Override
	@Transactional
	public void update(Entity entity)
	{
		if (!(repository instanceof Updateable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Updateable");
		}
		((Updateable) repository).update(entity);

		super.update(entity);
	}

	@Override
	@Transactional
	public void update(Iterable<? extends Entity> entities)
	{
		if (!(repository instanceof Updateable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Updateable");
		}
		((Updateable) repository).update(entities);

		super.update(entities);
	}

	@Override
	@Transactional
	public void delete(Entity entity)
	{
		if (!(repository instanceof Updateable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Updateable");
		}
		((Updateable) repository).delete(entity);

		super.delete(entity);
	}

	@Override
	@Transactional
	public void delete(Iterable<? extends Entity> entities)
	{
		if (!(repository instanceof Updateable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Updateable");
		}
		((Updateable) repository).delete(entities);

		super.delete(entities);
	}

	@Override
	@Transactional
	public void deleteById(Object id)
	{
		if (!(repository instanceof Updateable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Updateable");
		}
		((Updateable) repository).deleteById(id);

		super.deleteById(id);
	}

	@Override
	@Transactional
	public void deleteById(Iterable<Object> ids)
	{
		if (!(repository instanceof Updateable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Updateable");
		}
		((Updateable) repository).deleteById(ids);

		super.deleteById(ids);
	}

	@Override
	@Transactional
	public void deleteAll()
	{
		if (!(repository instanceof Updateable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Updateable");
		}
		((Updateable) repository).deleteAll();

		super.deleteAll();
	}

	@Override
	public void rebuildIndex()
	{
		elasticSearchService.rebuildIndex(repository, getEntityMetaData());
	}

	@Override
	public void create()
	{
		try
		{
			elasticSearchService.createMappings(getEntityMetaData());
		}
		catch (IOException e)
		{
			throw new MolgenisDataException(e);
		}
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
}
