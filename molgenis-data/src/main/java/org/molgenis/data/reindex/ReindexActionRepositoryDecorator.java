package org.molgenis.data.reindex;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.CudType.CREATE;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.CudType.DELETE;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.CudType.UPDATE;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.DataType.DATA;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;

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
		reindexActionRegisterService.register(getName(), UPDATE, DATA, entity.getIdValue().toString());
	}

	@Override
	public void delete(Entity entity)
	{
		reindexActionRegisterService.register(getName(), DELETE, DATA, entity.getIdValue().toString());
		decorated.delete(entity);
	}

	@Override
	public void deleteById(Object id)
	{
		reindexActionRegisterService.register(getName(), DELETE, DATA, id.toString());
		decorated.deleteById(id);
	}

	@Override
	public void deleteAll()
	{
		reindexActionRegisterService.register(getName(), DELETE, DATA, null);
		decorated.deleteAll();
	}

	@Override
	public void add(Entity entity)
	{
		decorated.add(entity);
		reindexActionRegisterService.register(getName(), CREATE, DATA, entity.getIdValue().toString());
	}

	@Override
	public Integer add(Stream<Entity> entities)
	{
		reindexActionRegisterService.register(getName(), CREATE, DATA, null);
		return decorated.add(entities);
	}

	@Override
	public void update(Stream<Entity> entities)
	{
		reindexActionRegisterService.register(getName(), UPDATE, DATA, null);
		decorated.update(entities);
	}

	@Override
	public void delete(Stream<Entity> entities)
	{
		reindexActionRegisterService.register(getName(), DELETE, DATA, null);
		decorated.delete(entities);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		reindexActionRegisterService.register(getName(), DELETE, DATA, null);
		decorated.deleteAll(ids);
	}
}
