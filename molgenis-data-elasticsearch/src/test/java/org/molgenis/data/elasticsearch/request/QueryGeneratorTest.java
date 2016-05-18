package org.molgenis.data.elasticsearch.request;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.molgenis.data.elasticsearch.index.ElasticsearchIndexCreator.DEFAULT_ANALYZER;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_LABEL;
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
import org.molgenis.data.DataConverter;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisQueryException;
import org.molgenis.data.Query;
import org.molgenis.data.elasticsearch.index.MappingsBuilder;
import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.meta.EntityMetaDataImpl;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.fieldtypes.EnumField;
import org.molgenis.util.MolgenisDateFormat;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

// FIXME add nillable tests
public class QueryGeneratorTest
{
	private SearchRequestBuilder searchRequestBuilder;
	private EntityMetaData entityMetaData;

	private final String idAttributeName = "xid";
	private final String boolAttributeName = "xbool";
	private final String categoricalAttributeName = "xcategorical";
	private final String compoundAttributeName = "xcompound";
	private final String compoundPart0AttributeName = "xcompoundpart0";
	private final String compoundPart1AttributeName = "xcompoundpart1";
	private final String dateAttributeName = "xdate";
	private final String dateTimeAttributeName = "xdatetime";
	private final String decimalAttributeName = "xdecimal";
	private final String emailAttributeName = "xemail";
	private final String enumAttributeName = "xenum";
	private final String htmlAttributeName = "xhtml";
	private final String hyperlinkAttributeName = "xhyperlink";
	private final String intAttributeName = "xint";
	private final String longAttributeName = "xlong";
	private final String mrefAttributeName = "xmref";
	private final String scriptAttributeName = "xscript";
	private final String stringAttributeName = "xstring";
	private final String textAttributeName = "xtext";
	private final String xrefAttributeName = "xxref";

	private final String refStringAttributeName = "ref_xstring";
	private final String refMrefAttributeName = "ref_xmref";

	@BeforeMethod
	public void setUp()
	{
		searchRequestBuilder = mock(SearchRequestBuilder.class);

		EntityMetaData refEntityMetaData = new EntityMetaDataImpl("ref_entity");
		refEntityMetaData.addAttribute(idAttributeName, ROLE_ID);
		refEntityMetaData.addAttribute(refStringAttributeName, ROLE_LABEL).setUnique(true);
		refEntityMetaData.addAttribute(refMrefAttributeName).setDataType(MolgenisFieldTypes.MREF).setNillable(true)
				.setRefEntity(refEntityMetaData);

		EntityMetaData entityMetaData = new EntityMetaDataImpl("entity");
		entityMetaData.addAttribute(idAttributeName, ROLE_ID);
		entityMetaData.addAttribute(boolAttributeName).setDataType(MolgenisFieldTypes.BOOL);
		entityMetaData.addAttribute(categoricalAttributeName).setDataType(MolgenisFieldTypes.CATEGORICAL)
				.setRefEntity(refEntityMetaData);
		AttributeMetaData compoundPart0Attribute = new AttributeMetaData(compoundPart0AttributeName)
				.setDataType(MolgenisFieldTypes.STRING);
		AttributeMetaData compoundPart1Attribute = new AttributeMetaData(compoundPart1AttributeName)
				.setDataType(MolgenisFieldTypes.STRING);
		entityMetaData.addAttribute(compoundAttributeName).setDataType(MolgenisFieldTypes.COMPOUND)
				.setAttributeParts(
						Arrays.<AttributeMetaData> asList(compoundPart0Attribute, compoundPart1Attribute));
		entityMetaData.addAttribute(dateAttributeName).setDataType(MolgenisFieldTypes.DATE);
		entityMetaData.addAttribute(dateTimeAttributeName).setDataType(MolgenisFieldTypes.DATETIME);
		entityMetaData.addAttribute(decimalAttributeName).setDataType(MolgenisFieldTypes.DECIMAL);
		entityMetaData.addAttribute(emailAttributeName).setDataType(MolgenisFieldTypes.EMAIL);
		entityMetaData.addAttribute(enumAttributeName).setDataType(new EnumField())
				.setEnumOptions(Arrays.asList("enum0", "enum1", "enum2"));
		entityMetaData.addAttribute(htmlAttributeName).setDataType(MolgenisFieldTypes.HTML);
		entityMetaData.addAttribute(hyperlinkAttributeName).setDataType(MolgenisFieldTypes.HYPERLINK);
		entityMetaData.addAttribute(intAttributeName).setDataType(MolgenisFieldTypes.INT);
		entityMetaData.addAttribute(longAttributeName).setDataType(MolgenisFieldTypes.LONG);
		entityMetaData.addAttribute(mrefAttributeName).setDataType(MolgenisFieldTypes.MREF)
				.setRefEntity(refEntityMetaData);
		entityMetaData.addAttribute(scriptAttributeName).setDataType(MolgenisFieldTypes.SCRIPT);
		entityMetaData.addAttribute(stringAttributeName).setDataType(MolgenisFieldTypes.STRING);
		entityMetaData.addAttribute(textAttributeName).setDataType(MolgenisFieldTypes.TEXT);
		entityMetaData.addAttribute(xrefAttributeName).setDataType(MolgenisFieldTypes.XREF)
				.setRefEntity(refEntityMetaData);

		this.entityMetaData = entityMetaData;
	}

	@Test(expectedExceptions = MolgenisQueryException.class)
	public void generateOneQueryRuleGreaterInvalidAttribute()
	{
		String value = "str";
		Query<Entity> q = new QueryImpl<Entity>().gt(stringAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.rangeFilter(stringAttributeName).gt(value));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleGreaterDate() throws ParseException
	{
		String date = "2015-01-22";
		Date value = MolgenisDateFormat.getDateFormat().parse(date);
		Query<Entity> q = new QueryImpl<Entity>().gt(dateAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.rangeFilter(dateAttributeName).gt(date));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleGreaterDateTime() throws ParseException
	{
		Date value = MolgenisDateFormat.getDateTimeFormat().parse("2015-05-22T11:12:13+0500");
		Query<Entity> q = new QueryImpl<Entity>().gt(dateTimeAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.rangeFilter(dateTimeAttributeName).gt(DataConverter.toString(value)));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleGreaterDecimal()
	{
		Double value = Double.valueOf(1.23);
		Query<Entity> q = new QueryImpl<Entity>().gt(decimalAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.rangeFilter(decimalAttributeName).gt(value));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleGreaterInt()
	{
		Integer value = Integer.valueOf(1);
		Query<Entity> q = new QueryImpl<Entity>().gt(intAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.rangeFilter(intAttributeName).gt(value));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleGreaterLong()
	{
		Long value = Long.valueOf(1l);
		Query<Entity> q = new QueryImpl<Entity>().gt(longAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.rangeFilter(longAttributeName).gt(value));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test(expectedExceptions = MolgenisQueryException.class)
	public void generateOneQueryRuleGreaterEqualInvalidAttribute()
	{
		String value = "str";
		Query<Entity> q = new QueryImpl<Entity>().ge(stringAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.rangeFilter(stringAttributeName).gte(value));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleGreaterEqualDate() throws ParseException
	{
		String date = "2015-05-22";
		Date value = MolgenisDateFormat.getDateFormat().parse(date);
		Query<Entity> q = new QueryImpl<Entity>().ge(dateAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.rangeFilter(dateAttributeName).gte(date));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleGreaterEqualDateTime() throws ParseException
	{
		Date value = MolgenisDateFormat.getDateTimeFormat().parse("2015-05-22T11:12:13+0500");
		Query<Entity> q = new QueryImpl<Entity>().ge(dateTimeAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.rangeFilter(dateTimeAttributeName).gte(DataConverter.toString(value)));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleGreaterEqualDecimal()
	{
		Double value = Double.valueOf(1.23);
		Query<Entity> q = new QueryImpl<Entity>().ge(decimalAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.rangeFilter(decimalAttributeName).gte(value));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleGreaterEqualInt()
	{
		Integer value = Integer.valueOf(1);
		Query<Entity> q = new QueryImpl<Entity>().ge(intAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.rangeFilter(intAttributeName).gte(value));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleGreaterEqualLong()
	{
		Long value = Long.valueOf(1l);
		Query<Entity> q = new QueryImpl<Entity>().ge(longAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.rangeFilter(longAttributeName).gte(value));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test(expectedExceptions = MolgenisQueryException.class)
	public void generateOneQueryRuleLesserEqualInvalidAttribute()
	{
		String value = "str";
		Query<Entity> q = new QueryImpl<>().le(stringAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.rangeFilter(stringAttributeName).lte(value));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleLesserEqualDate() throws ParseException
	{
		String date = "2015-05-22";
		Date value = MolgenisDateFormat.getDateFormat().parse(date);
		Query<Entity> q = new QueryImpl<>().le(dateAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.rangeFilter(dateAttributeName).lte(date));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleLesserEqualDateTime() throws ParseException
	{
		Date value = MolgenisDateFormat.getDateTimeFormat().parse("2015-05-22T11:12:13+0500");
		Query<Entity> q = new QueryImpl<>().le(dateTimeAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.rangeFilter(dateTimeAttributeName).lte(DataConverter.toString(value)));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleLesserEqualDecimal()
	{
		Double value = Double.valueOf(1.23);
		Query<Entity> q = new QueryImpl<>().le(decimalAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.rangeFilter(decimalAttributeName).lte(value));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleLesserEqualInt()
	{
		Integer value = Integer.valueOf(1);
		Query<Entity> q = new QueryImpl<>().le(intAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.rangeFilter(intAttributeName).lte(value));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleLesserEqualLong()
	{
		Long value = Long.valueOf(1l);
		Query<Entity> q = new QueryImpl<>().le(longAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.rangeFilter(longAttributeName).lte(value));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test(expectedExceptions = MolgenisQueryException.class)
	public void generateOneQueryRuleLesserInvalidAttribute()
	{
		String value = "str";
		Query<Entity> q = new QueryImpl<Entity>().lt(stringAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.rangeFilter(stringAttributeName).lt(value));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleInBool()
	{
		Iterable<Object> values = Arrays.<Object> asList(Boolean.TRUE, Boolean.FALSE);
		Query<Entity> q = new QueryImpl<>().in(boolAttributeName, values);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.inFilter(boolAttributeName, new Object[]
				{ Boolean.TRUE, Boolean.FALSE }));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleInCategorical_Ids()
	{
		Iterable<String> values = Arrays.asList("id0", "id1", "id2");
		Query<Entity> q = new QueryImpl<>().in(categoricalAttributeName, values);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.nestedFilter(categoricalAttributeName, FilterBuilders.inFilter(
						categoricalAttributeName + '.' + idAttributeName + '.' + MappingsBuilder.FIELD_NOT_ANALYZED,
						new Object[]
						{ "id0", "id1", "id2" })));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleInCategorical_Entities()
	{
		Entity ref0 = new MapEntity(idAttributeName);
		ref0.set(idAttributeName, "id0");
		Entity ref1 = new MapEntity(idAttributeName);
		ref1.set(idAttributeName, "id1");
		Entity ref2 = new MapEntity(idAttributeName);
		ref2.set(idAttributeName, "id2");

		Iterable<Object> values = Arrays.<Object> asList(ref0, ref1, ref2);
		Query<Entity> q = new QueryImpl<>().in(categoricalAttributeName, values);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.nestedFilter(categoricalAttributeName, FilterBuilders.inFilter(
						categoricalAttributeName + '.' + idAttributeName + '.' + MappingsBuilder.FIELD_NOT_ANALYZED,
						new Object[]
						{ "id0", "id1", "id2" })));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleInDate() throws ParseException
	{
		Date date1 = MolgenisDateFormat.getDateFormat().parse("2015-05-22");
		Date date2 = MolgenisDateFormat.getDateFormat().parse("2015-05-23");
		Iterable<Object> values = Arrays.<Object> asList(date1, date2);
		Query<Entity> q = new QueryImpl<>().in(dateAttributeName, values);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.inFilter(dateAttributeName, new Object[]
				{ date1, date2 }));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleInDateTime() throws ParseException
	{
		Date date1 = MolgenisDateFormat.getDateFormat().parse("2015-05-22");
		Date date2 = MolgenisDateFormat.getDateFormat().parse("2015-05-23");
		Iterable<Object> values = Arrays.<Object> asList(date1, date2);
		Query<Entity> q = new QueryImpl<>().in(dateTimeAttributeName, values);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.inFilter(dateTimeAttributeName, new Object[]
				{ date1, date2 }));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleInDecimal()
	{
		Double double1 = Double.valueOf(1.23);
		Double double2 = Double.valueOf(2.34);
		Iterable<Object> values = Arrays.<Object> asList(double1, double2);
		Query<Entity> q = new QueryImpl<>().in(decimalAttributeName, values);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.inFilter(decimalAttributeName, new Object[]
				{ double1, double2 }));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleInEmail()
	{
		String value1 = "e@mail.com";
		String value2 = "em@ail.com";
		Iterable<Object> values = Arrays.<Object> asList(value1, value2);
		Query<Entity> q = new QueryImpl<>().in(emailAttributeName, values);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.inFilter(emailAttributeName + '.' + MappingsBuilder.FIELD_NOT_ANALYZED, new Object[]
				{ value1, value2 }));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleInEnum()
	{
		String value1 = "enum0";
		String value2 = "enum1";
		Iterable<Object> values = Arrays.<Object> asList(value1, value2);
		Query<Entity> q = new QueryImpl<>().in(enumAttributeName, values);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.inFilter(enumAttributeName + '.' + MappingsBuilder.FIELD_NOT_ANALYZED, new Object[]
				{ value1, value2 }));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleInHtml()
	{
		String value1 = "<h1>title</h1>";
		String value2 = "<h2>subtitle</h2>";
		Iterable<Object> values = Arrays.<Object> asList(value1, value2);
		Query<Entity> q = new QueryImpl<>().in(htmlAttributeName, values);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.inFilter(htmlAttributeName + '.' + MappingsBuilder.FIELD_NOT_ANALYZED, new Object[]
				{ value1, value2 }));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleInHyperlink()
	{
		String value1 = "http://www.site0.com/";
		String value2 = "http://www.site1.com/";
		Iterable<Object> values = Arrays.<Object> asList(value1, value2);
		Query<Entity> q = new QueryImpl<>().in(hyperlinkAttributeName, values);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.inFilter(hyperlinkAttributeName + '.' + MappingsBuilder.FIELD_NOT_ANALYZED, new Object[]
				{ value1, value2 }));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleInInt()
	{
		Integer value1 = Integer.valueOf(1);
		Integer value2 = Integer.valueOf(2);
		Iterable<Object> values = Arrays.<Object> asList(value1, value2);
		Query<Entity> q = new QueryImpl<>().in(intAttributeName, values);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.inFilter(intAttributeName, new Object[]
				{ value1, value2 }));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleInLong()
	{
		Long value1 = Long.valueOf(0l);
		Long value2 = Long.valueOf(1l);
		Iterable<Object> values = Arrays.<Object> asList(value1, value2);
		Query<Entity> q = new QueryImpl<>().in(longAttributeName, values);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.inFilter(longAttributeName, new Object[]
				{ value1, value2 }));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleInMref_Entities()
	{
		Entity ref0 = new MapEntity(idAttributeName);
		ref0.set(idAttributeName, "id0");
		Entity ref1 = new MapEntity(idAttributeName);
		ref1.set(idAttributeName, "id1");
		Entity ref2 = new MapEntity(idAttributeName);
		ref2.set(idAttributeName, "id2");

		Iterable<Object> values = Arrays.<Object> asList(ref0, ref1, ref2);
		Query<Entity> q = new QueryImpl<>().in(mrefAttributeName, values);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.nestedFilter(mrefAttributeName,
						FilterBuilders.inFilter(
								mrefAttributeName + '.' + idAttributeName + '.' + MappingsBuilder.FIELD_NOT_ANALYZED,
								new Object[]
								{ "id0", "id1", "id2" })));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleInMref_Ids()
	{
		Iterable<String> values = Arrays.asList("id0", "id1", "id2");
		Query<Entity> q = new QueryImpl<>().in(mrefAttributeName, values);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.nestedFilter(mrefAttributeName,
						FilterBuilders.inFilter(
								mrefAttributeName + '.' + idAttributeName + '.' + MappingsBuilder.FIELD_NOT_ANALYZED,
								new Object[]
								{ "id0", "id1", "id2" })));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleInScript()
	{
		String value1 = "var a = 0;";
		String value2 = "var b = 'a'";
		Iterable<Object> values = Arrays.<Object> asList(value1, value2);
		Query<Entity> q = new QueryImpl<>().in(scriptAttributeName, values);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.inFilter(scriptAttributeName + '.' + MappingsBuilder.FIELD_NOT_ANALYZED, new Object[]
				{ value1, value2 }));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleInString()
	{
		String value1 = "str0";
		String value2 = "str1";
		Iterable<Object> values = Arrays.<Object> asList(value1, value2);
		Query<Entity> q = new QueryImpl<>().in(stringAttributeName, values);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.inFilter(stringAttributeName + '.' + MappingsBuilder.FIELD_NOT_ANALYZED, new Object[]
				{ value1, value2 }));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleInText()
	{
		String value1 = "some very long text";
		String value2 = "a bit shorter text";
		Iterable<Object> values = Arrays.<Object> asList(value1, value2);
		Query<Entity> q = new QueryImpl<>().in(textAttributeName, values);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.inFilter(textAttributeName + '.' + MappingsBuilder.FIELD_NOT_ANALYZED, new Object[]
				{ value1, value2 }));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleInXref_Ids()
	{
		Iterable<String> values = Arrays.asList("id0", "id1", "id2");
		Query<Entity> q = new QueryImpl<>().in(xrefAttributeName, values);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.nestedFilter(xrefAttributeName,
						FilterBuilders.inFilter(
								xrefAttributeName + '.' + idAttributeName + '.' + MappingsBuilder.FIELD_NOT_ANALYZED,
								new Object[]
								{ "id0", "id1", "id2" })));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleInXref_Entities()
	{
		Entity ref0 = new MapEntity(idAttributeName);
		ref0.set(idAttributeName, "id0");
		Entity ref1 = new MapEntity(idAttributeName);
		ref1.set(idAttributeName, "id1");
		Entity ref2 = new MapEntity(idAttributeName);
		ref2.set(idAttributeName, "id2");

		Iterable<Object> values = Arrays.<Object> asList(ref0, ref1, ref2);
		Query<Entity> q = new QueryImpl<>().in(xrefAttributeName, values);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.nestedFilter(xrefAttributeName,
						FilterBuilders.inFilter(
								xrefAttributeName + '.' + idAttributeName + '.' + MappingsBuilder.FIELD_NOT_ANALYZED,
								new Object[]
								{ "id0", "id1", "id2" })));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleLesserDate() throws ParseException
	{
		String date = "2015-05-22";
		Date value = MolgenisDateFormat.getDateFormat().parse(date);
		Query<Entity> q = new QueryImpl<Entity>().lt(dateAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.rangeFilter(dateAttributeName).lt(date));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleLesserDateTime() throws ParseException
	{
		Date value = MolgenisDateFormat.getDateTimeFormat().parse("2015-05-22T11:12:13+0500");
		Query<Entity> q = new QueryImpl<Entity>().lt(dateTimeAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.rangeFilter(dateTimeAttributeName).lt(DataConverter.toString(value)));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleLesserDecimal()
	{
		Double value = Double.valueOf(1.23);
		Query<Entity> q = new QueryImpl<Entity>().lt(decimalAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.rangeFilter(decimalAttributeName).lt(value));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleLesserInt()
	{
		Integer value = Integer.valueOf(1);
		Query<Entity> q = new QueryImpl<Entity>().lt(intAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.rangeFilter(intAttributeName).lt(value));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleLesserLong()
	{
		Long value = Long.valueOf(1l);
		Query<Entity> q = new QueryImpl<Entity>().lt(longAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.rangeFilter(longAttributeName).lt(value));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test(expectedExceptions = MolgenisQueryException.class)
	public void generateOneQueryRuleLikeBool()
	{
		String value = "value";
		Query<Entity> q = new QueryImpl<Entity>().like(boolAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void generateOneQueryRuleLikeCategorical()
	{
		String value = "value";
		Query<Entity> q = new QueryImpl<Entity>().like(categoricalAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
	}

	@Test(expectedExceptions = MolgenisQueryException.class)
	public void generateOneQueryRuleLikeCompound()
	{
		String value = "value";
		Query<Entity> q = new QueryImpl<Entity>().like(compoundAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
	}

	@Test
	public void generateOneQueryRuleLikeCompoundPartString()
	{
		String value = "value";
		Query<Entity> q = new QueryImpl<Entity>().like(compoundPart0AttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders
				.matchQuery(compoundPart0AttributeName + '.' + MappingsBuilder.FIELD_NGRAM_ANALYZED, value)
				.analyzer(DEFAULT_ANALYZER);
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test(expectedExceptions = MolgenisQueryException.class)
	public void generateOneQueryRuleLikeDate()
	{
		String value = "value";
		Query<Entity> q = new QueryImpl<Entity>().like(dateAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
	}

	@Test(expectedExceptions = MolgenisQueryException.class)
	public void generateOneQueryRuleLikeDateTime()
	{
		String value = "value";
		Query<Entity> q = new QueryImpl<Entity>().like(dateTimeAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
	}

	@Test(expectedExceptions = MolgenisQueryException.class)
	public void generateOneQueryRuleLikeDecimal()
	{
		String value = "value";
		Query<Entity> q = new QueryImpl<Entity>().like(decimalAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
	}

	@Test
	public void generateOneQueryRuleLikeEmail()
	{
		String value = "e@mail.com";
		Query<Entity> q = new QueryImpl<Entity>().like(emailAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders
				.matchQuery(emailAttributeName + '.' + MappingsBuilder.FIELD_NGRAM_ANALYZED, value)
				.analyzer(DEFAULT_ANALYZER);
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleLikeEnum()
	{
		String value = "enum0";
		Query<Entity> q = new QueryImpl<Entity>().like(enumAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders
				.matchQuery(enumAttributeName + '.' + MappingsBuilder.FIELD_NGRAM_ANALYZED, value)
				.analyzer(DEFAULT_ANALYZER);
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void generateOneQueryRuleLikeHtml()
	{
		String value = "<h1>html</h1>";
		Query<Entity> q = new QueryImpl<Entity>().like(htmlAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
	}

	@Test
	public void generateOneQueryRuleLikeHyperlink()
	{
		String value = "http://www.website.com/";
		Query<Entity> q = new QueryImpl<Entity>().like(hyperlinkAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders
				.matchQuery(hyperlinkAttributeName + '.' + MappingsBuilder.FIELD_NGRAM_ANALYZED, value)
				.analyzer(DEFAULT_ANALYZER);
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test(expectedExceptions = MolgenisQueryException.class)
	public void generateOneQueryRuleLikeInt()
	{
		String value = "value";
		Query<Entity> q = new QueryImpl<Entity>().like(intAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
	}

	@Test(expectedExceptions = MolgenisQueryException.class)
	public void generateOneQueryRuleLikeLong()
	{
		String value = "value";
		Query<Entity> q = new QueryImpl<Entity>().like(longAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void generateOneQueryRuleLikeMref()
	{
		String value = "value";
		Query<Entity> q = new QueryImpl<Entity>().like(mrefAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void generateOneQueryRuleLikeScript()
	{
		String value = "int a = 1;";
		Query<Entity> q = new QueryImpl<Entity>().like(scriptAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
	}

	@Test
	public void generateOneQueryRuleLikeString()
	{
		String value = "value";
		Query<Entity> q = new QueryImpl<Entity>().like(stringAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders
				.matchQuery(stringAttributeName + '.' + MappingsBuilder.FIELD_NGRAM_ANALYZED, value)
				.analyzer(DEFAULT_ANALYZER);
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void generateOneQueryRuleLikeText()
	{
		String value = "some long text";
		Query<Entity> q = new QueryImpl<Entity>().like(textAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void generateOneQueryRuleLikeXref()
	{
		String value = "value";
		Query<Entity> q = new QueryImpl<Entity>().like(xrefAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
	}

	@Test
	public void generateOneQueryRuleEqualsBoolNull()
	{
		Boolean value = null;
		Query<Entity> q = new QueryImpl<Entity>().eq(boolAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.missingFilter(boolAttributeName).existence(true).nullValue(true));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	// FIXME add test for ref entity where id attribute is int
	// FIXME add test where value is entity
	@Test
	public void generateOneQueryRuleEqualsCategoricalNull()
	{
		String value = null;
		Query<Entity> q = new QueryImpl<Entity>().eq(categoricalAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.notFilter(FilterBuilders.nestedFilter(categoricalAttributeName, FilterBuilders
						.existsFilter(categoricalAttributeName + ".xid." + MappingsBuilder.FIELD_NOT_ANALYZED))));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test(expectedExceptions = MolgenisQueryException.class)
	public void generateOneQueryRuleEqualsCompoundNull()
	{
		Object value = null;
		Query<Entity> q = new QueryImpl<Entity>().eq(compoundAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
	}

	@Test
	public void generateOneQueryRuleEqualsCompoundPartStringNull()
	{
		String value = null;
		Query<Entity> q = new QueryImpl<Entity>().eq(compoundPart0AttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.missingFilter(compoundPart0AttributeName).existence(true).nullValue(true));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleEqualsDateNull() throws ParseException
	{
		Date value = null;
		Query<Entity> q = new QueryImpl<Entity>().eq(dateAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.missingFilter(dateAttributeName).existence(true).nullValue(true));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleEqualsDateTimeNull() throws ParseException
	{
		Date value = null;
		Query<Entity> q = new QueryImpl<Entity>().eq(dateTimeAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.missingFilter(dateTimeAttributeName).existence(true).nullValue(true));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleEqualsDecimalNull()
	{
		Double value = null;
		Query<Entity> q = new QueryImpl<Entity>().eq(decimalAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.missingFilter(decimalAttributeName).existence(true).nullValue(true));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleEqualsEmailNull()
	{
		String value = null;
		Query<Entity> q = new QueryImpl<Entity>().eq(emailAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.missingFilter(emailAttributeName).existence(true).nullValue(true));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleEqualsEnumNull()
	{
		String value = null;
		Query<Entity> q = new QueryImpl<Entity>().eq(enumAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.missingFilter(enumAttributeName).existence(true).nullValue(true));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleEqualsHtmlNull()
	{
		String value = null;
		Query<Entity> q = new QueryImpl<Entity>().eq(htmlAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.missingFilter(htmlAttributeName).existence(true).nullValue(true));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleEqualsHyperlinkNull()
	{
		String value = null;
		Query<Entity> q = new QueryImpl<Entity>().eq(hyperlinkAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.missingFilter(hyperlinkAttributeName).existence(true).nullValue(true));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleEqualsIntNull()
	{
		Integer value = null;
		Query<Entity> q = new QueryImpl<Entity>().eq(intAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.missingFilter(intAttributeName).existence(true).nullValue(true));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleEqualsLongNull()
	{
		Long value = null;
		Query<Entity> q = new QueryImpl<Entity>().eq(longAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.missingFilter(longAttributeName).existence(true).nullValue(true));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	// TODO enable when implemented in QueryGenerator (see note in QueryGenerator)
	// @Test
	// public void generateOneQueryRuleEqualsMrefNull()
	// {
	// }

	@Test
	public void generateOneQueryRuleEqualsScriptNull()
	{
		String value = null;
		Query<Entity> q = new QueryImpl<Entity>().eq(scriptAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.missingFilter(scriptAttributeName).existence(true).nullValue(true));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleEqualsStringNull()
	{
		String value = null;
		Query<Entity> q = new QueryImpl<Entity>().eq(stringAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.missingFilter(stringAttributeName).existence(true).nullValue(true));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleEqualsTextNull()
	{
		String value = null;
		Query<Entity> q = new QueryImpl<Entity>().eq(textAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.missingFilter(textAttributeName).existence(true).nullValue(true));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	// FIXME add test for ref entity where id attribute is int
	// FIXME add test where value is entity
	@Test
	public void generateOneQueryRuleEqualsXrefNull()
	{
		String value = null;
		Query<Entity> q = new QueryImpl<Entity>().eq(xrefAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.notFilter(FilterBuilders.nestedFilter(xrefAttributeName, FilterBuilders
						.existsFilter(xrefAttributeName + ".xid." + MappingsBuilder.FIELD_NOT_ANALYZED))));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleNotEqualsBoolNull()
	{
		Boolean value = null;
		Query<Entity> q = new QueryImpl<Entity>().not().eq(boolAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.boolQuery()
				.mustNot(QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
						FilterBuilders.missingFilter(boolAttributeName).existence(true).nullValue(true)));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	// FIXME add test for ref entity where id attribute is int
	// FIXME add test where value is entity
	@Test
	public void generateOneQueryRuleNotEqualsCategoricalNull()
	{
		String value = null;
		Query<Entity> q = new QueryImpl<Entity>().not().eq(categoricalAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.boolQuery().mustNot(QueryBuilders.filteredQuery(
				QueryBuilders.matchAllQuery(),
				FilterBuilders.notFilter(FilterBuilders.nestedFilter(categoricalAttributeName, FilterBuilders
						.existsFilter(categoricalAttributeName + ".xid." + MappingsBuilder.FIELD_NOT_ANALYZED)))));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test(expectedExceptions = MolgenisQueryException.class)
	public void generateOneQueryRuleNotEqualsCompoundNull()
	{
		Object value = null;
		Query<Entity> q = new QueryImpl<Entity>().not().eq(compoundAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
	}

	@Test
	public void generateOneQueryRuleNotEqualsCompoundPartStringNull()
	{
		String value = null;
		Query<Entity> q = new QueryImpl<Entity>().not().eq(compoundPart0AttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.boolQuery()
				.mustNot(QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
						FilterBuilders.missingFilter(compoundPart0AttributeName).existence(true).nullValue(true)));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleNotEqualsDateNull() throws ParseException
	{
		Date value = null;
		Query<Entity> q = new QueryImpl<Entity>().not().eq(dateAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.boolQuery()
				.mustNot(QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
						FilterBuilders.missingFilter(dateAttributeName).existence(true).nullValue(true)));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleNotEqualsDateTimeNull() throws ParseException
	{
		Date value = null;
		Query<Entity> q = new QueryImpl<Entity>().not().eq(dateTimeAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.boolQuery()
				.mustNot(QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
						FilterBuilders.missingFilter(dateTimeAttributeName).existence(true).nullValue(true)));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleNotEqualsDecimalNull()
	{
		Double value = null;
		Query<Entity> q = new QueryImpl<Entity>().not().eq(decimalAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.boolQuery()
				.mustNot(QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
						FilterBuilders.missingFilter(decimalAttributeName).existence(true).nullValue(true)));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleNotEqualsEmailNull()
	{
		String value = null;
		Query<Entity> q = new QueryImpl<Entity>().not().eq(emailAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.boolQuery()
				.mustNot(QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
						FilterBuilders.missingFilter(emailAttributeName).existence(true).nullValue(true)));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleNotEqualsEnumNull()
	{
		String value = null;
		Query<Entity> q = new QueryImpl<Entity>().not().eq(enumAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.boolQuery()
				.mustNot(QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
						FilterBuilders.missingFilter(enumAttributeName).existence(true).nullValue(true)));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleNotEqualsHtmlNull()
	{
		String value = null;
		Query<Entity> q = new QueryImpl<Entity>().not().eq(htmlAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.boolQuery()
				.mustNot(QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
						FilterBuilders.missingFilter(htmlAttributeName).existence(true).nullValue(true)));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleNotEqualsHyperlinkNull()
	{
		String value = null;
		Query<Entity> q = new QueryImpl<Entity>().not().eq(hyperlinkAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.boolQuery()
				.mustNot(QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
						FilterBuilders.missingFilter(hyperlinkAttributeName).existence(true).nullValue(true)));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleNotEqualsIntNull()
	{
		Integer value = null;
		Query<Entity> q = new QueryImpl<Entity>().not().eq(intAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.boolQuery()
				.mustNot(QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
						FilterBuilders.missingFilter(intAttributeName).existence(true).nullValue(true)));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleNotEqualsLongNull()
	{
		Long value = null;
		Query<Entity> q = new QueryImpl<Entity>().not().eq(longAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.boolQuery()
				.mustNot(QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
						FilterBuilders.missingFilter(longAttributeName).existence(true).nullValue(true)));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	// TODO enable when implemented in QueryGenerator (see note in QueryGenerator)
	// @Test
	// public void generateOneQueryRuleNotEqualsMrefNull()
	// {
	// }

	@Test
	public void generateOneQueryRuleNotEqualsScriptNull()
	{
		String value = null;
		Query<Entity> q = new QueryImpl<Entity>().not().eq(scriptAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.boolQuery()
				.mustNot(QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
						FilterBuilders.missingFilter(scriptAttributeName).existence(true).nullValue(true)));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleNotEqualsStringNull()
	{
		String value = null;
		Query<Entity> q = new QueryImpl<Entity>().not().eq(stringAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.boolQuery()
				.mustNot(QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
						FilterBuilders.missingFilter(stringAttributeName).existence(true).nullValue(true)));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleNotEqualsTextNull()
	{
		String value = null;
		Query<Entity> q = new QueryImpl<Entity>().not().eq(textAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.boolQuery()
				.mustNot(QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
						FilterBuilders.missingFilter(textAttributeName).existence(true).nullValue(true)));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	// FIXME add test for ref entity where id attribute is int
	// FIXME add test where value is entity
	@Test
	public void generateOneQueryRuleNotEqualsXrefNull()
	{
		String value = null;
		Query<Entity> q = new QueryImpl<Entity>().not().eq(xrefAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.boolQuery()
				.mustNot(QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
						FilterBuilders.notFilter(FilterBuilders.nestedFilter(xrefAttributeName, FilterBuilders
								.existsFilter(xrefAttributeName + ".xid." + MappingsBuilder.FIELD_NOT_ANALYZED)))));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleEqualsBool()
	{
		Boolean value = Boolean.TRUE;
		Query<Entity> q = new QueryImpl<Entity>().eq(boolAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.termFilter(boolAttributeName, value));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	// FIXME add test for ref entity where id attribute is int
	// FIXME add test where value is entity
	@Test
	public void generateOneQueryRuleEqualsCategorical()
	{
		String value = "id";
		Query<Entity> q = new QueryImpl<Entity>().eq(categoricalAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.nestedFilter(categoricalAttributeName, FilterBuilders
						.termFilter(categoricalAttributeName + ".xid." + MappingsBuilder.FIELD_NOT_ANALYZED, value)));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test(expectedExceptions = MolgenisQueryException.class)
	public void generateOneQueryRuleEqualsCompound()
	{
		Object value = "value";
		Query<Entity> q = new QueryImpl<Entity>().eq(compoundAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
	}

	@Test
	public void generateOneQueryRuleEqualsCompoundPartString()
	{
		String value = "value";
		Query<Entity> q = new QueryImpl<Entity>().eq(compoundPart0AttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), FilterBuilders
				.termFilter(compoundPart0AttributeName + '.' + MappingsBuilder.FIELD_NOT_ANALYZED, value));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleEqualsDate() throws ParseException
	{
		Date value = MolgenisDateFormat.getDateFormat().parse("2015-01-15");
		Query<Entity> q = new QueryImpl<Entity>().eq(dateAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.termFilter(dateAttributeName, "2015-01-15"));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleEqualsDateTime() throws ParseException
	{
		Date value = MolgenisDateFormat.getDateTimeFormat().parse("2015-05-22T11:12:13+0500");
		Query<Entity> q = new QueryImpl<Entity>().eq(dateTimeAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.termFilter(dateTimeAttributeName, MolgenisDateFormat.getDateTimeFormat().format(value)));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleEqualsDecimal()
	{
		Double value = Double.valueOf(1.23);
		Query<Entity> q = new QueryImpl<Entity>().eq(decimalAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.termFilter(decimalAttributeName, value));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleEqualsEmail()
	{
		String value = "e@mail.com";
		Query<Entity> q = new QueryImpl<Entity>().eq(emailAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.termFilter(emailAttributeName + '.' + MappingsBuilder.FIELD_NOT_ANALYZED, value));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleEqualsEnum()
	{
		String value = "enum0";
		Query<Entity> q = new QueryImpl<Entity>().eq(enumAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.termFilter(enumAttributeName + '.' + MappingsBuilder.FIELD_NOT_ANALYZED, value));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleEqualsHtml()
	{
		String value = "<h1>html</h1>";
		Query<Entity> q = new QueryImpl<Entity>().eq(htmlAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.termFilter(htmlAttributeName + '.' + MappingsBuilder.FIELD_NOT_ANALYZED, value));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleEqualsHyperlink()
	{
		String value = "http://www.website.com/";
		Query<Entity> q = new QueryImpl<Entity>().eq(hyperlinkAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.termFilter(hyperlinkAttributeName + '.' + MappingsBuilder.FIELD_NOT_ANALYZED, value));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleEqualsInt()
	{
		Integer value = Integer.valueOf(1);
		Query<Entity> q = new QueryImpl<Entity>().eq(intAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.termFilter(intAttributeName, value));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleEqualsLong()
	{
		Long value = Long.valueOf(1);
		Query<Entity> q = new QueryImpl<Entity>().eq(longAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.termFilter(longAttributeName, value));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	// TODO enable when implemented in QueryGenerator (see note in QueryGenerator)
	// @Test
	// public void generateOneQueryRuleEqualsMref()
	// {
	// }

	@Test
	public void generateOneQueryRuleEqualsScript()
	{
		String value = "int a = 1;";
		Query<Entity> q = new QueryImpl<Entity>().eq(scriptAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.termFilter(scriptAttributeName + '.' + MappingsBuilder.FIELD_NOT_ANALYZED, value));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleEqualsString()
	{
		String value = "value";
		Query<Entity> q = new QueryImpl<Entity>().eq(stringAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.termFilter(stringAttributeName + '.' + MappingsBuilder.FIELD_NOT_ANALYZED, value));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleEqualsText()
	{
		String value = "some long text";
		Query<Entity> q = new QueryImpl<Entity>().eq(textAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.termFilter(textAttributeName + '.' + MappingsBuilder.FIELD_NOT_ANALYZED, value));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	// FIXME add test for ref entity where id attribute is int
	// FIXME add test where value is entity
	@Test
	public void generateOneQueryRuleEqualsXref()
	{
		String value = "id";
		Query<Entity> q = new QueryImpl<Entity>().eq(xrefAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.nestedFilter(xrefAttributeName, FilterBuilders
						.termFilter(xrefAttributeName + ".xid." + MappingsBuilder.FIELD_NOT_ANALYZED, value)));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleNotEqualsBool()
	{
		Boolean value = Boolean.TRUE;
		Query<Entity> q = new QueryImpl<Entity>().not().eq(boolAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.boolQuery().mustNot(QueryBuilders
				.filteredQuery(QueryBuilders.matchAllQuery(), FilterBuilders.termFilter(boolAttributeName, value)));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	// FIXME add test for ref entity where id attribute is int
	// FIXME add test where value is entity
	@Test
	public void generateOneQueryRuleNotEqualsCategorical()
	{
		String value = "id";
		Query<Entity> q = new QueryImpl<Entity>().not().eq(categoricalAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.boolQuery()
				.mustNot(
						QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
								FilterBuilders.nestedFilter(categoricalAttributeName,
										FilterBuilders.termFilter(
												categoricalAttributeName + ".xid." + MappingsBuilder.FIELD_NOT_ANALYZED,
												value))));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test(expectedExceptions = MolgenisQueryException.class)
	public void generateOneQueryRuleNotEqualsCompound()
	{
		Object value = "value";
		Query<Entity> q = new QueryImpl<Entity>().not().eq(compoundAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
	}

	@Test
	public void generateOneQueryRuleNotEqualsCompoundPartString()
	{
		String value = "value";
		Query<Entity> q = new QueryImpl<Entity>().not().eq(compoundPart0AttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.boolQuery()
				.mustNot(QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), FilterBuilders
						.termFilter(compoundPart0AttributeName + '.' + MappingsBuilder.FIELD_NOT_ANALYZED, value)));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleNotEqualsDate() throws ParseException
	{
		Date value = MolgenisDateFormat.getDateFormat().parse("2015-05-22");
		Query<Entity> q = new QueryImpl<Entity>().not().eq(dateAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.boolQuery().mustNot(QueryBuilders.filteredQuery(
				QueryBuilders.matchAllQuery(),
				FilterBuilders.termFilter(dateAttributeName, MolgenisDateFormat.getDateFormat().format(value))));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleNotEqualsDateTime() throws ParseException
	{
		Date value = MolgenisDateFormat.getDateTimeFormat().parse("2015-05-22T11:12:13+0500");
		Query<Entity> q = new QueryImpl<Entity>().not().eq(dateTimeAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.boolQuery()
				.mustNot(QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), FilterBuilders
						.termFilter(dateTimeAttributeName, MolgenisDateFormat.getDateTimeFormat().format(value))));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleNotEqualsDecimal()
	{
		Double value = Double.valueOf(1.23);
		Query<Entity> q = new QueryImpl<Entity>().not().eq(decimalAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.boolQuery().mustNot(QueryBuilders
				.filteredQuery(QueryBuilders.matchAllQuery(), FilterBuilders.termFilter(decimalAttributeName, value)));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleNotEqualsEmail()
	{
		String value = "e@mail.com";
		Query<Entity> q = new QueryImpl<Entity>().not().eq(emailAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.boolQuery().mustNot(QueryBuilders.filteredQuery(
				QueryBuilders.matchAllQuery(),
				FilterBuilders.termFilter(emailAttributeName + '.' + MappingsBuilder.FIELD_NOT_ANALYZED, value)));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleNotEqualsEnum()
	{
		String value = "enum0";
		Query<Entity> q = new QueryImpl<Entity>().not().eq(enumAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.boolQuery().mustNot(QueryBuilders.filteredQuery(
				QueryBuilders.matchAllQuery(),
				FilterBuilders.termFilter(enumAttributeName + '.' + MappingsBuilder.FIELD_NOT_ANALYZED, value)));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleNotEqualsHtml()
	{
		String value = "<h1>html</h1>";
		Query<Entity> q = new QueryImpl<Entity>().not().eq(htmlAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.boolQuery().mustNot(QueryBuilders.filteredQuery(
				QueryBuilders.matchAllQuery(),
				FilterBuilders.termFilter(htmlAttributeName + '.' + MappingsBuilder.FIELD_NOT_ANALYZED, value)));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleNotEqualsHyperlink()
	{
		String value = "http://www.website.com/";
		Query<Entity> q = new QueryImpl<Entity>().not().eq(hyperlinkAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.boolQuery().mustNot(QueryBuilders.filteredQuery(
				QueryBuilders.matchAllQuery(),
				FilterBuilders.termFilter(hyperlinkAttributeName + '.' + MappingsBuilder.FIELD_NOT_ANALYZED, value)));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleNotEqualsInt()
	{
		Integer value = Integer.valueOf(1);
		Query<Entity> q = new QueryImpl<Entity>().not().eq(intAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.boolQuery().mustNot(QueryBuilders
				.filteredQuery(QueryBuilders.matchAllQuery(), FilterBuilders.termFilter(intAttributeName, value)));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleNotEqualsLong()
	{
		Long value = Long.valueOf(1);
		Query<Entity> q = new QueryImpl<Entity>().not().eq(longAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.boolQuery().mustNot(QueryBuilders
				.filteredQuery(QueryBuilders.matchAllQuery(), FilterBuilders.termFilter(longAttributeName, value)));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	// TODO enable when implemented in QueryGenerator (see note in QueryGenerator)
	// @Test
	// public void generateOneQueryRuleNotEqualsMref()
	// {
	// }

	@Test
	public void generateOneQueryRuleNotEqualsScript()
	{
		String value = "int a = 1;";
		Query<Entity> q = new QueryImpl<Entity>().not().eq(scriptAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.boolQuery().mustNot(QueryBuilders.filteredQuery(
				QueryBuilders.matchAllQuery(),
				FilterBuilders.termFilter(scriptAttributeName + '.' + MappingsBuilder.FIELD_NOT_ANALYZED, value)));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleNotEqualsString()
	{
		String value = "value";
		Query<Entity> q = new QueryImpl<Entity>().not().eq(stringAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.boolQuery().mustNot(QueryBuilders.filteredQuery(
				QueryBuilders.matchAllQuery(),
				FilterBuilders.termFilter(stringAttributeName + '.' + MappingsBuilder.FIELD_NOT_ANALYZED, value)));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleNotEqualsText()
	{
		String value = "some long text";
		Query<Entity> q = new QueryImpl<Entity>().not().eq(textAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.boolQuery().mustNot(QueryBuilders.filteredQuery(
				QueryBuilders.matchAllQuery(),
				FilterBuilders.termFilter(textAttributeName + '.' + MappingsBuilder.FIELD_NOT_ANALYZED, value)));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	// FIXME add test for ref entity where id attribute is int
	// FIXME add test where value is entity
	@Test
	public void generateOneQueryRuleNotEqualsXref()
	{
		String value = "id";
		Query<Entity> q = new QueryImpl<Entity>().not().eq(xrefAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.boolQuery().mustNot(QueryBuilders.filteredQuery(
				QueryBuilders.matchAllQuery(), FilterBuilders.nestedFilter(xrefAttributeName, FilterBuilders
						.termFilter(xrefAttributeName + ".xid." + MappingsBuilder.FIELD_NOT_ANALYZED, value))));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleRangeInt()
	{
		Integer low = Integer.valueOf(3);
		Integer high = Integer.valueOf(9);
		Query<Entity> q = new QueryImpl<Entity>().rng(intAttributeName, low, high);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());

		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.rangeFilter(intAttributeName).gte(3).lte(9));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleRangeLong()
	{
		Long low = Long.valueOf(3);
		Long high = Long.valueOf(9);
		Query<Entity> q = new QueryImpl<Entity>().rng(longAttributeName, low, high);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());

		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.rangeFilter(longAttributeName).gte(3).lte(9));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleSearchAllFields()
	{
		String value = "my text";
		Query<Entity> q = new QueryImpl<>().search(value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.matchPhraseQuery("_all", value).slop(10);
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test(expectedExceptions = MolgenisQueryException.class)
	public void generateOneQueryRuleSearchOneFieldBool()
	{
		String value = "value";
		Query<Entity> q = new QueryImpl<>().search(boolAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
	}

	@Test
	public void generateOneQueryRuleSearchOneFieldCategorical()
	{
		String value = "text";
		Query<Entity> q = new QueryImpl<>().search(categoricalAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.nestedQuery(categoricalAttributeName,
				QueryBuilders.matchQuery(categoricalAttributeName + "._all", value));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test(expectedExceptions = MolgenisQueryException.class)
	public void generateOneQueryRuleSearchOneFieldCompound()
	{
		String value = "value";
		Query<Entity> q = new QueryImpl<>().search(compoundAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
	}

	@Test
	public void generateOneQueryRuleSearchOneFieldCompoundPartString()
	{
		String value = "value";
		Query<Entity> q = new QueryImpl<Entity>().search(compoundPart0AttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.matchQuery(compoundPart0AttributeName, value);
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleSearchOneFieldDate() throws ParseException
	{
		String value = MolgenisDateFormat.getDateFormat().parse("2015-05-22").toString();
		Query<Entity> q = new QueryImpl<Entity>().search(dateAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.matchQuery(dateAttributeName, value);
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleSearchOneFieldDateTime() throws ParseException
	{
		String value = MolgenisDateFormat.getDateFormat().parse("2015-05-22T11:12:13+0500").toString();
		Query<Entity> q = new QueryImpl<Entity>().search(dateTimeAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.matchQuery(dateTimeAttributeName, value);
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleSearchOneFieldDecimal()
	{
		String value = Double.valueOf(1.23).toString();
		Query<Entity> q = new QueryImpl<Entity>().search(decimalAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.matchQuery(decimalAttributeName, value);
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleSearchOneFieldEmail()
	{
		String value = "e@mail.com";
		Query<Entity> q = new QueryImpl<Entity>().search(emailAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.matchQuery(emailAttributeName, value);
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleSearchOneFieldEnum()
	{
		String value = "enum0";
		Query<Entity> q = new QueryImpl<Entity>().search(enumAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.matchQuery(enumAttributeName, value);
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleSearchOneFieldHtml()
	{
		String value = "<h1>html</h1>";
		Query<Entity> q = new QueryImpl<Entity>().search(htmlAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.matchQuery(htmlAttributeName, value);
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleSearchOneFieldHyperlink()
	{
		String value = "http://www.website.com/";
		Query<Entity> q = new QueryImpl<Entity>().search(hyperlinkAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.matchQuery(hyperlinkAttributeName, value);
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleSearchOneFieldInt()
	{
		String value = Integer.valueOf(1).toString();
		Query<Entity> q = new QueryImpl<Entity>().search(intAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.matchQuery(intAttributeName, value);
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleSearchOneFieldLong()
	{
		String value = Long.valueOf(1).toString();
		Query<Entity> q = new QueryImpl<Entity>().search(longAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.matchQuery(longAttributeName, value);
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleSearchOneFieldMref()
	{
		String value = "value";
		Query<Entity> q = new QueryImpl<Entity>().search(mrefAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.nestedQuery(mrefAttributeName,
				QueryBuilders.matchQuery(mrefAttributeName + "._all", value));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleSearchOneFieldScript()
	{
		String value = "int a = 1;";
		Query<Entity> q = new QueryImpl<Entity>().search(scriptAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.matchQuery(scriptAttributeName, value);
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleSearchOneFieldString()
	{
		String value = "value";
		Query<Entity> q = new QueryImpl<Entity>().search(stringAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.matchQuery(stringAttributeName, value);
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleSearchOneFieldText()
	{
		String value = "some long text";
		Query<Entity> q = new QueryImpl<Entity>().search(textAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.matchQuery(textAttributeName, value);
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleSearchOneFieldXref()
	{
		String value = "text";
		Query<Entity> q = new QueryImpl<Entity>().search(xrefAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.nestedQuery(xrefAttributeName,
				QueryBuilders.matchQuery(xrefAttributeName + "._all", value));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateMultipleQueryRule()
	{
		// query: a or (b and c)
		Boolean booleanValue = Boolean.TRUE;
		String stringValue = "str";
		Integer intValue = 1;
		Query<Entity> q = new QueryImpl<Entity>().eq(boolAttributeName, booleanValue).or().nest().eq(stringAttributeName, stringValue)
				.and().eq(intAttributeName, intValue).unnest();
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());

		FilteredQueryBuilder booleanQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.termFilter(boolAttributeName, booleanValue));
		QueryBuilder stringQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.termFilter(stringAttributeName + '.' + MappingsBuilder.FIELD_NOT_ANALYZED, stringValue));
		QueryBuilder intQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.termFilter(intAttributeName, intValue));
		BoolQueryBuilder stringIntQuery = QueryBuilders.boolQuery().must(stringQuery).must(intQuery);
		QueryBuilder expectedQuery = QueryBuilders.boolQuery().should(booleanQuery).should(stringIntQuery)
				.minimumNumberShouldMatch(1);
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	// regression test for https://github.com/molgenis/molgenis/issues/2326
	@Test
	public void generateMultipleQueryRuleMultipleNotClauses()
	{
		// query: a or (b and c)
		Boolean booleanValue = Boolean.TRUE;
		String stringValue = "str";
		Integer intValue = 1;
		Query<Entity> q = new QueryImpl<Entity>().eq(boolAttributeName, booleanValue).and().not().eq(stringAttributeName, stringValue)
				.and().not().eq(intAttributeName, intValue);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());

		FilteredQueryBuilder booleanQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.termFilter(boolAttributeName, booleanValue));
		QueryBuilder stringQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.termFilter(stringAttributeName + '.' + MappingsBuilder.FIELD_NOT_ANALYZED, stringValue));
		QueryBuilder intQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.termFilter(intAttributeName, intValue));
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
