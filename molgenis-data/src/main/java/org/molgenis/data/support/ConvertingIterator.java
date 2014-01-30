package org.molgenis.data.support;

import java.util.Iterator;

import org.molgenis.data.Entity;
import org.springframework.beans.BeanUtils;

public class ConvertingIterator<E extends Entity> implements Iterator<E>
{
	private final Iterator<Entity> iterator;
	private final Class<E> entityClass;

	public ConvertingIterator(Class<E> entityClass, Iterator<Entity> iterator)
	{
		this.iterator = iterator;
		this.entityClass = entityClass;
	}

	@Override
	public boolean hasNext()
	{
		return iterator.hasNext();
	}

	@SuppressWarnings("unchecked")
	@Override
	public E next()
	{
		Entity next = iterator.next();
		if (entityClass.isAssignableFrom(next.getClass()))
		{
			return (E) next;
		}

		E e = BeanUtils.instantiate(entityClass);
		e.set(next);
		return e;
	}

	@Override
	public void remove()
	{
		iterator.remove();
	}

}