package org.molgenis.data.support;

import java.util.Iterator;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;

import static java.util.Objects.requireNonNull;

public class ConvertingIterable<E extends Entity> implements Iterable<E>
{
	private final Class<E> entityClass;
	private final Iterable<Entity> iterable;

	public ConvertingIterable(Class<E> entityClass, Iterable<Entity> iterable)
	{
		this.entityClass = requireNonNull(entityClass);
		this.iterable = requireNonNull(iterable);
	}

	@SuppressWarnings(
	{ "unchecked", "rawtypes" })
	@Override
	public Iterator<E> iterator()
	{
		return new ConvertingIterator(entityClass, iterable.iterator());
	}
}