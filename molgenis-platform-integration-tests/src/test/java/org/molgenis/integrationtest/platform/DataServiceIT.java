package org.molgenis.integrationtest.platform;

import org.molgenis.data.*;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.index.job.IndexJobScheduler;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.data.staticentity.TestEntityStatic;
import org.molgenis.data.staticentity.TestEntityStaticMetaData;
import org.molgenis.data.staticentity.TestRefEntityStaticMetaData;
import org.molgenis.data.support.AggregateQueryImpl;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;
import static org.molgenis.data.EntityTestHarness.*;
import static org.molgenis.data.RepositoryCapability.*;
import static org.molgenis.data.security.EntityTypePermission.READ;
import static org.molgenis.data.util.MolgenisDateFormat.parseInstant;
import static org.molgenis.data.util.MolgenisDateFormat.parseLocalDate;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { PlatformITConfig.class })
@TestExecutionListeners(listeners = { WithSecurityContextTestExecutionListener.class })
@Transactional
public class DataServiceIT extends AbstractTestNGSpringContextTests
{
	private static final String USERNAME = "dataService-user";

	private static EntityType entityTypeDynamic;
	private static EntityType refEntityTypeDynamic;
	private static List<Entity> dynamicEntities;
	private static List<Entity> staticEntities;
	private static List<Entity> staticRefEntities;

	@Autowired
	private TestEntityStaticMetaData entityTypeStatic;
	@Autowired
	private TestRefEntityStaticMetaData refEntityTypeStatic;

	@Autowired
	private IndexJobScheduler indexJobScheduler;
	@Autowired
	private EntityTestHarness entityTestHarness;
	@Autowired
	private TestPermissionPopulator testPermissionPopulator;
	@Autowired
	private DataService dataService;

	@BeforeClass
	public void setUpBeforeClass() throws InterruptedException
	{
		// bootstrapper has finished but indexing of bootstrapped data might be in progress
		indexJobScheduler.waitForAllIndicesStable();

		runAsSystem(this::populate);
	}

	@AfterClass
	public void tearDownAfterClass()
	{
		runAsSystem(this::depopulate);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void testGetCapabilities()
	{
		Set<RepositoryCapability> capabilities = dataService.getCapabilities(entityTypeDynamic.getId());
		assertNotNull(capabilities);
		assertTrue(capabilities.containsAll(asList(MANAGABLE, QUERYABLE, WRITABLE, VALIDATE_REFERENCE_CONSTRAINT)));
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void testGetEntityType()
	{
		EntityType entityType = dataService.getEntityType(entityTypeDynamic.getId());
		assertNotNull(entityType);
		assertTrue(EntityUtils.equals(entityType, entityTypeDynamic));
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void testGetEntityNames()
	{
		Stream<String> names = dataService.getEntityTypeIds();
		assertNotNull(names);
		assertTrue(names.anyMatch(entityTypeDynamic.getId()::equals));
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void testGetMeta()
	{
		assertNotNull(dataService.getMeta());
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void testGetKnownRepository()
	{
		Repository<Entity> repo = dataService.getRepository(entityTypeDynamic.getId());
		assertNotNull(repo);
		assertEquals(repo.getName(), entityTypeDynamic.getId());
	}

	@WithMockUser(username = USERNAME)
	@Test(expectedExceptions = UnknownEntityTypeException.class)
	public void testGetUnknownRepository()
	{
		dataService.getRepository("bogus");
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void testHasRepository()
	{
		assertTrue(dataService.hasRepository(entityTypeDynamic.getId()));
		assertFalse(dataService.hasRepository("bogus"));
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void testIterator()
	{
		assertNotNull(dataService.iterator());
		Repository repo = dataService.getRepository(entityTypeDynamic.getId());

		// Repository equals not implemented: repository from dataService and dataService.getRepository are not the same
		assertTrue(StreamSupport.stream(dataService.spliterator(), false)
								.anyMatch(e -> repo.getName().equals(e.getName())));
	}

	@WithMockUser(username = USERNAME)
	@Test(expectedExceptions = UnknownEntityTypeException.class)
	public void testQuery()
	{
		assertNotNull(dataService.query(entityTypeDynamic.getId()));
		dataService.query("bogus");
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void testFindOne()
	{
		Entity entity = dynamicEntities.get(0);
		assertNotNull(dataService.findOneById(entityTypeDynamic.getId(), entity.getIdValue()));
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void testFindOneFetch()
	{
		Entity entity = dynamicEntities.get(0);
		assertNotNull(
				dataService.findOneById(entityTypeDynamic.getId(), entity.getIdValue(), new Fetch().field(ATTR_ID)));
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void testFindOneQuery()
	{
		Entity entity = dynamicEntities.get(0);
		entity = dataService.findOne(entityTypeDynamic.getId(), new QueryImpl<>().eq(ATTR_ID, entity.getIdValue()));
		assertNotNull(entity);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void testFindAll()
	{
		Stream<Entity> retrieved = dataService.findAll(entityTypeDynamic.getId());
		assertEquals(retrieved.count(), dynamicEntities.size());
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void testFindAllByIds()
	{
		Stream<Object> ids = Stream.concat(dynamicEntities.stream().map(Entity::getIdValue), of("bogus"));
		Stream<Entity> retrieved = dataService.findAll(entityTypeDynamic.getId(), ids);
		assertEquals(retrieved.count(), dynamicEntities.size());
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void testFindAllTyped()
	{
		Supplier<Stream<Entity>> retrieved = () -> dataService.findAll(entityTypeDynamic.getId(), Entity.class);
		assertEquals(retrieved.get().count(), dynamicEntities.size());
		assertEquals(retrieved.get().iterator().next().getIdValue(), dynamicEntities.get(0).getIdValue());
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void testFindAllStreamFetch()
	{
		Stream<Object> ids = concat(dynamicEntities.stream().map(Entity::getIdValue), of("bogus"));
		Stream<Entity> retrieved = dataService.findAll(entityTypeDynamic.getId(), ids, new Fetch().field(ATTR_ID));
		assertEquals(retrieved.count(), dynamicEntities.size());
	}

	@DataProvider(name = "findQueryOperatorEq")
	private static Object[][] findQueryOperatorEq()
	{
		return new Object[][] { { ATTR_ID, "1", singletonList(1) }, { ATTR_STRING, "string1", asList(0, 1, 2) },
				{ ATTR_BOOL, true, asList(0, 2) }, { ATTR_DATE, parseLocalDate("2012-12-21"), asList(0, 1, 2) },
				{ ATTR_DATETIME, parseInstant("1985-08-12T11:12:13+0500"), asList(0, 1, 2) },
				{ ATTR_DECIMAL, 1.123, singletonList(1) },
				{ ATTR_HTML, "<html>where is my head and where is my body</html>", singletonList(1) },
				{ ATTR_HYPERLINK, "http://www.molgenis.org", asList(0, 1, 2) },
				{ ATTR_LONG, 1000000L, singletonList(1) }, { ATTR_INT, 11, singletonList(1) },
				{ ATTR_SCRIPT, "/bin/blaat/script.sh", asList(0, 1, 2) },
				{ ATTR_EMAIL, "this.is@mail.address", asList(0, 1, 2) },
				// null checks
				{ ATTR_ID, null, emptyList() }, { ATTR_STRING, null, emptyList() }, { ATTR_BOOL, null, emptyList() },
				{ ATTR_CATEGORICAL, null, emptyList() }, { ATTR_CATEGORICAL_MREF, null, emptyList() },
				{ ATTR_DATE, null, emptyList() }, { ATTR_DATETIME, null, emptyList() },
				{ ATTR_DECIMAL, null, emptyList() }, { ATTR_HTML, null, asList(0, 2) },
				{ ATTR_HYPERLINK, null, emptyList() }, { ATTR_LONG, null, emptyList() },
				{ ATTR_INT, 11, singletonList(1) }, { ATTR_SCRIPT, null, emptyList() },
				{ ATTR_EMAIL, null, emptyList() }, { ATTR_XREF, null, emptyList() }, { ATTR_MREF, null, emptyList() } };
	}

	@WithMockUser(username = USERNAME)
	@Test(dataProvider = "findQueryOperatorEq")
	public void testFindQueryOperatorEq(String attrName, Object value, List<Integer> expectedEntityIndices)
	{

		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getId())
														  .eq(attrName, value)
														  .findAll();
		List<Entity> foundAsList = found.get().collect(toList());
		assertEquals(foundAsList.size(), expectedEntityIndices.size());
		for (int i = 0; i < expectedEntityIndices.size(); ++i)
		{
			assertTrue(EntityUtils.equals(foundAsList.get(i), dynamicEntities.get(expectedEntityIndices.get(i))));
		}
	}

	@DataProvider(name = "findQueryOperatorGreater")
	private static Object[][] findQueryOperatorGreater()
	{
		return new Object[][] { { 9, asList(0, 1, 2) }, { 10, asList(1, 2) }, { 11, singletonList(2) },
				{ 12, emptyList() } };
	}

	@WithMockUser(username = USERNAME)
	@Test(dataProvider = "findQueryOperatorGreater")
	public void testFindQueryOperatorGreater(int value, List<Integer> expectedEntityIndices)
	{
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getId())
														  .gt(ATTR_INT, value)
														  .findAll();
		List<Entity> foundAsList = found.get().collect(toList());
		assertEquals(foundAsList.size(), expectedEntityIndices.size());
		for (int i = 0; i < expectedEntityIndices.size(); ++i)
		{
			assertTrue(EntityUtils.equals(foundAsList.get(i), dynamicEntities.get(expectedEntityIndices.get(i))));
		}
	}

	@DataProvider(name = "findQueryOperatorGreaterEqual")
	private static Object[][] findQueryOperatorGreaterEqual()
	{
		return new Object[][] { { 9, asList(0, 1, 2) }, { 10, asList(0, 1, 2) }, { 11, asList(1, 2) },
				{ 12, singletonList(2) }, { 13, emptyList() } };
	}

	@WithMockUser(username = USERNAME)
	@Test(dataProvider = "findQueryOperatorGreaterEqual")
	public void testFindQueryOperatorGreaterEqual(int value, List<Integer> expectedEntityIndices)
	{
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getId())
														  .ge(ATTR_INT, value)
														  .findAll();
		List<Entity> foundAsList = found.get().collect(toList());
		assertEquals(foundAsList.size(), expectedEntityIndices.size());
		for (int i = 0; i < expectedEntityIndices.size(); ++i)
		{
			assertTrue(EntityUtils.equals(foundAsList.get(i), dynamicEntities.get(expectedEntityIndices.get(i))));
		}
	}

	@DataProvider(name = "findQueryOperatorRange")
	private static Object[][] findQueryOperatorRange()
	{
		return new Object[][] { { 0, 9, emptyList() }, { 0, 10, singletonList(0) }, { 10, 10, singletonList(0) },
				{ 10, 11, asList(0, 1) }, { 10, 12, asList(0, 1, 2) }, { 12, 20, singletonList(2) } };
	}

	@WithMockUser(username = USERNAME)
	@Test(dataProvider = "findQueryOperatorRange")
	public void testFindQueryOperatorRange(int low, int high, List<Integer> expectedEntityIndices)
	{
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getId())
														  .rng(ATTR_INT, low, high)
														  .findAll();
		List<Entity> foundAsList = found.get().collect(toList());
		assertEquals(foundAsList.size(), expectedEntityIndices.size());
		for (int i = 0; i < expectedEntityIndices.size(); ++i)
		{
			assertTrue(EntityUtils.equals(foundAsList.get(i), dynamicEntities.get(expectedEntityIndices.get(i))));
		}
	}

	@DataProvider(name = "findQueryOperatorNot")
	private static Object[][] findQueryOperatorNot()
	{
		return new Object[][] { { 9, asList(0, 1, 2) }, { 10, asList(1, 2) }, { 11, asList(0, 2) },
				{ 12, asList(0, 1) }, { 13, asList(0, 1, 2) } };
	}

	@WithMockUser(username = USERNAME)
	@Test(dataProvider = "findQueryOperatorNot")
	public void testFindQueryOperatorNot(int value, List<Integer> expectedEntityIndices)
	{
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getId())
														  .not()
														  .eq(ATTR_INT, value)
														  .findAll();
		List<Entity> foundAsList = found.get().collect(toList());
		assertEquals(foundAsList.size(), expectedEntityIndices.size());
		for (int i = 0; i < expectedEntityIndices.size(); ++i)
		{
			assertTrue(EntityUtils.equals(foundAsList.get(i), dynamicEntities.get(expectedEntityIndices.get(i))));
		}
	}

	@DataProvider(name = "findQueryOperatorAnd")
	private static Object[][] findQueryOperatorAnd()
	{
		return new Object[][] { { "string1", 10, singletonList(0) }, { "unknownString", 10, emptyList() },
				{ "string1", -1, emptyList() }, { "unknownString", -1, emptyList() } };
	}

	@WithMockUser(username = USERNAME)
	@Test(dataProvider = "findQueryOperatorAnd")
	public void testFindQueryOperatorAnd(String strValue, int value, List<Integer> expectedEntityIndices)
	{
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getId())
														  .eq(ATTR_STRING, strValue)
														  .and()
														  .eq(ATTR_INT, value)
														  .findAll();
		List<Entity> foundAsList = found.get().collect(toList());
		assertEquals(foundAsList.size(), expectedEntityIndices.size());
		for (int i = 0; i < expectedEntityIndices.size(); ++i)
		{
			assertTrue(EntityUtils.equals(foundAsList.get(i), dynamicEntities.get(expectedEntityIndices.get(i))));
		}
	}

	@DataProvider(name = "findQueryOperatorOr")
	private static Object[][] findQueryOperatorOr()
	{
		return new Object[][] { { "string1", 10, asList(0, 1, 2) }, { "unknownString", 10, singletonList(0) },
				{ "string1", -1, asList(0, 1, 2) }, { "unknownString", -1, emptyList() } };
	}

	@WithMockUser(username = USERNAME)
	@Test(dataProvider = "findQueryOperatorOr")
	public void testFindQueryOperatorOr(String strValue, int value, List<Integer> expectedEntityIndices)
	{
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getId())
														  .eq(ATTR_STRING, strValue)
														  .or()
														  .eq(ATTR_INT, value)
														  .findAll();
		List<Entity> foundAsList = found.get().collect(toList());
		assertEquals(foundAsList.size(), expectedEntityIndices.size());
		for (int i = 0; i < expectedEntityIndices.size(); ++i)
		{
			assertTrue(EntityUtils.equals(foundAsList.get(i), dynamicEntities.get(expectedEntityIndices.get(i))));
		}
	}

	@DataProvider(name = "findQueryOperatorNested")
	private static Object[][] findQueryOperatorNested()
	{
		return new Object[][] { { true, "string1", 10, asList(0, 2) }, { true, "unknownString", 10, singletonList(0) },
				{ true, "string1", -1, asList(0, 2) }, { true, "unknownString", -1, emptyList() },
				{ false, "string1", 10, singletonList(1) }, { false, "unknownString", 10, emptyList() },
				{ false, "string1", -1, singletonList(1) }, { false, "unknownString", -1, emptyList() } };
	}

	@WithMockUser(username = USERNAME)
	@Test(dataProvider = "findQueryOperatorNested")
	public void testFindQueryOperatorNested(boolean boolValue, String strValue, int value,
			List<Integer> expectedEntityIndices)
	{
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getId())
														  .eq(ATTR_BOOL, boolValue)
														  .and()
														  .nest()
														  .eq(ATTR_STRING, strValue)
														  .or()
														  .eq(ATTR_INT, value)
														  .unnest()
														  .findAll();
		List<Entity> foundAsList = found.get().collect(toList());
		assertEquals(foundAsList.size(), expectedEntityIndices.size());
		for (int i = 0; i < expectedEntityIndices.size(); ++i)
		{
			assertTrue(EntityUtils.equals(foundAsList.get(i), dynamicEntities.get(expectedEntityIndices.get(i))));
		}
	}

	@DataProvider(name = "findQueryOperatorLess")
	private static Object[][] findQueryOperatorLess()
	{
		return new Object[][] { { 9, emptyList() }, { 10, emptyList() }, { 11, singletonList(0) }, { 12, asList(0, 1) },
				{ 13, asList(0, 1, 2) } };
	}

	@WithMockUser(username = USERNAME)
	@Test(dataProvider = "findQueryOperatorLess")
	public void testFindQueryOperatorLess(int value, List<Integer> expectedEntityIndices)
	{
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getId())
														  .lt(ATTR_INT, value)
														  .findAll();
		List<Entity> foundAsList = found.get().collect(toList());
		assertEquals(foundAsList.size(), expectedEntityIndices.size());
		for (int i = 0; i < expectedEntityIndices.size(); ++i)
		{
			assertTrue(EntityUtils.equals(foundAsList.get(i), dynamicEntities.get(expectedEntityIndices.get(i))));
		}
	}

	@DataProvider(name = "findQueryOperatorLessEqual")
	private static Object[][] findQueryOperatorLessEqual()
	{
		return new Object[][] { { 9, emptyList() }, { 10, singletonList(0) }, { 11, asList(0, 1) },
				{ 12, asList(0, 1, 2) } };
	}

	@WithMockUser(username = USERNAME)
	@Test(dataProvider = "findQueryOperatorLessEqual")
	public void testFindQueryOperatorLessEqual(int value, List<Integer> expectedEntityIndices)
	{
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getId())
														  .le(ATTR_INT, value)
														  .findAll();
		List<Entity> foundAsList = found.get().collect(toList());
		assertEquals(foundAsList.size(), expectedEntityIndices.size());
		for (int i = 0; i < expectedEntityIndices.size(); ++i)
		{
			assertTrue(EntityUtils.equals(foundAsList.get(i), dynamicEntities.get(expectedEntityIndices.get(i))));
		}
	}

	@DataProvider(name = "findQueryOperatorLike")
	private static Object[][] findQueryOperatorLike()
	{
		return new Object[][] { { "ring", asList(0, 1, 2) }, { "Ring", emptyList() }, { "nomatch", emptyList() } };
	}

	@WithMockUser(username = USERNAME)
	@Test(dataProvider = "findQueryOperatorLike")
	public void testFindQueryOperatorLike(String likeStr, List<Integer> expectedEntityIndices)
	{
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getId())
														  .like(ATTR_STRING, likeStr)
														  .findAll();
		List<Entity> foundAsList = found.get().collect(toList());
		assertEquals(foundAsList.size(), expectedEntityIndices.size());
		for (int i = 0; i < expectedEntityIndices.size(); ++i)
		{
			assertTrue(EntityUtils.equals(foundAsList.get(i), dynamicEntities.get(expectedEntityIndices.get(i))));
		}
	}

	@DataProvider(name = "findQueryOperatorIn")
	private static Object[][] findQueryOperatorIn()
	{
		return new Object[][] { { singletonList("-1"), emptyList() }, { asList("-1", "0"), singletonList(0) },
				{ asList("0", "1"), asList(0, 1) }, { asList("1", "2", "3"), asList(1, 2) } };
	}

	@WithMockUser(username = USERNAME)
	@Test(dataProvider = "findQueryOperatorIn")
	public void testFindQueryOperatorIn(List<String> ids, List<Integer> expectedEntityIndices)
	{
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getId()).in(ATTR_ID, ids).findAll();
		List<Entity> foundAsList = found.get().collect(toList());
		assertEquals(foundAsList.size(), expectedEntityIndices.size());
		for (int i = 0; i < expectedEntityIndices.size(); ++i)
		{
			assertTrue(EntityUtils.equals(foundAsList.get(i), dynamicEntities.get(expectedEntityIndices.get(i))));
		}
	}

	@DataProvider(name = "findQueryOperatorSearch")
	private static Object[][] findQueryOperatorSearch()
	{
		return new Object[][] { { "body", singletonList(1) }, { "head", singletonList(1) },
				{ "unknownString", emptyList() } };
	}

	@WithMockUser(username = USERNAME)
	@Test(dataProvider = "findQueryOperatorSearch")
	public void testFindQueryOperatorSearch(String searchStr, List<Integer> expectedEntityIndices)
	{
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getId())
														  .search(ATTR_HTML, searchStr)
														  .findAll();
		List<Entity> foundAsList = found.get().collect(toList());
		assertEquals(foundAsList.size(), expectedEntityIndices.size());
		for (int i = 0; i < expectedEntityIndices.size(); ++i)
		{
			assertTrue(EntityUtils.equals(foundAsList.get(i), dynamicEntities.get(expectedEntityIndices.get(i))));
		}
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void testFindQueryLimitOffsetSort()
	{
		List<Entity> foundAsList = dataService.findAll(entityTypeDynamic.getId(),
				new QueryImpl<>().pageSize(2).offset(1).sort(new Sort(ATTR_ID, Sort.Direction.DESC))).collect(toList());
		assertEquals(foundAsList.size(), 2);
		assertTrue(EntityUtils.equals(foundAsList.get(0), dynamicEntities.get(1)));
		assertTrue(EntityUtils.equals(foundAsList.get(1), dynamicEntities.get(0)));
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void testFindQueryTypedStatic()
	{
		Supplier<Stream<TestEntityStatic>> found = () -> dataService.findAll(entityTypeStatic.getId(),
				new QueryImpl<TestEntityStatic>().eq(ATTR_ID, staticEntities.get(0).getIdValue()),
				TestEntityStatic.class);
		assertEquals(found.get().count(), 1);
		assertEquals(found.get().findFirst().get().getId(), staticEntities.get(0).getIdValue());
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void testFindOneTypedStatic()
	{
		Entity entity = staticEntities.get(0);
		TestEntityStatic testEntityStatic = dataService.findOneById(entityTypeStatic.getId(), entity.getIdValue(),
				TestEntityStatic.class);
		assertNotNull(testEntityStatic);
		assertEquals(testEntityStatic.getId(), entity.getIdValue());
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void testFindOneFetchTypedStatic()
	{
		Entity entity = staticEntities.get(0);
		TestEntityStatic testEntityStatic = dataService.findOneById(entityTypeStatic.getId(), entity.getIdValue(),
				new Fetch().field(ATTR_ID), TestEntityStatic.class);
		assertNotNull(testEntityStatic);
		assertEquals(testEntityStatic.getIdValue(), entity.getIdValue());
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void testFindOneQueryTypedStatic()
	{
		Entity entity = staticEntities.get(0);
		TestEntityStatic testEntityStatic = dataService.findOne(entityTypeStatic.getId(),
				new QueryImpl<TestEntityStatic>().eq(ATTR_ID, entity.getIdValue()), TestEntityStatic.class);
		assertNotNull(testEntityStatic);
		assertEquals(testEntityStatic.getId(), entity.getIdValue());
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void testFindAllByIdsTyped()
	{
		Supplier<Stream<TestEntityStatic>> retrieved = () -> dataService.findAll(entityTypeStatic.getId(),
				Stream.concat(staticEntities.stream().map(Entity::getIdValue), of("bogus")), TestEntityStatic.class);
		assertEquals(retrieved.get().count(), staticEntities.size());
		assertEquals(retrieved.get().iterator().next().getId(), staticEntities.get(0).getIdValue());
		assertEquals(retrieved.get().iterator().next().getIdValue(), staticEntities.get(0).getIdValue());
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void testAggregateOneDimensional()
	{
		AggregateQuery aggregateQuery = new AggregateQueryImpl().query(new QueryImpl<>())
																.attrX(entityTypeDynamic.getAttribute(ATTR_BOOL));
		AggregateResult result = dataService.aggregate(entityTypeDynamic.getId(), aggregateQuery);

		AggregateResult expectedResult = new AggregateResult(asList(singletonList(1L), singletonList(2L)),
				asList(0L, 1L), emptyList());
		assertEquals(result, expectedResult);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void testAggregateOneDimensionalDistinct()
	{
		AggregateQuery aggregateQuery = new AggregateQueryImpl().query(new QueryImpl<>())
																.attrX(entityTypeDynamic.getAttribute(ATTR_BOOL))
																.attrDistinct(
																		entityTypeDynamic.getAttribute(ATTR_ENUM));
		AggregateResult result = dataService.aggregate(entityTypeDynamic.getId(), aggregateQuery);

		AggregateResult expectedResult = new AggregateResult(asList(singletonList(1L), singletonList(1L)),
				asList(0L, 1L), emptyList());
		assertEquals(result, expectedResult);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void testAggregateTwoDimensional()
	{
		AggregateQuery aggregateQuery = new AggregateQueryImpl().query(new QueryImpl<>())
																.attrX(entityTypeDynamic.getAttribute(ATTR_BOOL))
																.attrY(entityTypeDynamic.getAttribute(ATTR_ENUM));
		AggregateResult result = dataService.aggregate(entityTypeDynamic.getId(), aggregateQuery);

		AggregateResult expectedResult = new AggregateResult(asList(asList(0L, 1L), asList(2L, 0L)), asList(0L, 1L),
				asList("option1", "option2"));
		assertEquals(result, expectedResult);
	}

	@WithMockUser(username = USERNAME)
	@Test(singleThreaded = true)
	public void testAggregateTwoDimensionalDistinct()
	{
		AggregateQuery aggregateQuery = new AggregateQueryImpl().query(new QueryImpl<>())
																.attrX(entityTypeDynamic.getAttribute(ATTR_BOOL))
																.attrY(entityTypeDynamic.getAttribute(ATTR_BOOL))
																.attrDistinct(
																		entityTypeDynamic.getAttribute(ATTR_ENUM));
		AggregateResult result = dataService.aggregate(entityTypeDynamic.getId(), aggregateQuery);
		AggregateResult expectedResult = new AggregateResult(asList(asList(1L, 0L), asList(0L, 1L)), asList(0L, 1L),
				asList(0L, 1L));
		assertEquals(result, expectedResult);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void testAggregateTwoDimensionalQuery()
	{
		AggregateQuery aggregateQuery = new AggregateQueryImpl().query(new QueryImpl<>())
																.attrX(entityTypeDynamic.getAttribute(ATTR_BOOL))
																.attrY(entityTypeDynamic.getAttribute(ATTR_BOOL))
																.query(new QueryImpl<>().gt(ATTR_INT, 10));
		AggregateResult result = dataService.aggregate(entityTypeDynamic.getId(), aggregateQuery);

		AggregateResult expectedResult = new AggregateResult(asList(asList(1L, 0L), asList(0L, 1L)), asList(0L, 1L),
				asList(0L, 1L));
		assertEquals(result, expectedResult);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void testAggregateTwoDimensionalQueryDistinct()
	{
		AggregateQuery aggregateQuery = new AggregateQueryImpl().query(new QueryImpl<>())
																.attrX(entityTypeDynamic.getAttribute(ATTR_BOOL))
																.attrY(entityTypeDynamic.getAttribute(ATTR_ENUM))
																.attrDistinct(entityTypeDynamic.getAttribute(ATTR_ENUM))
																.query(new QueryImpl<>().gt(ATTR_INT, 1));
		AggregateResult result = dataService.aggregate(entityTypeDynamic.getId(), aggregateQuery);

		AggregateResult expectedResult = new AggregateResult(asList(asList(0L, 1L), asList(1L, 0L)), asList(0L, 1L),
				asList("option1", "option2"));
		assertEquals(result, expectedResult);
	}

	private void populate()
	{
		populateData();
		populateDataPermissions();

		try
		{
			indexJobScheduler.waitForAllIndicesStable();
		}
		catch (InterruptedException e)
		{
			throw new RuntimeException(e);
		}
	}

	private void populateData()
	{
		refEntityTypeDynamic = entityTestHarness.createDynamicRefEntityType();
		dataService.getMeta().createRepository(refEntityTypeDynamic);
		List<Entity> refEntities = entityTestHarness.createTestRefEntities(refEntityTypeDynamic, 3);
		dataService.add(refEntityTypeDynamic.getId(), refEntities.stream());

		entityTypeDynamic = entityTestHarness.createDynamicTestEntityType(refEntityTypeDynamic);
		dataService.getMeta().createRepository(entityTypeDynamic);
		dynamicEntities = entityTestHarness.createTestEntities(entityTypeDynamic, 3, refEntities).collect(toList());
		dataService.add(entityTypeDynamic.getId(), dynamicEntities.stream());

		staticRefEntities = entityTestHarness.createTestRefEntities(refEntityTypeStatic, 3);
		dataService.add(refEntityTypeStatic.getId(), staticRefEntities.stream());
		staticEntities = entityTestHarness.createTestEntities(entityTypeStatic, 3, staticRefEntities).collect(toList());
		dataService.add(entityTypeStatic.getId(), staticEntities.stream());
	}

	private void populateDataPermissions()
	{
		Map<String, EntityTypePermission> entityTypePermissionMap = new HashMap<>();
		entityTypePermissionMap.put("sys_md_Package", READ);
		entityTypePermissionMap.put("sys_md_EntityType", READ);
		entityTypePermissionMap.put("sys_md_Attribute", READ);
		entityTypePermissionMap.put("sys_dec_DecoratorConfiguration", READ);
		entityTypePermissionMap.put(entityTypeDynamic.getId(), READ);
		entityTypePermissionMap.put(refEntityTypeDynamic.getId(), READ);
		entityTypePermissionMap.put(entityTypeStatic.getId(), READ);
		entityTypePermissionMap.put(refEntityTypeStatic.getId(), READ);
		testPermissionPopulator.populate(entityTypePermissionMap, USERNAME);
	}

	private void depopulate()
	{
		dataService.getMeta().deleteEntityType(asList(entityTypeDynamic, refEntityTypeDynamic));
		dataService.delete(entityTypeStatic.getId(), staticEntities.stream());
		dataService.delete(refEntityTypeStatic.getId(), staticRefEntities.stream());
		try
		{
			indexJobScheduler.waitForAllIndicesStable();
		}
		catch (InterruptedException e)
		{
			throw new RuntimeException(e);
		}
	}
}
