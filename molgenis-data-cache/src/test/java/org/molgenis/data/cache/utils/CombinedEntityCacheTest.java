package org.molgenis.data.cache.utils;

import com.google.common.cache.Cache;
import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityKey;
import org.molgenis.data.meta.model.EntityMetaData;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.Optional;

import static java.util.Optional.empty;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

public class CombinedEntityCacheTest
{
	private CombinedEntityCache entityCache;
	@Mock
	private EntityHydration entityHydration;
	@Mock
	private Cache<EntityKey, Optional<Map<String, Object>>> cache;
	@Mock
	EntityMetaData entityMetaData;
	@Mock
	Entity entity;
	@Mock
	Map<String, Object> dehydratedEntity;

	@BeforeClass
	public void beforeClass()
	{
		initMocks(this);
		entityCache = new CombinedEntityCache(entityHydration, cache);
	}

	@BeforeMethod
	public void beforeMethod()
	{
		reset(entityHydration, cache, entityMetaData);
		when(entityMetaData.getName()).thenReturn("TestEntity");
	}

	@Test
	public void getIfPresentIntegerIdEntityNotPresentInCache()
	{
		when(cache.getIfPresent(EntityKey.create("TestEntity", 123))).thenReturn(null);
		assertEquals(entityCache.getIfPresent(entityMetaData, 123), null);
	}

	@Test
	public void getIfPresentIntegerIdDeletionLoggedInCache()
	{
		when(cache.getIfPresent(EntityKey.create("TestEntity", 123))).thenReturn(empty());
		assertEquals(entityCache.getIfPresent(entityMetaData, 123), empty());
	}

	@Test
	public void getIfPresentIntegerIdEntityPresentInCache()
	{
		when(cache.getIfPresent(EntityKey.create("TestEntity", 123))).thenReturn(Optional.of(dehydratedEntity));
		when(entityHydration.hydrate(dehydratedEntity, entityMetaData)).thenReturn(entity);
		assertSame(entityCache.getIfPresent(entityMetaData, 123).get(), entity);
	}
}
