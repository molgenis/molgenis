package org.molgenis.data.elasticsearch.generator;

import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.mockito.quality.Strictness;
import org.molgenis.data.*;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.QueryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;

import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.elasticsearch.FieldConstants.DEFAULT_ANALYZER;
import static org.molgenis.data.elasticsearch.FieldConstants.FIELD_NOT_ANALYZED;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LABEL;
import static org.testng.Assert.assertEquals;

/**
 * Copy of standard QueryGeneratorTest but now with queries done on the referenced entity
 */
// FIXME add nillable tests
public class QueryGeneratorReferencesTest extends AbstractMolgenisSpringTest
{
	private static final String FIELD_NGRAM_ANALYZED = "ngram";

	private EntityType entityType;

	private final String refBoolAttributeName = "xbool";
	private final String refCategoricalAttributeName = "xcategorical";
	private final String refCompoundAttributeName = "xcompound";
	private final String refCompoundPart0AttributeName = "xcompoundpart0";
	private final String refDateAttributeName = "xdate";
	private final String refDateTimeAttributeName = "xdatetime";
	private final String refDecimalAttributeName = "xdecimal";
	private final String refIntAttributeName = "xint";
	private final String refLongAttributeName = "xlong";
	private final String refStringAttributeName = "xstring";

	private final String stringAttributeName = "string";

	private final String REF_ENTITY_ATT = "mref";
	private final String PREFIX = REF_ENTITY_ATT + QueryGenerator.ATTRIBUTE_SEPARATOR;

	@Autowired
	private EntityTypeFactory entityTypeFactory;

	@Autowired
	private AttributeFactory attrFactory;

	private QueryGenerator queryGenerator;

	public QueryGeneratorReferencesTest()
	{
		super(Strictness.WARN);
	}

	@BeforeMethod
	public void setUp()
	{
		EntityType refEntityType = entityTypeFactory.create("ref_entity");
		String refIdAttributeName = "xid";
		refEntityType.addAttribute(attrFactory.create().setName(refIdAttributeName), ROLE_ID);
		refEntityType.addAttribute(attrFactory.create().setName(refBoolAttributeName).setDataType(BOOL));
		refEntityType.addAttribute(attrFactory.create()
											  .setName(refCategoricalAttributeName)
											  .setDataType(CATEGORICAL)
											  .setRefEntity(refEntityType)
											  .setNillable(true));
		Attribute attrCompound = attrFactory.create().setName(refCompoundAttributeName).setDataType(COMPOUND);
		Attribute compoundPart0Attribute = attrFactory.create()
													  .setName(refCompoundPart0AttributeName)
													  .setDataType(STRING)
													  .setParent(attrCompound);
		String refCompoundPart1AttributeName = "xcompoundpart1";
		Attribute compoundPart1Attribute = attrFactory.create()
													  .setName(refCompoundPart1AttributeName)
													  .setDataType(STRING)
													  .setParent(attrCompound);
		refEntityType.addAttribute(attrCompound);
		refEntityType.addAttribute(compoundPart0Attribute);
		refEntityType.addAttribute(compoundPart1Attribute);
		refEntityType.addAttribute(attrFactory.create().setName(refDateAttributeName).setDataType(DATE));
		refEntityType.addAttribute(attrFactory.create().setName(refDateTimeAttributeName).setDataType(DATE_TIME));
		refEntityType.addAttribute(attrFactory.create().setName(refDecimalAttributeName).setDataType(DECIMAL));
		String refEmailAttributeName = "xemail";
		refEntityType.addAttribute(attrFactory.create().setName(refEmailAttributeName).setDataType(EMAIL));
		String refEnumAttributeName = "xenum";
		refEntityType.addAttribute(attrFactory.create()
											  .setName(refEnumAttributeName)
											  .setDataType(ENUM)
											  .setEnumOptions(Arrays.asList("enum0", "enum1", "enum2")));
		String refHtmlAttributeName = "xhtml";
		refEntityType.addAttribute(attrFactory.create().setName(refHtmlAttributeName).setDataType(HTML));
		String refHyperlinkAttributeName = "xhyperlink";
		refEntityType.addAttribute(attrFactory.create().setName(refHyperlinkAttributeName).setDataType(HYPERLINK));
		refEntityType.addAttribute(attrFactory.create().setName(refIntAttributeName).setDataType(INT));
		refEntityType.addAttribute(attrFactory.create().setName(refLongAttributeName).setDataType(LONG));
		String refMrefAttributeName = "xmref";
		refEntityType.addAttribute(attrFactory.create()
											  .setName(refMrefAttributeName)
											  .setDataType(MREF)
											  .setRefEntity(refEntityType)
											  .setNillable(true));
		String refScriptAttributeName = "xscript";
		refEntityType.addAttribute(attrFactory.create().setName(refScriptAttributeName).setDataType(SCRIPT));
		refEntityType.addAttribute(attrFactory.create().setName(refStringAttributeName).setDataType(STRING));
		String refTextAttributeName = "xtext";
		refEntityType.addAttribute(attrFactory.create().setName(refTextAttributeName).setDataType(TEXT));
		String refXrefAttributeName = "xxref";
		refEntityType.addAttribute(attrFactory.create()
											  .setName(refXrefAttributeName)
											  .setDataType(XREF)
											  .setRefEntity(refEntityType)
											  .setNillable(true));

		EntityType emd = entityTypeFactory.create("entity");
		String idAttributeName = "id";
		emd.addAttribute(attrFactory.create().setName(idAttributeName), ROLE_ID);
		emd.addAttribute(attrFactory.create().setName(stringAttributeName).setUnique(true), ROLE_LABEL);
		String mrefAttributeName = "mref";
		emd.addAttribute(attrFactory.create()
									.setName(mrefAttributeName)
									.setDataType(MREF)
									.setNillable(true)
									.setRefEntity(refEntityType));

		this.entityType = emd;

		DocumentIdGenerator documentIdGenerator = mock(DocumentIdGenerator.class);
		when(documentIdGenerator.generateId(any(Attribute.class))).thenAnswer(
				invocation -> ((Attribute) invocation.getArguments()[0]).getName());
		queryGenerator = new QueryGenerator(documentIdGenerator);
	}

	@Test
	public void generateOneQueryRuleGreaterDate() throws ParseException
	{
		String date = "2015-05-22";
		LocalDate value = LocalDate.parse(date);
		Query<Entity> q = new QueryImpl<>().gt(PREFIX + refDateAttributeName, value);
		QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
		QueryBuilder expectedQuery = constantScoreQuery(
				nestedQuery(REF_ENTITY_ATT, rangeQuery(PREFIX + refDateAttributeName).gt(date), ScoreMode.Avg));
		assertQueryBuilderEquals(query, expectedQuery);
	}

	@Test
	public void generateOneQueryRuleGreaterDateTime() throws ParseException
	{
		Instant value = Instant.parse("2015-05-22T06:12:13Z");
		Query<Entity> q = new QueryImpl<>().gt(PREFIX + refDateTimeAttributeName, value);
		QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
		QueryBuilder expectedQuery = constantScoreQuery(nestedQuery(REF_ENTITY_ATT,
				rangeQuery(PREFIX + refDateTimeAttributeName).gt(DataConverter.toString(value)), ScoreMode.Avg));
		assertQueryBuilderEquals(query, expectedQuery);
	}

	@Test
	public void generateOneQueryRuleGreaterDecimal()
	{
		Double value = 1.23;
		Query<Entity> q = new QueryImpl<>().gt(PREFIX + refDecimalAttributeName, value);
		QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
		QueryBuilder expectedQuery = constantScoreQuery(
				nestedQuery(REF_ENTITY_ATT, rangeQuery(PREFIX + refDecimalAttributeName).gt(value), ScoreMode.Avg));
		assertQueryBuilderEquals(query, expectedQuery);
	}

	@Test
	public void generateOneQueryRuleGreaterInt()
	{
		Integer value = 1;
		Query<Entity> q = new QueryImpl<>().gt(PREFIX + refIntAttributeName, value);
		QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
		QueryBuilder expectedQuery = constantScoreQuery(
				nestedQuery(REF_ENTITY_ATT, rangeQuery(PREFIX + refIntAttributeName).gt(value), ScoreMode.Avg));
		assertQueryBuilderEquals(query, expectedQuery);
	}

	@Test
	public void generateOneQueryRuleGreaterLong()
	{
		Long value = 1L;
		Query<Entity> q = new QueryImpl<>().gt(PREFIX + refLongAttributeName, value);
		QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
		QueryBuilder expectedQuery = constantScoreQuery(
				nestedQuery(REF_ENTITY_ATT, rangeQuery(PREFIX + refLongAttributeName).gt(value), ScoreMode.Avg));
		assertQueryBuilderEquals(query, expectedQuery);
	}

	@Test
	public void generateOneQueryRuleGreaterEqualDate() throws ParseException
	{
		String date = "2015-05-22";
		LocalDate value = LocalDate.parse(date);
		Query<Entity> q;
		q = new QueryImpl<>().ge(PREFIX + refDateAttributeName, value);
		QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
		QueryBuilder expectedQuery = constantScoreQuery(
				nestedQuery(REF_ENTITY_ATT, rangeQuery(PREFIX + refDateAttributeName).gte(date), ScoreMode.Avg));
		assertQueryBuilderEquals(query, expectedQuery);
	}

	@Test
	public void generateOneQueryRuleLesserEqualDecimal()
	{
		Double value = 1.23;
		Query<Entity> q = new QueryImpl<>().le(PREFIX + refDecimalAttributeName, value);
		QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
		QueryBuilder expectedQuery = constantScoreQuery(
				nestedQuery(REF_ENTITY_ATT, rangeQuery(PREFIX + refDecimalAttributeName).lte(value), ScoreMode.Avg));
		assertQueryBuilderEquals(query, expectedQuery);
	}

	@Test
	public void generateOneQueryRuleLesserInt()
	{
		Integer value = 1;
		Query<Entity> q = new QueryImpl<>().lt(PREFIX + refIntAttributeName, value);
		QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
		QueryBuilder expectedQuery = constantScoreQuery(
				nestedQuery(REF_ENTITY_ATT, rangeQuery(PREFIX + refIntAttributeName).lt(value), ScoreMode.Avg));
		assertQueryBuilderEquals(query, expectedQuery);
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void generateOneQueryRuleInCategorical_Ids()
	{
		Iterable<String> values = Arrays.asList("id0", "id1", "id2");
		Query<Entity> q = new QueryImpl<>().in(PREFIX + refCategoricalAttributeName, values);
		queryGenerator.createQueryBuilder(q, entityType);
	}

	@Test
	public void generateOneQueryRuleLikeCompoundPartString()
	{
		String value = "value";
		Query<Entity> q = new QueryImpl<>().like(PREFIX + refCompoundPart0AttributeName, value);
		QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
		QueryBuilder expectedQuery = QueryBuilders.nestedQuery(REF_ENTITY_ATT,
				QueryBuilders.matchPhrasePrefixQuery(PREFIX + refCompoundPart0AttributeName, value).slop(10)
							 .analyzer(DEFAULT_ANALYZER), ScoreMode.Avg);
		assertQueryBuilderEquals(query, expectedQuery);
	}

	@Test
	public void generateOneQueryRuleEqualsBool()
	{
		Boolean value = Boolean.TRUE;
		Query<Entity> q = new QueryImpl<>().eq(PREFIX + refBoolAttributeName, value);
		QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
		QueryBuilder expectedQuery = constantScoreQuery(
				nestedQuery(REF_ENTITY_ATT, termQuery(PREFIX + refBoolAttributeName, value), ScoreMode.Avg));
		assertQueryBuilderEquals(query, expectedQuery);
	}

	@Test
	public void generateOneQueryRuleEqualsString()
	{
		String value = "value";
		Query<Entity> q = new QueryImpl<>().eq(PREFIX + refStringAttributeName, value);
		QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
		QueryBuilder expectedQuery = constantScoreQuery(nestedQuery(REF_ENTITY_ATT,
				termQuery(PREFIX + refStringAttributeName + '.' + FIELD_NOT_ANALYZED, value), ScoreMode.Avg));
		assertQueryBuilderEquals(query, expectedQuery);
	}

	@Test(expectedExceptions = MolgenisQueryException.class)
	public void generateOneQueryRuleEqualsCategorical()
	{
		String value = "id";
		Query<Entity> q = new QueryImpl<>().eq(PREFIX + refCategoricalAttributeName, value);
		QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
	}

	@Test
	public void generateOneQueryRuleNotEqualsEqualsBool()
	{
		Boolean value = Boolean.TRUE;
		Query<Entity> q = new QueryImpl<>().not().eq(PREFIX + refBoolAttributeName, value);
		QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
		QueryBuilder expectedQuery = boolQuery().mustNot(constantScoreQuery(
				nestedQuery(REF_ENTITY_ATT, termQuery(PREFIX + refBoolAttributeName, value), ScoreMode.Avg)));
		assertQueryBuilderEquals(query, expectedQuery);
	}

	@Test(expectedExceptions = MolgenisQueryException.class)
	public void generateOneQueryRuleNotEqualsCategorical()
	{
		String value = "id";
		Query<Entity> q = new QueryImpl<>().not().eq(PREFIX + refCategoricalAttributeName, value);
		queryGenerator.createQueryBuilder(q, entityType);
	}

	@Test(expectedExceptions = MolgenisQueryException.class)
	public void generateOneQueryRuleNotEqualsCompound()
	{
		Object value = "value";
		Query<Entity> q = new QueryImpl<>().not().eq(PREFIX + refCompoundAttributeName, value);
		queryGenerator.createQueryBuilder(q, entityType);
	}

	@Test
	public void generateOneQueryRuleNotEqualsCompoundPartString()
	{
		String value = "value";
		Query<Entity> q = new QueryImpl<>().not().eq(PREFIX + refCompoundPart0AttributeName, value);
		QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
		QueryBuilder expectedQuery = boolQuery().mustNot(constantScoreQuery(nestedQuery(REF_ENTITY_ATT,
				termQuery(PREFIX + refCompoundPart0AttributeName + '.' + FIELD_NOT_ANALYZED, value), ScoreMode.Avg)));
		assertQueryBuilderEquals(query, expectedQuery);
	}

	@Test
	public void generateOneQueryRuleRangeInt()
	{
		Integer low = 3;
		Integer high = 9;
		Query<Entity> q = new QueryImpl<>().rng(PREFIX + refIntAttributeName, low, high);
		QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
		QueryBuilder expectedQuery = constantScoreQuery(
				nestedQuery(REF_ENTITY_ATT, rangeQuery(PREFIX + refIntAttributeName).gte(3).lte(9), ScoreMode.Avg));
		assertQueryBuilderEquals(query, expectedQuery);
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void generateOneQueryRuleSearchOneFieldCategorical()
	{
		String value = "text";
		Query<Entity> q = new QueryImpl<>().search(PREFIX + refCategoricalAttributeName, value);
		queryGenerator.createQueryBuilder(q, entityType);
	}

	@Test
	public void generateOneQueryRuleSearchOneFieldCompoundPartString()
	{
		String value = "value";
		Query<Entity> q = new QueryImpl<>().search(PREFIX + refCompoundPart0AttributeName, value);
		QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
		QueryBuilder expectedQuery = nestedQuery(REF_ENTITY_ATT,
				matchQuery(PREFIX + refCompoundPart0AttributeName, value), ScoreMode.Avg);
		assertQueryBuilderEquals(query, expectedQuery);
	}

	@Test
	public void generateMultipleQueryRule()
	{
		// query: ref.a or (b and ref.c)
		Boolean booleanValue = Boolean.TRUE;
		String stringValue = "str";
		Integer intValue = 1;
		Query<Entity> q = new QueryImpl<>().eq(PREFIX + refBoolAttributeName, booleanValue)
										   .or()
										   .nest()
										   .eq(stringAttributeName, stringValue)
										   .and()
										   .eq(PREFIX + refIntAttributeName, intValue)
										   .unnest();
		QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
		QueryBuilder booleanQuery = constantScoreQuery(
				nestedQuery(REF_ENTITY_ATT, termQuery(PREFIX + refBoolAttributeName, booleanValue), ScoreMode.Avg));
		QueryBuilder stringQuery = constantScoreQuery(
				termQuery(stringAttributeName + '.' + FIELD_NOT_ANALYZED, stringValue));
		QueryBuilder intQuery = constantScoreQuery(
				nestedQuery(REF_ENTITY_ATT, termQuery(PREFIX + refIntAttributeName, intValue), ScoreMode.Avg));
		BoolQueryBuilder stringIntQuery = QueryBuilders.boolQuery().must(stringQuery).must(intQuery);
		QueryBuilder expectedQuery = QueryBuilders.boolQuery()
												  .should(booleanQuery)
												  .should(stringIntQuery)
												  .minimumShouldMatch(1);
		assertQueryBuilderEquals(query, expectedQuery);
	}

	@Test
	public void generateMultipleQueryRuleMultipleNotClauses()
	{
		// query: ref.a and not b and not ref.c
		Boolean booleanValue = Boolean.TRUE;
		String stringValue = "str";
		Integer intValue = 1;
		Query<Entity> q = new QueryImpl<>().eq(PREFIX + refBoolAttributeName, booleanValue)
										   .and()
										   .not()
										   .eq(stringAttributeName, stringValue)
										   .and()
										   .not()
										   .eq(PREFIX + refIntAttributeName, intValue);
		QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
		QueryBuilder booleanQuery = constantScoreQuery(
				nestedQuery(REF_ENTITY_ATT, termQuery(PREFIX + refBoolAttributeName, booleanValue), ScoreMode.Avg));
		QueryBuilder stringQuery = constantScoreQuery(
				termQuery(stringAttributeName + '.' + FIELD_NOT_ANALYZED, stringValue));
		QueryBuilder intQuery = constantScoreQuery(
				nestedQuery(REF_ENTITY_ATT, termQuery(PREFIX + refIntAttributeName, intValue), ScoreMode.Avg));
		QueryBuilder expectedQuery = QueryBuilders.boolQuery()
												  .must(booleanQuery)
												  .mustNot(stringQuery)
												  .mustNot(intQuery);
		assertQueryBuilderEquals(query, expectedQuery);
	}

	private void assertQueryBuilderEquals(QueryBuilder actual, QueryBuilder expected)
	{
		// QueryBuilder classes do not implement equals
		assertEquals(actual.toString().replaceAll("\\s", ""), expected.toString().replaceAll("\\s", ""));
	}
}
