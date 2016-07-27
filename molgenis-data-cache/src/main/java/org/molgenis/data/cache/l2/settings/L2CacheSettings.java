package org.molgenis.data.cache.l2.settings;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.StaticEntity;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.molgenis.data.cache.l2.settings.L2CacheSettingsMetaData.*;

public class L2CacheSettings extends StaticEntity
{
	public L2CacheSettings(Entity entity)
	{
		super(entity);
	}

	public L2CacheSettings(EntityMetaData entityMeta)
	{
		super(entityMeta);
	}

	public L2CacheSettings(String id, EntityMetaData entityMeta)
	{
		super(entityMeta);
		setId(id);
	}

	private void setId(String id)
	{
		set(ID, id);
	}

	public void setCachedEntity(EntityMetaData entity)
	{
		set(CACHED_ENTITY, entity);
	}

	public EntityMetaData getCachedEntity()
	{
		return getEntity(CACHED_ENTITY, EntityMetaData.class);
	}

	public String getCacheBuilderSpecString()
	{
		StringBuilder result = new StringBuilder();
		result.append("concurrencyLevel=");
		result.append(getConcurrencyLevel());
		result.append(",initialCapacity=");
		result.append(getInitialCapacity());
		if (getMaximumSize() != null)
		{
			result.append(",maximumSize=");
			result.append(getMaximumSize());
		}
		if (!isEmpty(getExpireAfterAccess()))
		{
			result.append(",expireAfterAccess=");
			result.append(getExpireAfterAccess());
		}
		if (!isEmpty(getExpireAfterWrite()))
		{
			result.append(",expireAfterWrite=");
			result.append(getExpireAfterWrite());
		}
		if (!isEmpty(getRefreshAfterWrite()))
		{
			result.append(",refreshAfterWrite=");
			result.append(getRefreshAfterWrite());
		}
		if (isWeakKeys())
		{
			result.append(",weakKeys");
		}
		switch (getValueReferenceType())
		{
			case Soft:
				result.append(",softValues");
				break;
			case Weak:
				result.append(",weakValues");
				break;
		}
		if (isRecordStats())
		{
			result.append(",recordStats");
		}
		return result.toString();
	}

	public int getConcurrencyLevel()
	{
		return getInt(CONCURRENCY_LEVEL);
	}

	public void setConcurrencyLevel(int concurrencyLevel)
	{
		set(CONCURRENCY_LEVEL, concurrencyLevel);
	}

	public int getInitialCapacity()
	{
		return getInt(INITIAL_CAPACITY);
	}

	public void setInitialCapacity(int initialCapacity)
	{
		set(INITIAL_CAPACITY, initialCapacity);
	}

	public Long getMaximumSize()
	{
		return getLong(MAXIMUM_SIZE);
	}

	public void setMaximumSize(Long maximumSize)
	{
		set(MAXIMUM_SIZE, maximumSize);
	}

	public String getExpireAfterAccess()
	{
		return getString(EXPIRE_AFTER_ACCESS);
	}

	public void setExpireAfterAccess(String duration)
	{
		set(EXPIRE_AFTER_ACCESS, duration);
	}

	public String getExpireAfterWrite()
	{
		return getString(EXPIRE_AFTER_WRITE);
	}

	public void setExpireAfterWrite(String duration)
	{
		set(EXPIRE_AFTER_WRITE, duration);
	}

	public String getRefreshAfterWrite()
	{
		return getString(REFRESH_AFTER_WRITE);
	}

	public void setRefreshAfterWrite(String duration)
	{
		set(REFRESH_AFTER_WRITE, duration);
	}

	public boolean getWeakKeys()
	{
		return getBoolean(WEAK_KEYS);
	}

	public void setWeakKeys(boolean weakKeys)
	{
		set(WEAK_KEYS, weakKeys);
	}

	public boolean isWeakKeys()
	{
		return getBoolean(WEAK_KEYS);
	}

	public ValueReferenceType getValueReferenceType()
	{
		return ValueReferenceType.valueOf(getString(VALUE_REFERENCE_TYPE));
	}

	public void setValueReferenceType(ValueReferenceType valueReferenceType)
	{
		set(VALUE_REFERENCE_TYPE, valueReferenceType.toString());
	}

	enum ValueReferenceType
	{
		Strong, Weak, Soft
	}

	public boolean isRecordStats()
	{
		return getBoolean(RECORD_STATS);
	}

	public void setRecordStats(boolean recordStats)
	{
		set(RECORD_STATS, recordStats);
	}
}
