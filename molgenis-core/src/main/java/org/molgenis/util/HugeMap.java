package org.molgenis.util;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.mapdb.DB;
import org.mapdb.DBMaker;

public class HugeMap<K, V> implements Map<K, V>, Closeable
{
	private final DB mapDB;
	private final Map<K, V> mapDBMap;

	public HugeMap()
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
		mapDBMap = mapDB.createHashMap("map").make();
	}

	@Override
	public int size()
	{
		return mapDBMap.size();
	}

	@Override
	public boolean isEmpty()
	{
		return mapDBMap.isEmpty();
	}

	@Override
	public boolean containsKey(Object key)
	{
		return mapDBMap.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value)
	{
		return mapDBMap.containsValue(value);
	}

	@Override
	public V get(Object key)
	{
		return mapDBMap.get(key);
	}

	@Override
	public V put(K key, V value)
	{
		return mapDBMap.put(key, value);
	}

	@Override
	public V remove(Object key)
	{
		return mapDBMap.remove(key);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m)
	{
		mapDBMap.putAll(m);
	}

	@Override
	public void clear()
	{
		mapDBMap.clear();
	}

	@Override
	public Set<K> keySet()
	{
		return mapDBMap.keySet();
	}

	@Override
	public Collection<V> values()
	{
		return mapDBMap.values();
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet()
	{
		return mapDBMap.entrySet();
	}

	@Override
	public void close() throws IOException
	{
		mapDB.close();
	}

	@Override
	public String toString()
	{
		return new HashMap<K, V>(this.mapDBMap).toString();
	}

}
