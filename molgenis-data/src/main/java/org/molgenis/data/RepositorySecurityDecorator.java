package org.molgenis.data;

import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.Permission;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.molgenis.util.SecurityDecoratorUtils.validatePermission;

/**
 * Repository decorated that validates that current user has permission to perform an operation for an entity type.
 */
public class RepositorySecurityDecorator implements Repository<Entity>
{
	private final Repository<Entity> decoratedRepository;

	public RepositorySecurityDecorator(Repository<Entity> decoratedRepository)
	{
		this.decoratedRepository = requireNonNull(decoratedRepository);
	}

	@Override
	public Iterator<Entity> iterator()
	{
		validatePermission(decoratedRepository.getName(), Permission.READ);
		return decoratedRepository.iterator();
	}

	@Override
	public void forEachBatched(Fetch fetch, Consumer<List<Entity>> consumer, int batchSize)
	{
		validatePermission(decoratedRepository.getName(), Permission.READ);
		decoratedRepository.forEachBatched(fetch, consumer, batchSize);
	}

	@Override
	public void close() throws IOException
	{
		validatePermission(decoratedRepository.getName(), Permission.WRITE);
		decoratedRepository.close();
	}

	@Override
	public String getName()
	{
		return decoratedRepository.getName();
	}

	public EntityType getEntityType()
	{
		return decoratedRepository.getEntityType();
	}

	@Override
	public Query<Entity> query()
	{
		return new QueryImpl<>(this);
	}

	@Override
	public long count(Query<Entity> q)
	{
		validatePermission(decoratedRepository.getName(), Permission.COUNT);
		return decoratedRepository.count(q);
	}

	@Override
	public Stream<Entity> findAll(Query<Entity> q)
	{
		validatePermission(decoratedRepository.getName(), Permission.READ);
		return decoratedRepository.findAll(q);
	}

	@Override
	public Entity findOne(Query<Entity> q)
	{
		validatePermission(decoratedRepository.getName(), Permission.READ);
		return decoratedRepository.findOne(q);
	}

	@Override
	public Entity findOneById(Object id)
	{
		validatePermission(decoratedRepository.getName(), Permission.READ);
		return decoratedRepository.findOneById(id);
	}

	@Override
	public Entity findOneById(Object id, Fetch fetch)
	{
		validatePermission(decoratedRepository.getName(), Permission.READ);
		return decoratedRepository.findOneById(id, fetch);
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids)
	{
		validatePermission(decoratedRepository.getName(), Permission.READ);
		return decoratedRepository.findAll(ids);
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids, Fetch fetch)
	{
		validatePermission(decoratedRepository.getName(), Permission.READ);
		return decoratedRepository.findAll(ids, fetch);
	}

	@Override
	public long count()
	{
		validatePermission(decoratedRepository.getName(), Permission.COUNT);
		return decoratedRepository.count();
	}

	@Override
	public void update(Entity entity)
	{
		validatePermission(decoratedRepository.getName(), Permission.WRITE);
		decoratedRepository.update(entity);
	}

	@Override
	public void update(Stream<Entity> entities)
	{
		validatePermission(decoratedRepository.getName(), Permission.WRITE);
		decoratedRepository.update(entities);
	}

	@Override
	public void delete(Entity entity)
	{
		validatePermission(decoratedRepository.getName(), Permission.WRITE);
		decoratedRepository.delete(entity);
	}

	@Override
	public void delete(Stream<Entity> entities)
	{
		validatePermission(decoratedRepository.getName(), Permission.WRITE);
		decoratedRepository.delete(entities);
	}

	@Override
	public void deleteById(Object id)
	{
		validatePermission(decoratedRepository.getName(), Permission.WRITE);
		decoratedRepository.deleteById(id);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		validatePermission(decoratedRepository.getName(), Permission.WRITE);
		decoratedRepository.deleteAll(ids);
	}

	@Override
	public void deleteAll()
	{
		validatePermission(decoratedRepository.getName(), Permission.WRITE);
		decoratedRepository.deleteAll();
	}

	@Override
	public void add(Entity entity)
	{
		validatePermission(decoratedRepository.getName(), Permission.WRITE);
		decoratedRepository.add(entity);
	}

	@Override
	public Integer add(Stream<Entity> entities)
	{
		validatePermission(decoratedRepository.getName(), Permission.WRITE);
		return decoratedRepository.add(entities);
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		validatePermission(decoratedRepository.getName(), Permission.COUNT);
		return decoratedRepository.aggregate(aggregateQuery);
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return decoratedRepository.getCapabilities();
	}

	@Override
	public Set<Operator> getQueryOperators()
	{
		return decoratedRepository.getQueryOperators();
	}
}
