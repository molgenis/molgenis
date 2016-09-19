package org.molgenis.data.cache.utils;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.support.EntityWithComputedAttributes;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.molgenis.test.data.EntityTestHarness;
import org.molgenis.util.EntityUtils;
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

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.data.EntityManager.CreationMode.NO_POPULATE;
import static org.molgenis.test.data.EntityTestHarness.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@ContextConfiguration(classes = { EntityHydrationTest.Config.class })
public class EntityHydrationTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private EntityTestHarness entityTestHarness;

	private EntityMetaData entityMetaData;
	private Entity hydratedEntity;
	private Map<String, Object> dehydratedEntity;
	private EntityHydration entityHydration;

	@Captor
	private ArgumentCaptor<EntityMetaData> entityMetaDataArgumentCaptor;

	@BeforeClass
	public void beforeClass() throws ParseException
	{
		initMocks(this);
		// create metadata
		entityMetaData = entityTestHarness.createDynamicTestEntityMetaData();

		// create referenced entities
		EntityMetaData refEntityMetaData = entityTestHarness.createDynamicRefEntityMetaData();
		List<Entity> refEntities = entityTestHarness.createTestRefEntities(refEntityMetaData, 1);

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
		dehydratedEntity.put(ATTR_DECIMAL, 0.123);
		dehydratedEntity.put(ATTR_HTML, null);
		dehydratedEntity.put(ATTR_HYPERLINK, "http://www.molgenis.org");
		dehydratedEntity.put(ATTR_LONG, 0L);
		dehydratedEntity.put(ATTR_INT, 10);
		dehydratedEntity.put(ATTR_SCRIPT, "/bin/blaat/script.sh");
		dehydratedEntity.put(ATTR_XREF, "0");
		dehydratedEntity.put(ATTR_MREF, singletonList("0"));

		// mock entity manager
		EntityManager entityManager = when(mock(EntityManager.class).create(entityMetaData, NO_POPULATE))
				.thenReturn(new EntityWithComputedAttributes(new DynamicEntity(entityMetaData))).getMock();
		when(entityManager.getReference(entityMetaDataArgumentCaptor.capture(), eq("0")))
				.thenReturn(refEntities.get(0));
		when(entityManager.getReferences(entityMetaDataArgumentCaptor.capture(), eq(newArrayList("0"))))
				.thenReturn(refEntities);
		entityHydration = new EntityHydration(entityManager);
	}

	@Test
	public void hydrateTest()
	{
		Entity actualHydratedEntity = entityHydration.hydrate(dehydratedEntity, entityMetaData);
		assertTrue(EntityUtils.equals(actualHydratedEntity, hydratedEntity));
		// check that it has retrieved references of type TypeTestRef
		assertTrue(entityMetaDataArgumentCaptor.getAllValues().stream()
				.allMatch(emd -> emd.getName().equals("test_TypeTestRefDynamic")));
	}

	@Test
	public void dehydrateTest()
	{
		Map<String, Object> actualDehydratedEntity = entityHydration.dehydrate(hydratedEntity);
		assertEquals(actualDehydratedEntity, dehydratedEntity);
	}

	@Configuration
	@ComponentScan(basePackages = "org.molgenis.test.data")
	public static class Config
	{
	}
}
