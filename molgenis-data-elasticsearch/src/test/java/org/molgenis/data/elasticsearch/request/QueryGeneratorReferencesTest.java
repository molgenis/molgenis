package org.molgenis.data.elasticsearch.request;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.elasticsearch.index.ElasticsearchIndexCreator.DEFAULT_ANALYZER;
import static org.testng.Assert.assertEquals;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.FilteredQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.mockito.ArgumentCaptor;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataConverter;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisQueryException;
import org.molgenis.data.Query;
import org.molgenis.data.elasticsearch.index.MappingsBuilder;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.fieldtypes.EnumField;
import org.molgenis.util.MolgenisDateFormat;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Copy of standard QueryGeneratorTest but now with queries done on the referenced entity
 */
// FIXME add nillable tests
public class QueryGeneratorReferencesTest
{
	private SearchRequestBuilder searchRequestBuilder;
	private EntityMetaData entityMetaData;

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

	@BeforeMethod
	public void setUp()
	{
		searchRequestBuilder = mock(SearchRequestBuilder.class);

		DefaultEntityMetaData refEntityMetaData = new DefaultEntityMetaData("ref_entity");
		refEntityMetaData.addAttribute(refIdAttributeName, ROLE_ID);
		refEntityMetaData.addAttribute(refBoolAttributeName).setDataType(MolgenisFieldTypes.BOOL);
		refEntityMetaData.addAttribute(refCategoricalAttributeName).setDataType(MolgenisFieldTypes.CATEGORICAL)
				.setRefEntity(refEntityMetaData).setNillable(true);
		DefaultAttributeMetaData compoundPart0Attribute = new DefaultAttributeMetaData(refCompoundPart0AttributeName)
				.setDataType(MolgenisFieldTypes.STRING);
		DefaultAttributeMetaData compoundPart1Attribute = new DefaultAttributeMetaData(refCompoundPart1AttributeName)
				.setDataType(MolgenisFieldTypes.STRING);
		refEntityMetaData.addAttribute(refCompoundAttributeName).setDataType(MolgenisFieldTypes.COMPOUND)
				.setAttributesMetaData(
						Arrays.<AttributeMetaData> asList(compoundPart0Attribute, compoundPart1Attribute));
		refEntityMetaData.addAttribute(refDateAttributeName).setDataType(MolgenisFieldTypes.DATE);
		refEntityMetaData.addAttribute(refDateTimeAttributeName).setDataType(MolgenisFieldTypes.DATETIME);
		refEntityMetaData.addAttribute(refDecimalAttributeName).setDataType(MolgenisFieldTypes.DECIMAL);
		refEntityMetaData.addAttribute(refEmailAttributeName).setDataType(MolgenisFieldTypes.EMAIL);
		refEntityMetaData.addAttribute(refEnumAttributeName).setDataType(new EnumField())
				.setEnumOptions(Arrays.asList("enum0", "enum1", "enum2"));
		refEntityMetaData.addAttribute(refHtmlAttributeName).setDataType(MolgenisFieldTypes.HTML);
		refEntityMetaData.addAttribute(refHyperlinkAttributeName).setDataType(MolgenisFieldTypes.HYPERLINK);
		refEntityMetaData.addAttribute(refIntAttributeName).setDataType(MolgenisFieldTypes.INT);
		refEntityMetaData.addAttribute(refLongAttributeName).setDataType(MolgenisFieldTypes.LONG);
		refEntityMetaData.addAttribute(refMrefAttributeName).setDataType(MolgenisFieldTypes.MREF)
				.setRefEntity(refEntityMetaData).setNillable(true);
		refEntityMetaData.addAttribute(refScriptAttributeName).setDataType(MolgenisFieldTypes.SCRIPT);
		refEntityMetaData.addAttribute(refStringAttributeName).setDataType(MolgenisFieldTypes.STRING);
		refEntityMetaData.addAttribute(refTextAttributeName).setDataType(MolgenisFieldTypes.TEXT);
		refEntityMetaData.addAttribute(refXrefAttributeName).setDataType(MolgenisFieldTypes.XREF)
				.setRefEntity(refEntityMetaData).setNillable(true);

		DefaultEntityMetaData emd = new DefaultEntityMetaData("entity");
		emd.addAttribute(idAttributeName, ROLE_ID);
		emd.addAttribute(stringAttributeName, ROLE_LABEL).setUnique(true);
		emd.addAttribute(mrefAttributeName).setDataType(MolgenisFieldTypes.MREF).setNillable(true)
				.setRefEntity(refEntityMetaData);

		this.entityMetaData = emd;
	}

	@Test
	public void generateOneQueryRuleGreaterDate() throws ParseException
	{
		String date = "2015-05-22";
		Date value = MolgenisDateFormat.getDateFormat().parse(date);
		Query q = new QueryImpl().gt(PREFIX + refDateAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
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
		Query q = new QueryImpl().gt(PREFIX + refDateTimeAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.nestedFilter(REF_ENTITY_ATT, FilterBuilders
						.rangeFilter(PREFIX + refDateTimeAttributeName).gt(DataConverter.toString(value))));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleGreaterDecimal()
	{
		Double value = Double.valueOf(1.23);
		Query q = new QueryImpl().gt(PREFIX + refDecimalAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
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
		Query q = new QueryImpl().gt(PREFIX + refIntAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
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
		Query q = new QueryImpl().gt(PREFIX + refLongAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
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
		Query q = new QueryImpl().ge(PREFIX + refDateAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
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
		Query q = new QueryImpl().le(PREFIX + refDecimalAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
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
		Query q = new QueryImpl().lt(PREFIX + refIntAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
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
		Query q = new QueryImpl().in(PREFIX + refCategoricalAttributeName, values);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
	}

	@Test
	public void generateOneQueryRuleLikeCompoundPartString()
	{
		String value = "value";
		Query q = new QueryImpl().like(PREFIX + refCompoundPart0AttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
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
		Query q = new QueryImpl().eq(PREFIX + refBoolAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
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
		Query q = new QueryImpl().eq(PREFIX + refStringAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.nestedFilter(REF_ENTITY_ATT, FilterBuilders.termFilter(
						PREFIX + refStringAttributeName + '.' + MappingsBuilder.FIELD_NOT_ANALYZED, value)));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void generateOneQueryRuleEqualsCategorical()
	{
		String value = "id";
		Query q = new QueryImpl().eq(PREFIX + refCategoricalAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
	}

	@Test
	public void generateOneQueryRuleNotEqualsEqualsBool()
	{
		Boolean value = Boolean.TRUE;
		Query q = new QueryImpl().not().eq(PREFIX + refBoolAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.boolQuery().mustNot(
				QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), FilterBuilders.nestedFilter(REF_ENTITY_ATT,
						FilterBuilders.termFilter(PREFIX + refBoolAttributeName, value))));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void generateOneQueryRuleNotEqualsCategorical()
	{
		String value = "id";
		Query q = new QueryImpl().not().eq(PREFIX + refCategoricalAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
	}

	@Test(expectedExceptions = MolgenisQueryException.class)
	public void generateOneQueryRuleNotEqualsCompound()
	{
		Object value = "value";
		Query q = new QueryImpl().not().eq(PREFIX + refCompoundAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
	}

	@Test
	public void generateOneQueryRuleNotEqualsCompoundPartString()
	{
		String value = "value";
		Query q = new QueryImpl().not().eq(PREFIX + refCompoundPart0AttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.boolQuery()
				.mustNot(QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
						FilterBuilders.nestedFilter(REF_ENTITY_ATT, FilterBuilders.termFilter(
								PREFIX + refCompoundPart0AttributeName + '.' + MappingsBuilder.FIELD_NOT_ANALYZED,
								value))));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleRangeInt()
	{
		Integer low = Integer.valueOf(3);
		Integer high = Integer.valueOf(9);
		Query q = new QueryImpl().rng(PREFIX + refIntAttributeName, low, high);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
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
		Query q = new QueryImpl().search(PREFIX + refCategoricalAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
	}

	@Test
	public void generateOneQueryRuleSearchOneFieldCompoundPartString()
	{
		String value = "value";
		Query q = new QueryImpl().search(PREFIX + refCompoundPart0AttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.nestedQuery(REF_ENTITY_ATT,
				QueryBuilders.matchQuery(PREFIX + refCompoundPart0AttributeName, value));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateMultipleQueryRule()
	{
		// query: ref.a or (b and ref.c)
		Boolean booleanValue = Boolean.TRUE;
		String stringValue = "str";
		Integer intValue = 1;
		Query q = new QueryImpl().eq(PREFIX + refBoolAttributeName, booleanValue).or().nest()
				.eq(stringAttributeName, stringValue).and().eq(PREFIX + refIntAttributeName, intValue).unnest();
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
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
		Query q = new QueryImpl().eq(PREFIX + refBoolAttributeName, booleanValue).and().not()
				.eq(stringAttributeName, stringValue).and().not().eq(PREFIX + refIntAttributeName, intValue);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
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
