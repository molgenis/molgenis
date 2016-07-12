package org.molgenis.data.cache.utils;

import com.google.common.cache.Cache;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static autovalue.shaded.com.google.common.common.collect.Lists.newArrayList;
import static org.testng.Assert.assertEquals;

public class CombinedEntityCacheTest
{
	private CombinedEntityCache entityCache;
	@Mock
	private EntityHydration entityHydration;
	@Mock
	private Cache<String, Optional<Map<String, Object>>> cache;

	@BeforeClass
	public void beforeClass()
	{
		MockitoAnnotations.initMocks(this);
		entityCache = new CombinedEntityCache(entityHydration, cache);
	}

	@Test
	public void generateCacheKeyTest()
	{
		String expectedKey = "TestEntity__id1";
		assertEquals(entityCache.generateCacheKey("TestEntity", "id1"), expectedKey);

		String expectedKey2 = "TestEntity__2";
		assertEquals(entityCache.generateCacheKey("TestEntity", 2), expectedKey2);
	}

	@Test
	public void testGetKeyFilter()
	{
		Predicate<String> filter = entityCache.getKeyFilter("TestEntity");

		List<String> testEntityKeys = newArrayList("TestEntity__id1", "TestEntity__id2", "org_TestEntity__id3",
				"MyTestEntity__id1").stream().filter(filter).collect(Collectors.toList());

		assertEquals(testEntityKeys, newArrayList("TestEntity__id1", "TestEntity__id2"));

		String expectedKey2 = "TestEntity__2";
		assertEquals(entityCache.generateCacheKey("TestEntity", 2), expectedKey2);
	}
}
