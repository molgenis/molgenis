package org.molgenis.data.semanticsearch.explain.service;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import org.apache.lucene.search.Explanation;
import org.elasticsearch.common.collect.Sets;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.meta.AttributeMetaDataMetaData;
import org.molgenis.util.ResourceUtils;
import org.testng.annotations.Test;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class ExplainServiceHelperTest
{

	ExplainServiceHelper explainServiceHelper = new ExplainServiceHelper();

	@Test
	public void discoverMatchedQueries() throws JsonSyntaxException, IOException
	{
		Explanation explanation = new Gson().fromJson(ResourceUtils.getString("explain_api_example.json"),
				Explanation.class);

		assertEquals(explainServiceHelper.discoverMatchedQueries(explanation), "high blood|medic");
	}

	@Test
	public void joinTermsTest()
	{
		assertEquals(explainServiceHelper.joinTerms("|high blood pressure| |medication"),
				"high blood pressure|medication");

		assertEquals(explainServiceHelper.joinTerms("|high||pressure|  |medication"), "high|pressure|medication");

		assertEquals(explainServiceHelper.joinTerms("high pressure |medication"), "high pressure|medication");
	}

	@Test
	public void getMatchedWord()
	{
		assertEquals(
				explainServiceHelper.getMatchedWord("weight(label:blood in 20697) [PerFieldSimilarity], result of:"),
				"blood");
		assertEquals(
				explainServiceHelper
						.getMatchedWord("weight(label:blood^0.5 in 20697) [PerFieldSimilarity], result of:"),
				"blood");
	}

	@Test
	public void recursivelyFindQuery()
	{
		QueryRule queryRule_1 = new QueryRule(AttributeMetaDataMetaData.LABEL, Operator.FUZZY_MATCH, "hypertension");
		QueryRule queryRule_2 = new QueryRule(AttributeMetaDataMetaData.LABEL, Operator.FUZZY_MATCH,
				"hypertensive disorder");
		QueryRule queryRule_3 = new QueryRule(AttributeMetaDataMetaData.LABEL, Operator.FUZZY_MATCH,
				"high blood pressure");

		QueryRule queryRule_4 = new QueryRule(AttributeMetaDataMetaData.LABEL, Operator.FUZZY_MATCH, "drug");
		QueryRule queryRule_5 = new QueryRule(AttributeMetaDataMetaData.LABEL, Operator.FUZZY_MATCH, "medication");
		QueryRule queryRule_6 = new QueryRule(AttributeMetaDataMetaData.LABEL, Operator.FUZZY_MATCH, "pill");

		QueryRule disMaxQueryRule_1 = new QueryRule(Arrays.asList(queryRule_1, queryRule_2, queryRule_3));
		disMaxQueryRule_1.setOperator(Operator.DIS_MAX);

		QueryRule disMaxQueryRule_2 = new QueryRule(Arrays.asList(queryRule_4, queryRule_5, queryRule_6));
		disMaxQueryRule_2.setOperator(Operator.DIS_MAX);

		QueryRule shouldQueryRule = new QueryRule(Arrays.asList(disMaxQueryRule_1, disMaxQueryRule_2));
		shouldQueryRule.setOperator(Operator.SHOULD);

		QueryRule finalDisMaxQueryRule = new QueryRule(Arrays.asList(shouldQueryRule));
		finalDisMaxQueryRule.setOperator(Operator.DIS_MAX);

		assertEquals(explainServiceHelper.recursivelyFindQuery("blood high", finalDisMaxQueryRule.getNestedRules())
				.toString(), "{high blood pressure=73.333}");
	}

	@Test
	public void termsConsistOfSingleWord()
	{
		assertTrue(explainServiceHelper.termsConsistOfSingleWord(Sets.newHashSet("blood", "high")));
		assertTrue(explainServiceHelper.termsConsistOfSingleWord(Sets.newHashSet("blood", "high_pressure")));
		assertFalse(explainServiceHelper.termsConsistOfSingleWord(Sets.newHashSet("blood", "high pressure")));
	}
}
