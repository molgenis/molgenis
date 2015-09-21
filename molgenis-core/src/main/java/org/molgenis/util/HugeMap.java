package org.molgenis.util;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.mapdb.DB;
import org.mapdb.DBMaker;

public class HugeMap<K, V> implements Map<K, V>, Closeable
{
	private static final int THRESHOLD = 10000;
	private DB mapDB;
	private Map<K, V> map = new HashMap<>();

	@Override
	public int size()
	{
		return map.size();
	}

	@Override
	public boolean isEmpty()
	{
		return map.isEmpty();
	}

	@Override
	public boolean containsKey(Object key)
	{
		return map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value)
	{
		return map.containsValue(value);
	}

	@Override
	public V get(Object key)
	{
		return map.get(key);
	}

	@Override
	public V put(K key, V value)
	{
		if (map.size() == THRESHOLD)
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

			Map<K, V> temp = new HashMap<>(map);
			mapDB = DBMaker.newFileDB(dbFile).deleteFilesAfterClose().transactionDisable().make();
			map = mapDB.createHashMap("map").make();
			map.putAll(temp);
		}

		return map.put(key, value);
	}

	@Override
	public V remove(Object key)
	{
		return map.remove(key);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m)
	{
		map.putAll(m);
	}

	@Override
	public void clear()
	{
		map.clear();
	}

	@Override
	public Set<K> keySet()
	{
		return map.keySet();
	}

	@Override
	public Collection<V> values()
	{
		return map.values();
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet()
	{
		return map.entrySet();
	}

	@Override
	public void close() throws IOException
	{
		if (mapDB != null)
		{
			mapDB.close();
		}
	}

	@Override
	public String toString()
	{
		return new HashMap<K, V>(this.map).toString();
	}

}
