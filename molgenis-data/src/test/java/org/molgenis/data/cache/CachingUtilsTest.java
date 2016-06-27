package org.molgenis.data.cache;

import org.molgenis.data.EntityManager;
import org.molgenis.data.meta.model.EntityMetaData;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.molgenis.data.cache.CachingUtils.generateCacheKey;
import static org.molgenis.data.cache.CachingUtils.hydrate;
import static org.testng.Assert.assertEquals;

public class CachingUtilsTest
{
	private EntityMetaData entityMetaData;
	private EntityManager entityManager;

	@BeforeClass
	public void beforeClass()
	{
		entityManager = mock(EntityManager.class);
		entityMetaData = 
	}

	@Test
	public void generateCacheKeyTest()
	{
		String expectedKey = "TestEntity__id1";
		assertEquals(generateCacheKey("TestEntity", "id1"), expectedKey);

		String expectedKey2 = "TestEntity__2";
		assertEquals(generateCacheKey("TestEntity", 2), expectedKey2);
	}

	@Test
	public void createCacheTest()
	{

	}

	@Test
	public void hydrateTest()
	{

		hydrate(null, entityMetaData, entityManager);
	}
}
