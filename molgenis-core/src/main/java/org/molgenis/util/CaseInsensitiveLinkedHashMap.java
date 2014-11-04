package org.molgenis.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

/**
 * LinkedHashMap with a string as key. The key is caseinsensitive.
 * 
 * 
 * 
 * @param <V>
 */
public class CaseInsensitiveLinkedHashMap<V> extends LinkedHashMap<String, V>
{
	private static final long serialVersionUID = -5765647414721292250L;
	private final Map<String, String> keyMap = new LinkedHashMap<String, String>();

	@Override
	public V put(String key, V value)
	{
		if (key == null) throw new IllegalArgumentException("key is null");
		keyMap.put(key.toLowerCase(), key);
		return super.put(key.toLowerCase(), value);
	}

	@Override
	public V remove(Object key)
	{
		if (!(key instanceof String))
		{
			return null;
		}

		String keyStr = ((String) key).toLowerCase();
		keyMap.remove(keyStr);

		return super.remove(keyStr);
	}

	@Override
	public V get(Object key)
	{
		if (!(key instanceof String))
		{
			return null;
		}

		return super.get(((String) key).toLowerCase());
	}

	@Override
	public Set<String> keySet()
	{
		// Return the original keys
		Set<String> keys = super.keySet();
		Set<String> originalKeys = Sets.newLinkedHashSetWithExpectedSize(keys.size());
		for (String key : keys)
		{
			originalKeys.add(keyMap.get(key));
		}

		return originalKeys;
	}

	@Override
	public Set<java.util.Map.Entry<String, V>> entrySet()
	{
		// We broke this method
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof CaseInsensitiveLinkedHashMap<?>))
		{
			return false;
		}
		CaseInsensitiveLinkedHashMap<?> other = (CaseInsensitiveLinkedHashMap<?>) o;
		if (!keySet().equals(other.keySet()))
		{
			return false;
		}
		for (String key : keySet())
		{
			Object value = get(key);
			if (value == null)
			{
				if (other.get(key) != null)
				{
					return false;
				}
			}
			else
			{
				if (!get(key).equals(other.get(key)))
				{
					return false;
				}
			}
		}
		return true;
	}

}
