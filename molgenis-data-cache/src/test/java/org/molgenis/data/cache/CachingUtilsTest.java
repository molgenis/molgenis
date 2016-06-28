package org.molgenis.data.cache;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.molgenis.test.data.EntityTestHarness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.cache.CachingUtils.*;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { CachingUtilsTest.Config.class })
public class CachingUtilsTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private EntityMetaDataFactory entityMetaFactory;

	@Autowired
	private AttributeMetaDataFactory attrMetaFactory;

	@Autowired
	private EntityTestHarness entityTestHarness;

	private EntityMetaData entityMetaData;
	private EntityManager entityManager;
	private Entity hydratedEntity;
	private Map<String, Object> dehydratedEntity;

	@BeforeClass
	public void beforeClass()
	{
		entityMetaData = entityTestHarness.createDynamicTestEntityMetaData();

		EntityMetaData refEntityMetaData = entityTestHarness.createDynamicRefEntityMetaData();
		List<Entity> refEntities = entityTestHarness.createTestRefEntities(refEntityMetaData, 1);

		// create
		hydratedEntity = entityTestHarness.createTestEntities(entityMetaData, 1, refEntities)
				.collect(toList()).get(0);

		// mock dehydrated entity
		dehydratedEntity = newHashMap();
		dehydratedEntity.put("identifier", 1);
		dehydratedEntity.put("height", 170.50);
		dehydratedEntity.put("person", xrefEntity);

		// mock entity manager
		entityManager = when(mock(EntityManager.class).create(entityMetaData)).thenReturn(hydratedEntity).getMock();
	}

	@Test
	public void hydrateTest()
	{
		assertEquals(hydrate(dehydratedEntity, entityMetaData, entityManager), hydratedEntity);
	}

	@Test
	public void dehydrateTest()
	{
		assertEquals(dehydrate(hydratedEntity), dehydratedEntity);
	}

	@Test
	public void generateCacheKeyTest()
	{
		String expectedKey = "TestEntity__id1";
		assertEquals(generateCacheKey("TestEntity", "id1"), expectedKey);

		String expectedKey2 = "TestEntity__2";
		assertEquals(generateCacheKey("TestEntity", 2), expectedKey2);
	}

	@Configuration
	@ComponentScan(basePackages = "org.molgenis.test.data")
	public static class Config
	{
	}
}
