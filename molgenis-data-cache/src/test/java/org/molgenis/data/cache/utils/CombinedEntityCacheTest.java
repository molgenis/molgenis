package org.molgenis.data.cache.utils;

import com.google.common.cache.Cache;
import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityKey;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.Optional;

import static java.util.Optional.empty;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

public class CombinedEntityCacheTest extends AbstractMockitoTest
{
	private CombinedEntityCache entityCache;
	@Mock
	private EntityHydration entityHydration;
	@Mock
	private Cache<EntityKey, Optional<Map<String, Object>>> cache;
	@Mock
	EntityType entityType;
	@Mock
	Entity entity;
	@Mock
	Map<String, Object> dehydratedEntity;

	@BeforeMethod
	public void beforeMethod()
	{
		when(entityType.getId()).thenReturn("TestEntity");
		entityCache = new CombinedEntityCache(entityHydration, cache);
	}

	@Test
	public void getIfPresentIntegerIdEntityNotPresentInCache()
	{
		when(cache.getIfPresent(EntityKey.create("TestEntity", 123))).thenReturn(null);
		assertEquals(entityCache.getIfPresent(entityType, 123), null);
	}

	@Test
	public void getIfPresentIntegerIdDeletionLoggedInCache()
	{
		when(cache.getIfPresent(EntityKey.create("TestEntity", 123))).thenReturn(empty());
		assertEquals(entityCache.getIfPresent(entityType, 123), empty());
	}

	@Test
	public void getIfPresentIntegerIdEntityPresentInCache()
	{
		when(cache.getIfPresent(EntityKey.create("TestEntity", 123))).thenReturn(Optional.of(dehydratedEntity));
		when(entityHydration.hydrate(dehydratedEntity, entityType)).thenReturn(entity);
		assertSame(entityCache.getIfPresent(entityType, 123).get(), entity);
	}
}
