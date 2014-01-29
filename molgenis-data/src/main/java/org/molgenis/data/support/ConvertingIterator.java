package org.molgenis.data.support;

import java.util.Iterator;

import org.molgenis.data.Entity;

public class ConvertingIterator<E extends Entity> implements Iterator<E>
{
	private Iterator<Entity> iterator;
	private Class<E> entityClass;
	
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
		if(next.getClass().equals(entityClass))
		{
			return (E)next;
		}
		else
		{
			E e;
			try
			{
				e = entityClass.newInstance();
				e.set(next);
				return e;
			}
			catch (Exception e1)
			{
				throw new RuntimeException(e1);
			}

		}
	}

	@Override
	public void remove()
	{
		iterator.remove();
	}
	
}