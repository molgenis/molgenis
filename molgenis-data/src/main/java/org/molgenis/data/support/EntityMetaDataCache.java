package org.molgenis.data.support;

import java.util.HashMap;
import java.util.Map;

import org.molgenis.data.EntityMetaData;

public class EntityMetaDataCache
{
	private static final Map<String, EntityMetaData> cache = new HashMap<String, EntityMetaData>();

	public static void add(EntityMetaData entityMetaData)
	{
		cache.put(entityMetaData.getName(), entityMetaData);
	}

	public static EntityMetaData get(String name)
	{
		return cache.get(name);
	}
}
