package org.molgenis.util;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
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
	private final DB mapDB;
	private final Set<E> mapDBSet;

	public HugeSet()
	{
		File dbFile;
		try
		{
			dbFile = File.createTempFile("mapdb", "temp");
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}

		mapDB = DBMaker.newFileDB(dbFile).deleteFilesAfterClose().transactionDisable().make();
		mapDBSet = mapDB.createHashSet("set").make();
	}

	@Override
	public int size()
	{
		return mapDBSet.size();
	}

	@Override
	public boolean isEmpty()
	{
		return mapDBSet.isEmpty();
	}

	@Override
	public boolean contains(Object o)
	{
		return mapDBSet.contains(o);
	}

	@Override
	public Iterator<E> iterator()
	{
		return mapDBSet.iterator();
	}

	@Override
	public Object[] toArray()
	{
		return mapDBSet.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a)
	{
		return mapDBSet.toArray(a);
	}

	@Override
	public boolean add(E e)
	{
		return mapDBSet.add(e);
	}

	@Override
	public boolean remove(Object o)
	{
		return mapDBSet.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		return mapDBSet.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends E> c)
	{
		return mapDBSet.addAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		return mapDBSet.retainAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		return mapDBSet.removeAll(c);
	}

	@Override
	public void clear()
	{
		mapDBSet.clear();
	}

	@Override
	public void close() throws IOException
	{
		mapDB.close();
	}

}
