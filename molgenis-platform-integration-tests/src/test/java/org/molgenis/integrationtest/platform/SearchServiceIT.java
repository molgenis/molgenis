package org.molgenis.integrationtest.platform;

import org.molgenis.data.Entity;
import org.molgenis.data.EntitySelfXrefTestHarness;
import org.molgenis.data.EntityTestHarness;
import org.molgenis.data.index.IndexingMode;
import org.molgenis.data.index.SearchService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { PlatformITConfig.class })
public class SearchServiceIT extends AbstractTestNGSpringContextTests
{
	private final Logger LOG = LoggerFactory.getLogger(SearchServiceIT.class);

	private static EntityType entityTypeDynamic;
	private static EntityType refEntityTypeDynamic;
	private static EntityType selfXrefEntityType;

	@Autowired
	private EntityTestHarness testHarness;
	@Autowired
	private EntitySelfXrefTestHarness entitySelfXrefTestHarness;
	@Autowired
	private SearchService searchService;

	@BeforeMethod
	public void setUp()
	{
		searchService.refreshIndex();
		refEntityTypeDynamic = testHarness.createDynamicRefEntityType();
		entityTypeDynamic = testHarness.createDynamicTestEntityType(refEntityTypeDynamic);
		selfXrefEntityType = entitySelfXrefTestHarness.createDynamicEntityType();

		searchService.createMappings(refEntityTypeDynamic);
		searchService.createMappings(entityTypeDynamic);
	}

	@AfterMethod
	public void afterMethod()
	{
		searchService.delete(entityTypeDynamic);
		searchService.delete(refEntityTypeDynamic);
		searchService.refreshIndex();
	}

	@Test(singleThreaded = true)
	public void testIndex() throws InterruptedException
	{
		List<Entity> entities = createDynamic(2).collect(toList());
		searchService.index(entities.stream(), entityTypeDynamic, IndexingMode.ADD);
		searchService.refreshIndex();

		assertEquals(searchService.count(entityTypeDynamic), 2);
	}

	@Test(singleThreaded = true)
	public void testCount()
	{
		List<Entity> entities = createDynamic(2).collect(toList());
		searchService.index(entities.stream(), entityTypeDynamic, IndexingMode.ADD);
		searchService.refreshIndex();

		assertEquals(searchService.count(new QueryImpl<>(), entityTypeDynamic), 2);
		assertEquals(searchService.count(entityTypeDynamic), 2);
	}

	@Test(singleThreaded = true)
	public void testDelete()
	{
		Entity entity = createDynamic(1).findFirst().get();
		searchService.index(entity, entityTypeDynamic, IndexingMode.ADD);
		searchService.refreshIndex();

		searchService.delete(entity, entityTypeDynamic);
		searchService.refreshIndex();
		assertEquals(searchService.count(entityTypeDynamic), 0);
	}

	//
	//	@Test(singleThreaded = true)
	//	public void testDeleteById()
	//	{
	//		Entity entity = createDynamic(1).findFirst().get();
	//		dataService.add(entityTypeDynamic.getId(), entity);
	//		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//		assertPresent(entityTypeDynamic, entity);
	//
	//		dataService.deleteById(entityTypeDynamic.getId(), entity.getIdValue());
	//		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//		assertNotPresent(entity);
	//	}
	//
	//	@Test(singleThreaded = true)
	//	public void testDeleteStream()
	//	{
	//		List<Entity> entities = createDynamic(2).collect(toList());
	//		dataService.add(entityTypeDynamic.getId(), entities.stream());
	//		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//		assertEquals(dataService.count(entityTypeDynamic.getId(), new QueryImpl<>()), entities.size());
	//
	//		dataService.delete(entityTypeDynamic.getId(), entities.stream());
	//		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//		assertEquals(dataService.count(entityTypeDynamic.getId(), new QueryImpl<>()), 0);
	//	}
	//
	//	@Test(singleThreaded = true)
	//	public void testDeleteAll()
	//	{
	//		List<Entity> entities = createDynamic(5).collect(toList());
	//		dataService.add(entityTypeDynamic.getId(), entities.stream());
	//		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//		assertEquals(dataService.count(entityTypeDynamic.getId(), new QueryImpl<>()), entities.size());
	//
	//		dataService.deleteAll(entityTypeDynamic.getId());
	//		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//		assertEquals(dataService.count(entityTypeDynamic.getId(), new QueryImpl<>()), 0);
	//	}
	//
	//	@Test(singleThreaded = true)
	//	public void testFindAllEmpty()
	//	{
	//		Stream<Entity> retrieved = dataService.findAll(entityTypeDynamic.getId());
	//		assertEquals(retrieved.count(), 0);
	//	}
	//
	//	@Test(singleThreaded = true)
	//	public void testFindAll()
	//	{
	//		List<Entity> entities = createDynamic(5).collect(toList());
	//		dataService.add(entityTypeDynamic.getId(), entities.stream());
	//		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//		Stream<Entity> retrieved = dataService.findAll(entityTypeDynamic.getId());
	//		assertEquals(retrieved.count(), entities.size());
	//	}
	//
	//	@Test(singleThreaded = true)
	//	public void testFindAllTyped()
	//	{
	//		List<Entity> entities = createDynamic(1).collect(toList());
	//		dataService.add(entityTypeDynamic.getId(), entities.stream());
	//		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//		Supplier<Stream<Entity>> retrieved = () -> dataService.findAll(entityTypeDynamic.getId(), Entity.class);
	//		assertEquals(retrieved.get().count(), 1);
	//		assertEquals(retrieved.get().iterator().next().getIdValue(), entities.get(0).getIdValue());
	//	}
	//
	//	@Test(singleThreaded = true)
	//	public void testFindAllByIds()
	//	{
	//		List<Entity> entities = createDynamic(5).collect(toList());
	//		dataService.add(entityTypeDynamic.getId(), entities.stream());
	//		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//		Stream<Object> ids = Stream.concat(entities.stream().map(Entity::getIdValue), of("bogus"));
	//		Stream<Entity> retrieved = dataService.findAll(entityTypeDynamic.getId(), ids);
	//		assertEquals(retrieved.count(), entities.size());
	//	}
	//
	//	@Test(singleThreaded = true)
	//	public void testFindAllByIdsTyped()
	//	{
	//		List<Entity> entities = createStatic(5).collect(toList());
	//		dataService.add(entityTypeStatic.getId(), entities.stream());
	//		waitForIndexToBeStable(entityTypeStatic, indexService, LOG);
	//
	//		Supplier<Stream<TestEntityStatic>> retrieved = () -> dataService.findAll(entityTypeStatic.getId(),
	//						Stream.concat(entities.stream().map(Entity::getIdValue), of("bogus")), TestEntityStatic.class);
	//		assertEquals(retrieved.get().count(), entities.size());
	//		assertEquals(retrieved.get().iterator().next().getId(), entities.get(0).getIdValue());
	//		assertEquals(retrieved.get().iterator().next().getIdValue(), entities.get(0).getIdValue());
	//	}
	//
	//	@Test(singleThreaded = true)
	//	public void testFindAllStreamFetch()
	//	{
	//		List<Entity> entities = createDynamic(5).collect(toList());
	//		dataService.add(entityTypeDynamic.getId(), entities.stream());
	//		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//		Stream<Object> ids = concat(entities.stream().map(Entity::getIdValue), of("bogus"));
	//		Stream<Entity> retrieved = dataService.findAll(entityTypeDynamic.getId(), ids, new Fetch().field(ATTR_ID));
	//		assertEquals(retrieved.count(), entities.size());
	//	}
	//
	//	@DataProvider(name = "findQueryOperatorEq")
	//	private static Object[][] findQueryOperatorEq() throws ParseException
	//	{
	//		return new Object[][] { { ATTR_ID, "1", singletonList(1) }, { ATTR_STRING, "string1", asList(0, 1, 2) },
	//				{ ATTR_BOOL, true, asList(0, 2) }, { ATTR_DATE, parseLocalDate("2012-12-21"), asList(0, 1, 2) },
	//				{ ATTR_DATETIME, parseInstant("1985-08-12T11:12:13+0500"), asList(0, 1, 2) },
	//				{ ATTR_DECIMAL, 1.123, singletonList(1) },
	//				{ ATTR_HTML, "<html>where is my head and where is my body</html>", singletonList(1) },
	//				{ ATTR_HYPERLINK, "http://www.molgenis.org", asList(0, 1, 2) },
	//				{ ATTR_LONG, 1000000L, singletonList(1) }, { ATTR_INT, 11, singletonList(1) },
	//				{ ATTR_SCRIPT, "/bin/blaat/script.sh", asList(0, 1, 2) },
	//				{ ATTR_EMAIL, "this.is@mail.address", asList(0, 1, 2) },
	//				// null checks
	//				{ ATTR_ID, null, emptyList() }, { ATTR_STRING, null, emptyList() }, { ATTR_BOOL, null, emptyList() },
	//				{ ATTR_CATEGORICAL, null, emptyList() }, { ATTR_CATEGORICAL_MREF, null, emptyList() },
	//				{ ATTR_DATE, null, emptyList() }, { ATTR_DATETIME, null, emptyList() },
	//				{ ATTR_DECIMAL, null, emptyList() }, { ATTR_HTML, null, asList(0, 2) },
	//				{ ATTR_HYPERLINK, null, emptyList() }, { ATTR_LONG, null, emptyList() },
	//				{ ATTR_INT, 11, singletonList(1) }, { ATTR_SCRIPT, null, emptyList() },
	//				{ ATTR_EMAIL, null, emptyList() }, { ATTR_XREF, null, emptyList() }, { ATTR_MREF, null, emptyList() } };
	//	}
	//
	//	@Test(singleThreaded = true, dataProvider = "findQueryOperatorEq")
	//	public void testFindQueryOperatorEq(String attrName, Object value, List<Integer> expectedEntityIndices)
	//	{
	//		List<Entity> entities = createDynamic(3).collect(toList());
	//		dataService.add(entityTypeDynamic.getId(), entities.stream());
	//		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getId())
	//				.eq(attrName, value).findAll();
	//		List<Entity> foundAsList = found.get().collect(toList());
	//		assertEquals(foundAsList.size(), expectedEntityIndices.size());
	//		for (int i = 0; i < expectedEntityIndices.size(); ++i)
	//		{
	//			assertTrue(EntityUtils.equals(foundAsList.get(i), entities.get(expectedEntityIndices.get(i))));
	//		}
	//	}
	//
	//	@DataProvider(name = "findQueryOperatorIn")
	//	private static Object[][] findQueryOperatorIn()
	//	{
	//		return new Object[][] { { singletonList("-1"), emptyList() }, { asList("-1", "0"), singletonList(0) },
	//				{ asList("0", "1"), asList(0, 1) }, { asList("1", "2"), singletonList(1) } };
	//	}
	//
	//	@Test(singleThreaded = true, dataProvider = "findQueryOperatorIn")
	//	public void testFindQueryOperatorIn(List<String> ids, List<Integer> expectedEntityIndices)
	//	{
	//		List<Entity> entities = createDynamic(2).collect(toList());
	//		dataService.add(entityTypeDynamic.getId(), entities.stream());
	//		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getId())
	//				.in(ATTR_ID, ids).findAll();
	//		List<Entity> foundAsList = found.get().collect(toList());
	//		assertEquals(foundAsList.size(), expectedEntityIndices.size());
	//		for (int i = 0; i < expectedEntityIndices.size(); ++i)
	//		{
	//			assertTrue(EntityUtils.equals(foundAsList.get(i), entities.get(expectedEntityIndices.get(i))));
	//		}
	//	}
	//
	//	@DataProvider(name = "findQueryOperatorLess")
	//	private static Object[][] findQueryOperatorLess()
	//	{
	//		return new Object[][] { { 9, emptyList() }, { 10, emptyList() }, { 11, singletonList(0) }, { 12, asList(0, 1) },
	//				{ 13, asList(0, 1, 2) } };
	//	}
	//
	//	@Test(singleThreaded = true, dataProvider = "findQueryOperatorLess")
	//	public void testFindQueryOperatorLess(int value, List<Integer> expectedEntityIndices)
	//	{
	//		List<Entity> entities = createDynamic(5).collect(toList());
	//		dataService.add(entityTypeDynamic.getId(), entities.stream());
	//		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getId())
	//				.lt(ATTR_INT, value).findAll();
	//		List<Entity> foundAsList = found.get().collect(toList());
	//		assertEquals(foundAsList.size(), expectedEntityIndices.size());
	//		for (int i = 0; i < expectedEntityIndices.size(); ++i)
	//		{
	//			assertTrue(EntityUtils.equals(foundAsList.get(i), entities.get(expectedEntityIndices.get(i))));
	//		}
	//	}
	//
	//	@DataProvider(name = "findQueryOperatorLessEqual")
	//	private static Object[][] findQueryOperatorLessEqual()
	//	{
	//		return new Object[][] { { 9, emptyList() }, { 10, singletonList(0) }, { 11, asList(0, 1) },
	//				{ 12, asList(0, 1, 2) }, { 13, asList(0, 1, 2, 3) } };
	//	}
	//
	//	@Test(singleThreaded = true, dataProvider = "findQueryOperatorLessEqual")
	//	public void testFindQueryOperatorLessEqual(int value, List<Integer> expectedEntityIndices)
	//	{
	//		List<Entity> entities = createDynamic(5).collect(toList());
	//		dataService.add(entityTypeDynamic.getId(), entities.stream());
	//		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getId())
	//				.le(ATTR_INT, value).findAll();
	//		List<Entity> foundAsList = found.get().collect(toList());
	//		assertEquals(foundAsList.size(), expectedEntityIndices.size());
	//		for (int i = 0; i < expectedEntityIndices.size(); ++i)
	//		{
	//			assertTrue(EntityUtils.equals(foundAsList.get(i), entities.get(expectedEntityIndices.get(i))));
	//		}
	//	}
	//
	//	@DataProvider(name = "findQueryOperatorGreater")
	//	private static Object[][] findQueryOperatorGreater()
	//	{
	//		return new Object[][] { { 9, asList(0, 1, 2) }, { 10, asList(1, 2) }, { 11, singletonList(2) },
	//				{ 12, emptyList() } };
	//	}
	//
	//	@Test(singleThreaded = true, dataProvider = "findQueryOperatorGreater")
	//	public void testFindQueryOperatorGreater(int value, List<Integer> expectedEntityIndices)
	//	{
	//		List<Entity> entities = createDynamic(3).collect(toList());
	//		dataService.add(entityTypeDynamic.getId(), entities.stream());
	//		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getId())
	//				.gt(ATTR_INT, value).findAll();
	//		List<Entity> foundAsList = found.get().collect(toList());
	//		assertEquals(foundAsList.size(), expectedEntityIndices.size());
	//		for (int i = 0; i < expectedEntityIndices.size(); ++i)
	//		{
	//			assertTrue(EntityUtils.equals(foundAsList.get(i), entities.get(expectedEntityIndices.get(i))));
	//		}
	//	}
	//
	//	@DataProvider(name = "findQueryOperatorGreaterEqual")
	//	private static Object[][] findQueryOperatorGreaterEqual()
	//	{
	//		return new Object[][] { { 9, asList(0, 1, 2) }, { 10, asList(0, 1, 2) }, { 11, asList(1, 2) },
	//				{ 12, singletonList(2) }, { 13, emptyList() } };
	//	}
	//
	//	@Test(singleThreaded = true, dataProvider = "findQueryOperatorGreaterEqual")
	//	public void testFindQueryOperatorGreaterEqual(int value, List<Integer> expectedEntityIndices)
	//	{
	//		List<Entity> entities = createDynamic(3).collect(toList());
	//		dataService.add(entityTypeDynamic.getId(), entities.stream());
	//		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getId())
	//				.ge(ATTR_INT, value).findAll();
	//		List<Entity> foundAsList = found.get().collect(toList());
	//		assertEquals(foundAsList.size(), expectedEntityIndices.size());
	//		for (int i = 0; i < expectedEntityIndices.size(); ++i)
	//		{
	//			assertTrue(EntityUtils.equals(foundAsList.get(i), entities.get(expectedEntityIndices.get(i))));
	//		}
	//	}
	//
	//	@DataProvider(name = "findQueryOperatorRange")
	//	private static Object[][] findQueryOperatorRange()
	//	{
	//		return new Object[][] { { 0, 9, emptyList() }, { 0, 10, asList(0) }, { 10, 10, asList(0) },
	//				{ 10, 11, asList(0, 1) }, { 10, 12, asList(0, 1, 2) }, { 12, 20, asList(2) } };
	//	}
	//
	//	@Test(singleThreaded = true, dataProvider = "findQueryOperatorRange")
	//	public void testFindQueryOperatorRange(int low, int high, List<Integer> expectedEntityIndices)
	//	{
	//		List<Entity> entities = createDynamic(3).collect(toList());
	//		dataService.add(entityTypeDynamic.getId(), entities.stream());
	//		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getId())
	//				.rng(ATTR_INT, low, high).findAll();
	//		List<Entity> foundAsList = found.get().collect(toList());
	//		assertEquals(foundAsList.size(), expectedEntityIndices.size());
	//		for (int i = 0; i < expectedEntityIndices.size(); ++i)
	//		{
	//			assertTrue(EntityUtils.equals(foundAsList.get(i), entities.get(expectedEntityIndices.get(i))));
	//		}
	//	}
	//
	//	@DataProvider(name = "findQueryOperatorLike")
	//	private static Object[][] findQueryOperatorLike()
	//	{
	//		return new Object[][] { { "ring", asList(0, 1) }, { "Ring", emptyList() }, { "nomatch", emptyList() } };
	//	}
	//
	//	@Test(singleThreaded = true, dataProvider = "findQueryOperatorLike")
	//	public void testFindQueryOperatorLike(String likeStr, List<Integer> expectedEntityIndices)
	//	{
	//		List<Entity> entities = createDynamic(2).collect(toList());
	//		dataService.add(entityTypeDynamic.getId(), entities.stream());
	//		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getId())
	//				.like(ATTR_STRING, likeStr).findAll();
	//		List<Entity> foundAsList = found.get().collect(toList());
	//		assertEquals(foundAsList.size(), expectedEntityIndices.size());
	//		for (int i = 0; i < expectedEntityIndices.size(); ++i)
	//		{
	//			assertTrue(EntityUtils.equals(foundAsList.get(i), entities.get(expectedEntityIndices.get(i))));
	//		}
	//	}
	//
	//	@DataProvider(name = "findQueryOperatorNot")
	//	private static Object[][] findQueryOperatorNot()
	//	{
	//		return new Object[][] { { 9, asList(0, 1, 2) }, { 10, asList(1, 2) }, { 11, asList(0, 2) },
	//				{ 12, asList(0, 1) }, { 13, asList(0, 1, 2) } };
	//	}
	//
	//	@Test(singleThreaded = true, dataProvider = "findQueryOperatorNot")
	//	public void testFindQueryOperatorNot(int value, List<Integer> expectedEntityIndices)
	//	{
	//		List<Entity> entities = createDynamic(3).collect(toList());
	//		dataService.add(entityTypeDynamic.getId(), entities.stream());
	//		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getId()).not()
	//				.eq(ATTR_INT, value).findAll();
	//		List<Entity> foundAsList = found.get().collect(toList());
	//		assertEquals(foundAsList.size(), expectedEntityIndices.size());
	//		for (int i = 0; i < expectedEntityIndices.size(); ++i)
	//		{
	//			assertTrue(EntityUtils.equals(foundAsList.get(i), entities.get(expectedEntityIndices.get(i))));
	//		}
	//	}
	//
	//	/**
	//	 * Test used as a caching benchmark
	//	 */
	//	@Test(singleThreaded = true, enabled = false)
	//	public void cachePerformanceTest()
	//	{
	//		List<Entity> entities = createDynamic(10000).collect(toList());
	//		dataService.add(entityTypeDynamic.getId(), entities.stream());
	//		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//
	//		Query<Entity> q1 = new QueryImpl<>().eq(EntityTestHarness.ATTR_STRING, "string1");
	//		q1.pageSize(1000);
	//
	//		Query<Entity> q2 = new QueryImpl<>().eq(EntityTestHarness.ATTR_BOOL, true);
	//		q2.pageSize(500);
	//
	//		Query<Entity> q3 = new QueryImpl<>().eq(ATTR_DECIMAL, 1.123);
	//
	//		runAsSystem(() ->
	//		{
	//			for (int i = 0; i < 100000; i++)
	//			{
	//				dataService.findAll(entityTypeDynamic.getId(), q1);
	//				dataService.findAll(entityTypeDynamic.getId(), q2);
	//				dataService.findOne(entityTypeDynamic.getId(), q3);
	//			}
	//		});
	//	}
	//
	//	@DataProvider(name = "findQueryOperatorAnd")
	//	private static Object[][] findQueryOperatorAnd()
	//	{
	//		return new Object[][] { { "string1", 10, asList(0) }, { "unknownString", 10, emptyList() },
	//				{ "string1", -1, emptyList() }, { "unknownString", -1, emptyList() } };
	//	}
	//
	//	@Test(singleThreaded = true, dataProvider = "findQueryOperatorAnd")
	//	public void testFindQueryOperatorAnd(String strValue, int value, List<Integer> expectedEntityIndices)
	//	{
	//		List<Entity> entities = createDynamic(3).collect(toList());
	//		dataService.add(entityTypeDynamic.getId(), entities.stream());
	//		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getId())
	//				.eq(ATTR_STRING, strValue).and().eq(ATTR_INT, value).findAll();
	//		List<Entity> foundAsList = found.get().collect(toList());
	//		assertEquals(foundAsList.size(), expectedEntityIndices.size());
	//		for (int i = 0; i < expectedEntityIndices.size(); ++i)
	//		{
	//			assertTrue(EntityUtils.equals(foundAsList.get(i), entities.get(expectedEntityIndices.get(i))));
	//		}
	//	}
	//
	//	@DataProvider(name = "findQueryOperatorOr")
	//	private static Object[][] findQueryOperatorOr()
	//	{
	//		return new Object[][] { { "string1", 10, asList(0, 1, 2) }, { "unknownString", 10, asList(0) },
	//				{ "string1", -1, asList(0, 1, 2) }, { "unknownString", -1, emptyList() } };
	//	}
	//
	//	@Test(singleThreaded = true, dataProvider = "findQueryOperatorOr")
	//	public void testFindQueryOperatorOr(String strValue, int value, List<Integer> expectedEntityIndices)
	//	{
	//		List<Entity> entities = createDynamic(3).collect(toList());
	//		dataService.add(entityTypeDynamic.getId(), entities.stream());
	//		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getId())
	//				.eq(ATTR_STRING, strValue).or().eq(ATTR_INT, value).findAll();
	//		List<Entity> foundAsList = found.get().collect(toList());
	//		assertEquals(foundAsList.size(), expectedEntityIndices.size());
	//		for (int i = 0; i < expectedEntityIndices.size(); ++i)
	//		{
	//			assertTrue(EntityUtils.equals(foundAsList.get(i), entities.get(expectedEntityIndices.get(i))));
	//		}
	//	}
	//
	//	@DataProvider(name = "findQueryOperatorNested")
	//	private static Object[][] findQueryOperatorNested()
	//	{
	//		return new Object[][] { { true, "string1", 10, asList(0, 2) }, { true, "unknownString", 10, asList(0) },
	//				{ true, "string1", -1, asList(0, 2) }, { true, "unknownString", -1, emptyList() },
	//				{ false, "string1", 10, singletonList(1) }, { false, "unknownString", 10, emptyList() },
	//				{ false, "string1", -1, asList(1) }, { false, "unknownString", -1, emptyList() } };
	//	}
	//
	//	@Test(singleThreaded = true, dataProvider = "findQueryOperatorNested")
	//	public void testFindQueryOperatorNested(boolean boolValue, String strValue, int value,
	//			List<Integer> expectedEntityIndices)
	//	{
	//		List<Entity> entities = createDynamic(3).collect(toList());
	//		dataService.add(entityTypeDynamic.getId(), entities.stream());
	//		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getId())
	//				.eq(ATTR_BOOL, boolValue).and().nest().eq(ATTR_STRING, strValue).or().eq(ATTR_INT, value).unnest()
	//				.findAll();
	//		List<Entity> foundAsList = found.get().collect(toList());
	//		assertEquals(foundAsList.size(), expectedEntityIndices.size());
	//		for (int i = 0; i < expectedEntityIndices.size(); ++i)
	//		{
	//			assertTrue(EntityUtils.equals(foundAsList.get(i), entities.get(expectedEntityIndices.get(i))));
	//		}
	//	}
	//
	//	@DataProvider(name = "findQueryOperatorSearch")
	//	private static Object[][] findQueryOperatorSearch()
	//	{
	//		return new Object[][] { { "body", asList(1) }, { "head", asList(1) }, { "unknownString", emptyList() } };
	//	}
	//
	//	@Test(singleThreaded = true, dataProvider = "findQueryOperatorSearch")
	//	public void testFindQueryOperatorSearch(String searchStr, List<Integer> expectedEntityIndices)
	//	{
	//		List<Entity> entities = createDynamic(2).collect(toList());
	//		dataService.add(entityTypeDynamic.getId(), entities.stream());
	//		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getId())
	//				.search(ATTR_HTML, searchStr).findAll();
	//		List<Entity> foundAsList = found.get().collect(toList());
	//		assertEquals(foundAsList.size(), expectedEntityIndices.size());
	//		for (int i = 0; i < expectedEntityIndices.size(); ++i)
	//		{
	//			assertTrue(EntityUtils.equals(foundAsList.get(i), entities.get(expectedEntityIndices.get(i))));
	//		}
	//	}
	//
	//	@Test(singleThreaded = true)
	//	public void testFindQueryLimit2_Offset2_sortOnInt()
	//	{
	//		List<Entity> testRefEntities = testHarness.createTestRefEntities(refEntityTypeDynamic, 6);
	//		List<Entity> testEntities = testHarness.createTestEntities(entityTypeDynamic, 10, testRefEntities)
	//				.collect(toList());
	//		runAsSystem(() ->
	//		{
	//			dataService.add(refEntityTypeDynamic.getId(), testRefEntities.stream());
	//			dataService.add(entityTypeDynamic.getId(), testEntities.stream());
	//		});
	//		waitForIndexToBeStable(refEntityTypeDynamic, indexService, LOG);
	//		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//		Supplier<Stream<Entity>> found = () -> dataService.findAll(entityTypeDynamic.getId(),
	//				new QueryImpl<>().pageSize(2).offset(2).sort(new Sort(ATTR_ID, Sort.Direction.DESC)));
	//		List<Entity> foundAsList = found.get().collect(toList());
	//		assertEquals(foundAsList.size(), 2);
	//		assertTrue(EntityUtils.equals(foundAsList.get(0), testEntities.get(7)));
	//		assertTrue(EntityUtils.equals(foundAsList.get(1), testEntities.get(6)));
	//	}
	//
	//	@Test(singleThreaded = true)
	//	public void testFindQueryTypedStatic()
	//	{
	//		List<Entity> entities = createStatic(5).collect(toList());
	//		dataService.add(entityTypeStatic.getId(), entities.stream());
	//		waitForIndexToBeStable(entityTypeStatic, indexService, LOG);
	//		Supplier<Stream<TestEntityStatic>> found = () -> dataService.findAll(entityTypeStatic.getId(),
	//				new QueryImpl<TestEntityStatic>().eq(ATTR_ID, entities.get(0).getIdValue()), TestEntityStatic.class);
	//		assertEquals(found.get().count(), 1);
	//		assertEquals(found.get().findFirst().get().getId(), entities.get(0).getIdValue());
	//	}
	//
	//	@Test(singleThreaded = true)
	//	public void testFindOne()
	//	{
	//		Entity entity = createDynamic(1).findFirst().get();
	//		dataService.add(entityTypeDynamic.getId(), Stream.of(entity));
	//		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//		assertNotNull(dataService.findOneById(entityTypeDynamic.getId(), entity.getIdValue()));
	//	}
	//
	//	@Test(singleThreaded = true)
	//	public void testFindOneTypedStatic()
	//	{
	//		Entity entity = createStatic(1).findFirst().get();
	//		dataService.add(entityTypeStatic.getId(), Stream.of(entity));
	//		waitForIndexToBeStable(entityTypeStatic, indexService, LOG);
	//		TestEntityStatic testEntityStatic = dataService
	//				.findOneById(entityTypeStatic.getId(), entity.getIdValue(), TestEntityStatic.class);
	//		assertNotNull(testEntityStatic);
	//		assertEquals(testEntityStatic.getId(), entity.getIdValue());
	//	}
	//
	//	@Test(singleThreaded = true)
	//	public void testFindOneFetch()
	//	{
	//		Entity entity = createDynamic(1).findFirst().get();
	//		dataService.add(entityTypeDynamic.getId(), Stream.of(entity));
	//		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//		assertNotNull(dataService.findOneById(entityTypeDynamic.getId(), entity.getIdValue(),
	//				new Fetch().field(ATTR_ID)));
	//	}
	//
	//	@Test(singleThreaded = true)
	//	public void testFindOneFetchTypedStatic()
	//	{
	//		TestEntityStatic entity = new TestEntityStatic(entityTypeStatic);
	//		entity.set(ATTR_ID, "1");
	//		entity.set(ATTR_STRING, "string1");
	//		entity.set(ATTR_BOOL, true);
	//
	//		dataService.add(entityTypeStatic.getId(), Stream.of(entity));
	//		waitForIndexToBeStable(entityTypeStatic, indexService, LOG);
	//		TestEntityStatic testEntityStatic = dataService
	//				.findOneById(entityTypeStatic.getId(), entity.getIdValue(), new Fetch().field(ATTR_ID),
	//						TestEntityStatic.class);
	//		assertNotNull(testEntityStatic);
	//		assertEquals(testEntityStatic.getIdValue(), entity.getIdValue());
	//	}
	//
	//	@Test(singleThreaded = true)
	//	public void testFindOneQuery()
	//	{
	//		Entity entity = createDynamic(1).findFirst().get();
	//		dataService.add(entityTypeDynamic.getId(), entity);
	//		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//		entity = dataService.findOne(entityTypeDynamic.getId(), new QueryImpl<>().eq(ATTR_ID, entity.getIdValue()));
	//		assertNotNull(entity);
	//	}
	//
	//	@Test(singleThreaded = true)
	//	public void testFindOneQueryTypedStatic()
	//	{
	//		Entity entity = createStatic(1).findFirst().get();
	//		dataService.add(entityTypeStatic.getId(), entity);
	//		waitForIndexToBeStable(entityTypeStatic, indexService, LOG);
	//		TestEntityStatic testEntityStatic = dataService.findOne(entityTypeStatic.getId(),
	//				new QueryImpl<TestEntityStatic>().eq(ATTR_ID, entity.getIdValue()), TestEntityStatic.class);
	//		assertNotNull(testEntityStatic);
	//		assertEquals(testEntityStatic.getId(), entity.getIdValue());
	//	}
	//
	//	@Test(singleThreaded = true)
	//	public void testGetCapabilities()
	//	{
	//		Set<RepositoryCapability> capabilities = dataService.getCapabilities(entityTypeDynamic.getId());
	//		assertNotNull(capabilities);
	//		assertTrue(capabilities.containsAll(asList(MANAGABLE, QUERYABLE, WRITABLE, VALIDATE_REFERENCE_CONSTRAINT)));
	//	}
	//
	//	@Test(singleThreaded = true)
	//	public void testGetEntityType()
	//	{
	//		EntityType entityType = dataService.getEntityType(entityTypeDynamic.getId());
	//		assertNotNull(entityType);
	//		assertTrue(EntityUtils.equals(entityType, entityTypeDynamic));
	//	}
	//
	//	@Test(singleThreaded = true)
	//	public void testGetEntityNames()
	//	{
	//		Stream<String> names = dataService.getEntityTypeIds();
	//		assertNotNull(names);
	//		assertTrue(names.filter(entityTypeDynamic.getId()::equals).findFirst().isPresent());
	//	}
	//
	//	@Test(singleThreaded = true)
	//	public void testGetMeta()
	//	{
	//		assertNotNull(dataService.getMeta());
	//	}
	//
	//	@Test(singleThreaded = true)
	//	public void testGetKnownRepository()
	//	{
	//		Repository<Entity> repo = dataService.getRepository(entityTypeDynamic.getId());
	//		assertNotNull(repo);
	//		assertEquals(repo.getName(), entityTypeDynamic.getId());
	//	}
	//
	//	@Test(singleThreaded = true, expectedExceptions = UnknownEntityException.class)
	//	public void testGetUnknownRepository()
	//	{
	//		dataService.getRepository("bogus");
	//	}
	//
	//	@Test(singleThreaded = true)
	//	public void testHasRepository()
	//	{
	//		assertTrue(dataService.hasRepository(entityTypeDynamic.getId()));
	//		assertFalse(dataService.hasRepository("bogus"));
	//	}
	//
	//	@Test(singleThreaded = true)
	//	public void testIterator()
	//	{
	//		assertNotNull(dataService.iterator());
	//		StreamSupport.stream(dataService.spliterator(), false).forEach(repo -> LOG.info(repo.getName()));
	//		Repository repo = dataService.getRepository(entityTypeDynamic.getId());
	//
	//		/*
	//			Repository equals is not implemented. The repository from dataService
	//			and from the dataService.getRepository are not the same instances.
	//		*/
	//		assertTrue(StreamSupport.stream(dataService.spliterator(), false)
	//				.anyMatch(e -> repo.getName().equals(e.getName())));
	//	}
	//
	//	@Test(singleThreaded = true)
	//	public void testQuery()
	//	{
	//		assertNotNull(dataService.query(entityTypeDynamic.getId()));
	//		try
	//		{
	//			dataService.query("bogus");
	//			fail("Should have thrown UnknownEntityException");
	//		}
	//		catch (UnknownEntityException e)
	//		{
	//			// Expected
	//		}
	//	}
	//
	//	@Test(singleThreaded = true)
	//	public void testUpdate()
	//	{
	//		Entity entity = createDynamic(1).findFirst().get();
	//		dataService.add(entityTypeDynamic.getId(), entity);
	//		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//
	//		entity = dataService.findOneById(entityTypeDynamic.getId(), entity.getIdValue());
	//		assertNotNull(entity);
	//		assertEquals(entity.get(ATTR_STRING), "string1");
	//
	//		Query<Entity> q = new QueryImpl<>();
	//		q.eq(ATTR_STRING, "qwerty");
	//		entity.set(ATTR_STRING, "qwerty");
	//
	//		assertEquals(searchService.count(q, entityTypeDynamic), 0);
	//		dataService.update(entityTypeDynamic.getId(), entity);
	//		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//		assertEquals(searchService.count(q, entityTypeDynamic), 1);
	//
	//		assertPresent(entityTypeDynamic, entity);
	//
	//		entity = dataService.findOneById(entityTypeDynamic.getId(), entity.getIdValue());
	//		assertNotNull(entity.get(ATTR_STRING));
	//		assertEquals(entity.get(ATTR_STRING), "qwerty");
	//	}
	//
	//	@Test(singleThreaded = true)
	//	public void testUpdateSingleRefEntityIndexesReferencingEntities()
	//	{
	//		dataService.add(entityTypeDynamic.getId(), createDynamic(30));
	//		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//
	//		Entity refEntity4 = dataService.findOneById(refEntityTypeDynamic.getId(), "4");
	//
	//		Query<Entity> q = new QueryImpl<>().search("refstring4");
	//
	//		assertEquals(searchService.count(q, entityTypeDynamic), 5);
	//		refEntity4.set(ATTR_REF_STRING, "qwerty");
	//		runAsSystem(() -> dataService.update(refEntityTypeDynamic.getId(), refEntity4));
	//		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//		assertEquals(searchService.count(q, entityTypeDynamic), 0);
	//		assertEquals(searchService.count(new QueryImpl<>().search("qwerty"), entityTypeDynamic), 5);
	//	}
	//
	//	@Test(singleThreaded = true, enabled = false) //FIXME: sys_md_attributes spam
	//	public void testUpdateSingleRefEntityIndexesLargeAmountOfReferencingEntities()
	//	{
	//		dataService.add(entityTypeDynamic.getId(), createDynamic(10000));
	//		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//
	//		Query<Entity> q = new QueryImpl<>().search("refstring4").or().search("refstring5");
	//
	//		assertEquals(searchService.count(q, entityTypeDynamic), 3333);
	//		Entity refEntity4 = dataService.findOneById(refEntityTypeDynamic.getId(), "4");
	//		refEntity4.set(ATTR_REF_STRING, "qwerty");
	//		runAsSystem(() -> dataService.update(refEntityTypeDynamic.getId(), refEntity4));
	//
	//		Entity refEntity5 = dataService.findOneById(refEntityTypeDynamic.getId(), "5");
	//		refEntity5.set(ATTR_REF_STRING, "qwerty");
	//		runAsSystem(() -> dataService.update(refEntityTypeDynamic.getId(), refEntity5));
	//
	//		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//		assertEquals(searchService.count(q, entityTypeDynamic), 0);
	//
	//		assertEquals(searchService.count(new QueryImpl<>().search("qwerty"), entityTypeDynamic), 3333);
	//	}
	//
	//	@Test(singleThreaded = true)
	//	public void testUpdateStream()
	//	{
	//		Entity entity = createDynamic(1).findFirst().get();
	//
	//		dataService.add(entityTypeDynamic.getId(), entity);
	//		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//		assertPresent(entityTypeDynamic, entity);
	//
	//		entity = dataService.findOneById(entityTypeDynamic.getId(), entity.getIdValue());
	//		assertNotNull(entity);
	//		assertEquals(entity.get(ATTR_STRING), "string1");
	//
	//		entity.set(ATTR_STRING, "qwerty");
	//		Query<Entity> q = new QueryImpl<>();
	//		q.eq(ATTR_STRING, "qwerty");
	//
	//		assertEquals(searchService.count(q, entityTypeDynamic), 0);
	//
	//		dataService.update(entityTypeDynamic.getId(), of(entity));
	//		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//
	//		assertEquals(searchService.count(q, entityTypeDynamic), 1);
	//
	//		assertPresent(entityTypeDynamic, entity);
	//		entity = dataService.findOneById(entityTypeDynamic.getId(), entity.getIdValue());
	//		assertNotNull(entity.get(ATTR_STRING));
	//		assertEquals(entity.get(ATTR_STRING), "qwerty");
	//	}
	//
	private Stream<Entity> createDynamic(int count)
	{
		List<Entity> refEntities = testHarness.createTestRefEntities(refEntityTypeDynamic, 6);
		searchService.index(refEntities.stream(), refEntityTypeDynamic, IndexingMode.ADD);
		return testHarness.createTestEntities(entityTypeDynamic, count, refEntities);
	}
	//
	//	private Stream<Entity> createStatic(int count)
	//	{
	//		List<Entity> refEntities = testHarness.createTestRefEntities(refEntityTypeStatic, 6);
	//		runAsSystem(() -> dataService.add(refEntityTypeStatic.getId(), refEntities.stream()));
	//		return testHarness.createTestEntities(entityTypeStatic, count, refEntities);
	//	}
	//
	//	private void assertPresent(EntityType emd, List<Entity> entities)
	//	{
	//		entities.forEach(e -> assertPresent(emd, e));
	//	}
	//
	//	private void assertPresent(EntityType emd, Entity entity)
	//	{
	//		// Found in PostgreSQL
	//		assertNotNull(dataService.findOneById(emd.getId(), entity.getIdValue()));
	//
	//		// Found in index Elasticsearch
	//		Query<Entity> q = new QueryImpl<>();
	//		q.eq(emd.getIdAttribute().getName(), entity.getIdValue());
	//		assertEquals(searchService.count(q, emd), 1);
	//	}
	//
	//	private void assertNotPresent(Entity entity)
	//	{
	//		// Found in PostgreSQL
	//		assertNull(dataService.findOneById(entityTypeDynamic.getId(), entity.getIdValue()));
	//
	//		// Not found in index Elasticsearch
	//		Query<Entity> q = new QueryImpl<>();
	//		q.eq(entityTypeDynamic.getIdAttribute().getName(), entity.getIdValue());
	//		assertEquals(searchService.count(q, entityTypeDynamic), 0);
	//	}
	//
	//	@Test(singleThreaded = true)
	//	public void testCreateSelfXref()
	//	{
	//		Entity entitySelfXref = entitySelfXrefTestHarness.createTestEntities(selfXrefEntityType, 1).collect(toList())
	//				.get(0);
	//
	//		//Create
	//		dataService.add(selfXrefEntityType.getId(), entitySelfXref);
	//		waitForIndexToBeStable(selfXrefEntityType, indexService, LOG);
	//		Entity entity = dataService.findOneById(selfXrefEntityType.getId(), entitySelfXref.getIdValue());
	//		assertPresent(selfXrefEntityType, entity);
	//
	//		Query<Entity> q1 = new QueryImpl<>();
	//		q1.eq(ATTR_STRING, "attr_string_old");
	//		Query<Entity> q2 = new QueryImpl<>();
	//		q2.eq(ATTR_STRING, "attr_string_new");
	//		entity.set(ATTR_STRING, "attr_string_new");
	//
	//		// Verify value in elasticsearch before update
	//		assertEquals(searchService.count(q1, selfXrefEntityType), 1);
	//		assertEquals(searchService.count(q2, selfXrefEntityType), 0);
	//
	//		// Update
	//		dataService.update(selfXrefEntityType.getId(), entity);
	//		waitForIndexToBeStable(selfXrefEntityType, indexService, LOG);
	//		assertPresent(selfXrefEntityType, entity);
	//
	//		// Verify value in elasticsearch after update
	//		assertEquals(searchService.count(q2, selfXrefEntityType), 1);
	//		assertEquals(searchService.count(q1, selfXrefEntityType), 0);
	//
	//		// Verify value in PostgreSQL after update
	//		entity = dataService.findOneById(selfXrefEntityType.getId(), entity.getIdValue());
	//		assertNotNull(entity.get(ATTR_STRING));
	//		assertEquals(entity.get(ATTR_STRING), "attr_string_new");
	//
	//		// Check id are equals
	//		assertEquals(entity.getEntity(ATTR_XREF).getIdValue(), entity.getIdValue());
	//	}
	//
	//	@Test(singleThreaded = true)
	//	public void testIndexCreateMetaData()
	//	{
	//		IndexMetadataCUDOperationsPlatformIT
	//				.testIndexCreateMetaData(searchService, entityTypeStatic, entityTypeDynamic, metaDataService);
	//	}
	//
	//	@Test(singleThreaded = true)
	//	public void testIndexDeleteMetaData()
	//	{
	//		IndexMetadataCUDOperationsPlatformIT
	//				.testIndexDeleteMetaData(searchService, dataService, entityTypeDynamic, metaDataService, indexService);
	//	}
	//
	//	@Test(singleThreaded = true)
	//	public void testIndexUpdateMetaDataUpdateAttribute()
	//	{
	//		IndexMetadataCUDOperationsPlatformIT
	//				.testIndexUpdateMetaDataUpdateAttribute(searchService, entityTypeDynamic, metaDataService,
	//						indexService);
	//	}
	//
	//	@Test(singleThreaded = true)
	//	public void testIndexUpdateMetaDataRemoveAttribute()
	//	{
	//		IndexMetadataCUDOperationsPlatformIT
	//				.testIndexUpdateMetaDataRemoveAttribute(entityTypeDynamic, EntityTestHarness.ATTR_CATEGORICAL,
	//						searchService, metaDataService, indexService);
	//
	//		IndexMetadataCUDOperationsPlatformIT
	//				.testIndexUpdateMetaDataRemoveAttribute(entityTypeDynamic, EntityTestHarness.ATTR_BOOL, searchService,
	//						metaDataService, indexService);
	//
	//		IndexMetadataCUDOperationsPlatformIT
	//				.testIndexUpdateMetaDataRemoveAttribute(entityTypeDynamic, EntityTestHarness.ATTR_DATE, searchService,
	//						metaDataService, indexService);
	//
	//		IndexMetadataCUDOperationsPlatformIT
	//				.testIndexUpdateMetaDataRemoveAttribute(entityTypeDynamic, EntityTestHarness.ATTR_XREF, searchService,
	//						metaDataService, indexService);
	//
	//		IndexMetadataCUDOperationsPlatformIT
	//				.testIndexUpdateMetaDataRemoveAttribute(entityTypeDynamic, EntityTestHarness.ATTR_DATETIME,
	//						searchService, metaDataService, indexService);
	//
	//		IndexMetadataCUDOperationsPlatformIT
	//				.testIndexUpdateMetaDataRemoveAttribute(entityTypeDynamic, EntityTestHarness.ATTR_DECIMAL,
	//						searchService, metaDataService, indexService);
	//
	//		IndexMetadataCUDOperationsPlatformIT
	//				.testIndexUpdateMetaDataRemoveAttribute(entityTypeDynamic, EntityTestHarness.ATTR_EMAIL, searchService,
	//						metaDataService, indexService);
	//
	//		IndexMetadataCUDOperationsPlatformIT
	//				.testIndexUpdateMetaDataRemoveAttribute(entityTypeDynamic, EntityTestHarness.ATTR_HTML, searchService,
	//						metaDataService, indexService);
	//
	//		IndexMetadataCUDOperationsPlatformIT
	//				.testIndexUpdateMetaDataRemoveAttribute(entityTypeDynamic, EntityTestHarness.ATTR_INT, searchService,
	//						metaDataService, indexService);
	//
	//		IndexMetadataCUDOperationsPlatformIT
	//				.testIndexUpdateMetaDataRemoveAttribute(entityTypeDynamic, EntityTestHarness.ATTR_HYPERLINK,
	//						searchService, metaDataService, indexService);
	//
	//		IndexMetadataCUDOperationsPlatformIT
	//				.testIndexUpdateMetaDataRemoveAttribute(entityTypeDynamic, EntityTestHarness.ATTR_COMPOUND,
	//						searchService, metaDataService, indexService);
	//	}
	//
	//	// Derived from fix: https://github.com/molgenis/molgenis/issues/5227
	//	@Test(singleThreaded = true)
	//	public void testIndexBatchUpdate()
	//	{
	//		List<Entity> refEntities = testHarness.createTestRefEntities(refEntityTypeDynamic, 2);
	//		List<Entity> entities = testHarness.createTestEntities(entityTypeDynamic, 2, refEntities).collect(toList());
	//		runAsSystem(() ->
	//		{
	//			dataService.add(refEntityTypeDynamic.getId(), refEntities.stream());
	//			dataService.add(entityTypeDynamic.getId(), entities.stream());
	//			waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//		});
	//
	//		// test string1 from entity
	//		Query<Entity> q0 = new QueryImpl<>();
	//		q0.search("string1");
	//		Stream<Entity> result0 = searchService.searchAsStream(q0, entityTypeDynamic);
	//		assertEquals(result0.count(), 2);
	//
	//		// test refstring1 from ref entity
	//		Query<Entity> q1 = new QueryImpl<>();
	//		q1.search("refstring0");
	//		Stream<Entity> result1 = searchService.searchAsStream(q1, entityTypeDynamic);
	//		assertEquals(result1.count(), 1);
	//
	//		// test refstring1 from ref entity
	//		Query<Entity> q2 = new QueryImpl<>();
	//		q2.search("refstring1");
	//		Stream<Entity> result2 = searchService.searchAsStream(q2, entityTypeDynamic);
	//		assertEquals(result2.count(), 1);
	//
	//		refEntities.get(0).set(ATTR_REF_STRING, "searchTestBatchUpdate");
	//		runAsSystem(() ->
	//		{
	//			dataService.update(refEntityTypeDynamic.getId(), refEntities.stream());
	//			waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//		});
	//
	//		// test string1 from entity
	//		Stream<Entity> result3 = searchService.searchAsStream(q0, entityTypeDynamic);
	//		assertEquals(result3.count(), 2);
	//
	//		// test refstring1 from ref entity
	//		Query<Entity> q4 = new QueryImpl<>();
	//		q4.search("refstring0");
	//		Stream<Entity> result4 = searchService.searchAsStream(q4, entityTypeDynamic);
	//		assertEquals(result4.count(), 0);
	//
	//		// test refstring1 from ref entity
	//		Query<Entity> q5 = new QueryImpl<>();
	//		q5.search("refstring1");
	//		Stream<Entity> result5 = searchService.searchAsStream(q5, entityTypeDynamic);
	//		assertEquals(result5.count(), 1);
	//
	//		// test refstring1 from ref entity
	//		Query<Entity> q6 = new QueryImpl<>();
	//		q6.search("searchTestBatchUpdate");
	//		Stream<Entity> result6 = searchService.searchAsStream(q6, entityTypeDynamic);
	//		assertEquals(result6.count(), 1);
	//	}
	//
	//	/**
	//	 * Test add and remove of a single attribute of a dynamic entity
	//	 */
	//	@Test(singleThreaded = true)
	//	public void addAndDeleteSingleAttribute()
	//	{
	//		final String NEW_ATTRIBUTE = "new_attribute";
	//		Attribute newAttr = attributeFactory.create().setName(NEW_ATTRIBUTE);
	//		EntityType entityType = dataService.getEntityType(entityTypeDynamic.getId());
	//		newAttr.setEntity(entityType);
	//
	//		runAsSystem(() ->
	//		{
	//			dataService.getMeta().addAttribute(newAttr);
	//
	//			List<Entity> refEntities = testHarness.createTestRefEntities(refEntityTypeDynamic, 2);
	//			List<Entity> entities = testHarness.createTestEntities(entityTypeDynamic, 2, refEntities).collect(toList());
	//
	//			dataService.add(refEntityTypeDynamic.getId(), refEntities.stream());
	//			dataService.add(entityTypeDynamic.getId(), entities.stream());
	//			waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//
	//			dataService.update(entityType.getId(),
	//					StreamSupport.stream(dataService.findAll(entityType.getId()).spliterator(), false)
	//							.peek(e -> e.set(NEW_ATTRIBUTE, "NEW_ATTRIBUTE_" + e.getIdValue())));
	//		});
	//		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//
	//		// Tunnel via L3 flow
	//		Query<Entity> q0 = new QueryImpl<>().eq(NEW_ATTRIBUTE, "NEW_ATTRIBUTE_0").or()
	//				.eq(NEW_ATTRIBUTE, "NEW_ATTRIBUTE_1");
	//		q0.pageSize(10); // L3 only caches queries with a page size
	//		q0.sort(new Sort().on(NEW_ATTRIBUTE));
	//
	//		runAsSystem(() ->
	//		{
	//			List expected = dataService.findAll(entityTypeDynamic.getId(), q0).map(Entity::getIdValue)
	//					.collect(toList());
	//			assertEquals(expected, Arrays.asList("0", "1"));
	//
	//			// Remove added attribute
	//			dataService.getMeta().deleteAttributeById(newAttr.getIdValue());
	//			waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//		});
	//
	//		// verify attribute is deleted by adding and removing it again
	//		runAsSystem(() ->
	//		{
	//			// Add attribute
	//			dataService.getMeta().addAttribute(newAttr);
	//
	//			// Delete attribute
	//			dataService.getMeta().deleteAttributeById(newAttr.getIdValue());
	//			waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//		});
	//	}
	//
	//	/**
	//	 * Test add stream attribute of a dynamic entity
	//	 */
	//	@Test(singleThreaded = true)
	//	public void addStreamAttribute()
	//	{
	//		final String NEW_ATTRIBUTE = "new_attribute";
	//		Attribute newAttr = attributeFactory.create().setName(NEW_ATTRIBUTE);
	//		EntityType entityType = dataService.getEntityType(entityTypeDynamic.getId());
	//		newAttr.setEntity(entityType);
	//
	//		runAsSystem(() ->
	//		{
	//			dataService.getMeta().addAttributes(entityType.getId(), Stream.of(newAttr));
	//			waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//
	//			Attribute attribute = dataService.findOneById(ATTRIBUTE_META_DATA, newAttr.getIdValue(), Attribute.class);
	//			assertNotNull(attribute);
	//
	//			// Tunnel via L3 flow
	//			Query<Entity> q0 = new QueryImpl<>().eq(NEW_ATTRIBUTE, "NEW_ATTRIBUTE_0").or()
	//					.eq(NEW_ATTRIBUTE, "NEW_ATTRIBUTE_1");
	//			q0.pageSize(10); // L3 only caches queries with a page size
	//			q0.sort(new Sort().on(NEW_ATTRIBUTE));
	//
	//			List actual = dataService.findAll(entityTypeDynamic.getId(), q0).map(Entity::getIdValue)
	//					.collect(toList());
	//			assertEquals(actual, Arrays.asList());
	//
	//			// Remove added attribute
	//			dataService.getMeta().deleteAttributeById(newAttr.getIdValue());
	//			waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//		});
	//	}
	//
	//	/**
	//	 * Test update of a single attribute of a dynamic entity
	//	 */
	//	@Test(singleThreaded = true)
	//	public void updateAttribute()
	//	{
	//		final String NEW_ATTRIBUTE = "new_attribute";
	//		Attribute newAttr = attributeFactory.create().setName(NEW_ATTRIBUTE);
	//		EntityType entityType = dataService.getEntityType(entityTypeDynamic.getId());
	//		newAttr.setEntity(entityType);
	//
	//		// Add attribute
	//		runAsSystem(() ->
	//		{
	//			dataService.getMeta().addAttribute(newAttr);
	//
	//			List<Entity> refEntities = testHarness.createTestRefEntities(refEntityTypeDynamic, 2);
	//			List<Entity> entities = testHarness.createTestEntities(entityTypeDynamic, 2, refEntities).collect(toList());
	//
	//			dataService.add(refEntityTypeDynamic.getId(), refEntities.stream());
	//			dataService.add(entityTypeDynamic.getId(), entities.stream());
	//			waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//
	//			dataService.update(entityType.getId(),
	//					StreamSupport.stream(dataService.findAll(entityType.getId()).spliterator(), false)
	//							.peek(e -> e.set(NEW_ATTRIBUTE, "NEW_ATTRIBUTE_" + e.getIdValue())));
	//		});
	//		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//
	//		// Verify old values
	//		assertNotEquals(newAttr.getSequenceNumber(), 0);
	//		assertFalse(newAttr.isReadOnly());
	//		assertFalse(newAttr.isUnique());
	//		assertNotEquals(newAttr.getLabel(), "test");
	//		assertNotEquals(newAttr.getDescription(), "test");
	//		assertTrue(newAttr.isNillable());
	//
	//		// New values
	//		newAttr.setSequenceNumber(0);
	//		newAttr.setReadOnly(true);
	//		newAttr.setUnique(true);
	//		newAttr.setLabel("test");
	//		newAttr.setNillable(false);
	//		newAttr.setDescription("test");
	//
	//		// Update attribute
	//		runAsSystem(() ->
	//		{
	//			// Update added attribute
	//			dataService.update(ATTRIBUTE_META_DATA, newAttr);
	//			waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//		});
	//
	//		Attribute attr = dataService.findOneById(ATTRIBUTE_META_DATA, newAttr.getIdValue(), Attribute.class);
	//		assertEquals(attr.getSequenceNumber(), Integer.valueOf(0));
	//		assertTrue(attr.isReadOnly());
	//		assertTrue(attr.isUnique());
	//		assertEquals(attr.getLabel(), "test");
	//		assertEquals(attr.getDescription(), "test");
	//
	//		// Delete attribute
	//		runAsSystem(() ->
	//		{
	//			// Remove added attribute
	//			dataService.getMeta().deleteAttributeById(newAttr.getIdValue());
	//			waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//		});
	//	}
	//
	//	/*
	//	 * Test add and remove of a single attribute of a dynamic entity
	//	 */
	//	@Test(singleThreaded = true)
	//	public void addAndDeleteSingleAttributeStream()
	//	{
	//		final String NEW_ATTRIBUTE = "new_attribute";
	//		Attribute newAttr = attributeFactory.create().setName(NEW_ATTRIBUTE);
	//		EntityType entityType = dataService.getEntityType(entityTypeDynamic.getId());
	//		newAttr.setEntity(entityType);
	//		newAttr.setSequenceNumber(2);
	//		entityType.addAttribute(newAttr);
	//
	//		assertEquals(newAttr.getSequenceNumber(), Integer.valueOf(2)); // Test if sequence number is 2
	//
	//		runAsSystem(() ->
	//		{
	//			dataService.update(ENTITY_TYPE_META_DATA, Stream.of(entityType)); // Adds the column to the table
	//			dataService.add(ATTRIBUTE_META_DATA, Stream.of(newAttr));
	//
	//			List<Entity> refEntities = testHarness.createTestRefEntities(refEntityTypeDynamic, 2);
	//			List<Entity> entities = testHarness.createTestEntities(entityTypeDynamic, 2, refEntities).collect(toList());
	//
	//			dataService.add(refEntityTypeDynamic.getId(), refEntities.stream());
	//			dataService.add(entityTypeDynamic.getId(), entities.stream());
	//			waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//
	//			dataService.update(entityType.getId(),
	//					StreamSupport.stream(dataService.findAll(entityType.getId()).spliterator(), false)
	//							.peek(e -> e.set(NEW_ATTRIBUTE, "NEW_ATTRIBUTE_" + e.getIdValue())));
	//		});
	//		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//
	//		// Tunnel via L3 flow
	//		Query<Entity> q0 = new QueryImpl<>().eq(NEW_ATTRIBUTE, "NEW_ATTRIBUTE_0").or()
	//				.eq(NEW_ATTRIBUTE, "NEW_ATTRIBUTE_1");
	//		q0.pageSize(10); // L3 only caches queries with a page size
	//		q0.sort(new Sort().on(NEW_ATTRIBUTE));
	//
	//		runAsSystem(() ->
	//		{
	//			List expected = dataService.findAll(entityTypeDynamic.getId(), q0).map(Entity::getIdValue)
	//					.collect(toList());
	//			assertEquals(expected, Arrays.asList("0", "1"));
	//
	//			// Remove added attribute
	//			entityType.removeAttribute(newAttr);
	//			dataService.update(ENTITY_TYPE_META_DATA, Stream.of(entityType));
	//			dataService.delete(ATTRIBUTE_META_DATA, Stream.of(newAttr));
	//			waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//		});
	//
	//		// verify attribute is deleted by adding and removing it again
	//		runAsSystem(() ->
	//		{
	//			entityType.addAttribute(newAttr);
	//			dataService.update(ENTITY_TYPE_META_DATA, Stream.of(entityType));
	//			dataService.add(ATTRIBUTE_META_DATA, Stream.of(newAttr));
	//
	//			// Remove added attribute
	//			entityType.removeAttribute(newAttr);
	//			dataService.update(ENTITY_TYPE_META_DATA, Stream.of(entityType));
	//			dataService.delete(ATTRIBUTE_META_DATA, Stream.of(newAttr));
	//			waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//		});
	//	}
	//
	//	@Test(singleThreaded = true)
	//	@Transactional
	//	public void storeIndexActions()
	//	{
	//		List<Entity> refEntities = testHarness.createTestRefEntities(refEntityTypeDynamic, 2);
	//		List<Entity> entities = testHarness.createTestEntities(entityTypeDynamic, 2, refEntities).collect(toList());
	//		runAsSystem(() ->
	//		{
	//			dataService.add(refEntityTypeDynamic.getId(), refEntities.stream());
	//			dataService.add(entityTypeDynamic.getId(), entities.stream());
	//			waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//
	//			indexActionRegisterService.register(entityTypeDynamic, "1");
	//			indexActionRegisterService.register(entityTypeDynamic, null);
	//
	//			Query<IndexAction> q = new QueryImpl<>();
	//			q.eq(IndexActionMetaData.ENTITY_TYPE_ID, "sys_test_TypeTestDynamic");
	//			Stream<IndexAction> all = dataService
	//					.findAll(IndexActionMetaData.INDEX_ACTION, q, IndexAction.class);
	//			all.forEach(e ->
	//			{
	//				LOG.info(e.getEntityTypeId() + "." + e.getEntityId());
	//			});
	//			waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	//		});
	//	}
}
