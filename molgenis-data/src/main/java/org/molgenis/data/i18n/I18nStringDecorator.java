package org.molgenis.data.i18n;

import java.io.IOException;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Stream;

import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityListener;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;

/**
 * Decorator for the I18nString respository.
 * 
 * Clears the ResourceBundle cache after an update
 */
public class I18nStringDecorator implements Repository
{
	private final Repository decorated;

	public I18nStringDecorator(Repository decorated)
	{
		this.decorated = decorated;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return decorated.iterator();
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
	public String getName()
	{
		return decorated.getName();
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return decorated.getEntityMetaData();
	}

	@Override
	public long count()
	{
		return decorated.count();
	}

	@Override
	public Query query()
	{
		return decorated.query();
	}

	@Override
	public long count(Query q)
	{
		return decorated.count(q);
	}

	@Override
	public Stream<Entity> findAll(Query q)
	{
		return decorated.findAll(q);
	}

	@Override
	public Entity findOne(Query q)
	{
		return decorated.findOne(q);
	}

	@Override
	public Entity findOne(Object id)
	{
		return decorated.findOne(id);
	}

	@Override
	public Entity findOne(Object id, Fetch fetch)
	{
		return decorated.findOne(id, fetch);
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
	public void update(Stream<? extends Entity> records)
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
	public void delete(Stream<? extends Entity> entities)
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
	public void deleteById(Stream<Object> ids)
	{
		decorated.deleteById(ids);
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
	public Integer add(Stream<? extends Entity> entities)
	{
		Integer result = decorated.add(entities);
		ResourceBundle.clearCache();

		return result;
	}

	@Override
	public void flush()
	{
		decorated.flush();
	}

	@Override
	public void clearCache()
	{
		decorated.clearCache();
		ResourceBundle.clearCache();
	}

	@Override
	public void create()
	{
		decorated.create();
	}

	@Override
	public void drop()
	{
		decorated.drop();
		ResourceBundle.clearCache();
	}

	@Override
	public void rebuildIndex()
	{
		decorated.rebuildIndex();
		ResourceBundle.clearCache();
	}

	@Override
	public void addEntityListener(EntityListener entityListener)
	{
		decorated.addEntityListener(entityListener);
	}

	@Override
	public void removeEntityListener(EntityListener entityListener)
	{
		decorated.removeEntityListener(entityListener);
	}

}
