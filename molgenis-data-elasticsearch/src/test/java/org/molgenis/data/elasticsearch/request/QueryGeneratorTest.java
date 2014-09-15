package org.molgenis.data.elasticsearch.request;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.mockito.ArgumentCaptor;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.elasticsearch.index.MappingsBuilder;
import org.molgenis.data.elasticsearch.request.QueryGenerator;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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

		DefaultEntityMetaData refEntityMetaData = new DefaultEntityMetaData("ref_entity");
		refEntityMetaData.addAttribute(idAttributeName).setIdAttribute(true).setUnique(true);
		refEntityMetaData.addAttribute(refStringAttributeName).setLabelAttribute(true).setUnique(true);
		refEntityMetaData.addAttribute(refMrefAttributeName).setDataType(MolgenisFieldTypes.MREF).setNillable(true)
				.setRefEntity(refEntityMetaData);

		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData("entity");
		entityMetaData.addAttribute(idAttributeName).setIdAttribute(true).setUnique(true);
		entityMetaData.addAttribute(boolAttributeName).setDataType(MolgenisFieldTypes.BOOL);
		entityMetaData.addAttribute(categoricalAttributeName).setDataType(MolgenisFieldTypes.CATEGORICAL)
				.setRefEntity(refEntityMetaData);
		DefaultAttributeMetaData compoundPart0Attribute = new DefaultAttributeMetaData(compoundPart0AttributeName)
				.setDataType(MolgenisFieldTypes.STRING);
		DefaultAttributeMetaData compoundPart1Attribute = new DefaultAttributeMetaData(compoundPart1AttributeName)
				.setDataType(MolgenisFieldTypes.STRING);
		entityMetaData
				.addAttribute(compoundAttributeName)
				.setDataType(MolgenisFieldTypes.COMPOUND)
				.setAttributesMetaData(
						Arrays.<AttributeMetaData> asList(compoundPart0Attribute, compoundPart1Attribute));
		entityMetaData.addAttribute(dateAttributeName).setDataType(MolgenisFieldTypes.DATE);
		entityMetaData.addAttribute(dateTimeAttributeName).setDataType(MolgenisFieldTypes.DATETIME);
		entityMetaData.addAttribute(decimalAttributeName).setDataType(MolgenisFieldTypes.DECIMAL);
		entityMetaData.addAttribute(emailAttributeName).setDataType(MolgenisFieldTypes.EMAIL);
		entityMetaData.addAttribute(enumAttributeName).setDataType(MolgenisFieldTypes.ENUM);
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

	@Test
	public void generateOneQueryRuleEqualsBool()
	{
		Object value = "value";
		Query q = new QueryImpl().eq(boolAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.termFilter(boolAttributeName, value));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	@Test
	public void generateOneQueryRuleEqualsString()
	{
		Object value = "value";
		Query q = new QueryImpl().eq(stringAttributeName, value);
		new QueryGenerator().generate(searchRequestBuilder, q, entityMetaData);
		ArgumentCaptor<QueryBuilder> captor = ArgumentCaptor.forClass(QueryBuilder.class);
		verify(searchRequestBuilder).setQuery(captor.capture());
		QueryBuilder expectedQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
				FilterBuilders.termFilter(stringAttributeName + '.' + MappingsBuilder.FIELD_NOT_ANALYZED, value));
		assertQueryBuilderEquals(captor.getValue(), expectedQuery);
	}

	private void assertQueryBuilderEquals(QueryBuilder actual, QueryBuilder expected)
	{
		// QueryBuilder classes do not implement equals
		assertEquals(actual.toString().replaceAll("\\s", ""), expected.toString().replaceAll("\\s", ""));
	}
}
