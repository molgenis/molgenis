package org.molgenis.data.support;

import java.util.Iterator;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;

public class ConvertingIterable<E extends Entity> implements Iterable<E>
{
	private final Class<E> entityClass;
	private final Iterable<Entity> iterable;
	private final DataService dataService;

	public ConvertingIterable(Class<E> entityClass, Iterable<Entity> iterable, DataService dataService)
	{
		this.iterable = iterable;
		this.entityClass = entityClass;
		this.dataService = dataService;
	}

	@SuppressWarnings(
	{ "unchecked", "rawtypes" })
	@Override
	public Iterator<E> iterator()
	{
		return new ConvertingIterator(entityClass, iterable.iterator(), dataService);
	}
}