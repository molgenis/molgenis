package org.molgenis.integrationtest.platform;

import org.apache.lucene.search.Explanation;
import org.molgenis.data.*;
import org.molgenis.data.elasticsearch.ElasticsearchService;
import org.molgenis.data.index.exception.UnknownIndexException;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.semanticsearch.explain.bean.ExplainedQueryString;
import org.molgenis.semanticsearch.explain.service.ElasticSearchExplainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.EntityTestHarness.*;
import static org.molgenis.data.QueryRule.Operator.*;
import static org.molgenis.data.util.MolgenisDateFormat.parseInstant;
import static org.molgenis.data.util.MolgenisDateFormat.parseLocalDate;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { PlatformITConfig.class })
public class SearchServiceIT extends AbstractTestNGSpringContextTests
{
	private static EntityType entityTypeDynamic;
	private static EntityType refEntityTypeDynamic;

	@Autowired
	private EntityTestHarness testHarness;
	@Autowired
	private ElasticsearchService searchService;
	@Autowired
	private ElasticSearchExplainService explainService;

	@BeforeMethod
	public void setUp()
	{
		searchService.refreshIndex();
		refEntityTypeDynamic = testHarness.createDynamicRefEntityType();
		entityTypeDynamic = testHarness.createDynamicTestEntityType(refEntityTypeDynamic);

		searchService.createIndex(refEntityTypeDynamic);
		searchService.createIndex(entityTypeDynamic);
	}

	@AfterMethod
	public void afterMethod()
	{
		try
		{
			searchService.deleteIndex(entityTypeDynamic);
		}
		catch (UnknownIndexException e)
		{ // silently ignore
		}
		try
		{
			searchService.deleteIndex(refEntityTypeDynamic);
		}
		catch (UnknownIndexException e)
		{ // silently ignore
		}
		searchService.refreshIndex();
	}

	@Test
	public void testFuzzyMatch()
	{
		// Fuzzy match is used to find child nodes of an OntologyTerm.
		List<Entity> ontologyTerms = createDynamic(6).collect(toList());
		ontologyTerms.get(0).getEntity(ATTR_XREF).set(ATTR_REF_STRING, "0[0]");
		ontologyTerms.get(1).getEntity(ATTR_XREF).set(ATTR_REF_STRING, "0[0].1[1]");
		ontologyTerms.get(2).getEntity(ATTR_XREF).set(ATTR_REF_STRING, "0[0].1[1].0[2]");
		ontologyTerms.get(3).getEntity(ATTR_XREF).set(ATTR_REF_STRING, "0[0].1[1].1[2]");
		ontologyTerms.get(4).getEntity(ATTR_XREF).set(ATTR_REF_STRING, "0[0].1[1].1[2].0[3]");
		ontologyTerms.get(5).getEntity(ATTR_XREF).set(ATTR_REF_STRING, "0[0].1[1].1[2].0[3]");

		Entity ontology1 = ontologyTerms.get(0).getEntity(ATTR_CATEGORICAL);
		Entity ontology2 = ontologyTerms.get(1).getEntity(ATTR_CATEGORICAL);
		for (Entity term : ontologyTerms)
		{
			term.set(ATTR_CATEGORICAL, ontology1);
		}
		ontologyTerms.get(5).set(ATTR_CATEGORICAL, ontology2);

		searchService.index(entityTypeDynamic, ontologyTerms.stream());
		searchService.refreshIndex();

		Query<Entity> query = new QueryImpl<>(new QueryRule(ATTR_XREF, FUZZY_MATCH, "\"0[0].1[1]\"")).and()
																									 .eq(ATTR_CATEGORICAL,
																											 ontology1);
		List<Object> ids = searchService.search(entityTypeDynamic, query).collect(toList());

		assertEquals(ids, asList("1", "2", "3", "4"));
	}

	@Test
	public void testSemanticSearch()
	{
		List<Entity> attributes = createDynamic(6).collect(toList());
		attributes.get(0).set(ATTR_STRING, "High chance of pulmonary disease");
		attributes.get(1).set(ATTR_STRING, "And now for something completely different...");
		attributes.get(2).set(ATTR_STRING, "Are you taking hypertensive medication?");
		attributes.get(3).set(ATTR_STRING, "Have you ever had high blood pressure? (Repeat) (1)");
		attributes.get(4).set(ATTR_STRING, "Do you suffer from Ocular hypertension?");
		attributes.get(5).set(ATTR_STRING, "Do you have a vascular disorder?");

		Entity ontology1 = attributes.get(0).getEntity(ATTR_CATEGORICAL);
		Entity ontology2 = attributes.get(1).getEntity(ATTR_CATEGORICAL);
		for (Entity term : attributes)
		{
			term.set(ATTR_CATEGORICAL, ontology1);
		}
		attributes.get(5).set(ATTR_CATEGORICAL, ontology2);

		searchService.index(entityTypeDynamic, attributes.stream());
		searchService.refreshIndex();

		List<String> queryTerms = asList("hypertension", "disorder vascular hypertensive", "increased pressure blood",
				"high pressure blood", "ocular^0.5 hypertension^0.5",
				"hypertension^0.25 idiopathic^0.25 pulmonary^0.25");

		QueryRule finalDisMaxQuery = new QueryRule(queryTerms.stream()
															 .flatMap(term -> Stream.of(
																	 new QueryRule(ATTR_STRING, FUZZY_MATCH, term),
																	 new QueryRule(ATTR_SCRIPT, FUZZY_MATCH, term)))
															 .collect(toList()));
		finalDisMaxQuery.setOperator(DIS_MAX);

		List<String> attributeIds = asList("0", "1", "2", "3", "4", "5");
		Query<Entity> query = new QueryImpl<>(
				asList(new QueryRule(ATTR_ID, IN, attributeIds), new QueryRule(AND), finalDisMaxQuery));

		List<Object> matchingAttributeIDs = searchService.search(entityTypeDynamic, query).collect(toList());
		assertEquals(matchingAttributeIDs.get(0), "3");
		assertEquals(matchingAttributeIDs.get(1), "5");
		assertFalse(matchingAttributeIDs.contains("1"));

		List<Explanation> explanations = attributeIds.stream()
													 .map(id -> explainService.explain(query, entityTypeDynamic, id))
													 .collect(toList());

		List<Float> scores = explanations.stream().map(Explanation::getValue).collect(toList());
		// FIXME these scores vary between runs
		// assertEquals(scores, asList(0.3463153, 0, 0.7889965, 1.7814579, 0.76421005, 1.0707202));

		Map<String, String> expandedQueryMap = new HashMap<>();
		for (String term : asList("hypertens", "disord vascular hypertens", "increased pressur blood",
				"high pressur blood", "ocular hypertens", "hypertens idiopathic pulmonary"))
		{
			expandedQueryMap.put(term, "hypertension");
		}
		List<Set<ExplainedQueryString>> explanationStrings = explanations.stream()
																		 .map(explanation -> explainService.findQueriesFromExplanation(
																				 expandedQueryMap, explanation))
																		 .collect(toList());

		List<Set<ExplainedQueryString>> expectedExplanationStrings = asList(
				// High chance of pulmonary disease
				singleton(ExplainedQueryString.create("high", "high pressur blood", "hypertension", 41.66666666666667)),
				// And now for something completely different...
				emptySet(),
				// Are you taking hypertensive medication?
				singleton(ExplainedQueryString.create("hypertens", "hypertens", "hypertension", 100.0)),
				// Have you ever had high blood pressure? (Repeat) (1)
				singleton(
						ExplainedQueryString.create("high pressur blood", "high pressur blood", "hypertension", 100.0)),
				// Do you suffer from Ocular hypertension?
				singleton(ExplainedQueryString.create("ocular hypertens", "ocular hypertens", "hypertension", 100.0)),
				// Do you have a vascular disorder?
				singleton(ExplainedQueryString.create("disord vascular", "disord vascular hypertens", "hypertension",
						78.04878048780488)));

		assertEquals(explanationStrings, expectedExplanationStrings);
	}

	@Test(singleThreaded = true)
	public void testIndex() throws InterruptedException
	{
		createAndIndexEntities(2);

		assertEquals(searchService.count(entityTypeDynamic), 2);
	}

	@Test(singleThreaded = true)
	public void testCount()
	{
		createAndIndexEntities(2);

		assertEquals(searchService.count(entityTypeDynamic, new QueryImpl<>()), 2);
		assertEquals(searchService.count(entityTypeDynamic), 2);
	}

	@Test(singleThreaded = true)
	public void testDelete()
	{
		Entity entity = createAndIndexEntities(1).get(0);

		searchService.delete(entityTypeDynamic, entity);
		searchService.refreshIndex();
		assertEquals(searchService.count(entityTypeDynamic), 0);
	}

	@Test(singleThreaded = true)
	public void testDeleteById()
	{
		Entity entity = createAndIndexEntities(1).get(0);

		searchService.deleteById(entityTypeDynamic, entity.getIdValue());
		searchService.refreshIndex();
		assertEquals(searchService.count(entityTypeDynamic), 0);
	}

	@Test(singleThreaded = true)
	public void testDeleteStream()
	{
		List<Entity> entities = createAndIndexEntities(2);

		searchService.delete(entityTypeDynamic, entities.stream());
		searchService.refreshIndex();
		assertEquals(searchService.count(entityTypeDynamic), 0);
	}

	@Test(singleThreaded = true, expectedExceptions = UnknownIndexException.class)
	public void testDeleteAll()
	{
		createAndIndexEntities(5);

		searchService.deleteIndex(entityTypeDynamic);
		searchService.refreshIndex();
		assertEquals(searchService.count(entityTypeDynamic), 0);
	}

	@Test(singleThreaded = true)
	public void testFindAllEmpty()
	{
		long count = searchService.search(entityTypeDynamic, new QueryImpl<>()).count();
		assertEquals(count, 0L);
	}

	@Test(singleThreaded = true)
	public void testFindAll()
	{
		List<Entity> entities = createAndIndexEntities(5);

		long count = searchService.search(entityTypeDynamic, new QueryImpl<>()).count();
		assertEquals(count, entities.size());
	}

	@Test(singleThreaded = true)
	public void testFindAllStreaming()
	{
		createAndIndexEntities(3);

		Supplier<Stream<Object>> retrieved = () -> searchService.search(entityTypeDynamic, new QueryImpl<>());
		assertEquals(retrieved.get().count(), 3);
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
		createAndIndexEntities(3);

		Query<Entity> query = new QueryImpl<>().eq(attrName, value);
		List<Object> ids = searchService.search(entityTypeDynamic, query).collect(toList());

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
		createAndIndexEntities(2);

		Query<Entity> query = new QueryImpl<>().in(ATTR_ID, ids);
		List<Object> foundIds = searchService.search(entityTypeDynamic, query).collect(toList());

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
		createAndIndexEntities(5);

		Query<Entity> query = new QueryImpl<>().lt(ATTR_INT, value);
		List<Object> foundIds = searchService.search(entityTypeDynamic, query).collect(toList());

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
		createAndIndexEntities(5);

		Query<Entity> query = new QueryImpl<>().le(ATTR_INT, value);
		List<Object> foundIds = searchService.search(entityTypeDynamic, query).collect(toList());

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
		createAndIndexEntities(3);

		Query<Entity> query = new QueryImpl<>().gt(ATTR_INT, value);
		List<Object> foundIds = searchService.search(entityTypeDynamic, query).collect(toList());

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
		createAndIndexEntities(3);

		Query<Entity> query = new QueryImpl<>().ge(ATTR_INT, value);
		List<Object> foundIds = searchService.search(entityTypeDynamic, query).collect(toList());

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
		createAndIndexEntities(3);

		Query<Entity> nestedQuery = new QueryImpl<>().rng(ATTR_INT, low, high);
		List<Object> foundAsList = searchService.search(entityTypeDynamic, nestedQuery).collect(toList());
		assertEquals(foundAsList, expectedEntityIDs);
	}

	@DataProvider(name = "findQueryOperatorLike")
	private static Object[][] findQueryOperatorLike()
	{
		return new Object[][] { { "stri", asList("0", "1") }, { "Stri", asList("0", "1") },
				{ "nomatch", emptyList() } };
	}

	@Test(singleThreaded = true, dataProvider = "findQueryOperatorLike")
	public void testFindQueryOperatorLike(String likeStr, List<Integer> expectedEntityIDs)
	{
		createAndIndexEntities(2);

		Query<Entity> nestedQuery = new QueryImpl<>().like(ATTR_STRING, likeStr);
		List<Object> foundAsList = searchService.search(entityTypeDynamic, nestedQuery).collect(toList());
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
		createAndIndexEntities(3);

		Query<Entity> nestedQuery = new QueryImpl<>().not().eq(ATTR_INT, value);
		List<Object> foundAsList = searchService.search(entityTypeDynamic, nestedQuery).collect(toList());
		assertEquals(foundAsList, expectedEntityIDs);
	}

	@DataProvider(name = "findQueryOperatorAnd")
	private static Object[][] findQueryOperatorAnd()
	{
		return new Object[][] { { "string1", 10, singletonList("0") }, { "unknownString", 10, emptyList() },
				{ "string1", -1, emptyList() }, { "unknownString", -1, emptyList() } };
	}

	@Test(singleThreaded = true, dataProvider = "findQueryOperatorAnd")
	public void testFindQueryOperatorAnd(String strValue, int value, List<Integer> expectedEntityIDs)
	{
		createAndIndexEntities(3);

		Query<Entity> nestedQuery = new QueryImpl<>().eq(ATTR_STRING, strValue).and().eq(ATTR_INT, value);
		List<Object> foundAsList = searchService.search(entityTypeDynamic, nestedQuery).collect(toList());
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
		createAndIndexEntities(3);

		Query<Entity> nestedQuery = new QueryImpl<>().eq(ATTR_STRING, strValue).or().eq(ATTR_INT, value);
		List<Object> foundAsList = searchService.search(entityTypeDynamic, nestedQuery).collect(toList());
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
		createAndIndexEntities(3);

		Query<Entity> nestedQuery = new QueryImpl<>().eq(ATTR_BOOL, boolValue)
													 .and()
													 .nest()
													 .eq(ATTR_STRING, strValue)
													 .or()
													 .eq(ATTR_INT, value)
													 .unnest();
		List<Object> foundAsList = searchService.search(entityTypeDynamic, nestedQuery).collect(toList());
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
		createAndIndexEntities(2);

		Query<Entity> query = new QueryImpl<>().search(ATTR_HTML, searchStr);
		List<Object> ids = searchService.search(entityTypeDynamic, query).collect(toList());

		assertEquals(ids, expectedEntityIds);
	}

	@Test(singleThreaded = true)
	public void testSearchRanking()
	{
		List<Entity> entities = createDynamic(5).collect(toList());
		entities.get(0).set(ATTR_STRING, "ligament carcinoma");
		entities.get(1).set(ATTR_STRING, "cars in omaha");
		entities.get(2).set(ATTR_STRING, "multiple carcinomas");
		entities.get(3).set(ATTR_STRING, "and now for something completely different");
		searchService.index(entityTypeDynamic, entities.stream());
		searchService.refreshIndex();

		Query<Entity> query = new QueryImpl<>().search(ATTR_STRING, "carcinoma");
		List<Object> foundIds = searchService.search(entityTypeDynamic, query).collect(toList());

		assertEquals(foundIds, asList("0", "2"));
	}

	@Test(singleThreaded = true)
	public void testSearchRankingMultipleWords()
	{
		List<Entity> entities = createDynamic(50).collect(toList());
		entities.get(0).set(ATTR_STRING, "ligament carcinoma");
		entities.get(1).set(ATTR_STRING, "cars in omaha");
		entities.get(2).set(ATTR_STRING, "multiple carcinomas");
		entities.get(3).set(ATTR_STRING, "and now for something completely different");
		searchService.index(entityTypeDynamic, entities.stream());
		searchService.refreshIndex();

		Query<Entity> query = new QueryImpl<>().search(ATTR_STRING, "car carcinoma");
		List<Object> foundIds = searchService.search(entityTypeDynamic, query).collect(toList());

		assertEquals(foundIds, asList("0", "2", "1"));
	}

	@Test(singleThreaded = true)
	public void testSearchQueryLimit2_Offset2_sortOnInt()
	{
		List<Entity> testEntities = createAndIndexEntities(10);

		Query<Entity> query = new QueryImpl<>().pageSize(2).offset(2).sort(new Sort(ATTR_ID, Sort.Direction.DESC));
		List<Object> ids = searchService.search(entityTypeDynamic, query).collect(toList());
		List<Object> expected = asList(testEntities.get(7).getIdValue(), testEntities.get(6).getIdValue());
		assertEquals(ids, expected);
	}

	@Test(singleThreaded = true)
	public void testSearchAsStreamQueryLimit2_Offset2_sortOnInt()
	{
		List<Entity> testEntities = createAndIndexEntities(10);

		Query<Entity> query = new QueryImpl<>().pageSize(2).offset(2).sort(new Sort(ATTR_ID, Sort.Direction.DESC));
		List<Object> ids = searchService.search(entityTypeDynamic, query).collect(toList());

		List<Object> expected = asList(testEntities.get(7).getIdValue(), testEntities.get(6).getIdValue());
		assertEquals(ids, expected);
	}

	@Test(singleThreaded = true)
	public void testFindOneQuery()
	{
		Entity entity = createAndIndexEntities(1).get(0);

		Object entityId = searchService.searchOne(entityTypeDynamic,
				new QueryImpl<>().eq(ATTR_ID, entity.getIdValue()));
		assertNotNull(entityId);
	}

	private List<Entity> createAndIndexEntities(int count)
	{
		List<Entity> entities = createDynamic(count).collect(toList());
		searchService.index(entityTypeDynamic, entities.stream());
		searchService.refreshIndex();
		return entities;
	}

	private Stream<Entity> createDynamic(int count)
	{
		List<Entity> refEntities = testHarness.createTestRefEntities(refEntityTypeDynamic, 6);
		searchService.index(refEntityTypeDynamic, refEntities.stream());
		return testHarness.createTestEntities(entityTypeDynamic, count, refEntities);
	}
}
