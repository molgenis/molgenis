package org.molgenis.data.index;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.index.meta.IndexActionMetaData.CudType.*;
import static org.molgenis.data.index.meta.IndexActionMetaData.DataType.DATA;

/**
 * {@link Repository} decorator that registers changes with a {@link IndexActionRegisterServiceImpl}.
 */
public class IndexActionRepositoryDecorator extends AbstractRepositoryDecorator<Entity>
{
	private final IndexActionRegisterService indexActionRegisterService;
	private final Repository<Entity> decorated;

	public IndexActionRepositoryDecorator(Repository<Entity> decorated,
			IndexActionRegisterService indexActionRegisterService)
	{
		this.decorated = decorated;
		this.indexActionRegisterService = requireNonNull(indexActionRegisterService);
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
		indexActionRegisterService.register(getName(), UPDATE, DATA, entity.getIdValue().toString());
	}

	@Override
	public void delete(Entity entity)
	{
		indexActionRegisterService.register(getName(), DELETE, DATA, entity.getIdValue().toString());
		delegate().delete(entity);
	}

	@Override
	public void deleteById(Object id)
	{
		indexActionRegisterService.register(getName(), DELETE, DATA, id.toString());
		delegate().deleteById(id);
	}

	@Override
	public void deleteAll()
	{
		indexActionRegisterService.register(getName(), DELETE, DATA, null);
		delegate().deleteAll();
	}

	@Override
	public void add(Entity entity)
	{
		delegate().add(entity);
		indexActionRegisterService.register(getName(), CREATE, DATA, entity.getIdValue().toString());
	}

	@Override
	public Integer add(Stream<Entity> entities)
	{
		indexActionRegisterService.register(getName(), CREATE, DATA, null);
		return delegate().add(entities);
	}

	@Override
	public void update(Stream<Entity> entities)
	{
		indexActionRegisterService.register(getName(), UPDATE, DATA, null);
		delegate().update(entities);
	}

	@Override
	public void delete(Stream<Entity> entities)
	{
		indexActionRegisterService.register(getName(), DELETE, DATA, null);
		delegate().delete(entities);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		indexActionRegisterService.register(getName(), DELETE, DATA, null);
		delegate().deleteAll(ids);
	}
}
