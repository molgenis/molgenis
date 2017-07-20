package org.molgenis.security.acl;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;

/**
 * ACL cache based on Caffeine that logs cache stats every minute.
 */
@Component
class AclCache implements Cache
{
	private static final Logger LOG = LoggerFactory.getLogger(AclCache.class);
	private static final int MAX_CACHE_SIZE = 10000;

	private final com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCaffeineCache;
	private final Cache delegateCache;

	AclCache()
	{
		this.nativeCaffeineCache = Caffeine.newBuilder().maximumSize(MAX_CACHE_SIZE).recordStats().build();
		this.delegateCache = new CaffeineCache("aclCache", nativeCaffeineCache);
	}

	@Override
	public String getName()
	{
		return delegateCache.getName();
	}

	@Override
	public Object getNativeCache()
	{
		return delegateCache.getNativeCache();
	}

	@Override
	public ValueWrapper get(Object key)
	{
		return delegateCache.get(key);
	}

	@Override
	public <T> T get(Object key, Class<T> type)
	{
		return delegateCache.get(key, type);
	}

	@Override
	public <T> T get(Object key, Callable<T> valueLoader)
	{
		return delegateCache.get(key, valueLoader);
	}

	@Override
	public void put(Object key, Object value)
	{
		delegateCache.put(key, value);
	}

	@Override
	public ValueWrapper putIfAbsent(Object key, Object value)
	{
		return delegateCache.putIfAbsent(key, value);
	}

	@Override
	public void evict(Object key)
	{
		delegateCache.evict(key);
	}

	@Override
	public void clear()
	{
		delegateCache.clear();
	}

	/**
	 * Logs cumulative cache statistics for all known caches.
	 */
	@Scheduled(fixedRate = 60000)
	private void logStatistics()
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug(nativeCaffeineCache.stats().toString());
		}
	}
}
