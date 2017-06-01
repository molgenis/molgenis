package org.molgenis.integrationtest.platform;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.molgenis.data.*;
import org.molgenis.data.index.IndexingMode;
import org.molgenis.data.index.SearchService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.semanticsearch.explain.service.ElasticSearchExplainService;
import org.molgenis.data.support.QueryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.EntityTestHarness.*;
import static org.molgenis.data.index.IndexingMode.ADD;
import static org.molgenis.util.MolgenisDateFormat.parseInstant;
import static org.molgenis.util.MolgenisDateFormat.parseLocalDate;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

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
	@Autowired
	private ElasticSearchExplainService explainService;

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
		searchService.index(entities.stream(), entityTypeDynamic, ADD);
		searchService.refreshIndex();

		assertEquals(searchService.count(entityTypeDynamic), 2);
	}

	@Test(singleThreaded = true)
	public void testCount()
	{
		List<Entity> entities = createDynamic(2).collect(toList());
		searchService.index(entities.stream(), entityTypeDynamic, ADD);
		searchService.refreshIndex();

		assertEquals(searchService.count(new QueryImpl<>(), entityTypeDynamic), 2);
		assertEquals(searchService.count(entityTypeDynamic), 2);
	}

	@Test(singleThreaded = true)
	public void testDelete()
	{
		Entity entity = createDynamic(1).findFirst().get();
		searchService.index(entity, entityTypeDynamic, ADD);
		searchService.refreshIndex();

		searchService.delete(entity, entityTypeDynamic);
		searchService.refreshIndex();
		assertEquals(searchService.count(entityTypeDynamic), 0);
	}

	@Test(singleThreaded = true)
	public void testDeleteById()
	{
		Entity entity = createDynamic(1).findFirst().get();
		searchService.index(entity, entityTypeDynamic, IndexingMode.ADD);
		searchService.refreshIndex();

		searchService.deleteById(entity.getIdValue().toString(), entityTypeDynamic);
		searchService.refreshIndex();
		assertEquals(searchService.count(entityTypeDynamic), 0);
	}

	@Test(singleThreaded = true)
	public void testDeleteStream()
	{
		List<Entity> entities = createDynamic(2).collect(toList());
		searchService.index(entities.stream(), entityTypeDynamic, IndexingMode.ADD);
		searchService.refreshIndex();

		searchService.delete(entities.stream(), entityTypeDynamic);
		searchService.refreshIndex();
		assertEquals(searchService.count(entityTypeDynamic), 0);
	}

	@Test(singleThreaded = true)
	public void testDeleteAll()
	{
		List<Entity> entities = createDynamic(5).collect(toList());
		searchService.index(entities.stream(), entityTypeDynamic, IndexingMode.ADD);
		searchService.refreshIndex();

		searchService.delete(entityTypeDynamic);
		searchService.refreshIndex();
		assertEquals(searchService.count(entityTypeDynamic), 0);
	}

	@Test(singleThreaded = true)
	public void testFindAllEmpty()
	{
		Iterable<Entity> retrieved = searchService.search(new QueryImpl<>(), entityTypeDynamic);
		assertEquals(Iterables.size(retrieved), 0);
	}

	@Test(singleThreaded = true)
	public void testFindAll()
	{
		List<Entity> entities = createDynamic(5).collect(toList());
		searchService.index(entities.stream(), entityTypeDynamic, IndexingMode.ADD);
		searchService.refreshIndex();

		Iterable<Entity> retrieved = searchService.search(new QueryImpl<>(), entityTypeDynamic);
		assertEquals(Iterables.size(retrieved), entities.size());
	}

	@Test(singleThreaded = true)
	public void testFindAllTyped()
	{
		List<Entity> entities = createDynamic(3).collect(toList());
		searchService.index(entities.stream(), entityTypeDynamic, IndexingMode.ADD);
		searchService.refreshIndex();

		Supplier<Stream<Entity>> retrieved = () -> searchService.searchAsStream(new QueryImpl<>(), entityTypeDynamic);
		assertEquals(retrieved.get().count(), 3);
		assertEquals(retrieved.get().iterator().next().getIdValue(), entities.get(0).getIdValue());

	}

	@DataProvider(name = "findQueryOperatorEq")
	private static Object[][] findQueryOperatorEq()
	{
		return new Object[][] { { ATTR_ID, "1", singletonList("1") }, { ATTR_STRING, "string1", asList("0", "1", "2") },
				{ ATTR_BOOL, true, asList("0", "2") },
				{ ATTR_DATE, parseLocalDate("2012-12-21"), asList("0", "1", "2") },
				{ ATTR_DATETIME, parseInstant("1985-08-12T11:12:13+0500"), asList("0", "1", "2") },
				{ ATTR_DECIMAL, 1.123, singletonList("1") },
				{ ATTR_HTML, "<html>where is my head and where is my body</html>", singletonList("1") },
				{ ATTR_HYPERLINK, "http://www.molgenis.org", asList("0", "1", "2") },
				{ ATTR_LONG, 1000000L, singletonList("1") }, { ATTR_INT, 11, singletonList("1") },
				{ ATTR_SCRIPT, "/bin/blaat/script.sh", asList("0", "1", "2") },
				{ ATTR_EMAIL, "this.is@mail.address", asList("0", "1", "2") },
				// null checks
				{ ATTR_ID, null, emptyList() }, { ATTR_STRING, null, emptyList() }, { ATTR_BOOL, null, emptyList() },
				{ ATTR_CATEGORICAL, null, emptyList() }, { ATTR_CATEGORICAL_MREF, null, emptyList() },
				{ ATTR_DATE, null, emptyList() }, { ATTR_DATETIME, null, emptyList() },
				{ ATTR_DECIMAL, null, emptyList() }, { ATTR_HTML, null, asList("0", "2") },
				{ ATTR_HYPERLINK, null, emptyList() }, { ATTR_LONG, null, emptyList() },
				{ ATTR_INT, 11, singletonList("1") }, { ATTR_SCRIPT, null, emptyList() },
				{ ATTR_EMAIL, null, emptyList() }, { ATTR_XREF, null, emptyList() }, { ATTR_MREF, null, emptyList() } };
	}

	@Test(singleThreaded = true, dataProvider = "findQueryOperatorEq")
	public void testFindQueryOperatorEq(String attrName, Object value, List<Integer> expectedEntityIds)
	{
		List<Entity> testEntities = createDynamic(3).collect(toList());
		searchService.index(testEntities, entityTypeDynamic, ADD);
		searchService.refreshIndex();

		Query<Entity> query = new QueryImpl<>().eq(attrName, value);
		List<Object> ids = searchService.searchAsStream(query, entityTypeDynamic).map(Entity::getIdValue).collect(
				toList());

		assertEquals(ids, expectedEntityIds);
	}

	@DataProvider(name = "findQueryOperatorIn")
	private static Object[][] findQueryOperatorIn()
	{
		return new Object[][] { { singletonList("-1"), emptyList() }, { asList("-1", "0"), singletonList("0") },
				{ asList("0", "1"), asList("0", "1") }, { asList("1", "2"), singletonList("1") } };
	}

	@Test(singleThreaded = true, dataProvider = "findQueryOperatorIn")
	public void testFindQueryOperatorIn(List<String> ids, List<Integer> expectedEntityIds)
	{
		List<Entity> testEntities = createDynamic(2).collect(toList());
		searchService.index(testEntities, entityTypeDynamic, ADD);
		searchService.refreshIndex();

		Query<Entity> query = new QueryImpl<>().in(ATTR_ID, ids);
		List<Object> foundIds = searchService.searchAsStream(query, entityTypeDynamic).map(Entity::getIdValue).collect(
				toList());

		assertEquals(foundIds, expectedEntityIds);
	}

	@DataProvider(name = "findQueryOperatorLess")
	private static Object[][] findQueryOperatorLess()
	{
		return new Object[][] { { 9, emptyList() }, { 10, emptyList() }, { 11, singletonList("0") },
				{ 12, asList("0", "1") }, { 13, asList("0", "1", "2") } };
	}

	@Test(singleThreaded = true, dataProvider = "findQueryOperatorLess")
	public void testFindQueryOperatorLess(int value, List<Integer> expectedEntityIds)
	{
		List<Entity> testEntities = createDynamic(5).collect(toList());
		searchService.index(testEntities, entityTypeDynamic, ADD);
		searchService.refreshIndex();

		Query<Entity> query = new QueryImpl<>().lt(ATTR_INT, value);
		List<Object> foundIds = searchService.searchAsStream(query, entityTypeDynamic).map(Entity::getIdValue).collect(
				toList());

		assertEquals(foundIds, expectedEntityIds);
	}

	@DataProvider(name = "findQueryOperatorLessEqual")
	private static Object[][] findQueryOperatorLessEqual()
	{
		return new Object[][] { { 9, emptyList() }, { 10, singletonList("0") }, { 11, asList("0", "1") },
				{ 12, asList("0", "1", "2") }, { 13, asList("0", "1", "2", "3") } };
	}

	@Test(singleThreaded = true, dataProvider = "findQueryOperatorLessEqual")
	public void testFindQueryOperatorLessEqual(int value, List<Integer> expectedEntityIds)
	{
		List<Entity> testEntities = createDynamic(5).collect(toList());
		searchService.index(testEntities, entityTypeDynamic, ADD);
		searchService.refreshIndex();

		Query<Entity> query = new QueryImpl<>().le(ATTR_INT, value);
		List<Object> foundIds = searchService.searchAsStream(query, entityTypeDynamic).map(Entity::getIdValue).collect(
				toList());

		assertEquals(foundIds, expectedEntityIds);
	}

	@DataProvider(name = "findQueryOperatorGreater")
	private static Object[][] findQueryOperatorGreater()
	{
		return new Object[][] { { 9, asList("0", "1", "2") }, { 10, asList("1", "2") }, { 11, singletonList("2") },
				{ 12, emptyList() } };
	}

	@Test(singleThreaded = true, dataProvider = "findQueryOperatorGreater")
	public void testFindQueryOperatorGreater(int value, List<Integer> expectedEntityIds)
	{
		List<Entity> testEntities = createDynamic(3).collect(toList());
		searchService.index(testEntities, entityTypeDynamic, ADD);
		searchService.refreshIndex();

		Query<Entity> query = new QueryImpl<>().gt(ATTR_INT, value);
		List<Object> foundIds = searchService.searchAsStream(query, entityTypeDynamic).map(Entity::getIdValue).collect(
				toList());

		assertEquals(foundIds, expectedEntityIds);
	}

	@DataProvider(name = "findQueryOperatorGreaterEqual")
	private static Object[][] findQueryOperatorGreaterEqual()
	{
		return new Object[][] { { 9, asList("0", "1", "2") }, { 10, asList("0", "1", "2") }, { 11, asList("1", "2") },
				{ 12, singletonList("2") }, { 13, emptyList() } };
	}

	@Test(singleThreaded = true, dataProvider = "findQueryOperatorGreaterEqual")
	public void testFindQueryOperatorGreaterEqual(int value, List<Integer> expectedEntityIds)
	{
		List<Entity> testEntities = createDynamic(3).collect(toList());
		searchService.index(testEntities, entityTypeDynamic, ADD);
		searchService.refreshIndex();

		Query<Entity> query = new QueryImpl<>().ge(ATTR_INT, value);
		List<Object> foundIds = searchService.searchAsStream(query, entityTypeDynamic).map(Entity::getIdValue).collect(
				toList());

		assertEquals(foundIds, expectedEntityIds);
	}

	@DataProvider(name = "findQueryOperatorRange")
	private static Object[][] findQueryOperatorRange()
	{
		return new Object[][] { { 0, 9, emptyList() }, { 0, 10, singletonList("0") }, { 10, 10, singletonList("0") },
				{ 10, 11, asList("0", "1") }, { 10, 12, asList("0", "1", "2") }, { 12, 20, singletonList("2") } };
	}

	@Test(singleThreaded = true, dataProvider = "findQueryOperatorRange")
	public void testFindQueryOperatorRange(int low, int high, List<Integer> expectedEntityIDs)
	{
		List<Entity> testEntities = createDynamic(3).collect(toList());
		searchService.index(testEntities, entityTypeDynamic, ADD);
		searchService.refreshIndex();

		Query<Entity> nestedQuery = new QueryImpl<>().rng(ATTR_INT, low, high);
		List<Object> foundAsList = searchService.searchAsStream(nestedQuery, entityTypeDynamic).map(
				Entity::getIdValue).collect(toList());
		assertEquals(foundAsList, expectedEntityIDs);
	}

	@DataProvider(name = "findQueryOperatorLike")
	private static Object[][] findQueryOperatorLike()
	{
		return new Object[][] { { "ring", asList("0", "1") }, { "Ring", asList("0", "1") },
				{ "nomatch", emptyList() } };
	}

	@Test(singleThreaded = true, dataProvider = "findQueryOperatorLike")
	public void testFindQueryOperatorLike(String likeStr, List<Integer> expectedEntityIDs)
	{
		List<Entity> testEntities = createDynamic(2).collect(toList());
		searchService.index(testEntities, entityTypeDynamic, ADD);
		searchService.refreshIndex();

		Query<Entity> nestedQuery = new QueryImpl<>().like(ATTR_STRING, likeStr);
		List<Object> foundAsList = searchService.searchAsStream(nestedQuery, entityTypeDynamic).map(
				Entity::getIdValue).collect(toList());
		assertEquals(foundAsList, expectedEntityIDs);
	}

	@DataProvider(name = "findQueryOperatorNot")
	private static Object[][] findQueryOperatorNot()
	{
		return new Object[][] { { 9, asList("0", "1", "2") }, { 10, asList("1", "2") }, { 11, asList("0", "2") },
				{ 12, asList("0", "1") }, { 13, asList("0", "1", "2") } };
	}

	@Test(singleThreaded = true, dataProvider = "findQueryOperatorNot")
	public void testFindQueryOperatorNot(int value, List<Integer> expectedEntityIDs)
	{
		List<Entity> testEntities = createDynamic(3).collect(toList());
		searchService.index(testEntities, entityTypeDynamic, ADD);
		searchService.refreshIndex();

		Query<Entity> nestedQuery = new QueryImpl<>().not().eq(ATTR_INT, value);
		List<Object> foundAsList = searchService.searchAsStream(nestedQuery, entityTypeDynamic).map(
				Entity::getIdValue).collect(toList());
		assertEquals(foundAsList, expectedEntityIDs);
	}

	@DataProvider(name = "findQueryOperatorAnd")
	private static Object[][] findQueryOperatorAnd()
	{
		return new Object[][] { { "string1", 10, asList("0") }, { "unknownString", 10, emptyList() },
				{ "string1", -1, emptyList() }, { "unknownString", -1, emptyList() } };
	}

	@Test(singleThreaded = true, dataProvider = "findQueryOperatorAnd")
	public void testFindQueryOperatorAnd(String strValue, int value, List<Integer> expectedEntityIDs)
	{
		List<Entity> testEntities = createDynamic(3).collect(toList());
		searchService.index(testEntities, entityTypeDynamic, ADD);
		searchService.refreshIndex();

		Query<Entity> nestedQuery = new QueryImpl<>().eq(ATTR_STRING, strValue).and().eq(ATTR_INT, value);
		List<Object> foundAsList = searchService.searchAsStream(nestedQuery, entityTypeDynamic).map(
				Entity::getIdValue).collect(toList());
		assertEquals(foundAsList, expectedEntityIDs);
	}

	@DataProvider(name = "findQueryOperatorOr")
	private static Object[][] findQueryOperatorOr()
	{
		return new Object[][] { { "string1", 10, asList("0", "1", "2") }, { "unknownString", 10, singletonList("0") },
				{ "string1", -1, asList("0", "1", "2") }, { "unknownString", -1, emptyList() } };
	}

	@Test(singleThreaded = true, dataProvider = "findQueryOperatorOr")
	public void testFindQueryOperatorOr(String strValue, int value, List<Integer> expectedEntityIDs)
	{
		List<Entity> testEntities = createDynamic(3).collect(toList());
		searchService.index(testEntities, entityTypeDynamic, ADD);
		searchService.refreshIndex();

		Query<Entity> nestedQuery = new QueryImpl<>().eq(ATTR_STRING, strValue).or().eq(ATTR_INT, value);
		List<Object> foundAsList = searchService.searchAsStream(nestedQuery, entityTypeDynamic).map(
				Entity::getIdValue).collect(toList());
		assertEquals(foundAsList, expectedEntityIDs);
	}

	@DataProvider(name = "findQueryOperatorNested")
	private static Object[][] findQueryOperatorNested()
	{
		return new Object[][] { { true, "string1", 10, asList("0", "2") },
				{ true, "unknownString", 10, singletonList("0") }, { true, "string1", -1, asList("0", "2") },
				{ true, "unknownString", -1, emptyList() }, { false, "string1", 10, singletonList("1") },
				{ false, "unknownString", 10, emptyList() }, { false, "string1", -1, singletonList("1") },
				{ false, "unknownString", -1, emptyList() } };
	}

	@Test(singleThreaded = true, dataProvider = "findQueryOperatorNested")
	public void testFindQueryOperatorNested(boolean boolValue, String strValue, int value,
			List<Integer> expectedEntityIDs)
	{
		List<Entity> testEntities = createDynamic(3).collect(toList());
		searchService.index(testEntities, entityTypeDynamic, ADD);
		searchService.refreshIndex();

		Query<Entity> nestedQuery = new QueryImpl<>().eq(ATTR_BOOL, boolValue).and().nest().eq(ATTR_STRING,
				strValue).or().eq(ATTR_INT, value).unnest();
		List<Object> foundAsList = searchService.searchAsStream(nestedQuery, entityTypeDynamic).map(
				Entity::getIdValue).collect(toList());
		assertEquals(foundAsList, expectedEntityIDs);
	}

	@DataProvider(name = "findQueryOperatorSearch")
	private static Object[][] findQueryOperatorSearch()
	{
		return new Object[][] { { "body", singletonList("1") }, { "head", singletonList("1") },
				{ "unknownString", emptyList() } };
	}

	@Test(singleThreaded = true, dataProvider = "findQueryOperatorSearch")
	public void testFindQueryOperatorSearch(String searchStr, List<Integer> expectedEntityIds)
	{
		List<Entity> testEntities = createDynamic(2).collect(toList());
		searchService.index(testEntities, entityTypeDynamic, ADD);
		searchService.refreshIndex();

		Query<Entity> query = new QueryImpl<>().search(ATTR_HTML, searchStr);
		List<Object> ids = searchService.searchAsStream(query, entityTypeDynamic).map(Entity::getIdValue).collect(
				toList());

		assertEquals(ids, expectedEntityIds);
	}

	@Test(singleThreaded = true)
	public void testSearchQueryLimit2_Offset2_sortOnInt()
	{
		List<Entity> testEntities = createDynamic(10).collect(toList());
		searchService.index(testEntities, entityTypeDynamic, ADD);
		searchService.refreshIndex();

		Query<Entity> query = new QueryImpl<>().pageSize(2).offset(2).sort(new Sort(ATTR_ID, Sort.Direction.DESC));
		Iterable<Entity> result = searchService.search(query, entityTypeDynamic);

		List<Object> ids = Lists.newArrayList(Iterables.transform(result, Entity::getIdValue));
		List<Object> expected = asList(testEntities.get(7).getIdValue(), testEntities.get(6).getIdValue());
		assertEquals(ids, expected);
	}

	@Test(singleThreaded = true)
	public void testSearchAsStreamQueryLimit2_Offset2_sortOnInt()
	{
		List<Entity> testEntities = createDynamic(10).collect(toList());
		searchService.index(testEntities, entityTypeDynamic, ADD);
		searchService.refreshIndex();

		Query<Entity> query = new QueryImpl<>().pageSize(2).offset(2).sort(new Sort(ATTR_ID, Sort.Direction.DESC));
		List<Object> ids = searchService.searchAsStream(query, entityTypeDynamic).map(Entity::getIdValue).collect(
				toList());

		List<Object> expected = asList(testEntities.get(7).getIdValue(), testEntities.get(6).getIdValue());
		assertEquals(ids, expected);
	}

	@Test(singleThreaded = true)
	public void testFindOneQuery()
	{
		Entity entity = createDynamic(1).findFirst().get();
		searchService.index(entity, entityTypeDynamic, ADD);
		searchService.refreshIndex();

		entity = searchService.findOne(new QueryImpl<>().eq(ATTR_ID, entity.getIdValue()), entityTypeDynamic);
		assertNotNull(entity);
	}

	private Stream<Entity> createDynamic(int count)
	{
		List<Entity> refEntities = testHarness.createTestRefEntities(refEntityTypeDynamic, 6);
		searchService.index(refEntities.stream(), refEntityTypeDynamic, ADD);
		return testHarness.createTestEntities(entityTypeDynamic, count, refEntities);
	}

	private void assertPresent(EntityType emd, Entity entity)
	{
		// Found in index Elasticsearch
		Query<Entity> q = new QueryImpl<>();
		q.eq(emd.getIdAttribute().getName(), entity.getIdValue());
		assertEquals(searchService.count(q, emd), 1);
	}

	private void assertNotPresent(Entity entity)
	{
		// Not found in index Elasticsearch
		Query<Entity> q = new QueryImpl<>();
		q.eq(entityTypeDynamic.getIdAttribute().getName(), entity.getIdValue());
		assertEquals(searchService.count(q, entityTypeDynamic), 0);
	}

}
