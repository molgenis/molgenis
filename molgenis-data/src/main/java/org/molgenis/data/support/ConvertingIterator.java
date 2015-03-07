package org.molgenis.data.support;

import java.util.Iterator;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.util.EntityUtils;

public class ConvertingIterator<E extends Entity> implements Iterator<E>
{
	private final Iterator<Entity> iterator;
	private final Class<E> entityClass;
	private final DataService dataService;

	public ConvertingIterator(Class<E> entityClass, Iterator<Entity> iterator, DataService dataService)
	{
		this.iterator = iterator;
		this.entityClass = entityClass;
		this.dataService = dataService;
	}

	@Override
	public boolean hasNext()
	{
		return iterator.hasNext();
	}

	@Override
	public E next()
	{
		return EntityUtils.convert(iterator.next(), entityClass, dataService);
	}

	@Override
	public void remove()
	{
		iterator.remove();
	}

}