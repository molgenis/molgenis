package org.molgenis.data.elasticsearch.request;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.*;
import org.mockito.ArgumentCaptor;
import org.molgenis.data.DataConverter;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisQueryException;
import org.molgenis.data.Query;
import org.molgenis.data.elasticsearch.index.MappingsBuilder;
import org.molgenis.data.elasticsearch.util.DocumentIdGenerator;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.molgenis.util.MolgenisDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.molgenis.data.elasticsearch.index.ElasticsearchIndexCreator.DEFAULT_ANALYZER;
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
	private SearchRequestBuilder searchRequestBuilder;
	private EntityType entityType;

	private final String refIdAttributeName = "xid";
	private final String refBoolAttributeName = "xbool";
	private final String refCategoricalAttributeName = "xcategorical";
	private final String refCompoundAttributeName = "xcompound";
	private final String refCompoundPart0AttributeName = "xcompoundpart0";
	private final String refCompoundPart1AttributeName = "xcompoundpart1";
	private final String refDateAttributeName = "xdate";
	private final String refDateTimeAttributeName = "xdatetime";
	private final String refDecimalAttributeName = "xdecimal";
	private final String refEmailAttributeName = "xemail";
	private final String refEnumAttributeName = "xenum";
	private final String refHtmlAttributeName = "xhtml";
	private final String refHyperlinkAttributeName = "xhyperlink";
	private final String refIntAttributeName = "xint";
	private final String refLongAttributeName = "xlong";
	private final String refMrefAttributeName = "xmref";
	private final String refScriptAttributeName = "xscript";
	private final String refStringAttributeName = "xstring";
	private final String refTextAttributeName = "xtext";
	private final String refXrefAttributeName = "xxref";

	private final String idAttributeName = "id";
	private final String stringAttributeName = "string";
	private final String mrefAttributeName = "mref";

	private final String REF_ENTITY_ATT = "mref";
	private final String PREFIX = REF_ENTITY_ATT + QueryGenerator.ATTRIBUTE_SEPARATOR;

	@Autowired
	EntityTypeFactory entityTypeFactory;

	@Autowired
	AttributeFactory attrFactory;

	private QueryGenerator queryGenerator;

	@BeforeMethod
	public void setUp()
	{
		searchRequestBuilder = mock(SearchRequestBuilder.class);

		EntityType refEntityType = entityTypeFactory.create().setName("ref_entity");
		refEntityType.addAttribute(attrFactory.create().setName(refIdAttributeName), ROLE_ID);
		refEntityType.addAttribute(attrFactory.create().setName(refBoolAttributeName).setDataType(BOOL));
		refEntityType.addAttribute(attrFactory.create().setName(refCategoricalAttributeName).setDataType(CATEGORICAL)
				.setRefEntity(refEntityType).setNillable(true));
		Attribute attrCompound = attrFactory.create().setName(refCompoundAttributeName).setDataType(COMPOUND);
		Attribute compoundPart0Attribute = attrFactory.create().setName(refCompoundPart0AttributeName)
				.setDataType(STRING).setParent(attrCompound);
		Attribute compoundPart1Attribute = attrFactory.create().setName(refCompoundPart1AttributeName)
				.setDataType(STRING).setParent(attrCompound);
		refEntityType.addAttribute(attrCompound);
		refEntityType.addAttribute(compoundPart0Attribute);
		refEntityType.addAttribute(compoundPart1Attribute);
		refEntityType.addAttribute(attrFactory.create().setName(refDateAttributeName).setDataType(DATE));
		refEntityType.addAttribute(attrFactory.create().setName(refDateTimeAttributeName).setDataType(DATE_TIME));
		refEntityType.addAttribute(attrFactory.create().setName(refDecimalAttributeName).setDataType(DECIMAL));
		refEntityType.addAttribute(attrFactory.create().setName(refEmailAttributeName).setDataType(EMAIL));
		refEntityType.addAttribute(attrFactory.create().setName(refEnumAttributeName).setDataType(ENUM)
				.setEnumOptions(Arrays.asList("enum0", "enum1", "enum2")));
		refEntityType.addAttribute(attrFactory.create().setName(refHtmlAttributeName).setDataType(HTML));
		refEntityType.addAttribute(attrFactory.create().setName(refHyperlinkAttributeName).setDataType(HYPERLINK));
		refEntityType.addAttribute(attrFactory.create().setName(refIntAttributeName).setDataType(INT));
		refEntityType.addAttribute(attrFactory.create().setName(refLongAttributeName).setDataType(LONG));
		refEntityType.addAttribute(
				attrFactory.create().setName(refMrefAttributeName).setDataType(MREF).setRefEntity(refEntityType)
						.setNillable(true));
		refEntityType.addAttribute(attrFactory.create().setName(refScriptAttributeName).setDataType(SCRIPT));
		refEntityType.addAttribute(attrFactory.create().setName(refStringAttributeName).setDataType(STRING));
		refEntityType.addAttribute(attrFactory.create().setName(refTextAttributeName).setDataType(TEXT));
		refEntityType.addAttribute(
				attrFactory.create().setName(refXrefAttributeName).setDataType(XREF).setRefEntity(refEntityType)
						.setNillable(true));

		EntityType emd = entityTypeFactory.create().setName("entity");
		emd.addAttribute(attrFactory.create().setName(idAttributeName), ROLE_ID);
		emd.addAttribute(attrFactory.create().setName(stringAttributeName).setUnique(true), ROLE_LABEL);
		emd.addAttribute(attrFactory.create().setName(mrefAttributeName).setDataType(MREF).setNillable(true)
				.setRefEntity(refEntityType));

		this.entityType = emd;

		DocumentIdGenerator documentIdGenerator = mock(DocumentIdGenerator.class);
		when(documentIdGenerator.generateId(any(Attribute.class)))
				.thenAnswer(invocation -> ((Attribute) invocation.getArguments()[0]).getName());
		queryGenerator = new QueryGenerator(documentIdGenerator);
	}

	@Test
	public void generateOneQueryRuleGreaterDate() throws ParseException
	{
		String date = "2015-05-22";
		Date value = MolgenisDateFormat.getDateFormat().parse(date);
		Query<Entity> q = new QueryImpl<Entity>().gt(PREFIX + refDateAttributeName, value);
		queryGenerator.generate(searchRequestBuilder, q, entityType);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), FilterBuilders
				.nestedFilter(REF_ENTITY_ATT, FilterBuilders.rangeFilter(PREFIX + refDateAttributeName).gt(date)));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleGreaterDateTime() throws ParseException
	{
		Date value = MolgenisDateFormat.getDateFormat().parse("2015-05-22T11:12:13+0500");
		Query<Entity> q = new QueryImpl<Entity>().gt(PREFIX + refDateTimeAttributeName, value);
		queryGenerator.generate(searchRequestBuilder, q, entityType);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), FilterBuilders
				.nestedFilter(REF_ENTITY_ATT, FilterBuilders.rangeFilter(PREFIX + refDateTimeAttributeName)
						.gt(DataConverter.toString(value))));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleGreaterDecimal()
	{
		Double value = Double.valueOf(1.23);
		Query<Entity> q = new QueryImpl<Entity>().gt(PREFIX + refDecimalAttributeName, value);
		queryGenerator.generate(searchRequestBuilder, q, entityType);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), FilterBuilders
				.nestedFilter(REF_ENTITY_ATT, FilterBuilders.rangeFilter(PREFIX + refDecimalAttributeName).gt(value)));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleGreaterInt()
	{
		Integer value = Integer.valueOf(1);
		Query<Entity> q = new QueryImpl<Entity>().gt(PREFIX + refIntAttributeName, value);
		queryGenerator.generate(searchRequestBuilder, q, entityType);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), FilterBuilders
				.nestedFilter(REF_ENTITY_ATT, FilterBuilders.rangeFilter(PREFIX + refIntAttributeName).gt(value)));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleGreaterLong()
	{
		Long value = Long.valueOf(1l);
		Query<Entity> q = new QueryImpl<Entity>().gt(PREFIX + refLongAttributeName, value);
		queryGenerator.generate(searchRequestBuilder, q, entityType);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), FilterBuilders
				.nestedFilter(REF_ENTITY_ATT, FilterBuilders.rangeFilter(PREFIX + refLongAttributeName).gt(value)));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleGreaterEqualDate() throws ParseException
	{
		String date = "2015-05-22";
		Date value = MolgenisDateFormat.getDateFormat().parse(date);
		Query<Entity> q = new QueryImpl<Entity>().ge(PREFIX + refDateAttributeName, value);
		queryGenerator.generate(searchRequestBuilder, q, entityType);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), FilterBuilders
				.nestedFilter(REF_ENTITY_ATT, FilterBuilders.rangeFilter(PREFIX + refDateAttributeName).gte(date)));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleLesserEqualDecimal()
	{
		Double value = Double.valueOf(1.23);
		Query<Entity> q = new QueryImpl<>().le(PREFIX + refDecimalAttributeName, value);
		queryGenerator.generate(searchRequestBuilder, q, entityType);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), FilterBuilders
				.nestedFilter(REF_ENTITY_ATT, FilterBuilders.rangeFilter(PREFIX + refDecimalAttributeName).lte(value)));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleLesserInt()
	{
		Integer value = Integer.valueOf(1);
		Query<Entity> q = new QueryImpl<>().lt(PREFIX + refIntAttributeName, value);
		queryGenerator.generate(searchRequestBuilder, q, entityType);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), FilterBuilders
				.nestedFilter(REF_ENTITY_ATT, FilterBuilders.rangeFilter(PREFIX + refIntAttributeName).lt(value)));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void generateOneQueryRuleInCategorical_Ids()
	{
		Iterable<String> values = Arrays.asList("id0", "id1", "id2");
		Query<Entity> q = new QueryImpl<>().in(PREFIX + refCategoricalAttributeName, values);
		queryGenerator.generate(searchRequestBuilder, q, entityType);
	}

	@Test
	public void generateOneQueryRuleLikeCompoundPartString()
	{
		String value = "value";
		Query<Entity> q = new QueryImpl<Entity>().like(PREFIX + refCompoundPart0AttributeName, value);
		queryGenerator.generate(searchRequestBuilder, q, entityType);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.nestedQuery(REF_ENTITY_ATT, QueryBuilders
				.matchQuery(PREFIX + refCompoundPart0AttributeName + '.' + MappingsBuilder.FIELD_NGRAM_ANALYZED, value)
				.analyzer(DEFAULT_ANALYZER));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleEqualsBool()
	{
		Boolean value = Boolean.TRUE;
		Query<Entity> q = new QueryImpl<Entity>().eq(PREFIX + refBoolAttributeName, value);
		queryGenerator.generate(searchRequestBuilder, q, entityType);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), FilterBuilders
				.nestedFilter(REF_ENTITY_ATT, FilterBuilders.termFilter(PREFIX + refBoolAttributeName, value)));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleEqualsString()
	{
		String value = "value";
		Query<Entity> q = new QueryImpl<Entity>().eq(PREFIX + refStringAttributeName, value);
		queryGenerator.generate(searchRequestBuilder, q, entityType);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), FilterBuilders
				.nestedFilter(REF_ENTITY_ATT, FilterBuilders
						.termFilter(PREFIX + refStringAttributeName + '.' + MappingsBuilder.FIELD_NOT_ANALYZED,
								value)));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test(expectedExceptions = MolgenisQueryException.class)
	public void generateOneQueryRuleEqualsCategorical()
	{
		String value = "id";
		Query<Entity> q = new QueryImpl<Entity>().eq(PREFIX + refCategoricalAttributeName, value);
		queryGenerator.generate(searchRequestBuilder, q, entityType);
	}

	@Test
	public void generateOneQueryRuleNotEqualsEqualsBool()
	{
		Boolean value = Boolean.TRUE;
		Query<Entity> q = new QueryImpl<Entity>().not().eq(PREFIX + refBoolAttributeName, value);
		queryGenerator.generate(searchRequestBuilder, q, entityType);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.boolQuery().mustNot(QueryBuilders
				.filteredQuery(QueryBuilders.matchAllQuery(), FilterBuilders.nestedFilter(REF_ENTITY_ATT,
						FilterBuilders.termFilter(PREFIX + refBoolAttributeName, value))));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test(expectedExceptions = MolgenisQueryException.class)
	public void generateOneQueryRuleNotEqualsCategorical()
	{
		String value = "id";
		Query<Entity> q = new QueryImpl<Entity>().not().eq(PREFIX + refCategoricalAttributeName, value);
		queryGenerator.generate(searchRequestBuilder, q, entityType);
	}

	@Test(expectedExceptions = MolgenisQueryException.class)
	public void generateOneQueryRuleNotEqualsCompound()
	{
		Object value = "value";
		Query<Entity> q = new QueryImpl<Entity>().not().eq(PREFIX + refCompoundAttributeName, value);
		queryGenerator.generate(searchRequestBuilder, q, entityType);
	}

	@Test
	public void generateOneQueryRuleNotEqualsCompoundPartString()
	{
		String value = "value";
		Query<Entity> q = new QueryImpl<Entity>().not().eq(PREFIX + refCompoundPart0AttributeName, value);
		queryGenerator.generate(searchRequestBuilder, q, entityType);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.boolQuery().mustNot(QueryBuilders
				.filteredQuery(QueryBuilders.matchAllQuery(), FilterBuilders.nestedFilter(REF_ENTITY_ATT, FilterBuilders
						.termFilter(PREFIX + refCompoundPart0AttributeName + '.' + MappingsBuilder.FIELD_NOT_ANALYZED,
								value))));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleRangeInt()
	{
		Integer low = Integer.valueOf(3);
		Integer high = Integer.valueOf(9);
		Query<Entity> q = new QueryImpl<Entity>().rng(PREFIX + refIntAttributeName, low, high);
		queryGenerator.generate(searchRequestBuilder, q, entityType);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());

		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), FilterBuilders
				.nestedFilter(REF_ENTITY_ATT, FilterBuilders.rangeFilter(PREFIX + refIntAttributeName).gte(3).lte(9)));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void generateOneQueryRuleSearchOneFieldCategorical()
	{
		String value = "text";
		Query<Entity> q = new QueryImpl<Entity>().search(PREFIX + refCategoricalAttributeName, value);
		queryGenerator.generate(searchRequestBuilder, q, entityType);
	}

	@Test
	public void generateOneQueryRuleSearchOneFieldCompoundPartString()
	{
		String value = "value";
		Query<Entity> q = new QueryImpl<Entity>().search(PREFIX + refCompoundPart0AttributeName, value);
		queryGenerator.generate(searchRequestBuilder, q, entityType);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders
				.nestedQuery(REF_ENTITY_ATT, QueryBuilders.matchQuery(PREFIX + refCompoundPart0AttributeName, value));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateMultipleQueryRule()
	{
		// query: ref.a or (b and ref.c)
		Boolean booleanValue = Boolean.TRUE;
		String stringValue = "str";
		Integer intValue = 1;
		Query<Entity> q = new QueryImpl<Entity>().eq(PREFIX + refBoolAttributeName, booleanValue).or().nest()
				.eq(stringAttributeName, stringValue).and().eq(PREFIX + refIntAttributeName, intValue).unnest();
		queryGenerator.generate(searchRequestBuilder, q, entityType);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());

		FilteredQueryBuilder booleanQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), FilterBuilders
				.nestedFilter(REF_ENTITY_ATT, FilterBuilders.termFilter(PREFIX + refBoolAttributeName, booleanValue)));
		QueryBuilder stringQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.termFilter(stringAttributeName + '.' + MappingsBuilder.FIELD_NOT_ANALYZED, stringValue));
		QueryBuilder intQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), FilterBuilders
				.nestedFilter(REF_ENTITY_ATT, FilterBuilders.termFilter(PREFIX + refIntAttributeName, intValue)));
		BoolQueryBuilder stringIntQuery = QueryBuilders.boolQuery().must(stringQuery).must(intQuery);
		QueryBuilder expectedQuery = QueryBuilders.boolQuery().should(booleanQuery).should(stringIntQuery)
				.minimumNumberShouldMatch(1);
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateMultipleQueryRuleMultipleNotClauses()
	{
		// query: ref.a and not b and not ref.c
		Boolean booleanValue = Boolean.TRUE;
		String stringValue = "str";
		Integer intValue = 1;
		Query<Entity> q = new QueryImpl<Entity>().eq(PREFIX + refBoolAttributeName, booleanValue).and().not()
				.eq(stringAttributeName, stringValue).and().not().eq(PREFIX + refIntAttributeName, intValue);
		queryGenerator.generate(searchRequestBuilder, q, entityType);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());

		FilteredQueryBuilder booleanQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), FilterBuilders
				.nestedFilter(REF_ENTITY_ATT, FilterBuilders.termFilter(PREFIX + refBoolAttributeName, booleanValue)));
		QueryBuilder stringQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.termFilter(stringAttributeName + '.' + MappingsBuilder.FIELD_NOT_ANALYZED, stringValue));
		QueryBuilder intQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), FilterBuilders
				.nestedFilter(REF_ENTITY_ATT, FilterBuilders.termFilter(PREFIX + refIntAttributeName, intValue)));
		QueryBuilder expectedQuery = QueryBuilders.boolQuery().must(booleanQuery).mustNot(stringQuery)
				.mustNot(intQuery);
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	private void assertQueryBuilderEquals(QueryBuilder actual, QueryBuilder expected)
	{
		// QueryBuilder classes do not implement equals
		assertEquals(actual.toString().replaceAll("\\s", ""), expected.toString().replaceAll("\\s", ""));
	}
}
