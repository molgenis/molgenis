package org.molgenis.data.i18n;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.support.QueryImpl;

import java.util.ResourceBundle;
import java.util.stream.Stream;

/**
 * Decorator for the I18nString respository.
 * <p>
 * Clears the ResourceBundle cache after an update
 */
public class I18nStringDecorator extends AbstractRepositoryDecorator<Entity>
{
	private final Repository<Entity> decorated;

	public I18nStringDecorator(Repository<Entity> decorated)
	{
		this.decorated = decorated;
	}

	@Override
	protected Repository<Entity> delegate()
	{
		return decorated;
	}

	@Override
	public Query<Entity> query()
	{
		return new QueryImpl<>(this);
	}

	@Override
	public void update(Entity entity)
	{
		decorated.update(entity);
		ResourceBundle.clearCache();
	}

	@Override
	public void update(Stream<Entity> entities)
	{
		decorated.update(entities);
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
