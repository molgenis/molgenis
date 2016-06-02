package org.molgenis.data.reindex;

import static java.util.Objects.requireNonNull;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.reindex.meta.ReindexActionMetaData.CudType;
import org.molgenis.data.reindex.meta.ReindexActionMetaData.DataType;

/**
 * {@link Repository} decorator that registers changes with a {@link ReindexActionRegisterService}.
 */
public class ReindexActionRepositoryDecorator extends AbstractRepositoryDecorator
{
	private final ReindexActionRegisterService reindexActionRegisterService;

	public ReindexActionRepositoryDecorator(Repository<Entity> decorated,
			ReindexActionRegisterService reindexActionRegisterService)
	{
		super(decorated);
		this.reindexActionRegisterService = requireNonNull(reindexActionRegisterService);
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		Set<RepositoryCapability> capabilities = new HashSet<>();
		capabilities.add(RepositoryCapability.INDEXABLE);
		capabilities.addAll(decorated.getCapabilities());
		return capabilities;
	}

	@Override
	public void update(Entity entity)
	{
		decorated.update(entity);
		reindexActionRegisterService.register(getName(), CudType.UPDATE, DataType.DATA, entity.getIdValue().toString());
	}

	@Override
	public void delete(Entity entity)
	{
		reindexActionRegisterService.register(getName(), CudType.DELETE, DataType.DATA, entity.getIdValue().toString());
		decorated.delete(entity);
	}

	@Override
	public void deleteById(Object id)
	{
		reindexActionRegisterService.register(getName(), CudType.DELETE, DataType.DATA,
				id.toString());
		decorated.deleteById(id);
	}

	@Override
	public void deleteAll()
	{
		reindexActionRegisterService.register(getName(), CudType.DELETE, DataType.DATA, null);
		decorated.deleteAll();
	}

	@Override
	public void add(Entity entity)
	{
		decorated.add(entity);
		reindexActionRegisterService.register(getName(), CudType.CREATE, DataType.DATA, entity
				.getIdValue().toString());
	}

	@Override
	public Integer add(Stream<Entity> entities)
	{
		reindexActionRegisterService.register(getName(), CudType.CREATE, DataType.DATA, null);
		return decorated.add(entities);
	}

	@Override
	public void update(Stream<Entity> entities)
	{
		reindexActionRegisterService.register(getName(), CudType.UPDATE, DataType.DATA, null);
		decorated.update(entities);
	}

	@Override
	public void delete(Stream<Entity> entities)
	{
		reindexActionRegisterService.register(getName(), CudType.DELETE, DataType.DATA, null);
		decorated.delete(entities);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		reindexActionRegisterService.register(getName(), CudType.DELETE, DataType.DATA, null);
		decorated.deleteAll(ids);
	}
}
