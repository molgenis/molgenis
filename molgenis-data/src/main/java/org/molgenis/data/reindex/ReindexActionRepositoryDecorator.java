package org.molgenis.data.reindex;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.CudType.*;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.DataType.DATA;

/**
 * {@link Repository} decorator that registers changes with a {@link ReindexActionRegisterServiceImpl}.
 */
public class ReindexActionRepositoryDecorator extends AbstractRepositoryDecorator
{
	private final ReindexActionRegisterService reindexActionRegisterService;
	private final Repository<Entity> decorated;

	public ReindexActionRepositoryDecorator(Repository<Entity> decorated,
			ReindexActionRegisterService reindexActionRegisterService)
	{
		this.decorated = decorated;
		this.reindexActionRegisterService = requireNonNull(reindexActionRegisterService);
	}

	@Override
	protected Repository<Entity> delegate()
	{
		return decorated;
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		Set<RepositoryCapability> capabilities = new HashSet<>();
		capabilities.add(RepositoryCapability.INDEXABLE);
		capabilities.addAll(delegate().getCapabilities());
		return capabilities;
	}

	@Override
	public void update(Entity entity)
	{
		delegate().update(entity);
		reindexActionRegisterService.register(getName(), UPDATE, DATA, entity.getIdValue().toString());
	}

	@Override
	public void delete(Entity entity)
	{
		reindexActionRegisterService.register(getName(), DELETE, DATA, entity.getIdValue().toString());
		delegate().delete(entity);
	}

	@Override
	public void deleteById(Object id)
	{
		reindexActionRegisterService.register(getName(), DELETE, DATA, id.toString());
		delegate().deleteById(id);
	}

	@Override
	public void deleteAll()
	{
		reindexActionRegisterService.register(getName(), DELETE, DATA, null);
		delegate().deleteAll();
	}

	@Override
	public void add(Entity entity)
	{
		delegate().add(entity);
		reindexActionRegisterService.register(getName(), CREATE, DATA, entity.getIdValue().toString());
	}

	@Override
	public Integer add(Stream<Entity> entities)
	{
		reindexActionRegisterService.register(getName(), CREATE, DATA, null);
		return delegate().add(entities);
	}

	@Override
	public void update(Stream<Entity> entities)
	{
		reindexActionRegisterService.register(getName(), UPDATE, DATA, null);
		delegate().update(entities);
	}

	@Override
	public void delete(Stream<Entity> entities)
	{
		reindexActionRegisterService.register(getName(), DELETE, DATA, null);
		delegate().delete(entities);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		reindexActionRegisterService.register(getName(), DELETE, DATA, null);
		delegate().deleteAll(ids);
	}
}
