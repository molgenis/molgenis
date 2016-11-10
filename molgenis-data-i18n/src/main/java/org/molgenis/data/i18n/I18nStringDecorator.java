package org.molgenis.data.i18n;

import org.molgenis.data.*;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.meta.model.EntityType;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Decorator for the I18nString respository.
 * <p>
 * Clears the ResourceBundle cache after an update
 */
public class I18nStringDecorator implements Repository<Entity>
{
	private final Repository<Entity> decorated;

	public I18nStringDecorator(Repository<Entity> decorated)
	{
		this.decorated = decorated;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return decorated.iterator();
	}

	@Override
	public void forEachBatched(Fetch fetch, Consumer<List<Entity>> consumer, int batchSize)
	{
		decorated.forEachBatched(fetch, consumer, batchSize);
	}

	@Override
	public void close() throws IOException
	{
		decorated.close();
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return decorated.getCapabilities();
	}

	@Override
	public Set<Operator> getQueryOperators()
	{
		return decorated.getQueryOperators();
	}

	@Override
	public String getName()
	{
		return decorated.getName();
	}

	public EntityType getEntityType()
	{
		return decorated.getEntityType();
	}

	@Override
	public long count()
	{
		return decorated.count();
	}

	@Override
	public Query<Entity> query()
	{
		return decorated.query();
	}

	@Override
	public long count(Query<Entity> q)
	{
		return decorated.count(q);
	}

	@Override
	public Stream<Entity> findAll(Query<Entity> q)
	{
		return decorated.findAll(q);
	}

	@Override
	public Entity findOne(Query<Entity> q)
	{
		return decorated.findOne(q);
	}

	@Override
	public Entity findOneById(Object id)
	{
		return decorated.findOneById(id);
	}

	@Override
	public Entity findOneById(Object id, Fetch fetch)
	{
		return decorated.findOneById(id, fetch);
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids)
	{
		return decorated.findAll(ids);
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids, Fetch fetch)
	{
		return decorated.findAll(ids, fetch);
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		return decorated.aggregate(aggregateQuery);
	}

	@Override
	public void update(Entity entity)
	{
		decorated.update(entity);
		ResourceBundle.clearCache();
	}

	@Override
	public void update(Stream<Entity> records)
	{
		decorated.update(records);
		ResourceBundle.clearCache();
	}

	@Override
	public void delete(Entity entity)
	{
		decorated.delete(entity);
		ResourceBundle.clearCache();
	}

	@Override
	public void delete(Stream<Entity> entities)
	{
		decorated.delete(entities);
		ResourceBundle.clearCache();
	}

	@Override
	public void deleteById(Object id)
	{
		decorated.deleteById(id);
		ResourceBundle.clearCache();
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		decorated.deleteAll(ids);
		ResourceBundle.clearCache();
	}

	@Override
	public void deleteAll()
	{
		decorated.deleteAll();
		ResourceBundle.clearCache();
	}

	@Override
	public void add(Entity entity)
	{
		decorated.add(entity);
		ResourceBundle.clearCache();
	}

	@Override
	public Integer add(Stream<Entity> entities)
	{
		Integer result = decorated.add(entities);
		ResourceBundle.clearCache();

		return result;
	}
}
