package org.molgenis.data.elasticsearch.reindex;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

import org.molgenis.data.*;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.elasticsearch.reindex.meta.ReindexActionMetaData.CudType;
import org.molgenis.data.elasticsearch.reindex.meta.ReindexActionMetaData.DataType;

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
		reindexActionRegisterService
				.register(getEntityMetaData(), CudType.UPDATE, DataType.DATA, entity.getIdValue().toString());
	}

	@Override
	public void delete(Entity entity)
	{
		reindexActionRegisterService
				.register(getEntityMetaData(), CudType.DELETE, DataType.DATA, entity.getIdValue().toString());
		decorated.delete(entity);
	}

	@Override
	public void deleteById(Object id)
	{
		reindexActionRegisterService.register(getEntityMetaData(), CudType.DELETE, DataType.DATA, id.toString());
		decorated.deleteById(id);
	}

	@Override
	public void deleteAll()
	{
		reindexActionRegisterService.register(getEntityMetaData(), CudType.DELETE, DataType.DATA, null);
		decorated.deleteAll();
	}

	@Override
	public void add(Entity entity)
	{
		decorated.add(entity);
		reindexActionRegisterService
				.register(getEntityMetaData(), CudType.CREATE, DataType.DATA, entity.getIdValue().toString());
	}

	@Override
	public Integer add(Stream<Entity> entities)
	{
		reindexActionRegisterService.register(getEntityMetaData(), CudType.CREATE, DataType.DATA, null);
		return decorated.add(entities);
	}

	@Override
	public void update(Stream<Entity> entities)
	{
		reindexActionRegisterService.register(getEntityMetaData(), CudType.UPDATE, DataType.DATA, null);
		decorated.update(entities);
	}

	@Override
	public void delete(Stream<Entity> entities)
	{
		reindexActionRegisterService.register(getEntityMetaData(), CudType.DELETE, DataType.DATA, null);
		decorated.delete(entities);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		reindexActionRegisterService.register(getEntityMetaData(), CudType.DELETE, DataType.DATA, null);
		decorated.deleteAll(ids);
	}
}
