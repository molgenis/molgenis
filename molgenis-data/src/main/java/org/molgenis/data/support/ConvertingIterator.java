package org.molgenis.data.support;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.SystemEntityFactory;
import org.molgenis.data.meta.SystemEntity;
import org.molgenis.data.meta.system.SystemEntityMetaDataRegistry;

import java.util.Iterator;

import static java.util.Objects.requireNonNull;
import static org.molgenis.util.ApplicationContextProvider.getApplicationContext;

public class ConvertingIterator<E extends Entity> implements Iterator<E>
{
	private final Iterator<Entity> iterator;
	private final Class<E> entityClass;

	public ConvertingIterator(Class<E> entityClass, Iterator<Entity> iterator)
	{
		this.entityClass = requireNonNull(entityClass);
		this.iterator = requireNonNull(iterator);
	}

	@Override public boolean hasNext()
	{
		return iterator.hasNext();
	}

	@Override public E next()
	{
		// TODO remove dependency on application context
		// TODO remove system entity <--> entity casts
		SystemEntityFactory<SystemEntity, Object> entityFactory = getApplicationContext()
				.getBean(SystemEntityMetaDataRegistry.class).getSystemEntityFactory((Class<SystemEntity>) entityClass);
		return (E) entityFactory.create(iterator.next());
	}

	@Override public void remove()
	{
		iterator.remove();
	}

}