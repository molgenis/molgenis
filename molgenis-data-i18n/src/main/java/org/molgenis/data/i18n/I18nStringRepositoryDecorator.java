package org.molgenis.data.i18n;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Repository;
import org.molgenis.data.i18n.model.I18nString;

import java.util.ResourceBundle;
import java.util.stream.Stream;

/**
 * Decorator for the I18nString respository.
 * <p>
 * Clears the ResourceBundle cache after an update
 */
public class I18nStringRepositoryDecorator extends AbstractRepositoryDecorator<I18nString>
{
	private final Repository<I18nString> decorated;

	public I18nStringRepositoryDecorator(Repository<I18nString> decorated)
	{
		this.decorated = decorated;
	}

	@Override
	protected Repository<I18nString> delegate()
	{
		return decorated;
	}

	@Override
	public void update(I18nString entity)
	{
		decorated.update(entity);
		ResourceBundle.clearCache();
	}

	@Override
	public void update(Stream<I18nString> entities)
	{
		decorated.update(entities);
		ResourceBundle.clearCache();
	}

	@Override
	public void delete(I18nString entity)
	{
		decorated.delete(entity);
		ResourceBundle.clearCache();
	}

	@Override
	public void delete(Stream<I18nString> entities)
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
	public void add(I18nString entity)
	{
		decorated.add(entity);
		ResourceBundle.clearCache();
	}

	@Override
	public Integer add(Stream<I18nString> entities)
	{
		Integer result = decorated.add(entities);
		ResourceBundle.clearCache();

		return result;
	}
}
