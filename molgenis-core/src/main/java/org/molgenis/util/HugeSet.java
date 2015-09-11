package org.molgenis.util;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.mapdb.DB;
import org.mapdb.DBMaker;

/**
 * Set that can contain a huge amount of data by storing it on disk.
 * 
 * @param <E>
 */
public class HugeSet<E> implements Set<E>, Closeable
{
	private static final int THRESHOLD = 10000;
	private DB mapDB;
	private Set<E> set = new HashSet<>();

	@Override
	public int size()
	{
		return set.size();
	}

	@Override
	public boolean isEmpty()
	{
		return set.isEmpty();
	}

	@Override
	public boolean contains(Object o)
	{
		return set.contains(o);
	}

	@Override
	public Iterator<E> iterator()
	{
		return set.iterator();
	}

	@Override
	public Object[] toArray()
	{
		return set.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a)
	{
		return set.toArray(a);
	}

	@Override
	public boolean add(E obj)
	{
		if (set.size() == THRESHOLD)
		{
			File dbFile;
			try
			{
				dbFile = File.createTempFile("mapdb", "temp");
			}
			catch (IOException e)
			{
				throw new UncheckedIOException(e);
			}

			Set<E> temp = new HashSet<>(set);
			mapDB = DBMaker.newFileDB(dbFile).deleteFilesAfterClose().transactionDisable().make();
			set = mapDB.createHashSet("set").make();
			set.addAll(temp);
		}

		return set.add(obj);
	}

	@Override
	public boolean remove(Object o)
	{
		return set.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		return set.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends E> c)
	{
		return set.addAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		return set.retainAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		return set.removeAll(c);
	}

	@Override
	public void clear()
	{
		set.clear();
	}

	@Override
	public void close() throws IOException
	{
		if (mapDB != null)
		{
			mapDB.close();
		}
	}

}
