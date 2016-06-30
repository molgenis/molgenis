package org.molgenis.data.cache.utils;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.molgenis.test.data.EntityTestHarness;
import org.molgenis.util.MolgenisDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.cache.utils.CachingUtils.*;
import static org.molgenis.test.data.EntityTestHarness.*;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { CachingUtilsTest.Config.class })
public class CachingUtilsTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private EntityTestHarness entityTestHarness;

	private EntityMetaData entityMetaData;
	private EntityManager entityManager;
	private Entity hydratedEntity;
	private Map<String, Object> dehydratedEntity;

	@BeforeClass
	public void beforeClass() throws ParseException
	{
		// create metadata
		entityMetaData = entityTestHarness.createDynamicTestEntityMetaData();

		// create referenced entities
		List<Entity> refEntities = entityTestHarness
				.createTestRefEntities(entityTestHarness.createDynamicRefEntityMetaData(), 1);

		// create hydrated entity
		hydratedEntity = entityTestHarness.createTestEntities(entityMetaData, 1, refEntities).collect(toList()).get(0);

		Date date = MolgenisDateFormat.getDateFormat().parse("2012-12-21");
		Date dateTime = MolgenisDateFormat.getDateTimeFormat().parse("1985-08-12T11:12:13+0500");

		// create dehydrated entity
		dehydratedEntity = newHashMap();
		dehydratedEntity.put(ATTR_ID, "0");
		dehydratedEntity.put(ATTR_STRING, "string1");
		dehydratedEntity.put(ATTR_BOOL, true);
		dehydratedEntity.put(ATTR_CATEGORICAL, "0");
		dehydratedEntity.put(ATTR_CATEGORICAL_MREF, singletonList("0"));
		dehydratedEntity.put(ATTR_DATE, date);
		dehydratedEntity.put(ATTR_DATETIME, dateTime);
		dehydratedEntity.put(ATTR_EMAIL, "this.is@mail.address");
		dehydratedEntity.put(ATTR_DECIMAL, 1.123);
		dehydratedEntity.put(ATTR_HTML, "<html>where is my head and where is my body</html>");
		dehydratedEntity.put(ATTR_HYPERLINK, "http://www.molgenis.org");
		dehydratedEntity.put(ATTR_LONG, new Long(1000000));
		dehydratedEntity.put(ATTR_INT, 18);
		dehydratedEntity.put(ATTR_SCRIPT, "/bin/blaat/script.sh");
		dehydratedEntity.put(ATTR_XREF, "0");
		dehydratedEntity.put(ATTR_MREF, singletonList("0"));

		// mock entity manager
		entityManager = when(mock(EntityManager.class).create(entityMetaData)).thenReturn(hydratedEntity).getMock();
	}

	@Test
	public void hydrateTest()
	{
		Entity actualHydratedEntity = hydrate(dehydratedEntity, entityMetaData, entityManager);
		assertEquals(actualHydratedEntity, hydratedEntity);
	}

	@Test
	public void dehydrateTest()
	{
		Map<String, Object> actualDehydratedEntity = dehydrate(hydratedEntity);
		assertEquals(actualDehydratedEntity, dehydratedEntity);
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
