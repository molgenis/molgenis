package org.molgenis.data.support;

import java.util.Iterator;

import org.molgenis.data.Entity;

public class ConvertingIterable<E extends Entity> implements Iterable<E>
{
	private Class<E> entityClass;
	private Iterable<Entity> iterable;
	
	public ConvertingIterable(Class<E> entityClass, Iterable<Entity> iterable)
	{
		this.iterable = iterable;
		this.entityClass = entityClass;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Iterator<E> iterator()
	{
		return new ConvertingIterator(entityClass,iterable.iterator());
	}
}