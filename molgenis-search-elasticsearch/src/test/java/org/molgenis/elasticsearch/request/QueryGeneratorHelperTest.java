package org.molgenis.elasticsearch.request;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.fieldtypes.EnumField;
import org.molgenis.fieldtypes.StringField;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class QueryGeneratorHelperTest
{
	private EntityMetaData entityMetaData;
	private List<QueryRule> listOfQueryRules1Test;
	private List<QueryRule> listOfQueryRules2Test;
	private QueryGeneratorHelper queryGeneratorHelper;

	@BeforeClass
	public void setUp()
	{
		entityMetaData = mock(EntityMetaData.class);

		EntityMetaData refEntityMetaData1 = mock(EntityMetaData.class);
		AttributeMetaData labelAttribute1 = mock(AttributeMetaData.class);
		when(refEntityMetaData1.getLabelAttribute()).thenReturn(labelAttribute1);
		when(labelAttribute1.getName()).thenReturn("identifier");

		EntityMetaData refEntityMetaData2 = mock(EntityMetaData.class);
		AttributeMetaData labelAttribute2 = mock(AttributeMetaData.class);
		when(refEntityMetaData2.getLabelAttribute()).thenReturn(labelAttribute2);
		when(labelAttribute2.getName()).thenReturn("identifier");

		AttributeMetaData attributeMetaData1 = mock(AttributeMetaData.class);
		EnumField enumField1 = mock(EnumField.class);
		when(entityMetaData.getAttribute("field1")).thenReturn(attributeMetaData1);
		when(attributeMetaData1.getDataType()).thenReturn(enumField1);
		when(enumField1.getEnumType()).thenReturn(FieldTypeEnum.MREF);
		when(attributeMetaData1.getRefEntity()).thenReturn(refEntityMetaData1);

		AttributeMetaData attributeMetaData2 = mock(AttributeMetaData.class);
		EnumField enumField2 = mock(EnumField.class);
		when(entityMetaData.getAttribute("field2")).thenReturn(attributeMetaData2);
		when(attributeMetaData2.getDataType()).thenReturn(enumField1);
		when(enumField2.getEnumType()).thenReturn(FieldTypeEnum.MREF);
		when(attributeMetaData2.getRefEntity()).thenReturn(refEntityMetaData2);

		AttributeMetaData attributeMetaData3 = mock(AttributeMetaData.class);
		StringField stringField3 = mock(StringField.class);
		when(entityMetaData.getAttribute("field3")).thenReturn(attributeMetaData3);
		when(attributeMetaData3.getDataType()).thenReturn(stringField3);
		when(stringField3.getEnumType()).thenReturn(FieldTypeEnum.STRING);

		QueryRule operatorAnd = new QueryRule(Operator.AND);
		QueryRule operatorOr = new QueryRule(Operator.OR);
		QueryRule rule0 = new QueryRule("field1", Operator.EQUALS, "value0");
		QueryRule rule1 = new QueryRule("field1", Operator.EQUALS, "value1");
		QueryRule rule2 = new QueryRule("field2", Operator.EQUALS, "value2");
		QueryRule rule3 = new QueryRule("field3", Operator.EQUALS, "string1");

		listOfQueryRules1Test = Arrays.asList(rule1, operatorOr, rule2, operatorAnd, rule3, operatorAnd, rule0);

		QueryRule rule4 = new QueryRule("field1", Operator.EQUALS, "value3");
		QueryRule rule5 = new QueryRule("field1", Operator.EQUALS, "value4");
		QueryRule rule6 = new QueryRule("field2", Operator.EQUALS, "value5");
		QueryRule rule7 = new QueryRule("field2", Operator.EQUALS, "value6");

		QueryRule nestedRule1 = new QueryRule(Arrays.asList(rule0, operatorOr, rule1));
		nestedRule1.setOperator(Operator.NESTED);
		QueryRule nestedRule2 = new QueryRule(Arrays.asList(rule4, operatorAnd, rule5));
		nestedRule1.setOperator(Operator.NESTED);
		QueryRule nestedRuleFirst = new QueryRule(Arrays.asList(nestedRule1, operatorAnd, nestedRule2));
		nestedRuleFirst.setOperator(Operator.NESTED);

		QueryRule nestedRule3 = new QueryRule(Arrays.asList(rule6, operatorAnd, rule7));
		nestedRule1.setOperator(Operator.NESTED);
		QueryRule nestedRuleSecond = new QueryRule(Arrays.asList(nestedRule3, operatorAnd, rule2));
		nestedRuleFirst.setOperator(Operator.NESTED);

		listOfQueryRules2Test = Arrays.asList(nestedRuleFirst, operatorAnd, nestedRuleSecond, operatorOr, rule3);
	}

	@Test
	public void generateQuery()
	{
		queryGeneratorHelper = new QueryGeneratorHelper(listOfQueryRules1Test, entityMetaData);

		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		boolQueryBuilder
				.must(QueryBuilders.queryString("field3:\"string1\""))
				.should(QueryBuilders.nestedQuery("field1",
						QueryBuilders.queryString("(field1.identifier:\"value1\" AND field1.identifier:\"value0\")")))
				.should(QueryBuilders.nestedQuery("field2", QueryBuilders.queryString("field2.identifier:\"value2\"")));

		assertEquals(queryGeneratorHelper.generateQuery().toString(), boolQueryBuilder.toString());

		queryGeneratorHelper = new QueryGeneratorHelper(listOfQueryRules2Test, entityMetaData);

		BoolQueryBuilder boolQueryBuilder2 = QueryBuilders.boolQuery();
		boolQueryBuilder2
				.must(QueryBuilders.nestedQuery(
						"field1",
						QueryBuilders
								.queryString("((field1.identifier:\"value0\" OR field1.identifier:\"value1\") AND (field1.identifier:\"value3\" AND field1.identifier:\"value4\"))")))
				.must(QueryBuilders.nestedQuery(
						"field2",
						QueryBuilders
								.queryString("((field2.identifier:\"value5\" AND field2.identifier:\"value6\") AND field2.identifier:\"value2\")")))
				.should(QueryBuilders.queryString("field3:\"string1\""));

		assertEquals(queryGeneratorHelper.generateQuery().toString(), boolQueryBuilder2.toString());
	}
}
