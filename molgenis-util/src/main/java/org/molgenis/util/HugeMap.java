package org.molgenis.util;

import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HugeMap<K, V> implements Map<K, V>, Closeable
{
	protected static final int THRESHOLD = 10000;
	private DB mapDB;
	private Map<K, V> map;
	private final Map<K, V> hashMap = new HashMap<>();

	@Override
	public int size()
	{
		if (map == null) return hashMap.size();
		return map.size();
	}

	@Override
	public boolean isEmpty()
	{
		if (map == null) return hashMap.isEmpty();
		return map.isEmpty();
	}

	@Override
	public boolean containsKey(Object key)
	{
		if (map == null) return hashMap.containsKey(key);
		return map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value)
	{
		if (map == null) return hashMap.containsValue(value);
		return map.containsValue(value);
	}

	@Override
	public V get(Object key)
	{
		if (map == null) return hashMap.get(key);
		return map.get(key);
	}

	@Override
	public V put(K key, V value)
	{
		if (hashMap.size() == THRESHOLD)
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
			map = mapDB.createHashMap("map").make();
			map.putAll(hashMap);
			hashMap.clear();
		}

		if (map == null)
		{
			return hashMap.put(key, value);
		}

		return map.put(key, value);
	}

	@Override
	public V remove(Object key)
	{
		if (map == null) return hashMap.remove(key);
		return map.remove(key);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m)
	{
		m.forEach(this::put);
	}

	@Override
	public void clear()
	{
		if (map == null)
		{
			hashMap.clear();
		}
		else
		{
			map.clear();
		}
	}

	@Override
	public Set<K> keySet()
	{
		if (map == null) return hashMap.keySet();
		return map.keySet();
	}

	@Override
	public Collection<V> values()
	{
		if (map == null) return hashMap.values();
		return map.values();
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet()
	{
		if (map == null) return hashMap.entrySet();
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

}
