package org.molgenis.util;

import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Set that can contain a huge amount of data by storing it on disk.
 *
 * @param <E>
 */
public class HugeSet<E> implements Set<E>, Closeable
{
	protected static final int THRESHOLD = 10000;
	private DB mapDB;
	private Set<E> set;
	private final HashSet<E> hashSet = new HashSet<>();

	@Override
	public int size()
	{
		if (set == null) return hashSet.size();
		return set.size();
	}

	@Override
	public boolean isEmpty()
	{
		if (set == null) return hashSet.isEmpty();
		return set.isEmpty();
	}

	@Override
	public boolean contains(Object o)
	{
		if (set == null) return hashSet.contains(o);
		return set.contains(o);
	}

	@Override
	public Iterator<E> iterator()
	{
		if (set == null) return hashSet.iterator();
		return set.iterator();
	}

	@Override
	public Object[] toArray()
	{
		if (set == null) return hashSet.toArray();
		return set.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a)
	{
		if (set == null) return hashSet.toArray(a);
		return set.toArray(a);
	}

	@Override
	public boolean add(E obj)
	{
		if (hashSet.size() == THRESHOLD)
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

			mapDB = DBMaker.newFileDB(dbFile).deleteFilesAfterClose().transactionDisable().make();
			set = mapDB.createHashSet("set").make();
			set.addAll(hashSet);
			hashSet.clear();
		}

		if (set == null)
		{
			return hashSet.add(obj);
		}

		return set.add(obj);
	}

	@Override
	public boolean remove(Object o)
	{
		if (set == null) return hashSet.remove(o);
		return set.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		if (set == null) return hashSet.containsAll(c);
		return set.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends E> c)
	{
		c.forEach(this::add);
		return true;
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		if (set == null) return hashSet.retainAll(c);
		return set.retainAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		if (set == null) return hashSet.removeAll(c);
		return set.removeAll(c);
	}

	@Override
	public void clear()
	{
		if (set == null)
		{
			hashSet.clear();
		}
		else
		{
			set.clear();
		}
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
